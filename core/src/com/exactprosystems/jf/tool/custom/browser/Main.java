////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.browser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application
{
	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		Browser browser = new Browser(new File("reports/20160226_111027_testLink_PASSED.html"));
		Scene scene = new Scene(browser, 600, 600);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
