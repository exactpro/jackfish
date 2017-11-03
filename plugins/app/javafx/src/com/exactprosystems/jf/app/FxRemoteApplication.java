package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.ProcessTools;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.app.FeatureNotSupportedException;
import com.exactprosystems.jf.api.error.app.NullParameterException;
import org.apache.log4j.*;
import org.w3c.dom.Document;

import java.awt.Rectangle;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class FxRemoteApplication extends RemoteApplication
{
	private Logger logger = null;
	private FxOperationExecutor operationExecutor;
	private PluginInfo          info;

	private boolean isInit = false;
	private Thread thread;

	@Override
	protected void createLoggerDerived(String logName, String serverLogLevel, String serverLogPattern) throws Exception
	{
		try
		{
			this.logger = Logger.getLogger(FxRemoteApplication.class);

			Layout layout = new PatternLayout(serverLogPattern);
			Appender appender = new FileAppender(layout, logName);
			this.logger.addAppender(appender);
			this.logger.setLevel(Level.toLevel(serverLogLevel, Level.ALL));

			MatcherFx.setLogger(this.logger);
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
		//TODO implement
		return null;
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

		this.thread = new Thread(() -> {
			try
			{
				ClassLoader classLoader = FxRemoteApplication.class.getClassLoader();
				try(URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{new URL("file://" + jarName)}, classLoader))
				{
					Class<?> applicationType = urlClassLoader.loadClass(mainClass);
					Method mainMethod = applicationType.getMethod("main", String[].class);
					this.operationExecutor = new FxOperationExecutor(this.useTrimText, this.logger);
					this.isInit = true;
					isLoading.set(true);

					mainMethod.invoke(null, new Object[]{arguments == null ? null : new String[]{arguments}});
				}
			}
			catch (Exception e)
			{
				logger.error("connectDerived. keys : " + args.keySet() + " , value : " + args.values());
				logger.error(e.getMessage(), e);
				last[0] = e;
				this.isInit = false;
				isLoading.set(true);
			}
		});
		this.thread.setName("Thread [" + this.thread.getId() + "] : start javafx application");
		logger.debug("Thread " + this.thread + " started");
		this.thread.start();

		//wait while mainMethod not invoked
		while (!isLoading.get())
		{
			Thread.sleep(1000);
		}

		//check it we have exception - throw it
		if (last[0] != null)
		{
			//we need throw exception and stop the thread
			if (this.thread.isAlive())
			{
				this.thread.interrupt();
				this.thread = null;
			}
			throw last[0];
		}

		return ProcessTools.currentProcessId();
	}

	@Override
	protected void stopDerived(boolean needKill) throws Exception
	{
		if (this.thread != null && this.thread.isAlive())
		{
			this.thread.interrupt();
		}
	}

	@Override
	protected Collection<String> titlesDerived() throws Exception
	{
		//TODO implement
		return null;
	}

	@Override
	protected String switchToDerived(Map <String, String> criteria, boolean softCondition) throws Exception
	{
		//TODO implement
		return null;
	}

	@Override
	protected void resizeDerived(Resize resize, int height, int width) throws Exception
	{

	}

	@Override
	protected Collection <String> findAllDerived(Locator owner, Locator element) throws Exception
	{
		return null;
	}

	@Override
	protected ImageWrapper getImageDerived(Locator owner, Locator element) throws Exception
	{
		return null;
	}

	@Override
	protected Rectangle getRectangleDerived(Locator owner, Locator element) throws Exception
	{
		return this.tryAndThrowOrReturn(
				() ->
				{
					if (element == null)
					{
						//TODO need implement
						return null;
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
		return this.tryAndThrowOrReturn(
				() -> operation.operate(this.operationExecutor, owner, element, rows, header),
				e ->
				{
					logger.error(String.format("operateDerived(%s,%s,%s,%s,%s)", owner, element, rows, header, operation));
					logger.error("EXCEPTION : " + e.getMessage(), e);
				}
		);
	}

	@Override
	protected CheckingLayoutResult checkLayoutDerived(Locator owner, Locator element, Spec spec) throws Exception
	{
		return this.tryAndThrowOrReturn(
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
		return 0;
	}

	@Override
	protected Document getTreeDerived(Locator owner) throws Exception
	{
		return null;
	}

	@Override
	protected void startNewDialogDerived() throws Exception
	{

	}

	@Override
	protected void moveWindowDerived(int x, int y) throws Exception
	{

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

	private <T> T tryAndThrowOrReturn(IReturn<T> func, Consumer<Exception> log) throws Exception
	{
		if (!this.isInit)
		{
			throw new Exception("Application is not init");
		}
		try
		{
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

	private interface IReturn<T>
	{
		T call() throws Exception;
	}

	//endregion
}
