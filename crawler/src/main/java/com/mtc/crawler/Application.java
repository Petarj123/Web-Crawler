package com.mtc.crawler;

import com.mtc.crawler.repository.ScrapedDataRepository;
import com.mtc.crawler.service.NLP;
import com.mtc.crawler.service.RobotstxtParser;
import com.mtc.crawler.service.WebCrawler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
