package org.example;

import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

public class LuceneSearcher {
    private final Directory indexDirectory;
    private final String defaultField;

    public LuceneSearcher(Analyzer analyzer, List<Document> documents, String defaultField) throws Exception {
        this.defaultField = defaultField;
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        this.indexDirectory = new ByteBuffersDirectory();
        try (IndexWriter writer = new IndexWriter(indexDirectory, config)) {
            for (Document document : documents) {
                writer.addDocument(document);
            }
            writer.commit();
        }
    }

    public long search(Analyzer analyzer, String query) throws Exception {
        try (DirectoryReader reader = DirectoryReader.open(indexDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser(defaultField, analyzer);
            Query q = parser.parse(query);
            System.out.println("Parsed query: " + q);
            TopDocs topDocs = searcher.search(q, Integer.MAX_VALUE);
            for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                int docId = topDocs.scoreDocs[i].doc;
                Document d = searcher.storedFields().document(docId);
                System.out.println(d.get(defaultField));
            }
            return topDocs.totalHits.value;
        }
    }
}
