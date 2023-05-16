package com.mtc.crawler;

import com.mtc.crawler.repository.ScrapedDataRepository;
import com.mtc.crawler.service.RobotstxtParser;
import com.mtc.crawler.service.WebCrawler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ApplicationTests {

	private WebCrawler webCrawler;
	private ScrapedDataRepository mockedRepository;
	private RobotstxtParser parser;
	@Test
	public void testCrawlWithValidUrlAndDepthShouldCrawlSuccessfully() {
		String url = "https://github.com/Petarj123/";
		int maxDepth = 2;
		RobotstxtParser parser = new RobotstxtParser();
		ScrapedDataRepository mockedRepository = Mockito.mock(ScrapedDataRepository.class);

		WebCrawler webCrawler = new WebCrawler(parser, mockedRepository);

		System.out.println(url);
		System.out.println(maxDepth);

		webCrawler.crawl(url, maxDepth);
	}
	@Test
	public void testCrawlerWithIncorrectUrl(){
		String url = "https://sadasdads.com";

		Exception exception = assertThrows(NullPointerException.class, () ->{
			webCrawler.crawl(url, 3);
		});
	}
	@Test
	public void testCrawlWithZeroDepthShouldThrowException() {
		String url = "https://youtube.com";
		int maxDepth = 0;
		String expectedMessage = "Cannot invoke \"com.mtc.crawler.service.WebCrawler.crawl(String, int)\" because \"this.webCrawler\" is null";

		Exception exception = assertThrows(RuntimeException.class, () -> {
			webCrawler.crawl(url, maxDepth);
		});
		assertEquals(expectedMessage, exception.getMessage());
	}
}
