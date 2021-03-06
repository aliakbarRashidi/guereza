package com.epita.guereza.reducer;

import com.epita.domain.SimpleCrawler;
import com.epita.eventbus.EventMessage;
import com.epita.eventbus.client.EventBusClient;
import com.epita.eventsourcing.Event;
import com.epita.eventsourcing.Reducer;
import com.epita.guereza.StringListWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UrlStore implements Reducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCrawler.class);
    private static final String STARTING_URL = "http://www.wikipedia.org";

    private final EventBusClient eventBus;

    private Set<String> allUrls = new HashSet<>();
    private Queue<String> crawlerTodo = new LinkedList<>();
    private Queue<String> indexerTodo = new LinkedList<>();

    public UrlStore(final EventBusClient eventBus) {
        this.eventBus = eventBus;

        final List<String> initialList = new ArrayList<>();
        initialList.add(STARTING_URL);
        store(initialList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void reduce(final Event<?> event) {
        switch (event.type) {
            case "ADD_URLS":
                addUrls((Event<StringListWrapper>) event);
                break;
            case "CRAWLER_REQUEST_URL":
                crawlerRequestUrl((Event<String>) event);
                break;
            case "INDEXER_REQUEST_URL":
                indexerRequestUrl((Event<String>) event);
                break;
        }
    }

    private void store(final List<String> urls) {
        for (final String url : urls) {
            if (url == null || url.isEmpty())
                continue;

            if (!allUrls.contains(url)) {
                allUrls.add(url);
                crawlerTodo.add(url);
                indexerTodo.add(url);
            }
        }
    }

    private void addUrls(final Event<StringListWrapper> event) {
        store(event.obj.list);
        LOGGER.info("added URLs to the repo");
    }

    private void crawlerRequestUrl(final Event<String> event) {
        try {
            LOGGER.info("Still {} urls to crawl", crawlerTodo.size());
            eventBus.publish(new EventMessage(event.obj, crawlerTodo.poll()));
        } catch (final JsonProcessingException e) {
            LOGGER.error("cannot serialize: {}", e.getMessage());
        }
    }

    private void indexerRequestUrl(final Event<String> event) {
        try {
            LOGGER.info("Still {} urls to index", indexerTodo.size());
            eventBus.publish(new EventMessage(event.obj, indexerTodo.poll()));
        } catch (final JsonProcessingException e) {
            LOGGER.error("cannot serialize: {}", e.getMessage());
        }
    }
}
