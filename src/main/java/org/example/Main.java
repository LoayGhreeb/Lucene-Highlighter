package org.example;

import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

public class Main {

    static final String[] fields = {
            "JohanssonGustafssonkopp2015Gustafsson",
            "H{\\aa}kan Johansson and Oscar Gustafsson, kopp",
            "2015 {IEEE} International Conference on Digital Signal Processing, {DSP} 2015, Singapore, July 21-24, 2015",
            "Filter-bank based all-digital channelizers and aggregators for multi-standard video distribution",
            "10.1109/ICDSP.2015.7252052",
            "1117--1120",
            "{IEEE}",
            "http://dx.doi.org/10.1109/ICDSP.2015.7252052",
            "dblp computer science bibliography, http://dblp.org",
            "http://dblp.uni-trier.de/rec/bib/conf/icdsp/JohanssonG15a",
            ":D\\:/tugboat-mirror-main/tugboat_issues/tb02complete.pdf:PDF;:D\\:/tugboat-mirror-main/tugboat_issues/tb01complete.pdf:PDF",
            "By rating",
            "rank4",
            "read",
            "Tue, 22 Sep 2015 18:08:19 +0200 kopp",
            "2015",
    };

    static final String QUERY = "issue";
    static final String defaultField = "f";
    static Directory dir = new ByteBuffersDirectory();
    static Analyzer analyzer = new NGramAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET, Integer.MAX_VALUE);

    public static void main(String[] args) throws Exception {
        makeIndex();
        search();
    }

    static void makeIndex() throws IOException {
        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(analyzer));

        Document document = new Document();
        for(String field : fields) {
            document.add(new TextField(defaultField, field, Field.Store.YES));
        }
        writer.addDocument(document);
        writer.close();
    }

    static void search() throws Exception {
        QueryParser parser = new QueryParser(defaultField, new StandardAnalyzer());
        Query query = parser.parse(QUERY);

        try (IndexReader reader = DirectoryReader.open(dir)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(query, 10);
            System.out.printf("QUERY : %s \n", QUERY);
            System.out.printf("total hits : %s \n", docs.totalHits);

            Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<mark>", "</mark>"), new QueryScorer(query));
            highlighter.setTextFragmenter(new NullFragmenter());

            String highlightedText = highlighter.getBestFragment(analyzer, defaultField, String.join("\n", fields));
            System.out.printf("highlightedText: \n %s", highlightedText);
        }
    }
}
