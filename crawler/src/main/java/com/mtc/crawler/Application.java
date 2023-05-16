package com.mtc.crawler;

import com.mtc.crawler.service.PdfScraper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		PdfScraper scraper = new PdfScraper();
		scraper.extractPdf("https://www.africau.edu/images/default/sample.pdf");
	}

}
