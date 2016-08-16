////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.layout.wizard;

import com.exactprosystems.jf.common.VerboseLevel;
import com.exactprosystems.jf.documents.ConsoleDocumentFactory;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.ApplicationConnector;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;

public class TestLayoutWizard extends Application
{

	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception
	{
		Table table = new Table(new String[][]{
			new String[]{"Main", "Button", "TextBox"},
			new String[]{"Button", ""},
			new String[]{"TextBox", ""}
		}, new Configuration().createEvaluator());
		Common.node = stage;
		Common.setTheme(Theme.WHITE);
		Common.setProgressBar(new ProgressBar());

		ConsoleDocumentFactory factory = new ConsoleDocumentFactory(VerboseLevel.All);

		Configuration configuration = new Configuration("asd", factory);
		factory.setConfiguration(configuration);
		configuration.load(new FileReader(new File("config_dev.xml")));


		ApplicationConnector connector = new ApplicationConnector("EXACTPRO", factory);
		connector.setApplicationListener((status, connection, throwable) -> {
			if (connection != null)
			{
				LayoutWizard wizard = null;
				try
				{
					wizard = new LayoutWizard(table, connection, configuration.createEvaluator());
					wizard.show();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		connector.startApplication();
		stage.setScene(new Scene(new Pane(), 10, 10));
		stage.show();
	}
}
