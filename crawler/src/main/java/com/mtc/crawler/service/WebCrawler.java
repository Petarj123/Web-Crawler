package com.mtc.crawler.service;

import com.mtc.crawler.mode.ScrapedData;
import com.mtc.crawler.repository.ScrapedDataRepository;
import com.mtc.crawler.service.RobotstxtParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WebCrawler {

    private final RobotstxtParser parser;
    private final Map<String, Map<String, Set<String>>> directivesMap = new HashMap<>();
    private final NLP nlp;
    private final ScrapedDataRepository scrapedDataRepository;
    @SneakyThrows
    public void crawl(String url) {
        String baseUrl = parser.getBaseUrl(url);
        directivesMap.put(baseUrl, parser.parseRobotstxt(baseUrl));

        int delay = parser.crawlDelay(baseUrl + "/robots.txt");

        Queue<String> queue = new LinkedList<>();
        Set<String> visitedLinks = new HashSet<>();
        queue.add(url);

        while (!queue.isEmpty()) {
            Thread.sleep(delay * 1000L);
            String currentUrl = queue.poll();
            Document document = request(currentUrl, visitedLinks);
            if (document != null) {
                for (Element link : document.select("a[href]")) {
                    String nextLink = link.absUrl("href");
                    if (!visitedLinks.contains(nextLink) && isAllowed(nextLink, directivesMap.get(baseUrl))) {
                        System.out.println("Allowed Link: " + nextLink);
                        System.out.println(document.body().text());
                        ScrapedData data = ScrapedData.builder()
                                .URL(nextLink)
                                .text(document.body().text())
                                .scrapedAt(new Date())
                                .build();
                        System.out.println(data.toString());
                        scrapedDataRepository.save(data);
                        queue.add(nextLink);
                        visitedLinks.add(nextLink);
                    }
                }
            }
        }
    }
    private Document request(String URL, Set<String> visitedLinks){
        try {
            Connection connection = Jsoup.connect(URL).userAgent("SentimentAnalyzerBot/1.0");
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

    private boolean isAllowed(String url, Map<String, Set<String>> directives) throws MalformedURLException, URISyntaxException {
        String baseUrl = parser.getBaseUrl(url);
        if (baseUrl == null) {
            return false;
        }
        String path = url.replace(baseUrl, "");

        // Deny has priority over allow.
        Set<String> disallowedPaths = directives.get("Disallow");
        if (disallowedPaths != null) {
            for (String disallowedPath : disallowedPaths) {
                if (path.startsWith(disallowedPath)) {
                    return false;
                }
            }
        }

        Set<String> allowedPaths = directives.get("Allow");
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
