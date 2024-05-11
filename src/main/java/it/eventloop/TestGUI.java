package it.eventloop;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import java.util.List;

public class TestGUI {

    public static void main(String[] args) {
        WordCounterGUI wordCounterGUI = new WordCounterGUI();
        wordCounterGUI.display();
    }
}
