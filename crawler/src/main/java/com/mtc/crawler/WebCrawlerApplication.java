package com.mtc.crawler;

import com.mtc.crawler.service.RobotstxtParser;
import com.mtc.crawler.service.WebCrawler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;

@SpringBootApplication
public class WebCrawlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebCrawlerApplication.class, args);
		RobotstxtParser parser = new RobotstxtParser();
		WebCrawler crawler = new WebCrawler(parser);
		crawler.crawl(1, "https://twitter.com", new ArrayList<>());
	}

}
