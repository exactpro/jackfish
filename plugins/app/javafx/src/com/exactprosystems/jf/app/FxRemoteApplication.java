package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.ProcessTools;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.app.FeatureNotSupportedException;
import com.exactprosystems.jf.api.error.app.NullParameterException;
import com.sun.javafx.stage.StageHelper;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FxRemoteApplication extends RemoteApplication
{
	private Logger logger = null;
	private FxOperationExecutor operationExecutor;
	private PluginInfo          info;

	private boolean isInit = false;
	private Thread mainThread;

	@Override
	protected void createLoggerDerived(String logName, String serverLogLevel, String serverLogPattern) throws Exception
	{
		try
		{
			this.logger = UtilsFx.createLogger(FxRemoteApplication.class, logName, serverLogLevel, serverLogPattern);
			MatcherFx.setLogger(UtilsFx.createLogger(MatcherFx.class, logName, serverLogLevel, serverLogPattern));
			UtilsFx.setLogger(UtilsFx.createLogger(UtilsFx.class, logName, serverLogLevel, serverLogPattern));
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			logger.error(String.format("createLoggerDerived(%s, %s,%s)", logName, serverLogLevel, serverLogPattern));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Serializable getProperty(String name, Serializable prop) throws RemoteException
	{
		try
		{
			if (this.isInit)
			{
				if (Str.areEqual(name, FxAppFactory.propertyTitle))
				{
					return UtilsFx.mainStage().getTitle();
				}
			}
			return null;
		}
		catch (Exception e)
		{
			logger.error(String.format("getProperty(%s, %s)", name, prop));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void setProperty(String name, Serializable prop) throws RemoteException
	{

	}

	@Override
	protected void setPluginInfoDerived(PluginInfo info) throws Exception
	{
		this.info = info;
		this.operationExecutor.setPluginInfo(this.info);
	}

	@Override
	protected int runDerived(Map <String, String> args) throws Exception
	{
		String mainClass = args.get(FxAppFactory.mainClassName);
		if (Str.IsNullOrEmpty(mainClass))
		{
			throw new NullParameterException("MainClass can't be empty or null");
		}

		String jarName = args.get(FxAppFactory.jarName);
		if (Str.IsNullOrEmpty(jarName))
		{
			throw new NullParameterException("Jar can't be empty or null");
		}

		String arguments = args.get(FxAppFactory.argsName);
		logger.debug("Launching application: class=" + mainClass + " jar=" + jarName + " arg=" + arguments);

		Exception[] last = new Exception[1];
		AtomicBoolean isLoading = new AtomicBoolean(false);

		this.mainThread = new Thread(() -> {
			try
			{
				logger.debug(String.format("Start loading class %s", mainClass));
				ClassLoader classLoader = FxRemoteApplication.class.getClassLoader();
				try (URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{new URL("file://" + jarName)}, classLoader))
				{
					Class<?> applicationType = urlClassLoader.loadClass(mainClass);
					Method mainMethod = applicationType.getMethod("main", String[].class);
					Logger executorLogger = UtilsFx.createLogger(FxOperationExecutor.class, this.logger);
					this.operationExecutor = new FxOperationExecutor(this.useTrimText, executorLogger);

					this.isInit = true;
					isLoading.set(true);
					this.logger.debug("Before invoke main method");
					mainMethod.invoke(null, new Object[]{arguments == null ? null : new String[]{arguments}});
				}
			}
			catch (InvocationTargetException e)
			{
				//ignore.
			}
			catch (Exception e)
			{
				logger.error(e.getClass() + " " + e.getCause());
				logger.error("startDerived. keys : " + args.keySet() + " , value : " + args.values());
				logger.error(e.getMessage(), e);
				last[0] = e;
				this.isInit = false;
				isLoading.set(true);
			}
		});
		this.mainThread.setName("Thread [" + this.mainThread.getId() + "] : start javafx application");
		logger.debug("Thread " + this.mainThread + " started");
		this.mainThread.start();

		//wait while mainMethod not invoked
		while (!isLoading.get())
		{
			Thread.sleep(1000);
		}

		//check it we have exception - throw it
		if (last[0] != null)
		{
			logger.debug("Application load failed");
			//we need throw exception and stop the mainThread
			if (this.mainThread.isAlive())
			{
				this.mainThread.interrupt();
				this.mainThread = null;
			}
			throw last[0];
		}
		logger.debug("Application load successful");
		return ProcessTools.currentProcessId();
	}

	@Override
	protected void stopDerived(boolean needKill) throws Exception
	{
		logger.debug("Stop derived");
		if (this.mainThread != null && this.mainThread.isAlive())
		{
			this.mainThread.interrupt();
			this.mainThread = null;
		}
	}

	@Override
	protected Collection<String> titlesDerived() throws Exception
	{
		return this.tryExecute(
				() -> StageHelper.getStages().stream().map(Stage::getTitle).collect(Collectors.toList()),
				e ->
				{
					logger.error("titlesDerived()");
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	protected void resizeDerived(Resize resize, int height, int width) throws Exception
	{
		this.tryExecute(
				() ->
				{
					Stage stage = UtilsFx.mainStage();
					logger.debug("Get main stage : %s" + MatcherFx.targetToString(stage));
					resizeStage(stage, resize, height, width);
					return null;
				},
				e ->
				{
					logger.error(String.format("resizeDerived(%s,%s,%s)", resize, height, width));
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
    protected void resizeDialogDerived(Locator element, Resize resize, int height, int width) throws Exception
    {
		this.tryExecute(
				()->
				{
					logger.debug("Element : " + element);
					EventTarget target = this.operationExecutor.find(null, element);
					if(target != null && target instanceof Node)
					{
						Node node = (Node) target;
						Stage stage = (Stage) node.getScene().getWindow();
						logger.debug("Get stage : %s" + MatcherFx.targetToString(stage));
						resizeStage(stage, resize, height, width);
					}
					return null;
				},
				e ->
				{
					logger.error(String.format("resizeDerived(%s,%s,%s,%s)", element, resize, height, width));
					logger.error(e.getMessage(), e);
				}
		);
    }

    @Override
	protected Collection <String> findAllDerived(Locator owner, Locator element) throws Exception
	{
		return this.tryExecute(
				() -> this.operationExecutor.findAll(owner, element)
						.stream()
						.filter(e -> e instanceof Node)
						.map(e -> (Node)e)
						.map(node ->
						{
							StringBuilder sb = new StringBuilder(MatcherFx.targetToString(node));
							if (node instanceof ComboBox)
							{
								sb.append("items : ").append(((ComboBox) node).getItems());
							}
							return sb.toString();
						})
						.collect(Collectors.toList())
				,
				e ->
				{
					logger.error(String.format("findAllDerived(%s,%s)", owner, element));
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	protected ImageWrapper getImageDerived(Locator owner, Locator element) throws Exception
	{
		return this.tryExecute(
				() ->
				{
					logger.debug(String.format("Start get image for owner = %s and element = %s", owner, element));
					EventTarget target = null;
					if (element != null)
					{
						target = this.operationExecutor.find(owner, element);
						logger.debug(String.format("Found target : %s", target));
					}
					if (element == null)
					{
						Stage mainStage = UtilsFx.mainStage();
						logger.debug(String.format("Found current stage: %s", mainStage));
						if (mainStage == null)
						{
							logger.debug("Stage is null. Getting image of whole screen");
							Rectangle desktopRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
							return new ImageWrapper(new java.awt.Robot().createScreenCapture(desktopRect));
						}
						else
						{
							target = mainStage.getScene().getRoot();
						}
					}
					logger.debug(String.format("target : %s", target));
					if (target instanceof Node)
					{
						BufferedImage image = UtilsFx.getNodeImage(target);
						return new ImageWrapper(image);
					}
					throw new Exception("Target is not instance of node");
				},
				e ->
				{
					logger.error(String.format("getImageDerived(%s,%s)", owner, element));
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	protected Rectangle getRectangleDerived(Locator owner, Locator element) throws Exception
	{
		return this.tryExecute(
				() ->
				{
					logger.debug(String.format("Start get rectangle for owner [%s] and element [%s]", owner, element));
					if (element == null)
					{
						return this.operationExecutor.getRectangle(UtilsFx.mainStage());
					}
					else
					{
						return this.operationExecutor.getRectangle(this.operationExecutor.find(owner, element));
					}
				},
				e ->
				{
					logger.error(String.format("getRectangleDerived(%s, %s)", owner, element));
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	protected OperationResult operateDerived(Locator owner, Locator element, Locator rows, Locator header, Operation operation) throws Exception
	{
		return this.tryExecute(
				() -> operation.operate(this.operationExecutor, owner, element, rows, header),
				e ->
				{
					logger.error(String.format("operateDerived(%s,%s,%s,%s,%s)", owner, element, rows, header, operation));
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	protected CheckingLayoutResult checkLayoutDerived(Locator owner, Locator element, Spec spec) throws Exception
	{
		return this.tryExecute(
				() -> spec.perform(this.operationExecutor, owner, element),
				e ->
				{
					logger.error(String.format("checkLayoutDerived(%s,%s,%s)", owner, element, spec));
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	protected int closeAllDerived(Locator element, Collection <LocatorAndOperation> operations) throws Exception
	{
		return this.tryExecute(
				()->
				{
					int closed = 0;
					logger.debug("Operations count : " + operations.size());
					logger.debug("Element : " + element);

					Node currentRoot = UtilsFx.currentRoot();
					logger.debug(String.format("Current root : %s", currentRoot));

					List<EventTarget> all = this.operationExecutor.findAll(ControlKind.Any, currentRoot, element);
					for (EventTarget target : all)
					{
						if (target instanceof Node)
						{
							Node node = (Node) target;
							Window closeWindow = node.getScene().getWindow();
							if (Objects.nonNull(closeWindow))
							{
								Platform.runLater(() -> Event.fireEvent(closeWindow, new WindowEvent(closeWindow, WindowEvent.WINDOW_CLOSE_REQUEST)));
								closed++;
							}
						}
					}
					return closed;
				},
				e ->
				{
					logger.error(String.format("closeAlLDerived(%s,%s)", element, operations));
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	protected Document getTreeDerived(Locator owner) throws Exception
	{
		return this.tryExecute(
				() -> MatcherFx.createDocument(this.info, this.operationExecutor.findOwner(owner), false, true),
				e ->
				{
					logger.error(String.format("getTreeDerived(%s)", owner));
					logger.error(e.getMessage(), e);
				}
		);
	}

	@Override
	protected void startNewDialogDerived() throws Exception
	{

	}

	@Override
	protected void moveWindowDerived(int x, int y) throws Exception
	{
		this.tryExecute(
				() ->
				{
					Stage stage = UtilsFx.mainStage();
					stage.setX(x);
					stage.setY(y);
					return null;
				},
				e ->
				{
					logger.error(String.format("moveWindowDerived(%s,%s)", x, y));
					logger.error(e.getMessage(), e);
				}
		);
	}

	//region FeatureNotSupported
	@Override
	protected int connectDerived(Map<String, String> args) throws Exception
	{
		logger.info("##########################################################################################################");
		throw new FeatureNotSupportedException("Connect");
	}

	@Override
	protected void refreshDerived() throws Exception
	{
		throw new FeatureNotSupportedException("refresh");
	}

	@Override
	protected String switchToDerived(Map <String, String> criteria, boolean softCondition) throws Exception
	{
		throw new FeatureNotSupportedException("switchTo");
	}

	@Override
	protected String getAlertTextDerived() throws Exception
	{
		throw new FeatureNotSupportedException("getAlertText");
	}

	@Override
	protected void navigateDerived(NavigateKind kind) throws Exception
	{
		throw new FeatureNotSupportedException("navigate");
	}

	@Override
	protected void setAlertTextDerived(String text, PerformKind performKind) throws Exception
	{
		throw new FeatureNotSupportedException("setAlertText");
	}

	@Override
	protected void switchToFrameDerived(Locator owner, Locator element) throws Exception
	{
		throw new FeatureNotSupportedException("switchToFrame");
	}

	@Override
	protected void newInstanceDerived(Map <String, String> args) throws Exception
	{
		throw new FeatureNotSupportedException("newInstance");
	}

	@Override
	protected String closeWindowDerived() throws Exception
	{
		throw new FeatureNotSupportedException("closeWindow()");
	}
	//endregion

	//region private methods

	private <T> T tryExecute(UtilsFx.IReturn<T> func, Consumer<Exception> log) throws Exception
	{
		return UtilsFx.tryExecute(
				() ->
				{
					if (!this.isInit)
					{
						throw new Exception("Application is not init");
					}
				}, func,log);
	}

	private void resizeStage(Stage stage, Resize resize, int height, int width)
	{
		if (stage != null)
		{
			if (resize != null)
			{
				switch (resize)
				{
					case Maximize:
						logger.debug("Change state to maximized");
						Platform.runLater(() -> stage.setMaximized(true));
						break;
					case Minimize:
						logger.debug("Change state to minimize");
						Platform.runLater(() -> stage.setIconified(true));
						break;
					case Normal:
						logger.debug("Change state to normal");
						Platform.runLater(() -> {
							stage.setIconified(false);
							stage.setMaximized(false);
						});
						break;
				}
			}
			else
			{
				logger.debug("Change size");
				stage.setWidth(width);
				stage.setHeight(height);
			}
		}
	}
	//endregion
}
