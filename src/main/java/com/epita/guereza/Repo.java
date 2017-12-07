package com.epita.guereza;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

public class Repo implements IRepo {
    private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);

    private Set<String> urlDone;
    private Set<String> urlDoing;
    private Set<String> urlTodo;

    public Repo() {
        urlDone = new LinkedHashSet<>();
        urlDoing = new LinkedHashSet<>();
        urlTodo = new LinkedHashSet<>();
    }

    @Override
    public void store(String[] urls) {
        for (String url : urls) {
            if (url == null || url.isEmpty())
                continue;

            if (!urlDone.contains(url) && !urlDoing.contains(url))
                urlTodo.add(url);
        }
    }

    @Override
    public String nextUrl() {
        if (!urlTodo.isEmpty()) {
            // There is still
            String url = urlTodo.iterator().next();
            urlTodo.remove(url);
            urlDone.add(url);
            return url;
        }
        logger.warn("No more url to analyse.");
        return null;
    }
}
