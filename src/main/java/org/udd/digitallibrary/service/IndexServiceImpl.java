package org.udd.digitallibrary.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.udd.digitallibrary.exception.BadRequestException;
import org.udd.digitallibrary.handler.DocumentHandler;
import org.udd.digitallibrary.handler.PDFHandler;
import org.udd.digitallibrary.model.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.udd.digitallibrary.model.IndexUnit.INDEX_NAME;
import static org.udd.digitallibrary.model.IndexUnit.SERBIAN_ANALYZER;

@Slf4j
@Service
public class IndexServiceImpl implements IndexService {

    private static final String DATA_DIR_PATH = "files";

    private final ElasticsearchOperations operations;


    @Autowired
    public IndexServiceImpl(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    @Override
    public void index(UploadModel model) {
        MultipartFile file = model.getFile();
        if (file == null || file.isEmpty()) {
            log.warn("Missing file!");
            return;
        }
        String filename = saveUploadedFile(file);
        if (filename != null) {
            DocumentHandler handler = getHandler(filename);
            if (handler == null) {
                log.error("No available handler for file: {}", filename);
                return;
            }
            IndexUnit indexUnit = handler.getIndexUnit(new File(filename));
            String[] nameParts = filename.split(Pattern.quote("\\"));
            indexUnit.setFilename(nameParts[nameParts.length - 1]);
            indexUnit.setMagazine(model.getMagazine());
            indexUnit.setTitle(model.getTitle());
            indexUnit.setAuthors(getAuthors(model.getAuthors()));
            indexUnit.setKeywords(model.getKeywords());
            indexUnit.setAreas(model.getAreas());
            operations.save(indexUnit);
            log.info("File '{}' saved!", indexUnit.getFilename());
        }
    }

    private String saveUploadedFile(MultipartFile file) {
        String retVal = null;
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                Path path = Paths.get(getResourceFilePath().getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.write(path, bytes);
                retVal = path.toString();
            } catch (IOException e) {
                throw new BadRequestException();
            }
        }
        return retVal;
    }

    private DocumentHandler getHandler(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return new PDFHandler();
        } else {
            return null;
        }
    }

    private List<IndexUser> getAuthors(String authors) {
        String[] splitAuthors = authors.split(" ");
        return Arrays.stream(splitAuthors)
                .map(s -> operations.get(s, IndexUser.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private File getResourceFilePath() {
        URL url = this.getClass().getClassLoader().getResource(DATA_DIR_PATH);
        File file;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            file = new File(url.getPath());
        }
        return file;
    }

    @Override
    public List<ResultUnit> querySearch(List<QueryData> queryData) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryData.forEach(data -> {
            QueryBuilder fieldQueryBuilder;
            boolean authorSearch = data.getField().equals("authors");
            if (authorSearch) {
                fieldQueryBuilder = getAuthorsNestedQueryBuilder(data);
            } else {
                fieldQueryBuilder = getRegularQueryBuilder(data);
            }
            if (data.getOperation().equalsIgnoreCase("I")) {
                queryBuilder.must(fieldQueryBuilder);
            } else if (data.getOperation().equalsIgnoreCase("ILI")) {
                queryBuilder.should(fieldQueryBuilder);
            } else if (data.getOperation().equalsIgnoreCase("I NE")) {
                queryBuilder.mustNot(fieldQueryBuilder);
            }
        });
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withHighlightFields(getFieldsForHighlighting(queryData))
                .build();

        SearchHits<IndexUnit> searchHits = operations.search(query, IndexUnit.class);

        return convertToResults(searchHits);
    }

    private QueryBuilder getAuthorsNestedQueryBuilder(QueryData data) {
        QueryBuilder firstNameQueryBuilder;
        QueryBuilder lastNameQueryBuilder;

        if (isPhraseSearch(data.getValue())) {
            String value = data.getValue().replaceAll("\"", "");
            firstNameQueryBuilder = QueryBuilders.matchPhraseQuery("firstName", value).analyzer(SERBIAN_ANALYZER);
            lastNameQueryBuilder = QueryBuilders.matchPhraseQuery("lastName", value).analyzer(SERBIAN_ANALYZER);
        } else {
            firstNameQueryBuilder = QueryBuilders.matchQuery("firstName", data.getValue()).analyzer(SERBIAN_ANALYZER);
            lastNameQueryBuilder = QueryBuilders.matchQuery("lastName", data.getValue()).analyzer(SERBIAN_ANALYZER);
        }
        BoolQueryBuilder authorsQueryBuilder = QueryBuilders.boolQuery()
                .should(firstNameQueryBuilder)
                .should(lastNameQueryBuilder);

        return QueryBuilders.nestedQuery("authors", authorsQueryBuilder, ScoreMode.Avg);
    }

    private QueryBuilder getRegularQueryBuilder(QueryData data) {
        QueryBuilder fieldQueryBuilder;
        if (isPhraseSearch(data.getValue())) {
            String value = data.getValue().replaceAll("\"", "");
            fieldQueryBuilder = QueryBuilders.matchPhraseQuery(data.getField(), value).analyzer(SERBIAN_ANALYZER);
        } else {
            fieldQueryBuilder = QueryBuilders.matchQuery(data.getField(), data.getValue()).analyzer(SERBIAN_ANALYZER);
        }
        return fieldQueryBuilder;
    }

    private HighlightBuilder.Field[] getFieldsForHighlighting(List<QueryData> data) {
        return data.stream()
                .map(queryData -> new HighlightBuilder.Field(queryData.getField())
                        .preTags("<b>")
                        .postTags("</b>"))
                .toArray(HighlightBuilder.Field[]::new);
    }

    private List<ResultUnit> convertToResults(SearchHits<IndexUnit> searchHits) {
        return searchHits.stream()
                .map(hit -> {
                    IndexUnit indexUnit = hit.getContent();
                    String highlight = getHighlight(hit);
                    StringBuilder authors = new StringBuilder("");
                    indexUnit.getAuthors().forEach(user -> authors.append(String.join(" ", user.getFirstName(), user.getLastName(), user.getLocationName())).append(","));

                    return ResultUnit.builder()
                            .filename(indexUnit.getFilename())
                            .magazine(indexUnit.getMagazine())
                            .title(indexUnit.getTitle())
                            .authors(authors.toString().substring(0, authors.length() - 1))
                            .keywords(indexUnit.getKeywords())
                            .areas(indexUnit.getAreas())
                            .highlight(highlight)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private boolean isPhraseSearch(String value) {
        return value.startsWith("\"") && value.endsWith("\"");
    }

    private String getHighlight(SearchHit<IndexUnit> hit) {
        return hit.getHighlightFields().values()
                .stream()
                .reduce((strings1, strings2) -> Stream.concat(strings1.stream(), strings2.stream()).collect(Collectors.toList()))
                .map(strings -> String.join(" ... ", strings))
                .orElseGet(() -> hit.getContent().getText().substring(0, 150).concat(" ... "));
    }

    @Override
    public List<ResultUnit> moreLikeThisSearch(String filename) {
        String fieldName = "text";
        MoreLikeThisQueryBuilder queryBuilder = QueryBuilders
                .moreLikeThisQuery(
                        new String[]{fieldName}, //fields
                        null,
                        new MoreLikeThisQueryBuilder.Item[]{new MoreLikeThisQueryBuilder.Item(INDEX_NAME, filename)} // like docs
                ).minTermFreq(2)
                .minDocFreq(2)
                .analyzer(SERBIAN_ANALYZER);

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withHighlightFields(new HighlightBuilder.Field(fieldName)
                        .preTags("<b>")
                        .postTags("</b>"))
                .build();

        SearchHits<IndexUnit> searchHits = operations.search(query, IndexUnit.class);

        return convertToResults(searchHits);
    }

    @Override
    public List<ResultUser> geoDistanceSearch(String filename) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        IndexUnit indexUnit = operations.get(filename, IndexUnit.class);
        if (indexUnit == null || indexUnit.getAuthors() == null) {
            return Collections.emptyList();
        }
        indexUnit.getAuthors().forEach(author ->
                boolQueryBuilder.mustNot(QueryBuilders
                        .geoDistanceQuery("location")
                        .point(new GeoPoint(author.getLocation().getLat(), author.getLocation().getLon()))
                        .distance(100, DistanceUnit.KILOMETERS)
                        .geoDistance(GeoDistance.ARC)));

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .build();

        SearchHits<IndexUser> searchHits = operations.search(query, IndexUser.class);

        return searchHits.stream()
                .map(hit -> {
                    IndexUser indexUser = hit.getContent();
                    return ResultUser.builder()
                            .firstName(indexUser.getFirstName())
                            .lastName(indexUser.getLastName())
                            .locationName(indexUser.getLocationName())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Resource getFile(String filename) {
        try {
            Path path = Paths.get(getResourceFilePath().getAbsolutePath() + File.separator + filename);
            return new ByteArrayResource(Files.readAllBytes(path));
        } catch (IOException e) {
            log.error("Error while reading file!");
            throw new BadRequestException();
        }
    }
}
