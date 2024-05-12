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
import java.util.concurrent.locks.ReentrantLock;

public class WordCounter {

    ConcurrentLinkedQueue<Report> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
    private int initialDepth = 0;
    private ReportObserver observer;
    private boolean stopped = false;
    private final ReentrantLock lock = new ReentrantLock();

    public WordCounter(ReportObserver observer) {
        this.observer = observer;
    }

    public WordCounter() {
    }

    private void getWordOccurrences(String url, String word, int maxDepth, int currentDepth) {
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
                lock.lock();
                if (observer != null && isRunning()) {
                    observer.notifyNewReport(new Report(url, wordCounter, currentDepth));
                }
                lock.unlock();
                //concurrentLinkedQueue.add(new Report(url, wordCounter, currentDepth));
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
                                getWordOccurrences(link.attr("href"), word, maxDepth, currentDepth + 1);
                            });
                        threads.add(thread);
                    }
                }
                threads.forEach(t -> {
                    try {
                        t.join();
                    } catch (Exception ignored) {};
                });
            }
        } catch (Exception ignored) {}
    }

    public void getWordOccurrences(String url, String word, int depth) {
        start();
        Thread mainThread = Thread
            .ofVirtual()
            .name("myVirtualThread-0")
            .start(() -> getWordOccurrences(url, word, depth, initialDepth));
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

    public synchronized void start() {
        this.stopped = false;
    }
}
