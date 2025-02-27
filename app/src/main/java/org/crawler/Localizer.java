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
            Response res = Jsoup.connect(srcURL).timeout(5 * 1000).ignoreContentType(true).execute();
            final String contentType = res.contentType();
            final String filePath = FileUtil.generateFilePath(srcURL, contentType);
            FileUtil.saveToFile(res.bodyAsBytes(), filePath);
            VisitedResource.register(srcURL, filePath);
            return filePath;
        } catch (IOException e) {
            System.err.println("Error fetching " + srcURL);
        }
        return srcURL;
    }

    public static void localize(Document doc) {
        Elements depFiles = doc.select("img, link[rel=stylesheet], script, source");

        for (Element element : depFiles) {
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

            if (srcURL.startsWith("data:") || srcURL.equals("")) {
                continue;
            }

            String path = DownloadContent(srcURL);
            element.attr(attrName, path);
        }
    }
}
