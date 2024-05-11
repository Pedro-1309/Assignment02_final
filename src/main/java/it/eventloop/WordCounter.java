package it.eventloop;

import io.vertx.core.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class WordCounter {

    private long startTime;

    private class WordCounterVerticle extends AbstractVerticle {

        // private int x = 0;

        public void start() {
            log("before");

            Promise<List<Report>> p = Promise.promise();
            // x++;

            Promise<List<Report>> result = getWordOccurencies(url, word, maxDepth, initialDepth, this.getVertx());

            log("after triggering a blocking computation...");
            // x++;

            result.future().onSuccess(r -> {
                printReport(r);
                log(" Ended in " + (System.currentTimeMillis() - startTime) + " ms");
            });
        }
        private void log(String msg) {
            System.out.println("[REACTIVE AGENT] ["+Thread.currentThread()+"] " + msg);
        }
    }

    private final int initialDepth = 0;
    private String url;
    private String word;
    private int maxDepth;

    private Promise<List<Report>> getWordOccurencies(String url, String word, int maxDepth, int currentDepth, Vertx vertx) {
        int wordCounter = 0;
        String docText;
        Promise<List<Report>> promise = Promise.promise();
        List<Report> reports = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();
            Element body = doc.body();

            //Word Counter
            docText = body.text();
            List<String> words = Arrays.stream(docText.split(" ")).toList();
            for (String w : words){
                if (w.equals(word)) wordCounter++;
            }
            if (wordCounter > 0){
                reports.add(new Report(url, wordCounter, currentDepth));
                //
            }

            //Visit all the links
            List<Promise<List<Report>>> partialResults = new ArrayList<>();
            if (currentDepth < maxDepth) {
                Elements links = body.getElementsByTag("a");
                for (Element link : links){
                    if (link.attr("href").startsWith("https://")) {
                        partialResults.add(
                                getWordOccurencies(
                                        link.attr("href"),
                                        word,
                                        maxDepth,
                                        currentDepth+1,
                                        vertx
                                )
                        );
                    }
                }
            }
            Future.all(partialResults.stream().map(Promise::future).toList())
                    .onSuccess((partialResult) -> {
                        partialResult.result().list().forEach(
                                el -> reports.addAll((Collection<? extends Report>) el)
                        );
                        promise.complete(reports);
                    });
        } catch (Exception e) {
            promise.fail(e);
        }
        return promise;
    }

    public void getWordOccurencies(String url, String word, int depth) {
        this.url = url;
        this.word = word;
        this.maxDepth = depth;
        this.startTime = System.currentTimeMillis();
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new WordCounterVerticle());
    }

    private void printReport(List<Report> reports) {
        reports.forEach(report -> System.out.println("Word \"" + word + "\" found " + report.wordCount() +
                " times in " + report.url() + " (depth: " + report.depth() + ")"));
    }
}
