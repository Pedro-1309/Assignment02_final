package it.virtualthreads;

import it.common.Report;
import it.common.ReportObserver;
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
    private final boolean bulkResult;
    private final ReentrantLock lock = new ReentrantLock();

    public WordCounter(ReportObserver observer) {
        this.bulkResult = false;
        this.observer = observer;
    }

    public WordCounter() {
        this.bulkResult = true;
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
                if (bulkResult) {
                    concurrentLinkedQueue.add(new Report(url, wordCounter, currentDepth));
                } else {
                    lock.lock();
                    if (observer != null && isRunning()) {
                        observer.notifyNewReport(new Report(url, wordCounter, currentDepth));
                    }
                    lock.unlock();
                }
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
                if (bulkResult) {
                    threads.forEach(t -> {
                        try {
                            t.join();
                        } catch (Exception ignored) {}
                    });
                }
            }
        } catch (Exception ignored) {}
    }

    private Thread mainThread;

    public void getWordOccurrences(String url, String word, int depth) {
        start();
        long startTime = System.currentTimeMillis();
        Thread.ofVirtual()
                .name("master")
                .start(() -> {
                    mainThread = Thread
                            .ofVirtual()
                            .name("myVirtualThread-0")
                            .start(() -> getWordOccurrences(url, word, depth, initialDepth));
                    if (bulkResult) {
                        try {
                            mainThread.join();
                        } catch (InterruptedException ignored) {
                        } finally {
                            System.out.println(" Elapsed time: " + (System.currentTimeMillis() - startTime) + " ms");
                            concurrentLinkedQueue.forEach(report -> System.out.println("Word \"" + word + "\" found " + report.wordCount() + " times in " + report.url() + " (depth: " + report.depth() + ")"));
                        }
                    }
                });
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
