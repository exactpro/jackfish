package com.exactprosystems.jf.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ClickApplication extends Application {
    // application for acceptance tests.
    @Override public void start(Stage stage) {
        Parent sceneRoot = new ClickPane();
        Scene scene = new Scene(sceneRoot, 400, 200);
        stage.setTitle("App under tests");
        stage.setScene(scene);
        stage.show();
        
        System.err.println("Application is closing");
    }

    // scene object for unit tests
    public static class ClickPane extends StackPane {
        public ClickPane() {
            super();
            this.setId("clickPaneId");
            Button button = new Button("click me!");
			button.getStyleClass().addAll("someAnotherClass");
			button.setId("clickButtonId");
            button.setOnAction(actionEvent -> button.setText("clicked!"));
            getChildren().add(button);
        }
    }

    public static void main(String[] args)
    {
        if (Platform.isFxApplicationThread()) {
            Stage someStage = new Stage();
            ClickApplication app = new ClickApplication();
            try
            {
                app.start(someStage);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        } else {
            launch(args);
        }
    }
}