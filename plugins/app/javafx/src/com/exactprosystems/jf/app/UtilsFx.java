package com.exactprosystems.jf.app;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.stage.StageHelper;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.log4j.*;

import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class UtilsFx
{
	private static Logger logger;

	interface IReturn<T>
	{
		T call() throws Exception;
	}

	interface ICheck
	{
		void check() throws Exception;
	}

	private UtilsFx()
	{

	}

	public static <T> T tryExecute(ICheck beforeCall, IReturn<T> func, Consumer<Exception> log) throws Exception
	{
		beforeCall.check();
		try
		{
			waitForIdle();
			return func.call();
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			log.accept(e);
			throw e;
		}
	}

	public static Logger createLogger(Class<?> clazz, String logName, String serverLogLevel, String serverLogPattern) throws Exception
	{
		Logger logger = Logger.getLogger(clazz);

		Layout layout = new PatternLayout(serverLogPattern);
		Appender appender = new FileAppender(layout, logName);
		logger.addAppender(appender);
		logger.setLevel(Level.toLevel(serverLogLevel, Level.ALL));
		return logger;
	}

	public static Logger createLogger(Class<?> clazz, Logger baseLogger)
	{
		Logger logger = Logger.getLogger(clazz);
		Enumeration allAppenders = baseLogger.getAllAppenders();
		while (allAppenders.hasMoreElements())
		{
			logger.addAppender(((Appender) allAppenders.nextElement()));
		}
		logger.setLevel(logger.getLevel());
		return logger;
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
			throw new RuntimeException("No one stages was found");
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

	static java.awt.Color convert(Color javafxColor)
	{
		return new java.awt.Color(
				(int) (javafxColor.getRed() * 255)
				, (int) (javafxColor.getGreen() * 255)
				, (int) (javafxColor.getBlue() * 255)
				, (int) (javafxColor.getOpacity() * 255)
		);
	}

	static BufferedImage getNodeImage(EventTarget target)
	{
		final BufferedImage[] img = {null};
		final CountDownLatch latch = new CountDownLatch(1);
		Node finalTarget = (Node) target;
		PlatformImpl.runLater(() -> {
			Optional.ofNullable(logger).ifPresent(l -> l.debug(String.format("Start get image of Node %s", finalTarget)));
			WritableImage snapshot = finalTarget.snapshot(new SnapshotParameters(), null);
			img[0] = SwingFXUtils.fromFXImage(snapshot, null);
			latch.countDown();
		});
		//wait image
		while (true)
		{
			try
			{
				latch.await();
				break;
			}
			catch (InterruptedException ignored)
			{}
		}
		Optional.ofNullable(logger).ifPresent(l -> l.debug(String.format("Getting image for target %s is done. Image size [width : %s, height : %s]", target, img[0].getWidth(), img[0].getHeight())));
		return img[0];
	}
}
