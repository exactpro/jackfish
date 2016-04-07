////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.common.Str;
import org.apache.log4j.*;
import org.w3c.dom.Document;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.exactprosystems.jf.app.WinAppFactory.*;

public class WinRemoteApplicationJNA extends RemoteApplication
{
	private Logger logger;
	private JnaDriverImpl driver;
	private WinOperationExecutorJNA operationExecutor;

	@Override
	protected void createLoggerDerived(String logName, String serverLogLevel, String serverLogPattern) throws Exception
	{
		try
		{
			logger = Logger.getLogger(WinRemoteApplicationJNA.class);
			Layout layout = new PatternLayout(serverLogPattern);
			Appender appender = new FileAppender(layout, logName);
			logger.addAppender(appender);
			logger.setLevel(Level.toLevel(serverLogLevel, Level.ALL));
		}
		catch (Exception e)
		{
			logger.error(String.format("createLoggerDerived(%s, %s,%s)", logName, serverLogLevel, serverLogPattern));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void connectDerived(Map<String, String> args, MetricsCounter metricsCounter) throws Exception
	{
		try
		{
			String title = args.get(WinAppFactory.mainWindowName);
			if (Str.IsNullOrEmpty(title))
			{
				throw new Exception("Connection title can't be null or empty");
			}
			logger.info("##########################################################################################################");
			logger.info("connectDerived(" + title + ")");
			this.driver = new JnaDriverImpl();
			this.operationExecutor = new WinOperationExecutorJNA(this.logger, this.driver);
			this.driver.connect(title);
		}
		catch (Exception e)
		{
			logger.error("connectionDerived(" + args + ")");
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void runDerived(Map<String, String> args, MetricsCounter metricsCounter) throws Exception
	{
		try
		{
			String exec = args.get(execName);
			String workDir = args.get(workDirName);
			String parameters = args.get(argsName);

			if (Str.IsNullOrEmpty(exec))
			{
				throw new Exception("Executable name can't be null or empty");
			}
			if (Str.IsNullOrEmpty(workDir))
			{
				throw new Exception("Working directory can't be null or empty");
			}
			this.logger.info("##########################################################################################################");
			this.logger.info("runDerived(" + args + ")");
			this.driver = new JnaDriverImpl();
			this.operationExecutor = new WinOperationExecutorJNA(this.logger, this.driver);
			this.driver.run(exec, workDir, parameters);
		}
		catch (Exception e)
		{
			logger.error("runDerived(" + args + ")");
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void stopDerived() throws Exception
	{
		try
		{
			this.driver.stop();
		}
		catch (Exception e)
		{
			logger.error("stopDerived()");
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void refreshDerived() throws Exception
	{
		try
		{
			this.driver.refresh();
		}
		catch (Exception e)
		{
			logger.error("refreshDerived()");
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected SerializablePair<String, Boolean> getAlertTextDerived() throws Exception
	{
		throw new Exception("Not presented here");
	}

	@Override
	protected void setAlertTextDerived(String text, PerformKind performKind) throws Exception
	{
		throw new Exception("Not presented here");
	}

	@Override
	protected Collection<String> titlesDerived() throws Exception
	{
		ArrayList<String> list = new ArrayList<>();
		list.add(this.driver.title());
		return list;
	}

	@Override
	protected String switchToDerived(String title, boolean softCondition) throws Exception
	{
		throw new Exception("Not presented here");
	}

	@Override
	protected void switchToFrameDerived(Locator owner) throws Exception
	{
		throw new Exception("Not presented here");
	}

	@Override
	protected void resizeDerived(int height, int width, boolean maximize, boolean minimize) throws Exception
	{
		//TODO need implement this method
	}

	@Override
	protected Collection<String> findAllDerived(Locator owner, Locator element) throws Exception
	{
		try
		{
			List<String> res = new ArrayList<>();
			UIProxyJNA ownerId = new UIProxyJNA(null);
			if (owner != null)
			{
				ownerId = this.operationExecutor.find(null, owner);
			}
			String result = this.driver.listAll(ownerId.getIdString(), element.getControlKind()
					.ordinal(), element.getUid(), element.getXpath(), element.getClazz(), element.getName(), element.getTitle(), element
					.getText());
			//TODO see Program.cs line 162. Or may be split via long string ====== ?
			String[] split = result.split("#####");
			for(int i = 0; i < split.length && !split[i].isEmpty(); i++)
			{
				res.add(split[i]);
			}
			return res;
		}
		catch (Exception e)
		{
			logger.error(String.format("findAllDerived(%s,%s)", owner, element));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected Locator getLocatorDerived(Locator owner, ControlKind controlKind, int x, int y) throws Exception
	{
		try
		{
			int initialLength = 100;
			int[] res = new int[initialLength];
			int returnLength = this.driver.elementByCoords(res, initialLength, controlKind.ordinal(), x, y);
			if (returnLength == 0)
			{
				throw new ElementNotFoundException(x, y);
			}
			if (returnLength > initialLength)
			{
				initialLength = returnLength;
				res = new int[initialLength];
				this.driver.elementByCoords(res, initialLength, controlKind.ordinal(), x, y);
			}
			int[] newRes = new int[returnLength];
			System.arraycopy(res, 0, newRes, 0, returnLength);
			return this.operationExecutor.locatorFromUIProxy(newRes);
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getLocatorDerived(%s,%s,%d,%d)", owner, controlKind, x, y));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected ImageWrapper getImageDerived(Locator owner, Locator element) throws Exception
	{
		try
		{
			UIProxyJNA uiProxyJNA = this.operationExecutor.find(owner, element);
			int length = 100 * 100;
			int[] arr = new int[length];
			int count = this.driver.getImage(arr, length, uiProxyJNA.getIdString());
			if (count > length)
			{
				length = count;
				arr = new int[length];
				this.driver.getImage(arr, length, uiProxyJNA.getIdString());
			}
			int[] result = new int[arr.length - 2];
			System.arraycopy(arr, 2, result, 0, arr.length - 2);
			return new ImageWrapper(arr[0], arr[1], result);
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getImagedDerived(%s,%s)", owner, element));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected Rectangle getRectangleDerived(Locator owner, Locator element) throws Exception
	{
		try
		{
			return this.operationExecutor.getRectangle(this.operationExecutor.find(owner, element));
		}
		catch (Exception e)
		{
			this.logger.error(String.format("getRectangleDerived(%s,%s)", owner, element));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected OperationResult operateDerived(Locator owner, Locator element, Locator rows, Locator header, Operation operation) throws Exception
	{
		try
		{
			return operation.operate(this.operationExecutor, owner, element, rows, header);
		}
		catch (Exception e)
		{
			this.logger.error(String.format("operateDerived(%s,%s,%s,%s,%s)", owner, element, rows, header, operation));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected CheckingLayoutResult checkLayoutDerived(Locator owner, Locator element, Spec spec) throws Exception
	{
		try
		{
			return spec.perform(this.operationExecutor, owner, element);
		}
		catch (Exception e)
		{
			this.logger.error(String.format("checkLayoutDerived(%s,%s,%s)", owner, element, spec));
			throw e;
		}
	}

	@Override
	protected void newInstanceDerived(Map<String, String> args) throws Exception
	{

	}

	@Override
	protected int closeAllDerived(Locator element, Collection<LocatorAndOperation> operations) throws Exception
	{
		return 0;
	}

	@Override
	protected String closeWindowDerived() throws Exception
	{
		return null;
	}

	@Override
	protected Document getTreeDerived(Locator owner) throws Exception
	{
		//TODO think about it
		return null;
	}

	@Override
	protected void startGrabbingDerived() throws Exception
	{
		//TODO think about it
	}

	@Override
	protected void endGrabbingDerived() throws Exception
	{
		//TODO think about it
	}
}
