package com.exactprosystems.jf;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
        Scene scene = new Scene(root, 800, 800);
        scene.getStylesheets().add(getClass().getResource("Main.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("JavaFx Mock");
        stage.show();
    }
}
