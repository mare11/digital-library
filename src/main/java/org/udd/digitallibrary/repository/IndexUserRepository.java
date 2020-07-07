package org.udd.digitallibrary.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import org.udd.digitallibrary.model.IndexUser;

@Repository
public interface IndexUserRepository extends ElasticsearchRepository<IndexUser, String> {
}
