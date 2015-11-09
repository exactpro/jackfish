////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import com.exactprosystems.jf.tool.settings.Theme;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainTestGrid extends Application
{
	public static void main(String[] args)
	{ 
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		BorderPane pane = new BorderPane();
		
		DataProvider<String> provider = new TableDataProvider();
		SpreadsheetView view = new SpreadsheetView(provider);
		pane.setCenter(view);

		Scene scene = new Scene(pane, 500, 500);
		pane.setBottom(new TextField());
		scene.getStylesheets().setAll(Theme.WHITE.getPath());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
