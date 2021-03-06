package com.epita.domain;

import java.util.List;
import java.util.Map;

public interface Indexer {

    /**
     * Get a document by url
     *
     * @param url The url to index
     * @return The result document
     */
    Document index(final String text, final String url);

    /**
     * Search a query in a list of document
     *
     * @param docs  The list of documents
     * @param query The string query to search
     * @return The document map result
     */
    Map<Document, Double> search(final List<Document> docs, final String query);

}
