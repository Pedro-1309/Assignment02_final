package it.reactive;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import it.common.Report;
import it.virtualthreads.ReportObserver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class WordCounter {

    private Element getBody(String url) {
        try {
            return Jsoup.connect(url).get().body();
        } catch (IOException e) {
            return null;
        }
    }

    private Optional<Report> getReport(SearchInput input) {
        List<String> words = Arrays.stream(input.getBody().text().split(" ")).toList();
        int wordCounter = 0;
        for (String w : words) {
            if (w.equals(input.getWord())) wordCounter++;
        }
        if (wordCounter > 0){
            return Optional.of(new Report(input.getUrl(), wordCounter, input.getDepth()));
        }
        return Optional.empty();
    }

    private Iterable<SearchInput> getChildSearches(SearchInput input) {
        return input.getBody().getElementsByTag("a")
                .stream()
                .filter(link -> link.attr("href").startsWith("https://"))
                .map(link -> new SearchInput(
                        link.attr("href"),
                        input.getWord(),
                        input.getDepth() + 1,
                        input.getMaxDepth()
                )).toList();
    }

    public void getWordOccurrences(String url, String word, int depth) throws InterruptedException {
        List<Report> result = new ArrayList<>();
        PublishSubject<SearchInput> searches = PublishSubject.create();
        Observable<Report> reports = searches
                .filter(input -> input.getDepth() <= input.getMaxDepth())
                .map(input -> {
                    log(" map1: " + input);
                    input.setBody(getBody(input.getUrl()));
                    return input;
                }).filter(input -> input.getBody() != null)
                .map(input -> {
                    log(" map2: " + input);
                    getChildSearches(input).forEach(searches::onNext);
                    return input;
                }).map(this::getReport)
                .filter(Optional::isPresent)
                .map(Optional::get);
        reports.subscribe(
                result::add,
                r -> {},
                () -> result.forEach(System.out::println)
        );
        searches.onNext(new SearchInput(url, word, 0, depth));
        searches.onComplete();
    }

    static private void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
    }
}
