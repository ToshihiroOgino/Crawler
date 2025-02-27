package org.crawler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class VisitedResource {
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Map<String, String> visited = new HashMap<>();

    public static void register(String url, String filePath) {
        url = FileUtil.cleanURL(url);
        lock.writeLock().lock();
        try {
            visited.put(url, filePath);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static boolean isVisited(String url) {
        url = FileUtil.cleanURL(url);
        lock.readLock().lock();
        boolean result = false;
        try {
            result = visited.containsKey(url);
        } finally {
            lock.readLock().unlock();
        }
        return result;
    }

    public static String getFilePath(String url) {
        url = FileUtil.cleanURL(url);
        lock.readLock().lock();
        String path = null;
        try {
            path = visited.get(url);
        } finally {
            lock.readLock().unlock();
        }
        return path;
    }
}
