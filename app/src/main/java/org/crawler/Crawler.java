package org.crawler;

import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler {
    private final int maxDepth;
    private final ExecutorService executor = Executors.newFixedThreadPool(32);
    // 追加
    private final List<Future<?>> futures = new ArrayList<>();

    public Crawler(final int maxDepth) {
        this.maxDepth = maxDepth;
    }

    private Document fetchDocument(final String url) {
        try {
            if (url.equals("https://www.rakuten.co.jp/"))
                return Jsoup
                        .connect(url)
                        .header("User-Agent",
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36")
                        .header("Accept", "*/*")
                        .header("Accept-Language", "ja;q=0.6")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Priority", "u=0, i")
                        .header("Cache-Control", "max-age=0")
                        .header("Sec-Fetch-Dest", "document")
                        .header("Sec-Fetch-Mode", "navigate")
                        .header("Sec-Fetch-Site", "none")
                        .header("Sec-Fetch-User", "?1")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("Pragma", "no-cache")
                        .header("Expires", "0")
                        .timeout(3 * 1000).get();
            else
                return Jsoup.connect(url).timeout(3 * 1000).get();
        } catch (IOException e) {
            System.err.println("Error fetching " + url);
            System.err.println(e.getMessage());
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
                var fut = executor.submit(() -> crawl(linkURL, currentDepth + 1));
                futures.add(fut);
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
        for (var fut : futures) {
            try {
                fut.get();
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error waiting for future: " + e.getMessage());
            }
        }

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
