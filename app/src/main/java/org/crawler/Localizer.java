package org.crawler;

import java.io.IOException;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Localizer {
    private static String DownloadContent(String srcURL) {
        String path = VisitedResource.getFilePath(srcURL);
        if (path != null) {
            return path;
        }

        try {
            Response res = Jsoup.connect(srcURL).timeout(3 * 1000).ignoreContentType(true).execute();
            final String contentType = res.contentType();
            path = FileUtil.generateFilePath(srcURL, contentType);
            VisitedResource.register(srcURL, path);
            FileUtil.saveToFile(res.bodyAsBytes(), path);
        } catch (IOException e) {
            System.err.println("Error fetching " + srcURL);
        }
        return path;
    }

    public static void localize(Document doc) {
        for (Element element : doc.getAllElements()) {
            final String tag = element.tag().getName();
            String srcURL;
            String attrName;
            switch (tag) {
                case "link" -> {
                    srcURL = element.attr("abs:href");
                    attrName = "href";
                }
                case "source" -> {
                    srcURL = element.attr("abs:srcset");
                    attrName = "srcset";
                }
                default -> {
                    srcURL = element.attr("abs:src");
                    attrName = "src";
                }
            }

            if (!(srcURL.startsWith("https:") || srcURL.startsWith("http:"))) {
                continue;
            }

            String path = DownloadContent(srcURL);
            element.attr(attrName, path);
        }
    }
}
