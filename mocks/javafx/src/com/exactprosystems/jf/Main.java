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
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Mock.MAIN.createWindow(primaryStage, FXMLLoader.load(getClass().getResource("Main.fxml")));
        Mock.ADDITIONAL.createWindow(new Stage(), FXMLLoader.load(getClass().getResource("Additional.fxml")));
    }

    private enum Mock
    {
        MAIN
        {
            @Override
            public void createWindow(Stage stage, Parent root)
            {
                stage.setTitle("JavaFx Mock");
                stage.setScene(new Scene(root, 800, 800));
                stage.setOnCloseRequest(event -> Platform.exit());
                stage.show();
            }
        },

        ADDITIONAL
        {
            @Override
            void createWindow(Stage stage, Parent root)
            {
                stage.setTitle("Dialog");
                stage.setScene(new Scene(root, 150, 200));
                stage.setX(1500);
                stage.setY(300);
                stage.show();
            }
        };

        abstract void createWindow(Stage stage, Parent root);
    }
}
