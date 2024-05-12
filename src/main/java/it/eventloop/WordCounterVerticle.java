package it.eventloop;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import it.common.Report;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class WordCounterVerticle extends AbstractVerticle {
    private final int initialDepth = 0;
    private final String url;
    private final String word;
    private final int maxDepth;
    private final long startTime;
    private boolean stopped;

    public WordCounterVerticle(String url, String word, int maxDepth, long startTime) {
        this.startTime = startTime;
        this.url = url;
        this.word = word;
        this.maxDepth = maxDepth;
        this.stopped = false;
    }

    @Override
    public void start() {
        log("before");
        // x++;

        Promise<List<Report>> result = getWordOccurencies(url, word, maxDepth, initialDepth);

        /*
        EventBus eb = this.getVertx().eventBus();
        eb.consumer("stopped", message -> {
            log("stopped received");
            stopped = true;
        });
         */

        log("after triggering a promise...");
        // x++;

        result.future().onSuccess(r -> {
            printReport(r);
            log(" Ended in " + (System.currentTimeMillis() - startTime) + " ms");
        });
    }

    private Promise<List<Report>> getWordOccurencies(String url, String word, int maxDepth, int currentDepth) {
        Promise<List<Report>> promise = Promise.promise();
        if (!isStopped()) {
            int wordCounter = 0;
            String docText;
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
                if (wordCounter > 0) {
                    Report newReport = new Report(url, wordCounter, currentDepth);
                    //System.out.println(newReport);
                    reports.add(newReport);
                    EventBus eb = this.getVertx().eventBus();
                    eb.publish("report", "Word \"" + word + "\" found " + newReport.wordCount() +
                            " times in " + newReport.url() + " (depth: " + newReport.depth() + ")");
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
                                            currentDepth+1
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
        } else {
            promise.fail(" search stopped ");
        }
        return promise;
    }

    private synchronized boolean isStopped() {
        return this.stopped;
    }

    private void printReport(List<Report> reports) {
        reports.forEach(report -> System.out.println("Word \"" + word + "\" found " + report.wordCount() +
                " times in " + report.url() + " (depth: " + report.depth() + ")"));
    }

    private void log(String msg) {
        System.out.println("[REACTIVE AGENT] ["+Thread.currentThread()+"] " + msg);
    }

    public synchronized void forceStop() {
        this.stopped = true;
    }
}
