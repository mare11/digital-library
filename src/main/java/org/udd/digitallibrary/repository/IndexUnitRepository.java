package org.udd.digitallibrary.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import org.udd.digitallibrary.model.IndexUnit;

@Repository
public interface IndexUnitRepository extends ElasticsearchRepository<IndexUnit, String> {
}
