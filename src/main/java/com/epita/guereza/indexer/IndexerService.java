package com.epita.guereza.indexer;

import com.epita.guereza.CrawlerService;
import com.epita.guereza.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

public class IndexerService implements Indexer {
    private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);
    private static final String REGEX_PONCTUATION = "[.!?]";
    private static final String REGEX_SPACE = "\\s+";
    private static final String REGEX_ALPHANUM = "[^-\\dA-Za-z ]";
    private static final String REGEX_SUFFIX_DUPLICATE = "([bdfgmnprt]){2}$";
    private static final String REGEX_SUFFIX_LONG = "(ing|ed|ly|ment|ency|ation|s|ent|e|ous|ator)$";
    private static final String REGEX_SUFFIX_VOWELS = "([oi])es$";


    @Override
    public Document index(final String url) {
        logger.info("indexing {}", url);
        final CrawlerService c = new CrawlerService();
        final RawDocument d = c.crawl(url);
        if (d == null)
            return null;
        final String text = c.extractText(d);

        final String[] sentences = getSentences(text);
        final Map<String, Long> tokens = Arrays.stream(sentences)
                .map(this::getWords)
                .flatMap(Function.identity())
                .collect(groupingBy(Function.identity(), counting()));

        final long totalTokens = tokens.values().stream().mapToLong(i -> i).sum();

        final HashMap<String, Term> terms = new HashMap<>();
        for (final Map.Entry<String, Long> entry : tokens.entrySet()) {
            terms.put(entry.getKey(), new Term(entry.getKey(), null,
                    (double) entry.getValue() / (double) totalTokens));
        }
        return new Document(url, terms);
    }

    @Override
    public HashMap<Document, Double> search(final List<Document> docs, final String query) {
        final HashMap<Document, Double> hits = new HashMap<>();
        for (final String q : getWords(query).toArray(String[]::new)) {
            final double idf = idf(docs, q);
            for (final Document doc : docs) {
                final HashMap<String, Term> terms = doc.getTerms();

                if (terms.containsKey(q)) {
                    final double tfIdf = idf * terms.get(q).getFrequency();
                    final double currentRanking = hits.getOrDefault(doc, 0.0);
                    hits.put(doc, currentRanking + tfIdf);
                }
            }
        }

        return hits;
    }

    @Override
    public void publish(final Index i, final Document d) {
        i.getDocs().add(d);
    }

    private double idf(final List<Document> docs, final String term) {
        double n = 0;
        for (final Document doc : docs) {
            if (doc.getTerms().containsKey(term)) {
                n++;
                break;
            }
        }
        return Math.log(docs.size() / (1 + n));
    }

    private String[] getSentences(final String text) {
        return Arrays.stream(text.split(REGEX_PONCTUATION))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private Stream<String> getWords(final String sentence) {
        return Arrays.stream(sentence.split(REGEX_SPACE))
                .map(String::toLowerCase)
                .map(s -> Normalizer.normalize(s, Normalizer.Form.NFD))
                .map(s -> s.replaceAll(REGEX_ALPHANUM, ""))
                .map(this::stemmed)
                .filter(s -> !s.isEmpty())
                .filter(w -> !StopWords.match(w));
    }

    private String stemmed(final String word) {
        return word.replaceAll(REGEX_SUFFIX_VOWELS, "$1")
                .replaceAll(REGEX_SUFFIX_LONG, "")
                .replaceAll("y$", "i")
                .replaceAll(REGEX_SUFFIX_DUPLICATE, "$1");
    }
}