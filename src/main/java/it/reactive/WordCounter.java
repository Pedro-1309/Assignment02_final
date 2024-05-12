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

    private List<SearchInput> getChildSearches(SearchInput input) {
        return input.getBody().getElementsByTag("a")
                .stream()
                .filter(link -> link.attr("href").startsWith("https://"))
                .map(link -> new SearchInput(
                        link.attr("href"),
                        input.getWord(),
                        input.getDepth() + 1
                )).toList();
    }

    private Observable<SearchInput> searchInputs;
    private Disposable linkedPageSupplier;

    public Observable<Report> getWordOccurrencesObservable(String url, String word, int maxDepth) {
        searchInputs = Observable.create(emitter -> {
            int depth = 0;
            List<SearchInput> previousSearches;
            List<SearchInput> newSearches = new ArrayList<>();
            SearchInput root = new SearchInput(url, word, depth);
            root.setBody(getBody(root.getUrl()));
            newSearches.add(root);
            emitter.onNext(root);
            depth++;
            while (depth <= maxDepth) {
                previousSearches = new ArrayList<>(newSearches);
                newSearches.clear();
                previousSearches.stream()
                        .map(this::getChildSearches)
                        .forEach(children -> {
                            children.forEach(search -> {
                                    search.setBody(getBody(search.getUrl()));
                                    if (search.getBody() != null) {
                                        //log(" produced " + search);
                                        newSearches.add(search);
                                        emitter.onNext(search);
                                    }
                            });
                        });
                depth++;
            }
            emitter.onComplete();
        });
        return searchInputs.subscribeOn(Schedulers.computation())
                .map(this::getReport)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    static private void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
    }
}
