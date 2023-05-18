package com.mtc.crawler.controller;

import com.mtc.crawler.service.WebCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/crawler")
@RequiredArgsConstructor
public class CrawlerController {

    private final WebCrawler webCrawler;

    @GetMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    private void start(@RequestParam String url, @RequestParam int maxDepth, @RequestParam int numOfThreads){
        webCrawler.start(url, maxDepth, numOfThreads);
    }
}
