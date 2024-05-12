package it.reactive;

import org.jsoup.nodes.Element;

public class SearchInput {

    private final String url;
    private final String word;
    private final int depth;
    private final int maxDepth;
    private Element body;

    public SearchInput(String url, String word, int depth, int maxDepth) {
        this.url = url;
        this.word = word;
        this.depth = depth;
        this.maxDepth = maxDepth;
    }

    public String getUrl() {
        return url;
    }

    public String getWord() {
        return word;
    }

    public int getDepth() {
        return depth;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public Element getBody() {
        return body;
    }

    public void setBody(Element body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "SearchInput{" +
                "url='" + url + '\'' +
                ", word='" + word + '\'' +
                ", depth=" + depth +
                '}';
    }
}
