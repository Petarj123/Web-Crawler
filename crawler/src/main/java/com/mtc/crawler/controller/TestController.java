package com.mtc.crawler.controller;

import com.mtc.crawler.service.WebCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    private final WebCrawler crawler;


    @GetMapping("/get")
    @ResponseStatus(HttpStatus.OK)
    public void test(@RequestParam String url, @RequestParam int depth, @RequestParam int numThreads) throws MalformedURLException, URISyntaxException {
        crawler.start(url, depth, numThreads);
    }
    @GetMapping("/get1")
    @ResponseStatus(HttpStatus.OK)
    public void test(@RequestParam String url, @RequestParam int depth) {
        crawler.crawl(url, depth);
    }
}
