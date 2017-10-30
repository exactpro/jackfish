package com.exactprosystems.jf.app;

import com.sun.javafx.application.LauncherImpl;
import com.sun.javafx.robot.impl.FXRobotHelper;
import com.sun.javafx.stage.StageHelper;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

public class Main
{
	
	
	public static void main(String[] args) throws InterruptedException 
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				String[] guiArgs = {};
				LauncherImpl.launchApplication(ClickApplication.class, guiArgs);
			}
		});
		
	
		thread.start();
		
		Thread.sleep(1000);
		
		ObservableList<Stage> s = StageHelper.getStages();
		
		Stage stage = s.get(0);
		System.err.println(stage.getTitle());
		
		thread.join();
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