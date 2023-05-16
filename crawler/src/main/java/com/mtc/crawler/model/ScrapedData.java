package com.mtc.crawler.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@Document(collection = "sentiment")
public class ScrapedData {
    @Id
    private String id;
    private String title;
    private String URL;
    private String text;
    private Date scrapedAt;

}
