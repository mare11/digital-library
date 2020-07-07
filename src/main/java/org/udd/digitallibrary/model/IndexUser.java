package org.udd.digitallibrary.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import static org.udd.digitallibrary.model.IndexUser.INDEX_NAME;

@Data
@Builder
@Document(indexName = INDEX_NAME, shards = 1, replicas = 0)
public class IndexUser {

    public static final String INDEX_NAME = "users";

    @Id
    @Field(type = FieldType.Text, store = true)
    private String username;

    @Field(type = FieldType.Text, store = true)
    private String firstName;

    @Field(type = FieldType.Text, store = true)
    private String lastName;

    @Field(type = FieldType.Text, store = true)
    private String locationName;

    @GeoPointField
    private GeoPoint location;

}
