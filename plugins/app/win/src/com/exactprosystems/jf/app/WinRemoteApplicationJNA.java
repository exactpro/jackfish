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
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.FeatureNotSupportedException;
import org.apache.log4j.*;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;

import static com.exactprosystems.jf.app.WinAppFactory.*;

public class WinRemoteApplicationJNA extends RemoteApplication
{
	private Logger logger;
	private JnaDriverImpl driver;
	private WinOperationExecutorJNA operationExecutor;
	private PluginInfo info;

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
    protected void setPluginInfoDerived(PluginInfo info) throws Exception
    {
        this.info = info;
        this.operationExecutor.setPluginInfo(info);
		this.driver.setPluginInfo(info);
	}

	@Override
	public Serializable getProperty(String name) throws RemoteException
	{
		switch (name) {
			case WinAppFactory.propertyWindowRectangle :
				try
				{
					return this.getRectangleDerived(null, null);
				}
				catch (RemoteException e)
				{
					throw e;
				}
				catch (Exception e)
				{
					throw new RemoteException(e.getMessage(), e.getCause());
				}


		}
		return null;
	}

	@Override
	protected int connectDerived(Map<String, String> args) throws Exception
	{
		try
		{
			String title = args.get(WinAppFactory.mainWindowName);
			int height = Integer.MIN_VALUE;
			int width = Integer.MIN_VALUE;
			String heightStr = args.get(WinAppFactory.mainWindowHeight);
			if (!Str.IsNullOrEmpty(heightStr) && !heightStr.equalsIgnoreCase("null"))
			{
				try
				{
					height = Integer.valueOf(heightStr);
				}
				catch (NumberFormatException e)
				{
					throw new Exception("Parameter " + WinAppFactory.mainWindowHeight + " must be from 0 to " + Integer.MAX_VALUE + " or empty/null");
				}
			}

			String widthStr = args.get(WinAppFactory.mainWindowWidth);
			if (!Str.IsNullOrEmpty(widthStr) && !widthStr.equalsIgnoreCase("null"))
			{
				try
				{
					width = Integer.valueOf(widthStr);
				}
				catch (NumberFormatException e)
				{
					throw new Exception("Parameter " + WinAppFactory.mainWindowWidth + " must be from 0 to " + Integer.MAX_VALUE + " or empty/null");
				}
			}

			int pid = Integer.MIN_VALUE;
			String pidStr = args.get(WinAppFactory.pidName);
			if (!Str.IsNullOrEmpty(pidStr) && !pidStr.equalsIgnoreCase("null"))
			{
				try
				{
					pid = Integer.valueOf(pidStr);
				}
				catch (NumberFormatException e)
				{
					throw new Exception("Parameter " + WinAppFactory.pidName + " must be from 0 to " + Integer.MAX_VALUE + " or empty/null");
				}
			}
			ControlKind controlKind = null;
			String controlKindStr = args.get(WinAppFactory.controlKindName);
			if (!Str.IsNullOrEmpty(controlKindStr) && !controlKindStr.equals("null"))
			{
				try
				{
					controlKind = ControlKind.valueOf(controlKindStr);
				}
				catch (IllegalArgumentException e)
				{
					throw new Exception("Parameter " + WinAppFactory.controlKindName + " must be only of " + Arrays.toString(ControlKind.values()) + " or empty/null");
				}
			}

			int timeout = 5000;
			String timeoutString = args.get(WinAppFactory.connectionTimeout);
			if (!Str.IsNullOrEmpty(timeoutString) && !timeoutString.equals("null"))
			{
				try
				{
					timeout = Integer.valueOf(timeoutString);
				}
				catch (NumberFormatException e)
				{
					throw new Exception("Parameter " + WinAppFactory.connectionTimeout + " must be from 0 to " + Integer.MAX_VALUE + " or empty/null");
				}
			}

			logger.info("##########################################################################################################");
			logger.info(String.format("connectionDerived(%s, %d, %d, %d, %s, $d)", title, height, width, pid, controlKind, timeout));
			logger.info("All parameters : " + args.toString());
			this.driver = new JnaDriverImpl(this.logger);
			setLogger(this.driver, args.get(logLevel));
			setMaxTimeout(this.driver, args.get(maxTimeout));
			this.operationExecutor = new WinOperationExecutorJNA(this.logger, this.driver);
			return this.driver.connect(title, height, width, pid, controlKind, timeout);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			logger.error("connectionDerived(" + args + ")");
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected int runDerived(Map<String, String> args) throws Exception
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
			this.driver = new JnaDriverImpl(this.logger);
			setLogger(this.driver, args.get(logLevel));
			setMaxTimeout(this.driver, args.get(maxTimeout));
			this.operationExecutor = new WinOperationExecutorJNA(this.logger, this.driver);
			return this.driver.run(exec, workDir, parameters);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			logger.error("runDerived(" + args + ")");
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private void setLogger(JnaDriverImpl driver, String logLevel) throws Exception
	{
		this.logger.debug("Set log level : " + logLevel);
		driver.createLogger(CSharpLogLevel.logLevelFromStr(logLevel).logLevel());
	}

	private void setMaxTimeout(JnaDriverImpl driver, String maxTimeout) throws Exception
	{
		this.logger.debug("Max timeout : " + maxTimeout);
		if (!Str.IsNullOrEmpty(maxTimeout))
		{
			try
			{
				int timeout = Integer.parseInt(maxTimeout);
				driver.maxTimeout(timeout);
			}
			catch (NumberFormatException e)
			{
				throw new Exception("Parameter " + WinAppFactory.maxTimeout + " must be from 0 to " + Integer.MAX_VALUE + " or empty/null");
			}
		}
	}

	@Override
	protected void stopDerived(boolean needKill) throws Exception
	{
		try
		{
			this.driver.stop(needKill);
		}
		catch (RemoteException e)
		{
			throw e;
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
		catch (RemoteException e)
		{
			throw e;
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
	protected Collection<String> titlesDerived() throws Exception
	{
		ArrayList<String> list = new ArrayList<>();
		list.add(this.driver.title());
		return list;
	}

	@Override
	protected String switchToDerived(String title, boolean softCondition) throws Exception
	{
		throw new FeatureNotSupportedException("switchTo");
	}

	@Override
	protected void switchToFrameDerived(Locator owner) throws Exception
	{
		throw new FeatureNotSupportedException("switchToFrame");
	}

	@Override
	protected void resizeDerived(int height, int width, boolean maximize, boolean minimize) throws Exception
	{
		try
		{
			//TODO it's right that we found main window? mb just get it on c# side and call patterns?
			UIProxyJNA currentWindow = currentWindow();
			if (currentWindow == null)
			{
				throw new ElementNotFoundException("Current window not found");
			}
			if (maximize)
			{
				this.driver.doPatternCall(currentWindow, WindowPattern.WindowPattern, "SetWindowVisualState", "Maximized", 3);
			}
			else if (minimize)
			{
				this.driver.doPatternCall(currentWindow, WindowPattern.WindowPattern, "SetWindowVisualState", "Minimized", 3);
			}
			else
			{
				this.driver.doPatternCall(currentWindow, WindowPattern.TransformPattern, "Resize", width + "%" + height, 1);
			}
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("resizeDerived(%d,%d,%b,%b)", height, width, maximize, minimize));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
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
			boolean many = element.getAddition() != null && element.getAddition() == Addition.Many;
			String result = this.driver.listAll(ownerId, element.getControlKind(), element.getUid(), element.getXpath(), element
					.getClazz(), element.getName(), element.getTitle(), element.getText(), many);
			//TODO see Program.cs method listAll. Or may be split via long string ====== ?
			String[] split = result.split("#####");
			for(int i = 0; i < split.length && !split[i].isEmpty(); i++)
			{
				res.add(split[i]);
			}
			return res;
		}
		catch (RemoteException e)
		{
			throw e;
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
			int returnLength = this.driver.elementByCoords(res, controlKind, x, y);
			if (returnLength == 0)
			{
				throw new ElementNotFoundException(x, y);
			}
			if (returnLength > initialLength)
			{
				initialLength = returnLength;
				res = new int[initialLength];
				this.driver.elementByCoords(res, controlKind, x, y);
			}
			int[] newRes = new int[returnLength];
			System.arraycopy(res, 0, newRes, 0, returnLength);
			return this.operationExecutor.locatorFromUIProxy(newRes);
		}
		catch (RemoteException e)
		{
			throw e;
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
			UIProxyJNA uiProxyJNA;
			if (element == null)
			{
				uiProxyJNA = currentWindow();
				if (uiProxyJNA == null)
				{
					Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
					BufferedImage capture = new Robot().createScreenCapture(screenRect);
					return new ImageWrapper(capture);
				}
			}
			else
			{
				uiProxyJNA = this.operationExecutor.find(owner, element);
			}
			int length = 100 * 100;
			int[] arr = new int[length];
			int count = this.driver.getImage(arr, uiProxyJNA);
			if (count > length)
			{
				length = count;
				arr = new int[length];
				this.driver.getImage(arr, uiProxyJNA);
			}
			int[] result = new int[arr.length - 2];
			System.arraycopy(arr, 2, result, 0, arr.length - 2);
			return new ImageWrapper(arr[0], arr[1], result);
		}
		catch (RemoteException e)
		{
			throw e;
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
			if (element == null)
			{
				UIProxyJNA currentWindow = currentWindow();
				if (currentWindow == null)
				{
					throw new ElementNotFoundException("Current window not found");
				}
				return this.operationExecutor.getRectangle(currentWindow);
			}
			else
			{
				return this.operationExecutor.getRectangle(this.operationExecutor.find(owner, element));
			}
		}
		catch (RemoteException e)
		{
			throw e;
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
		catch (RemoteException e)
		{
			throw e;
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
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			this.logger.error(String.format("checkLayoutDerived(%s,%s,%s)", owner, element, spec));
			this.logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	protected void newInstanceDerived(Map<String, String> args) throws Exception
	{
		throw new FeatureNotSupportedException("newInstance");
	}

	@Override
	protected int closeAllDerived(Locator element, Collection<LocatorAndOperation> operations) throws Exception
	{
		throw new FeatureNotSupportedException("closeAll");
	}

	@Override
	protected String closeWindowDerived() throws Exception
	{
		throw new FeatureNotSupportedException("closeWindow");
	}

	@Override
	protected void startNewDialogDerived() throws Exception
	{
		this.driver.clearCache();
	}

	@Override
	protected Document getTreeDerived(Locator owner) throws Exception
	{
		UIProxyJNA parent;
		if (owner == null)
		{
			int length = 100;
			int[] arr = new int[length];
			int res = this.driver.findAll(arr, new UIProxyJNA(null), WindowTreeScope.Element, WindowProperty.NameProperty, this.driver.title());
			if (res > length)
			{
				length = res;
				arr = new int[length];
				this.driver.findAll(arr, new UIProxyJNA(null), WindowTreeScope.Element, WindowProperty.NameProperty, this.driver.title());
			}
			if (arr[0] > 1)
			{
				throw new Exception("Found more that one main windows : " + arr[0]);
			}
			int[] windowRuntimeId = new int[arr[1]];
			System.arraycopy(arr, 2, windowRuntimeId, 0, arr[1]);
			parent = new UIProxyJNA(windowRuntimeId);
		}
		else
		{
			parent = this.operationExecutor.find(null, owner);
		}
//		return null;
		long start = System.currentTimeMillis();
		Document doc = createDoc(parent);
		this.logger.info("BUILD TREE TIME (MS) : " + (System.currentTimeMillis() - start));
		return doc;
	}

	@Override
	protected void startGrabbingDerived() throws Exception
	{
		//done
	}

	@Override
	protected void endGrabbingDerived() throws Exception
	{
		//done
	}

	private Document createDoc(UIProxyJNA owner) throws Exception
	{
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.newDocument();
		buildDom(document, document, owner, false, true);

		return document;
	}

	private void buildDom(Document document, Node current, UIProxyJNA parent, boolean addItems, boolean addRectangles) throws Exception
	{
		if (parent == null)
		{
			return;
		}
		Element node = null;
		String simpleName = this.driver.getProperty(parent, WindowProperty.LocalizedControlTypeProperty);
		if (simpleName.isEmpty())
		{
			simpleName = this.driver.getProperty(parent, WindowProperty.ControlTypeProperty);
		}
		String tag = this.driver.elementAttribute(parent, AttributeKind.TYPE_NAME);
		try
		{
			node = document.createElement(tag);
		}
		catch (DOMException e)
		{
			logger.debug("Current component : " + parent);
			logger.debug("Error on create element with tag : '" + tag +"'. Component class simple name : '"+ simpleName +"'.");
			node = document.createElement("ErrorTag");
		}

		if (addItems)
		{
			//	node.setUserData("item", parent, null);
		}
		if (addRectangles)
		{
			node.setUserData(IRemoteApplication.rectangleName, this.operationExecutor.getRectangle(parent), null);
		}
		try
		{
			for (AttributeKind kind : AttributeKind.values())
			{
				if (kind == AttributeKind.TYPE_NAME || kind == AttributeKind.ITEMS || kind == AttributeKind.ID)
				{
					continue;
				}
				String value = this.driver.elementAttribute(parent, kind);
				if (!Str.IsNullOrEmpty(value))
				{
					node.setAttribute(kind.name().toLowerCase(), value);
				}
			}
		}
		catch (Exception e)
		{
			node.setAttribute("ERROR", "Something wrong. See remote.log for understanding");
			this.logger.error("Error on add attribute");
			this.logger.error(e.getMessage(), e);
		}
		if (this.driver.getFrameworkId() != Framework.SILVER_LIGHT && tag.equalsIgnoreCase("table"))
		{
			return;
		}

		if (current.getNodeName().equalsIgnoreCase("datagrid"))
		{
			if (!tag.equalsIgnoreCase("header"))
			{
				return;
			}
		}
		current.appendChild(node);
		int length = 100;
		int arr[] = new int[length];
		int res = this.driver.findAll(arr, parent, WindowTreeScope.Children, WindowProperty.TrueProperty, null);
		if (res > length)
		{
			length = res;
			arr = new int[length];
			this.driver.findAll(arr, parent, WindowTreeScope.Children, WindowProperty.TrueProperty, null);
		}
		int foundElementCount = arr[0];
		if (foundElementCount > 0)
		{
			List<UIProxyJNA> elementList = new ArrayList<>();
			int currentPosition = 1;
			for (int i = 0; i < foundElementCount; i++)
			{
				int currentArrayLength = arr[currentPosition++];
				int[] elem = new int[currentArrayLength];
				for (int j = 0; j < currentArrayLength; j++)
				{
					elem[j] = arr[currentPosition++];
				}
				elementList.add(new UIProxyJNA(elem));
			}
			for (UIProxyJNA newParent : elementList)
			{
				buildDom(document, node, newParent, addItems, addRectangles);
			}
		}
	}

	private UIProxyJNA currentWindow() throws Exception
	{
		int length = 100;
		int[] arr = new int[length];
		int defaultValue = driver.findAll(arr, new UIProxyJNA(null), WindowTreeScope.Element, WindowProperty.NameProperty, this.driver.title());
		if (defaultValue == 0)
		{
			return null;
		}
		if (arr[0] > 1)
		{
			throw new Exception("Found more that one main windows : " + arr[0]);
		}
		int[] windowRuntimeId = new int[arr[1]];
		System.arraycopy(arr, 2, windowRuntimeId, 0, arr[1]);
		return new UIProxyJNA(windowRuntimeId);
	}
}
