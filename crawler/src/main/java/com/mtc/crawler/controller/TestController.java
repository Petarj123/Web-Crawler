package com.mtc.crawler.controller;

import com.mtc.crawler.mode.ScrapedData;
import com.mtc.crawler.repository.ScrapedDataRepository;
import com.mtc.crawler.service.WebCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    private final WebCrawler crawler;
    private final ScrapedDataRepository scrapedDataRepository;

    @GetMapping("/get")
    public void test(@RequestParam String url){
        crawler.crawl(url);
    }
    @PostMapping("/p")
    public void post(@RequestBody ScrapedData data){
        ScrapedData data1 = ScrapedData.builder()
                .URL(data.getURL())
                .text(data.getText())
                .scrapedAt(new Date())
                .build();
        scrapedDataRepository.save(data);
    }

}
