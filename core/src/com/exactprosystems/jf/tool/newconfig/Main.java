////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.settings.Theme;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.FileReader;

public class Main extends Application
{
	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception
	{
		Common.setTheme(Theme.WHITE);
		ConfigurationFxNew config = new ConfigurationFxNew();
		config.load(new FileReader(this.getClass().getResource("config_dev.xml").getFile()));
		Common.node = stage;
		Pair<BorderPane, BorderPane> mockView = createMockView(config);
		config.setPane(mockView.getKey());
		config.display();
		Scene scene = new Scene(mockView.getValue(), 800, 800);
		stage.setScene(scene);
		scene.getStylesheets().addAll(Theme.WHITE.getPath());

		stage.show();
	}

	private Pair<BorderPane, BorderPane> createMockView(ConfigurationFxNew config)
	{
		SplitPane splitPane = new SplitPane();
		BorderPane pane = new BorderPane();
		splitPane.setOrientation(Orientation.HORIZONTAL);
		splitPane.setDividerPositions(0.5);
		pane.setCenter(splitPane);

		VBox box = new VBox();
		Button undo = new Button("Undo");
		undo.setOnAction(e -> config.undo());
		Button redo = new Button("Redo");
		redo.setOnAction(e -> config.redo());
		box.getChildren().addAll(undo, redo);
		box.setSpacing(10);
		TabPane tabPane = new TabPane();
		tabPane.getTabs().addAll(new Tab("Tab 1"), new Tab("tab 2"));
		box.getChildren().add(tabPane);
		GridPane gridPane = new GridPane();
		gridPane.setMinWidth(20.0);
		ColumnConstraints c0 = new ColumnConstraints();
		c0.setHgrow(Priority.SOMETIMES);
		c0.setMaxWidth(30.0);
		c0.setMinWidth(30.0);
		c0.setPrefWidth(30.0);
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setMinWidth(10.0);
		c1.setPrefWidth(100.0);
		c1.setHgrow(Priority.SOMETIMES);
		RowConstraints r0 = new RowConstraints();
		r0.setMinHeight(10.0);
		r0.setPrefHeight(30);
		r0.setVgrow(Priority.SOMETIMES);
		gridPane.getRowConstraints().add(r0);
		gridPane.getColumnConstraints().addAll(c0, c1);

		Label project = new Label("Project");
		project.setOnMouseClicked(event -> {
			double position = splitPane.getDividerPositions()[0];
			if (position < 0.1)
			{
				splitPane.setDividerPositions(0.5);
			}
			else
			{
				splitPane.setDividerPositions(0.0);
			}
		});
		gridPane.add(project, 0, 0);
		gridPane.setGridLinesVisible(true);
		project.setRotate(-90.0);
		GridPane.setValignment(project, VPos.TOP);
		GridPane.setMargin(project, new Insets(20, 0, 0, -15.0));

		splitPane.getItems().addAll(gridPane, box);
		BorderPane treePane = new BorderPane();
		treePane.setMinWidth(0.0);
		gridPane.add(treePane, 1, 0);
		return new Pair<>(treePane, pane);
	}
}
