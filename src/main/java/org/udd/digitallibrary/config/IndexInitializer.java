package org.udd.digitallibrary.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Component;
import org.udd.digitallibrary.model.IndexUser;

import javax.annotation.PostConstruct;

@Component
public class IndexInitializer {

    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public IndexInitializer(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @PostConstruct
    public void initialize() {
        IndexUser user1 = IndexUser.builder()
                .username("milan")
                .firstName("Milan")
                .lastName("Milanovic")
                .location(new GeoPoint(45.254410, 19.842550))
                .locationName("Novi Sad")
                .build();

        IndexUser user2 = IndexUser.builder()
                .username("petar")
                .firstName("Petar")
                .lastName("Petrovic")
                .location(new GeoPoint(44.815070, 20.460480))
                .locationName("Beograd")
                .build();

        IndexUser user3 = IndexUser.builder()
                .username("marko")
                .firstName("Marko")
                .lastName("Markovic")
                .location(new GeoPoint(45.381561, 20.368574))
                .locationName("Zrenjanin")
                .build();

        IndexUser user4 = IndexUser.builder()
                .username("jovan")
                .firstName("Jovan")
                .lastName("Jovanovic")
                .location(new GeoPoint(51.507351, -0.127758))
                .locationName("London")
                .build();

        IndexUser user5 = IndexUser.builder()
                .username("filip")
                .firstName("Filip")
                .lastName("Filipovic")
                .location(new GeoPoint(48.856613, 2.352222))
                .locationName("Paris")
                .build();

        elasticsearchOperations.save(user1, user2, user3, user4, user5);
    }
}
