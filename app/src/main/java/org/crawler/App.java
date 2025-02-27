package org.crawler;

public class App {
    public static void main(String[] args) {
        final int maxDepth = 2;
        final String target = "https://www.yahoo.co.jp/";

        Crawler crawler = new Crawler(maxDepth);
        crawler.crawl(target, 1);
        crawler.shutdown();
        final String path = VisitedResource.getFilePath(target);
        System.out.println("Output: " + path);
    }
}
