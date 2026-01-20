package org.crawler;

public class App {
    public static void main(String[] args) {
        final int maxDepth = 1;
        final String target = "https://www.rakuten.co.jp/";

        Crawler crawler = new Crawler(maxDepth);
        crawler.crawl(target, 1);
        crawler.shutdown();
        final String path = VisitedResource.getFilePath(target);
        System.out.println("Output: " + path);
    }
}
