package com.mtc.crawler;

import com.mtc.crawler.repository.ScrapedDataRepository;
import com.mtc.crawler.service.RobotstxtParser;
import com.mtc.crawler.service.UserAgentGenerator;
import com.mtc.crawler.service.WebCrawler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ApplicationTests {

	private WebCrawler webCrawler;
	private ScrapedDataRepository mockedRepository;
	private RobotstxtParser parser;
	@Test
	public void testCrawlWithValidUrlAndDepthShouldCrawlSuccessfullyMT() {
		String url = "http://books.toscrape.com/";
		int maxDepth = 2;
		int numThreads = 50;

		RobotstxtParser parser = new RobotstxtParser();
		ScrapedDataRepository mockedRepository = Mockito.mock(ScrapedDataRepository.class);
        UserAgentGenerator agentGenerator = new UserAgentGenerator();
		try (ExecutorService executor = Executors.newFixedThreadPool(numThreads)) {

			for (int i = 0; i < numThreads; i++) {
				int threadId = i;

				executor.execute(() -> {
					WebCrawler webCrawler = new WebCrawler(parser, mockedRepository, agentGenerator);
					webCrawler.crawl(url, maxDepth, threadId);
				});
			}

			executor.shutdown();

			try {
				if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {  // Wait, but not forever
					executor.shutdownNow();  // Cancel currently executing tasks
				}
			} catch (InterruptedException ie) {
				executor.shutdownNow();  // (Re-)Cancel if current thread also interrupted
				Thread.currentThread().interrupt();  // Preserve interrupt status
			}
		}
	}
	@Test
	public void testCrawlWithValidUrlAndDepthShouldCrawlSuccessfully() {
		String url = "http://books.toscrape.com/";
		int maxDepth = 10;
		RobotstxtParser parser = new RobotstxtParser();
		ScrapedDataRepository mockedRepository = Mockito.mock(ScrapedDataRepository.class);
        UserAgentGenerator agentGenerator = new UserAgentGenerator();
		WebCrawler webCrawler = new WebCrawler(parser, mockedRepository, agentGenerator);

		System.out.println(url);
		System.out.println(maxDepth);

		webCrawler.start(url, maxDepth, 12);
	}
	@Test
	public void testCrawlerWithIncorrectUrl(){
		String url = "https://sadasdads.com";

		Exception exception = assertThrows(NullPointerException.class, () ->{
			webCrawler.crawl(url, 3, 12);
		});
	}
	@Test
	public void testCrawlWithZeroDepthShouldThrowException() {
		String url = "https://youtube.com";
		int maxDepth = 0;
		String expectedMessage = "Cannot invoke \"com.mtc.crawler.service.WebCrawler.start(String, int, int)\" because \"this.webCrawler\" is null";

		Exception exception = assertThrows(RuntimeException.class, () -> {
			webCrawler.start(url, maxDepth, 12);
		});
		assertEquals(expectedMessage, exception.getMessage());
	}
}
