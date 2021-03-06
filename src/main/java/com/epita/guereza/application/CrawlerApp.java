package com.epita.guereza.application;

import com.epita.domain.Crawler;
import com.epita.domain.RawDocument;
import com.epita.eventbus.client.EventBusClient;
import com.epita.guereza.StringListWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlerApp extends App {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerApp.class);

    private final Crawler crawler;
    private final String subscribeUrl;

    public CrawlerApp(final EventBusClient eventBus, final Crawler crawler) {
        super(eventBus);
        this.crawler = crawler;
        this.subscribeUrl = "/request/index/url/" + uid;
    }

    private String[] crawlAndExtract(final String url) {
        final RawDocument doc = crawler.crawl(url);
        if (doc == null)
            return null;

        return crawler.extractUrl(doc);
    }

    private void requestNextUrl() {
        LOGGER.info("Request next URL");
        sendMessage("/request/crawler/url", subscribeUrl);
    }

    private void storeUrls(final String[] urls) {
        if (urls != null) {
            LOGGER.info("Store {} urls", urls.length);
            sendMessage("/store/crawler", new StringListWrapper(urls));
        }
    }

    @Override
    public void run() {
        eventBus.subscribe(subscribeUrl, msg -> {
            if (msg != null) {
                final String url = (String) mappingObject(msg);
                if (url != null) {
                    LOGGER.info("Receive url: {}", url);
                    final String[] urls = crawlAndExtract(url);
                    storeUrls(urls);
                }

                requestNextUrl();
            } else {
                retryIn(30, this::requestNextUrl);
            }
        });

        requestNextUrl();
    }
}
