package it.reactive;

public class Test {

    private static final String WIKIPEDIA = "https://it.wikipedia.org/wiki/Pagina_principale";
    private static final String TAGLIATA = "https://it.wikipedia.org/wiki/Tagliata_(Cervia)";
    private static final String ADIDAS = "https://www.adidas.it";
    private static final String TRENITALIA = "https://www.trenitalia.com/it.html";
    private static WordCounter wordCounter = new WordCounter();

    public static void main(String[] args) throws InterruptedException {
        wordCounter.getWordOccurrences(TAGLIATA, "a", 3);
    }
}
