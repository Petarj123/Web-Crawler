package com.mtc.crawler.service;

import com.mtc.crawler.service.RobotstxtParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebCrawler {

    private final RobotstxtParser parser;
    @SneakyThrows
    public void crawl(int level, String url, List<String> visitedLinks) {
        if (level <= 5) {
            Map<String, List<String>> directives = parser.parseRobotstxt(url);

            Document document = request(url, visitedLinks);
            if (document != null) {
                for (Element link : document.select("a[href]")) {
                    String nextLink = link.absUrl("href");
                    if (!visitedLinks.contains(nextLink) && isAllowed(nextLink, directives)) {
                        System.out.println("Allowed Link: " + nextLink);
                        crawl(level + 1, nextLink, visitedLinks);
                    }
                }
            }
        }
    }
    private Document request(String URL, List<String> visitedLinks){
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
    @SneakyThrows
    private boolean isAllowed(String url, Map<String, List<String>> directives) {
        String path = url.replace(parser.getBaseUrl(url), "");

        // Deny has priority over allow.
        List<String> disallowedPaths = directives.get("Disallow");
        if (disallowedPaths != null) {
            for (String disallowedPath : disallowedPaths) {
                if (path.startsWith(disallowedPath)) {
                    return false;
                }
            }
        }

        List<String> allowedPaths = directives.get("Allow");
        if (allowedPaths != null) {
            for (String allowedPath : allowedPaths) {
                if (path.startsWith(allowedPath)) {
                    return true;
                }
            }
        }

        // If no directives are found, it's generally safe to crawl.
        return true;
    }
}
