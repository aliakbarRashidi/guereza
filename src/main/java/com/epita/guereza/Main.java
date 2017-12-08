package com.epita.guereza;

import com.epita.guereza.domain.Document;
import com.epita.guereza.domain.Index;
import com.epita.guereza.domain.RawDocument;
import com.epita.guereza.indexer.IndexerService;
import com.epita.guereza.winter.Scope;
import com.epita.guereza.winter.provider.Provider;
import com.epita.guereza.winter.provider.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.debug("Starting crawler");

        final Index index = new Index();
        final Repo repo = new RepoStore();

        repo.store(new String[]{"https://www.bbc.co.uk/food/recipes/saladenicoise_6572"});
        testWinter(repo);
        //testCrawl(repo);
        //testIndexing(repo, index);
        //testSearch(index, "onions courgettes pepper");
    }

    private static void testWinter(final Repo repo) {
        Scope scope = new Scope();

        //scope.bean(Repo.class, repo);
        //scope.provide(Repo.class, new Singleton<>(repo));
        //scope.provide(Repo.class, new Prototype<>((s) -> new RepoStore()));

        Provider<?> provider = new Singleton<>(repo);
        provider.before(Scope.getMethod(Repo.class, "nextUrl"), (s) -> System.out.println("before::"));
        provider.after(Scope.getMethod(Repo.class, "nextUrl"), (s) -> System.out.println("after::"));
        scope.provide(Repo.class, provider);

        Repo r = scope.instanceOf(Repo.class);
        System.out.println(r.nextUrl());
    }

    private static void testCrawl(final Repo repo) {
        CrawlerService crawler = new CrawlerService();

        int limit = 20;
        while (limit-- > 0) {
            final String url = repo.nextUrl();
            if (url == null)
                break;

            final RawDocument doc = crawler.crawl(url);
            if (doc == null)
                continue;

            final String[] urls = crawler.extractUrl(doc);
            repo.store(urls);
        }
    }

    private static void testIndexing(final Repo repo, final Index index) {
        IndexerService indexer = new IndexerService();

        String url = repo.nextUrl();
        int limit = 300;
        while (url != null && limit-- > 0) {
            final Document d = indexer.index(url);
            url = repo.nextUrl();

            if (d == null)
                continue;
            indexer.publish(index, d);
        }
    }

    private static void testSearch(final Index index, final String query) {
        IndexerService indexer = new IndexerService();

        System.out.printf("Results for '%s'\n", query);

        final Map<Document, Double> res = indexer.search(index.docs, query);
        for (final Map.Entry<Document, Double> doc : res.entrySet()) {
            System.out.printf("%100s %f\n", doc.getKey().url, doc.getValue());
        }
    }
}
