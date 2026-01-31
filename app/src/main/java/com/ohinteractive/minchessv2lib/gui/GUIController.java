package com.ohinteractive.minchessv2lib.gui;

import javafx.application.Platform;
import javafx.scene.paint.Color;

public class GUIController {

    public GUIController(BoardCanvas canvas, TextConsole console) {
        this.canvas = canvas;
        this.console = console;
    }

    public void renderPosition(long[] board) {
        Platform.runLater(() -> canvas.drawPosition(board));
    }

    public void logText(String text) {
        Platform.runLater(() -> console.appendText(text));
    }

    public void highlightSquare(int square, Color color) {
        Platform.runLater(() -> {
            canvas.setHighlight(square, color);
            canvas.drawPosition(new long[6]);
        });
    }

    public void clearHighlights() {
        Platform.runLater(() -> {
            canvas.clearHighlights();
            canvas.drawPosition(new long[6]);
        });
    }

    private final BoardCanvas canvas;
    private final TextConsole console;
}

