package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LuceneSearcherTest {
    private static final String DEFAULT_FIELD = "all";
    private static final List<String> FIELD_VALUES = List.of(
            "John",
            "John Doe",
            "john doe",
            "Doe John",
            "John something Doe"
    );

    public static Stream<Arguments> searchQuires() {
        return Stream.of(
                Arguments.of("all:/(john|doe).+(john|doe)/", 4),
                Arguments.of("john", 5)
        );
    }

    private static final Analyzer INDEX_ANALYZER = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            // source: https://stackoverflow.com/a/65512119/873282
            Tokenizer source = new StandardTokenizer();
            TokenStream tokenStream = source;
            tokenStream = new LowerCaseFilter(tokenStream);
            tokenStream = new ASCIIFoldingFilter(tokenStream); // https://lucene.apache.org/core/6_6_1/analyzers-common/index.html?org/apache/lucene/analysis/miscellaneous/ASCIIFoldingFilter.html
            tokenStream = new ShingleFilter(tokenStream, 2, Integer.MAX_VALUE);
            return new TokenStreamComponents(source, tokenStream);
        }
    };

    private static final Analyzer SEARCH_ANALYZER = new Analyzer() {
        @Override
        protected TokenStreamComponents createComponents(String fieldName) {
            Tokenizer source = new StandardTokenizer();
            TokenStream result = new LowerCaseFilter(source);
            result = new StopFilter(result, EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
            result = new ASCIIFoldingFilter(result);
            return new TokenStreamComponents(source, result);
        }
    };

    private static final List<Document> DOCUMENTS = FIELD_VALUES.stream().map(value -> {
        Document document = new Document();
        document.add(new TextField(DEFAULT_FIELD, value, Field.Store.YES));
        return document;
    }).toList();

    private static LuceneSearcher searcher;
    @BeforeAll
    static void setUp() throws Exception {
        searcher = new LuceneSearcher(INDEX_ANALYZER, DOCUMENTS, DEFAULT_FIELD);
    }

    @ParameterizedTest
    @MethodSource("searchQuires")
    public void testStandardAnalyzer(String query, int expectedHits) throws Exception {
        search(query, expectedHits);
    }

    private void search(String query, int expectedHits) throws Exception {
        long hits = searcher.search(SEARCH_ANALYZER, query);
        assertEquals(expectedHits, hits);
    }
}
