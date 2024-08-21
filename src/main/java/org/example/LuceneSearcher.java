package org.example;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

public class LuceneSearcher {
    static final String QUERY = "NOT title:programming";
    static final String defaultField = "title";
    static Directory directory = new ByteBuffersDirectory();
    static Analyzer analyzer = new StandardAnalyzer();

    public static void main(String[] args) throws Exception {
        makeIndex();
        search();
    }

    static void makeIndex() throws IOException {
        IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer));

        Document d1 = new Document();
        d1.add(new TextField(defaultField, "programming in Java", Field.Store.YES));
        writer.addDocument(d1);

        Document d2 = new Document();
        d2.add(new TextField(defaultField, "python", Field.Store.YES));
        writer.addDocument(d2);
        writer.close();
    }

    static void search() throws Exception {
        QueryParser parser = new QueryParser(defaultField, analyzer);
        Query query = parser.parse(QUERY);

        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs topDocs = searcher.search(query, 10);
            System.out.printf("parsed query: %s \n", query);
            System.out.printf("total hits: %s \n", topDocs.totalHits);
        }
    }
}
