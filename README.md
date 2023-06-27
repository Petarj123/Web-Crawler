# Web Crawler
 
Web Crawler

Description
This project is a Web Crawler designed to traverse the web and scrape relevant data while respecting the rules set out by "robots.txt" files.

Key Components

CrawlerController

This is the main control center for the web crawler. It coordinates the various components, controls the flow of the program, and manages the overall crawling process.

ScrapedData

This class represents the data that the web crawler scrapes from the web.

UrlDepth

The UrlDepth class encapsulates a URL along with its depth in the crawl hierarchy.

RobotstxtParser

This component is designed to read and interpret "robots.txt" files. It can fetch the "robots.txt" file from a given URL and parse the directives within it. It also provides functionality to check if a given URL is allowed to be crawled based on the robots.txt rules.

UserAgentGenerator

This component generates random user-agent strings that are used to identify the web crawler to the servers it interacts with.

WebCrawler

The core class where the actual crawling happens. It manages multiple threads for efficient crawling, respects crawl delays, and stores visited links and scraped data. The scraped data includes the URL, title of the page, and paragraph text.

ScrapedDataRepository

This component is where the scraped data gets stored and managed. It can handle large amounts of data and provides methods for querying and retrieving the stored data.
