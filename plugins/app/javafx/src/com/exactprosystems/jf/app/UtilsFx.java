package com.exactprosystems.jf.app;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.stage.StageHelper;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class UtilsFx
{
	private static Logger logger;

	private UtilsFx()
	{

	}

	static void setLogger(Logger logger)
	{
		UtilsFx.logger = logger;
	}

	/**
	 * collect roots from all windows
	 */
	static Parent currentRoot()
	{
		RootContainer rootContainer = new RootContainer();
		StageHelper.getStages().stream().map(s -> s.getScene().getRoot()).forEach(rootContainer::addTarget);
		return rootContainer;
	}

	static Stage mainStage()
	{
		ObservableList<Stage> stages = StageHelper.getStages();
		Optional.ofNullable(logger).ifPresent(l -> l.debug(String.format("Found %s stages", stages.size())));
		if (stages.isEmpty())
		{
			throw new RuntimeException("No one stages found");
		}

		return stages.stream()
				.peek(s -> Optional.ofNullable(logger).ifPresent(l -> l.debug(String.format("Found stage : %s. MxWindowType : %s", MatcherFx.targetToString(s), s.impl_getMXWindowType()))))
				.filter(s -> s.impl_getMXWindowType().equals("PrimaryStage"))
				.findFirst()
				.orElse(stages.get(0));
	}

	static void waitForIdle()
	{
		//code from BaseFXRobot
		final CountDownLatch latch = new CountDownLatch(1);
		PlatformImpl.runLater(latch::countDown);
		while (true)
		{
			try
			{
				latch.await();
				break;
			}
			catch (InterruptedException ignored)
			{
				;
			}
		}
	}
}
