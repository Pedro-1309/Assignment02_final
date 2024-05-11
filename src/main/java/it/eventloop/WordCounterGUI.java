package it.eventloop;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WordCounterGUI extends JFrame implements ReportObserver {
    private WordCounterTextArea textArea;
    private WordCounterVerticle wordCounterVerticle;

    private class GUIVerticle extends AbstractVerticle {

        public void start(Promise<Void> startPromise) {
            log("started.");
            EventBus eb = this.getVertx().eventBus();
            eb.consumer("report", message -> {
                System.out.println(message);
                textArea.update(message.body().toString());
            });
            log("Ready.");
            startPromise.complete();
        }

        private void log(String msg) {
            System.out.println("[REACTIVE AGENT] ["+Thread.currentThread()+"] " + msg);
        }
    }

    public WordCounterGUI() {
        super("WordCounter View");
        setSize(1500,600);

        JPanel inputPanel = new JPanel();
        JLabel urlLabel = new JLabel("Url:");
        JTextField urlTextField = new JTextField(20);
        JLabel wordLabel = new JLabel("Word:");
        JTextField wordTextField = new JTextField(20);
        JLabel depthLabel = new JLabel("Depth:");
        JTextField depthTextField = new JTextField(20);
        inputPanel.add(urlLabel);
        inputPanel.add(urlTextField);
        inputPanel.add(wordLabel);
        inputPanel.add(wordTextField);
        inputPanel.add(depthLabel);
        inputPanel.add(depthTextField);

        JPanel buttonPanel = new JPanel();
        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new GUIVerticle());
        startButton.addActionListener(e -> {
            String urlText = urlTextField.getText();
            String wordText = wordTextField.getText();
            String depthText = depthTextField.getText();
            int depth;
            try {
                depth = Integer.parseInt(depthText);
            } catch (NumberFormatException exception) {
                depth = 0;
            }
            if (!urlText.isEmpty() && !wordText.isEmpty() && depth>0) {
                wordCounterVerticle = new WordCounterVerticle(
                        urlText,
                        wordText,
                        depth,
                        System.currentTimeMillis()
                );
                vertx.deployVerticle(wordCounterVerticle);
            }
        });

        stopButton.addActionListener(e -> {
            vertx.undeploy(wordCounterVerticle.deploymentID());
        });

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BorderLayout());
        menuPanel.add(inputPanel, BorderLayout.WEST);
        menuPanel.add(buttonPanel, BorderLayout.EAST);


        textArea = new WordCounterTextArea(1500,600);
        textArea.setSize(1500, 600);
        textArea.setEditable(false);
        JPanel textAreaPanel = new JPanel();
        LayoutManager textAreaPanelLayout = new BorderLayout();
        textAreaPanel.setLayout(textAreaPanelLayout);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        textAreaPanel.add(BorderLayout.CENTER, scroll);

        JPanel cp = new JPanel();
        LayoutManager layout = new BorderLayout();
        cp.setLayout(layout);
        cp.add(BorderLayout.CENTER, textAreaPanel);
        cp.add(BorderLayout.SOUTH, menuPanel);
        setContentPane(cp);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void display() {
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
        });
    }

    @Override
    public void notifyNewReport(String report) {
        textArea.update(report);
    }


    class WordCounterTextArea extends JTextArea {

        List<String> reports = new ArrayList<>();

        public WordCounterTextArea(int w, int h){
        }

        public void update(String newReport) {
            this.reports.add(newReport);
            StringBuilder stringBuilder = new StringBuilder();
            System.out.println(" è arrivato qualcosa ");
            this.reports.forEach(string ->
                stringBuilder
                        .append(string)
                        .append("\n"));
            this.setText(stringBuilder.toString());
            this.repaint();
        }
    }
}
