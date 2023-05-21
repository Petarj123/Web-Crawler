package com.mtc.crawler.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "data")
public class ScrapedData {
    @Id
    private String id;
    private String title;
    private String url;
    private String text;
    private Date scrapedAt;

    @Override
    public int hashCode() {
        return Objects.hash(title, text);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ScrapedData other = (ScrapedData) obj;
        return Objects.equals(title, other.title)
                && Objects.equals(text, other.text);
    }
}
