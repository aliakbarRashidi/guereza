package com.epita.guereza;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.debug("Starting crawler");

        Repo repo = new Repo();


        repo.store(new String[]{"https://en.wikipedia.org/wiki/Halifax_Explosion"});
        Indexer indexer = new Indexer();
        indexer.index(repo.nextUrl());

        Crawler crawler = new Crawler();

        /*
        while (true) {
            String url = repo.nextUrl();
            if (url == null)
                break;

            RawDocument doc = crawler.crawl(url);
            if (doc == null)
                continue;

            String[] urls = crawler.extractUrl(doc);
            repo.store(urls);
        }
        */
    }
}
