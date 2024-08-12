package org.example;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class NGramAnalyzer extends Analyzer {
    private final CharArraySet stopWords;
    private final int maxGram;

    public NGramAnalyzer(CharArraySet stopWords, int maxGram) {
        this.stopWords = stopWords;
        this.maxGram = maxGram;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();
        TokenStream result = new LowerCaseFilter(source);
        result = new StopFilter(result, stopWords);
        result = new NGramTokenFilter(result, 1, maxGram, true);
        return new TokenStreamComponents(source, result);
    }
}
