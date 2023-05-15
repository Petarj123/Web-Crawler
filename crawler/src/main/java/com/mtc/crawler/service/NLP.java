package com.mtc.crawler.service;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Service
public class NLP {

    public boolean isAbout(Document document, String query){
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        props.setProperty("ner.useSUTime", "0");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Title check
        String title = document.title();
        if (containsNamedEntity(pipeline, title, query)){
            return true;
        }

        // Headers check
        Elements headers = document.select("h1, h2, h3, h4, h5, h6");
        for (Element header : headers){
            if (containsNamedEntity(pipeline, header.text(), query)){
                return true;
            }
        }

        // Body check
        String bodyText = document.body().text();
        if (containsNamedEntity(pipeline, bodyText, query)) {
            return true;
        }

        return false;
    }

    private boolean containsNamedEntity(StanfordCoreNLP pipeline, String text, String entity) {
        Annotation document = new Annotation(text);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences){
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)){
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                if (ne.equals(entity)){
                    return true;
                }
            }
        }
        return false;
    }
}
