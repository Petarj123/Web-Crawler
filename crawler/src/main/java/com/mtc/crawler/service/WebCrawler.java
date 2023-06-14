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
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class WebCrawler {
    private final RobotstxtParser parser;
    private final ScrapedDataRepository scrapedDataRepository;
    private final UserAgentGenerator userAgentGenerator;
    private final Map<String, Map<String, Set<String>>> directivesMap = new HashMap<>();
    private static final Set<String> visitedLinks = Collections.synchronizedSet(new HashSet<>());
    private static final Set<ScrapedData> scrapedDataSet = Collections.synchronizedSet(new HashSet<>());
    private static final BlockingQueue<UrlDepth> queue = new LinkedBlockingQueue<>();

    public void start(String url, int maxDepth, int numThreads) {
        try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {
            for (int i = 0; i < numThreads; i++) {
                WebCrawler crawler = new WebCrawler(parser, scrapedDataRepository, userAgentGenerator);
                int finalI = i;
                executor.execute(() -> crawler.crawl(url, maxDepth, finalI));
            }

            executor.shutdown();
        }
        scrapedDataRepository.saveAll(scrapedDataSet);
        visitedLinks.clear();
        queue.clear();
    }

    @SneakyThrows
    public void crawl(String url, int maxDepth, int threadId) {

        if (maxDepth <= 0) {
            throw new RuntimeException("Depth cant be less than or equal to 0");
        }

        String encodedUrl = parser.cleanUrl(url);
        System.out.println("Encoded url: " + encodedUrl);
        String baseUrl = parser.getBaseUrl(encodedUrl);
        directivesMap.put(baseUrl, parser.parseRobotstxt(baseUrl));

        int delay = parser.crawlDelay(baseUrl + "/robots.txt");
        int visitedUrls = 0;
        int totalResponseTime = 0;

        queue.add(new UrlDepth(encodedUrl, 0, baseUrl));

        while (!queue.isEmpty()) {
            UrlDepth urlDepth = queue.poll();
            String currentUrl = urlDepth.getUrl();
            int currentDepth = urlDepth.getDepth();
            String referenceUrl = urlDepth.getReferenceUrl();

            Thread.sleep(delay * 1000L);
            long startTime = System.currentTimeMillis();
            Document document = request(currentUrl);
            long endTime = System.currentTimeMillis();
            totalResponseTime += (endTime - startTime);
            if (document != null) {
                StringBuilder paragraphText = new StringBuilder();
                for (Element paragraph : document.select("p")) {
                    paragraphText.append(paragraph.text()).append("\n");
                }
                if (!paragraphText.isEmpty()) {
                    ScrapedData data = createScrapedData(currentUrl, document.title(), paragraphText.toString());
                    if (!scrapedDataSet.contains(data)) {
                        synchronized (scrapedDataSet) {
                            scrapedDataSet.add(data);
                        }
                    }
                }
                for (Element link : document.select("a[href]")) {
                    String nextLink = link.absUrl("href");
                    // Check that the link is allowed, has not been visited, and starts with the base URL
                    if (!visitedLinks.contains(nextLink) && isAllowed(nextLink, directivesMap.get(baseUrl)) && nextLink.startsWith(referenceUrl)) {
                        System.out.println("Allowed link: " + nextLink);
                        // Only add the link to the queue if we are not yet at max depth
                        if (currentDepth < maxDepth - 1) {
                            queue.add(new UrlDepth(nextLink, currentDepth + 1, referenceUrl));
                        }
                        visitedLinks.add(nextLink);
                        visitedUrls++;
                    }
                }
            }
        }
        System.out.println("Crawler " + threadId);
        System.out.println("Visited URLs: " + visitedUrls);
        System.out.println("Average Response Time: " + (totalResponseTime / visitedUrls) + " ms");
    }

    private Document request(String URL){
        String userAgent = userAgentGenerator.generateRandomUserAgent();
        try {
            Connection connection = Jsoup.connect(URL).userAgent(userAgent);
            Document document = connection.get();
            if (connection.response().statusCode() == 200){
                WebCrawler.visitedLinks.add(URL);
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
    private ScrapedData createScrapedData(String url, String title, String text){
        return ScrapedData.builder()
              .url(url)
              .title(title)
              .text(text)
              .scrapedAt(new Date())
              .build();
    }
}
