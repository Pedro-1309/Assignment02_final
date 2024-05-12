package it.virtualthreads;

import it.common.Report;
import it.common.ReportObserver;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WordCounterGUI extends JFrame implements ReportObserver {
    private WordCounterTextArea textArea;

    private WordCounter wordCounter = new WordCounter(this);

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

        startButton.addActionListener(e -> {
            String urlText = urlTextField.getText();
            String wordText = wordTextField.getText();
            String depthText = depthTextField.getText();
            textArea.clear();
            textArea.setWord(wordText);
            int depth;
            try {
                depth = Integer.parseInt(depthText);
            } catch (NumberFormatException exception) {
                depth = 0;
            }
            if (!urlText.isEmpty() && !wordText.isEmpty() && depth>0) {
                wordCounter.getWordOccurrences(urlText, wordText, depth);
            }
        });

        stopButton.addActionListener(e -> wordCounter.stop());

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
    public void notifyNewReport(Report report) {
        textArea.update(report);
    }


    class WordCounterTextArea extends JTextArea {

        List<Report> reports = new ArrayList<>();
        private String word;

        public WordCounterTextArea(int w, int h){
        }

        public void update(Report newReport) {
            this.reports.add(newReport);
            StringBuilder stringBuilder = new StringBuilder();
            this.reports.forEach(report ->
                stringBuilder
                        .append("Word \"")
                        .append(word)
                        .append("\" found ")
                        .append(report.wordCount())
                        .append(" times in ")
                        .append(report.url())
                        .append(" (depth: ")
                        .append(report.depth())
                        .append(")")
                        .append("\n"));
            this.setText(stringBuilder.toString());
            this.repaint();
        }

        public void setWord(String wordText) {
            this.word = wordText;
        }

        public void clear() {
            this.reports.clear();
            this.setText("");
            this.repaint();
        }
    }
}
