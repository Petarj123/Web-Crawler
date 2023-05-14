package com.mtc.crawler;

import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.List;

public class WebCrawler {
    @SneakyThrows
    public void crawl(int level, String url, List<String> visitedLinks){
        if (level <= 5) {
            Document document = request(url, visitedLinks);
            if (document != null) {
                for (Element link : document.select("a[href]")){
                    String nextLink = link.absUrl("href");
                    if (!visitedLinks.contains(nextLink)){
                        crawl(level + 1, nextLink, visitedLinks);
                    }
                }
            }
        }
    }
    public Document request(String URL, List<String> visitedLinks){
        try {
            Connection connection = Jsoup.connect(URL);
            Document document = connection.get();
            if (connection.response().statusCode() == 200){
                System.out.println("Link: " + URL);
                System.out.println(document.title());
                visitedLinks.add(URL);
                return document;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
