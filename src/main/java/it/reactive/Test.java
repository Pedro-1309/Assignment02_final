package it.reactive;

import io.reactivex.rxjava3.core.Observable;
import it.common.Report;

public class Test {

    private static final String WIKIPEDIA = "https://it.wikipedia.org/wiki/Pagina_principale";
    private static final String TAGLIATA = "https://it.wikipedia.org/wiki/Tagliata_(Cervia)";
    private static final String ADIDAS = "https://www.adidas.it";
    private static final String TRENITALIA = "https://www.trenitalia.com/it.html";
    private static WordCounter wordCounter = new WordCounter();

    public static void main(String[] args) {
        String word = "a";
        Observable<Report> reports = wordCounter.getWordOccurrencesObservable();
        reports.subscribe(report -> System.out.println("Word \"" + word + "\" found " + report.wordCount() + " times in " + report.url() + " (depth: " + report.depth() + ")"));
        wordCounter.supply(TAGLIATA, word, 1);
        reports.blockingSubscribe();
    }
}
