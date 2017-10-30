package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.app.PluginInfo;
import com.sun.javafx.application.LauncherImpl;
import com.sun.javafx.robot.impl.FXRobotHelper;
import com.sun.javafx.stage.StageHelper;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDate;
import java.util.Iterator;

public class Main extends Application
{
	public static void main(String[] args) throws InterruptedException
	{
		launch(args);
//
//
//		Thread thread = new Thread(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				String[] guiArgs = {};
//				LauncherImpl.launchApplication(ClickApplication.class, guiArgs);
//			}
//		});
//
//
//		thread.start();
//
//		Thread.sleep(1000);
//
//		ObservableList<Stage> s = StageHelper.getStages();
//
//		Stage stage = s.get(0);
//		System.err.println(stage.getTitle());
//
//		thread.join();
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		System.out.println("-- start --");

		new ClickApplication().main(new String[0]);

		Iterator <Window> windowIterator = Window.impl_getWindows();
		while (windowIterator.hasNext())
		{
			Window window = windowIterator.next();
			Scene scene = window.getScene();

			Locator locator = new Locator().text("click me!");

			AppFactoryFx appFactoryFx = new AppFactoryFx();
			MatcherFx<Node> matcherFx = new MatcherFx <>(appFactoryFx.getInfo(), locator, scene.getRoot());
			matcherFx.toString();
		}

		System.out.println("-- finish --");
	}
}

//public class ClickApplicationTest extends ApplicationTest {
//    @Override public void start(Stage stage) {
//        Parent sceneRoot = new ClickApplication.ClickPane();
//        Scene scene = new Scene(sceneRoot, 100, 100);
//        stage.setScene(scene);
//        stage.show();
//    }
//
//    @Test public void should_contain_button() {
//        // expect:
//        verifyThat(".button", hasText("click me!"));
//    }
//
//    @Test public void should_click_on_button() {
//        // when:
//        clickOn(".button");
//
//        // then:
//        verifyThat(".button", hasText("clicked!"));
//    }
//}