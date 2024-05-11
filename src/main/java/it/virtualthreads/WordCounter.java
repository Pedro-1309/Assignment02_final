package it.virtualthreads;

import it.common.Report;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WordCounter {

    ConcurrentLinkedQueue<Report> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

    private int initialDepth = 0;
    private ReportObserver observer;
    private boolean stopped = true;

    public WordCounter(ReportObserver observer) {
        this.observer = observer;
    }

    public WordCounter() {
    }

    private void getWordOccurencies(String url, String word, int maxDepth, int currentDepth) {
        if (isRunning()) {
            int wordCounter = 0;
            String docText;
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
                    if (observer != null) {
                        observer.notifyNewReport(new Report(url, wordCounter, currentDepth));
                    }
                    concurrentLinkedQueue.add(new Report(url, wordCounter, currentDepth));
                }

                //Visit all the links
                if (currentDepth < maxDepth && isRunning()) {
                    Elements links = body.getElementsByTag("a");
                    int threadCount = 0;
                    List<Thread> threads = new ArrayList<>();
                    for (Element link : links){
                        if (link.attr("href").startsWith("https://") && isRunning()) {
                            Thread thread = Thread
                                .ofVirtual()
                                .name("myVirtualThread-" + currentDepth + "-" + threadCount++)
                                .start(() -> {
                                    getWordOccurencies(link.attr("href"), word, maxDepth, currentDepth + 1);
                                });
                            threads.add(thread);
                        }
                    }
                    /*
                    threads.forEach(t -> {
                        try {
                            t.join();
                        } catch (Exception ignored) {};
                    });
                     */
                }
            } catch (Exception ignored) {}
        }
    }

    public void getWordOccurencies(String url, String word, int depth) {
        this.stopped = false;
        Thread mainThread = Thread
            .ofVirtual()
            .name("myVirtualThread-0")
            .start(() -> getWordOccurencies(url, word, depth, initialDepth));
        /*
        try {
            mainThread.join();
        } catch (InterruptedException ignored) {}
         */
        //concurrentLinkedQueue.forEach(report -> System.out.println("Word \"" + word + "\" found " + report.wordCount() + " times in " + report.url() + " (depth: " + report.depth() + ")"));
    }

    public synchronized boolean isRunning () {
        return !this.stopped;
    }

    public synchronized void stop() {
        this.stopped = true;
    }
}
