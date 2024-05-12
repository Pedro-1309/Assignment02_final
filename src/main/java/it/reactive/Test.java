package it.reactive;

import io.reactivex.rxjava3.core.Observable;
import it.common.Report;

import java.util.ArrayList;
import java.util.List;

public class Test {

    private static final String WIKIPEDIA = "https://it.wikipedia.org/wiki/Pagina_principale";
    private static final String TAGLIATA = "https://it.wikipedia.org/wiki/Tagliata_(Cervia)";
    private static final String ADIDAS = "https://www.adidas.it";
    private static final String TRENITALIA = "https://www.trenitalia.com/it.html";
    private static WordCounter wordCounter = new WordCounter();

    public static void main(String[] args) {
        String word = "il";
        long startTime = System.currentTimeMillis();
        Observable<Report> reports = wordCounter.getWordOccurrencesObservable(TAGLIATA, word, 2);
        List<Report> result = new ArrayList<>();
        reports.subscribe(
                result::add,
                r -> System.out.println(" ERRORE \n" + r),
                () -> {
                    System.out.println(" Time elapsed: " + (System.currentTimeMillis() - startTime) + " ms");
                    result.forEach(report -> System.out.println("Word \"" + word + "\" found " + report.wordCount() + " times in " + report.url() + " (depth: " + report.depth() + ")"));
                });
        reports.blockingSubscribe();
    }
}
