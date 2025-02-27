package org.crawler;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {
    private final int maxDepth;
    private final ExecutorService executor = Executors.newFixedThreadPool(32);

    public Crawler(final int maxDepth) {
        this.maxDepth = maxDepth;
    }

    private Document fetchDocument(final String url) {
        try {
            return Jsoup.connect(url).timeout(5 * 1000).get();
        } catch (IOException e) {
            System.err.println("Error fetching " + url);
            return null;
        }
    }

    private void crawlLinks(Document doc, int currentDepth) {
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String linkURL = link.attr("abs:href");
            if (!(linkURL.startsWith("https:") || linkURL.startsWith("http:"))) {
                continue;
            }

            String path = VisitedResource.getFilePath(linkURL);
            if (path == null) {
                // crawl(linkURL, currentDepth + 1);
                executor.submit(() -> crawl(linkURL, currentDepth + 1));
                path = FileUtil.generateHTMLFilePath(linkURL);
            }
            link.attr("href", path);
        }
    }

    public void crawl(final String url, final int depth) {
        if (depth > maxDepth || VisitedResource.isVisited(url)) {
            return;
        }

        System.out.printf("Crawling (depth: %d/%d) %s\n", depth, maxDepth, url);

        Document doc = fetchDocument(url);
        if (doc == null) {
            System.err.println("Failed to crawl " + url);
            return;
        }

        final String path = FileUtil.generateHTMLFilePath(doc.baseUri());
        VisitedResource.register(url, path);

        Localizer.localize(doc);

        if (depth < maxDepth) {
            crawlLinks(doc, depth);
        }

        FileUtil.saveToFile(doc.html(), path);
        System.out.println("Saved to " + path);
    }

    public void shutdown() {
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Crawler shutdown complete.");
    }
}
