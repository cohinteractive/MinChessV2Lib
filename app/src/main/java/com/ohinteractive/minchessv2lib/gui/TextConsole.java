package com.ohinteractive.minchessv2lib.gui;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class TextConsole {

    private final VBox container;
    private final TextFlow flow;
    private final ScrollPane scroll;

    public TextConsole() {
        container = new VBox();
        flow = new TextFlow();
        scroll = new ScrollPane(flow);
        scroll.setFitToWidth(true);
        container.getChildren().add(scroll);
    }

    public void appendText(String text) {
        Text line = new Text(text + "\n");
        flow.getChildren().add(line);
        scroll.setVvalue(1.0); // Scroll to bottom
    }

    public Node getNode() {
        return container;
    }
}

