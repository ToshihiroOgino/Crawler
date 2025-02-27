package org.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtil {
    public static String getTmpDirPath() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        if (!tmpDir.endsWith(java.io.File.separator)) {
            tmpDir += java.io.File.separator;
        }
        tmpDir += "crawler" + java.io.File.separator;
        return tmpDir;
    }

    public static String cleanURL(String url) {
        if (url.startsWith("http://")) {
            url = url.substring(7);
        } else if (url.startsWith("https://")) {
            url = url.substring(8);
        }
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.contains("#")) {
            url = url.substring(0, url.indexOf("#"));
        }
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static String generateFilePath(String url) {
        url = cleanURL(url).replace("https://", "").replace("http://", "");
        String filePath = url;

        if (filePath.length() > 100) {
            String ext = "";
            if (filePath.contains(".")) {
                ext = filePath.substring(filePath.lastIndexOf("."));
                if (ext.length() >= 5) {
                    ext = "";
                }
            }
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                var hashBytes = md.digest(filePath.getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hashBytes) {
                    hexString.append(String.format("%02x", b));
                }
                filePath = hexString.toString() + ext;
            } catch (NoSuchAlgorithmException e) {
                System.out.println("MD5 not found");
                filePath = filePath.substring(0, 100) + ext;
            }
        }

        // replace all invalid char
        filePath = filePath.replaceAll("[:?=<>|*]", "_");

        return getTmpDirPath() + filePath;
    }

    public static String generateHTMLFilePath(String url) {
        String filePath = generateFilePath(url);

        if (filePath.endsWith(File.separator)) {
            filePath += "index.html";
        }

        if (!filePath.endsWith(".html")) {
            filePath += ".html";
        }
        return filePath;
    }

    public static String generateFilePath(String url, String contentType) {
        String filePath = generateFilePath(url);

        if (contentType == null) {
            contentType = "";
        }

        final String ext = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            case "image/bmp" -> ".bmp";
            case "image/x-icon" -> ".ico";
            case "image/vnd.microsoft.icon" -> ".ico";
            case "image/tiff" -> ".tiff";
            case "image/vnd.adobe.photoshop" -> ".psd";
            case "image/x-xcf" -> ".xcf";
            case "image/vnd.dwg" -> ".dwg";
            case "image/vnd.dxf" -> ".dxf";
            case "image/vnd.fpx" -> ".fpx";
            case "image/vnd.net-fpx" -> ".npx";
            case "image/vnd.wap.wbmp" -> ".wbmp";
            case "image/x-xbitmap" -> ".xbm";
            case "image/x-xpixmap" -> ".xpm";
            case "image/x-xwindowdump" -> ".xwd";
            case "image/x-portable-anymap" -> ".pnm";
            case "image/x-portable-bitmap" -> ".pbm";
            case "image/x-portable-graymap" -> ".pgm";
            case "image/x-portable-pixmap" -> ".ppm";
            case "image/x-rgb" -> ".rgb";
            case "application/javascript" -> ".js";
            case "text/css" -> ".css";
            case "text/html" -> ".html";
            case "text/xml" -> ".xml";
            case "text/plain" -> ".txt";
            case "application/json" -> ".json";
            case "application/xml" -> ".xml";
            case "application/rss+xml" -> ".rss";
            default -> "";
        };

        if (!filePath.endsWith(ext)) {
            filePath += ext;
        }
        return filePath;
    }

    private static File createFile(String filePath) throws IOException {
        File file = new java.io.File(filePath);
        file.getParentFile().mkdirs();
        file.createNewFile();
        return file;
    }

    public static void saveToFile(String content, String filePath) {
        try {
            File file = createFile(filePath);
            try (FileWriter writer = new java.io.FileWriter(file)) {
                writer.write(content);
            }
        } catch (IOException e) {
            System.err.println("Error saving to " + filePath);
        }
    }

    public static void saveToFile(byte[] content, String filePath) {
        try {
            java.io.File file = createFile(filePath);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content);
            }
        } catch (java.io.IOException e) {
            System.err.println("Error saving to " + filePath);
        }
    }
}
