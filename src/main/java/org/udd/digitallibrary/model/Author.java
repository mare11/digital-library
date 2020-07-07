package org.udd.digitallibrary.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import static org.udd.digitallibrary.model.IndexUnit.SERBIAN_ANALYZER;

@Data
@Builder
public class Author {

    @Field(type = FieldType.Text, store = true, analyzer = SERBIAN_ANALYZER)
    private String firstName;

    @Field(type = FieldType.Text, store = true, analyzer = SERBIAN_ANALYZER)
    private String lastName;

    @GeoPointField
    private GeoPoint location;

}
