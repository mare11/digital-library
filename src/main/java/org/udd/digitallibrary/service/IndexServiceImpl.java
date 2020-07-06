package org.udd.digitallibrary.service;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        String fileName = saveUploadedFile(file);
        if (fileName != null) {
            DocumentHandler handler = getHandler(fileName);
            if (handler == null) {
                log.error("No available handler for file: {}", fileName);
                return;
            }
            IndexUnit indexUnit = handler.getIndexUnit(new File(fileName));
            indexUnit.setMagazine(model.getMagazine());
            indexUnit.setTitle(model.getTitle());
            indexUnit.setAuthors(getAuthors(model.getAuthors()));
            indexUnit.setKeywords(model.getKeywords());
            indexUnit.setAreas(model.getAreas());
            operations.save(indexUnit);
            log.info("File '{}' saved!", fileName);
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

    private List<Author> getAuthors(String authors) {
        List<Author> authorList = new ArrayList<>();
        String[] splitAuthors = authors.split(",");
        for (String splitAuthor : splitAuthors) {
            String[] authorNames = splitAuthor.split(" ");
            authorList.add(Author.builder()
                    .firstName(authorNames[0])
                    .lastName(authorNames[1])
                    .build());
        }
        return authorList;
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
    public List<ResultData> search(List<QueryData> queryData) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryData.forEach(data -> {
            QueryBuilder fieldQueryBuilder;
            if (isPhraseSearch(data.getValue())) {
                fieldQueryBuilder = QueryBuilders.matchPhraseQuery(data.getField(), data.getValue().replaceAll("\"", "")).analyzer(SERBIAN_ANALYZER);
            } else {
                fieldQueryBuilder = QueryBuilders.matchQuery(data.getField(), data.getValue()).analyzer(SERBIAN_ANALYZER);
            }
            if (data.getOperation().equalsIgnoreCase("I")) {
                queryBuilder.must(fieldQueryBuilder);
            } else if (data.getOperation().equalsIgnoreCase("ILI")) {
                queryBuilder.should(fieldQueryBuilder);
            } else if (data.getOperation().equalsIgnoreCase("I NE")) {
                queryBuilder.mustNot(fieldQueryBuilder);
            }
        });
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withHighlightFields(getFieldsForHighlighting(queryData))
                .build();
        SearchHits<IndexUnit> searchHits = operations.search(build, IndexUnit.class);
        return searchHits.stream()
                .map(hit -> {
                    IndexUnit indexUnit = hit.getContent();
                    String highlight = getHighlight(hit);
                    return ResultData.builder()
                            .magazine(indexUnit.getMagazine())
                            .title(indexUnit.getTitle())
//                            .authors(indexUnit.getAuthors().toString()) // TODO
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

    private HighlightBuilder.Field[] getFieldsForHighlighting(List<QueryData> data) {
        return data.stream()
                .map(queryData -> new HighlightBuilder.Field(queryData.getField()))
                .toArray(HighlightBuilder.Field[]::new);
    }

    private String getHighlight(SearchHit<IndexUnit> hit) {
        return hit.getHighlightFields().values()
                .stream()
                .reduce((strings1, strings2) -> {
                    strings1.addAll(strings2);
                    return strings1;
                })
                .map(strings -> String.join(" ... ", strings))
                .orElseGet(() -> hit.getContent().getText().substring(0, 150).concat(" ... "));
    }
}
