package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.Locator;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.Iterator;

public class Main extends Application
{
	public static void main(String[] args) throws InterruptedException
	{
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		System.out.println("-- start --");

		new ClickApplication().main(new String[0]);

		Iterator <Window> windowIterator = Window.impl_getWindows();
		while (windowIterator.hasNext())
		{
			String remoteLog 			= "remote.log";
			String remoteLogLevel 		= "ALL";
			String remoteLogPattern		= "%-5p %d{yyyy-MM-dd HH:mm:ss.SSS} %c{1}:%-3L-%m%n";

			new RemoteApplicationFx().createLogger(remoteLog, remoteLogLevel, remoteLogPattern);

			Window window = windowIterator.next();

			new MatcherFx <>(
					new AppFactoryFx().getInfo(),
					new Locator().xpath("//Button[@name=\"clickButtonId\"]"),
					window.getScene().getRoot());
		}

		System.out.println("-- finish --");
	}
}