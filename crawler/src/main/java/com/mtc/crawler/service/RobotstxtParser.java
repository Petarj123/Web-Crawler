package com.mtc.crawler.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RobotstxtParser {

    public static void main(String[] args) throws MalformedURLException, URISyntaxException {
        RobotstxtParser robotstxtParser = new RobotstxtParser();
        String string = robotstxtParser.getBaseUrl("https://twitter.com/");
        robotstxtParser.parseRobotstxt(string);
    }
    @SneakyThrows
    public Map<String, List<String>> parseRobotstxt(String url){
        String baseUrl = getBaseUrl(url);
        List<String> lines = fetchRobotstxt(baseUrl);
        Map<String, List<String>> directives = new HashMap<>();

        boolean currentUserAgentIsAll = false;

        for (String line : lines) {
            if (line.startsWith("User-agent:")) {
                String userAgent = line.split(": ")[1];
                currentUserAgentIsAll = userAgent.equals("*");
            } else if (currentUserAgentIsAll) {
                if (line.startsWith("Allow:")) {
                    String allowPath = line.split(": ")[1];
                    directives.putIfAbsent("Allow", new ArrayList<>());
                    directives.get("Allow").add(allowPath);
                } else if (line.startsWith("Disallow:")) {
                    String disallowPath = line.split(": ")[1];
                    directives.putIfAbsent("Disallow", new ArrayList<>());
                    directives.get("Disallow").add(disallowPath);
                }
            }
        }
        return directives;
    }

    @SneakyThrows
    private List<String> fetchRobotstxt(String URL){
        String baseUrl = getBaseUrl(URL);
        java.net.URL url = new URI(baseUrl + "/robots.txt").toURL();

        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        List<String> lines = new ArrayList<>();
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            lines.add(inputLine);
        in.close();

        return lines;
    }

    protected String getBaseUrl(String URL) throws URISyntaxException, MalformedURLException {
        URL url = new URI(URL).toURL();
        return url.getProtocol() + "://" + url.getHost() + (url.getPort() == -1 ? "" : ":" + url.getPort()) + "/";
    }
}
