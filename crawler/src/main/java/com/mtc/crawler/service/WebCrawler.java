package com.mtc.crawler.service;


import com.mtc.crawler.model.ScrapedData;
import com.mtc.crawler.model.UrlDepth;
import com.mtc.crawler.repository.ScrapedDataRepository;
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
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class WebCrawler {
    private final RobotstxtParser parser;
    private final Map<String, Map<String, Set<String>>> directivesMap = new HashMap<>();
    private final ScrapedDataRepository scrapedDataRepository;
    private static final Set<String> visitedLinks = Collections.synchronizedSet(new HashSet<>());
    private static final BlockingQueue<UrlDepth> queue = new LinkedBlockingQueue<>();
    public void start(String url, int maxDepth, int numThreads) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Create WebCrawler instances
        for (int i = 0; i < numThreads; i++) {
            WebCrawler crawler = new WebCrawler(parser, scrapedDataRepository);
            int finalI = i;
            executor.execute(() -> crawler.crawl(url, maxDepth, finalI));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        visitedLinks.clear();
        queue.clear();
    }
    
    @SneakyThrows
    public void crawl(String url, int maxDepth, int threadId) {

        if (maxDepth <= 0) {
            throw new RuntimeException("Depth cant be less than or equal to 0");
        }

        String encodedUrl = parser.cleanUrl(url);
        String baseUrl = parser.getBaseUrl(encodedUrl);
        directivesMap.put(baseUrl, parser.parseRobotstxt(baseUrl));

        int delay = parser.crawlDelay(baseUrl + "/robots.txt");
        int score = 0;
        queue.add(new UrlDepth(encodedUrl, 0));

        while (!queue.isEmpty()) {
            UrlDepth urlDepth = queue.poll();
            System.out.println("QUEUE " + queue);
            String currentUrl = urlDepth.getUrl();
            int currentDepth = urlDepth.getDepth();

            Thread.sleep(delay * 1000L);
            Document document = request(currentUrl, visitedLinks);
            if (document != null) {
                StringBuilder paragraphText = new StringBuilder();
                for (Element paragraph : document.select("p")) {
                    paragraphText.append(paragraph.text()).append("\n");
                }
                if (!paragraphText.isEmpty()) {
                    ScrapedData data = ScrapedData.builder()
                            .url(currentUrl)
                            .title(document.title())
                            .text(paragraphText.toString())
                            .scrapedAt(new Date())
                            .build();
                    if (!scrapedDataRepository.existsByUrl(currentUrl)) {
                        scrapedDataRepository.save(data);
                    }
                }
                for (Element link : document.select("a[href]")) {
                    String nextLink = link.absUrl("href");

                    // Check that the link is allowed, has not been visited, and contains url
                    if (!visitedLinks.contains(nextLink) && isAllowed(nextLink, directivesMap.get(baseUrl)) && nextLink.contains(encodedUrl)) {
                        System.out.println("Allowed Link: " + nextLink);

                        // Only add the link to the queue if we are not yet at max depth
                        if (currentDepth < maxDepth - 1) {
                            queue.add(new UrlDepth(nextLink, currentDepth + 1));
                        }
                        visitedLinks.add(nextLink);
                        score++;
                    }
                }
            }
        }
        System.out.println("Crawling completed for thread " + threadId);
        System.out.printf("Thread %d score: %d\n", threadId, score);
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

    private boolean isAllowed(String url, Map<String, Set<String>> directives) {
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
