package com.mtc.crawler.repository;

import com.mtc.crawler.model.ScrapedData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapedDataRepository extends MongoRepository<ScrapedData, String> {
}