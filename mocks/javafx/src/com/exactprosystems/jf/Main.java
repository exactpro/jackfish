////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Mock.MAIN.createWindow(primaryStage, new FXMLLoader(getClass().getResource("Main.fxml")));
        Mock.ADDITIONAL.createWindow(new Stage(), new FXMLLoader(getClass().getResource("Additional.fxml")));
    }

    private enum Mock
    {
        MAIN
        {
            @Override
            public void createWindow(Stage stage, FXMLLoader loader) throws IOException
            {
                loader.load();
                stage.setTitle("JavaFx Mock");
                stage.setScene(new Scene(loader.getRoot(), 800, 800));
                stage.setOnCloseRequest(event -> Platform.exit());
                stage.setX(200);
                stage.setY(32);
                stage.show();
                MainController controller = loader.getController();
                controller.init();
            }
        },

        ADDITIONAL
        {
            @Override
            void createWindow(Stage stage, FXMLLoader loader) throws IOException
            {
                loader.load();
                stage.setTitle("Dialog");
                stage.setScene(new Scene(loader.getRoot(), 150, 200));
                stage.setX(1500);
                stage.setY(300);
                stage.show();
            }
        };

        abstract void createWindow(Stage stage, FXMLLoader loader) throws IOException;
    }
}
