package org.udd.digitallibrary.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = IndexUnit.INDEX_NAME, shards = 1, replicas = 0)
public class IndexUnit {

    public static final String INDEX_NAME = "digital-library";
    public static final String SERBIAN_ANALYZER = "serbian";
    public static final String DATE_PATTERN = "yyyy-MM-dd";

    @Id
    @Field(type = FieldType.Text, index = false, store = true)
    private String filename;

    @Field(type = FieldType.Text, store = true, analyzer = SERBIAN_ANALYZER)
    private String magazine;

    @Field(type = FieldType.Text, store = true, analyzer = SERBIAN_ANALYZER)
    private String title;

    @Field(type = FieldType.Nested, store = true, analyzer = SERBIAN_ANALYZER)
    private List<Author> authors;

    @Field(type = FieldType.Text, store = true, analyzer = SERBIAN_ANALYZER)
    private String keywords;

    @Field(type = FieldType.Text, store = true, analyzer = SERBIAN_ANALYZER)
    private String areas;

    @Field(type = FieldType.Text, store = true, analyzer = SERBIAN_ANALYZER)
    private String text;

    @Field(type = FieldType.Text, store = true)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    private String fileDate;
}
