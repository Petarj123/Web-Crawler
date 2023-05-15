package com.mtc.crawler.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

@Component
public class RobotstxtParser {

    public static void main(String[] args) throws MalformedURLException, URISyntaxException {
        RobotstxtParser robotstxtParser = new RobotstxtParser();
        String string = robotstxtParser.getBaseUrl("https://twitter.com/robots.txt");
        robotstxtParser.crawlDelay("https://twitter.com/robots.txt");
    }
    @SneakyThrows
    public Map<String, Set<String>> parseRobotstxt(String url){
        String baseUrl = getBaseUrl(url);
        List<String> lines = fetchRobotstxt(baseUrl);
        Map<String, Set<String>> directives = new HashMap<>();

        boolean currentUserAgentIsAll = false;

        if (lines == null || lines.isEmpty()) {
            directives.put("Allow", new HashSet<>());
            directives.get("Allow").add("/*"); // allow all paths
            return directives;
        }

        for (String line : lines) {
            String[] parts = line.split(": ");
            if (parts.length < 2) continue; // Skip lines without ": "

            if (parts[0].trim().equalsIgnoreCase("User-agent")) {
                String userAgent = parts[1].trim();
                currentUserAgentIsAll = userAgent.equals("*");
            } else if (currentUserAgentIsAll) {
                if (parts[0].trim().equalsIgnoreCase("Allow")) {
                    String allowPath = parts[1].trim();
                    directives.putIfAbsent("Allow", new HashSet<>());
                    directives.get("Allow").add(allowPath);
                } else if (parts[0].trim().equalsIgnoreCase("Disallow")) {
                    String disallowPath = parts[1].trim();
                    directives.putIfAbsent("Disallow", new HashSet<>());
                    directives.get("Disallow").add(disallowPath);
                }
            }
        }
        return directives;
    }

    protected int crawlDelay(String URL) throws MalformedURLException, URISyntaxException {
        List<String> robotstxt = fetchRobotstxt(URL);
        int delay = 0;
        if (robotstxt == null || robotstxt.isEmpty()) {
            return delay;
        }
        for (String line : robotstxt) {
            if (line.startsWith("Crawl-delay:")){
                String crawlDelay = line.split(": ")[1];
                delay = Integer.parseInt(crawlDelay);
            }
        }
        return delay;
    }

    protected List<String> fetchRobotstxt(String URL) throws URISyntaxException, MalformedURLException {
        String baseUrl = getBaseUrl(URL);
        java.net.URL url = new URI(baseUrl + "/robots.txt").toURL();
        List<String> lines = new ArrayList<>();

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                lines.add(inputLine);
            in.close();
        } catch (IOException e) {
            // robots.txt does not exist for this URL
            return null;
        }

        return lines;
    }

    protected String getBaseUrl(String URL) {
        try {
            URL = URL.replace("##", "#");  // replace double hash symbols with a single one
            URL url = new URI(URL).toURL();
            return url.getProtocol() + "://" + url.getHost() + (url.getPort() == -1 ? "" : ":" + url.getPort()) + "/";
        } catch (URISyntaxException | MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
