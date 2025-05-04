package com.ohinteractive.minchessv2lib.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ChessGUI extends Application {

    private static GUIController controller;

    public static void launchGUI() {
        Application.launch();
    }

    public static GUIController getController() {
        return controller;
    }

    @Override
    public void start(Stage stage) {
        BoardCanvas boardCanvas = new BoardCanvas();
        TextConsole textConsole = new TextConsole();

        controller = new GUIController(boardCanvas, textConsole);

        BorderPane root = new BorderPane();
        root.setCenter(boardCanvas);
        root.setBottom(textConsole.getNode());

        Scene scene = new Scene(root, 600, 700);
        stage.setTitle("Chess Engine Debug GUI");
        stage.setScene(scene);
        stage.show();
    }
}
