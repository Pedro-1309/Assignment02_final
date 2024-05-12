package it.reactive;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import it.common.Report;
import it.common.ReportObserver;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

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

    private PublishSubject<SearchInput> searches;
    private Disposable linkedPageSupplier;

    public Observable<Report> getWordOccurrencesObservable() {
        searches = PublishSubject.create();
        Observable<SearchInput> fullInputs = searches
                .subscribeOn(Schedulers.computation())
                .map(input -> {
                    //log(input.toString());
                    input.setBody(getBody(input.getUrl()));
                    return input;
                }).filter(input -> input.getBody() != null);
        Observable<Report> reports = fullInputs.map(this::getReport)
                .filter(Optional::isPresent)
                .map(Optional::get);
        linkedPageSupplier = fullInputs.subscribe(input -> {
            if (input.getDepth() < input.getMaxDepth())
                getChildSearches(input).forEach(searches::onNext);
        });
        return reports;
    }

    public void supply(String url, String word, int depth) {
        searches.onNext(new SearchInput(url, word, 0, depth));
    }

    public void stop() {
        linkedPageSupplier.dispose();
    }

    static private void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
    }
}
