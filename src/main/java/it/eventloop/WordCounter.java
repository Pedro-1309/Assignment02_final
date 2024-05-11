package it.eventloop;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class WordCounter {
    //private ReportObserver observer;

    /*
    public WordCounter(ReportObserver observer) {
        this.observer = observer;
    }
     */

    public void getWordOccurencies(String url, String word, int depth) {
        System.out.println(" Searching word " + word + " in url " + url + " with depth " + depth);
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new WordCounterVerticle(url, word, depth, System.currentTimeMillis()));
    }
}
