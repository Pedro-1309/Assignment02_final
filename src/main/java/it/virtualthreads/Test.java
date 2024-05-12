package it.virtualthreads;

public class Test {

    private static final String WIKIPEDIA = "https://it.wikipedia.org/wiki/Pagina_principale";
    private static final String TAGLIATA = "https://it.wikipedia.org/wiki/Tagliata_(Cervia)";
    private static final String ADIDAS = "https://www.adidas.it";
    private static final String TRENITALIA = "https://www.trenitalia.com/it.html";
    private static final WordCounter wordCounter = new WordCounter();

    public static void main(String[] args) throws InterruptedException {
        //Visita di pagine online
//        Document doc = Jsoup.connect(TRENITALIA).get();
//        System.out.println(doc.title());
//        for (Element link : doc.body().getElementsByTag("a")){
//            if (link.attr("href").startsWith("https://")) {
//                System.out.println(link.attr("href"));
//            }
//        }
        //Tutto il body -> html
//        Document docFromFile = Jsoup.parse(new File("index.html"), "UTF-8");
//        Element body = docFromFile.body();
//        System.out.println(body);
//
        //Link
//        Elements links = body.getElementsByTag("a");
//        for (Element link : links){
//            System.out.println(link.attr("href"));
//        }
//
        //Testo della pagina: (stampa anche il testo dei button)
//        System.out.println(body.text());
        wordCounter.getWordOccurrences(TAGLIATA, "il", 3);
        Thread.sleep(100000);
    }
}
