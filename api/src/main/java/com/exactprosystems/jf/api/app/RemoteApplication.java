/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.ProxyException;
import org.w3c.dom.Document;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public abstract class RemoteApplication implements IRemoteApplication
{
	public static final String CONNECTED_PORT_DELIMITER = ":";
	public static final String CONNECTED_PORT = "connectedPort" + CONNECTED_PORT_DELIMITER;

	protected boolean useTrimText = true;

	public static void main(String[] args)
	{
		try
		{
			if (args.length < 2)
			{
				throw new Exception(String.format(R.REMOTE_APPLICATION_TOO_FEW_ARGUMENTS.get(), args.length));
			}
			System.err.println(time() + " Starting remote");

			String mainClassName = removeExtraQuotes(args[0]);
			String portSt = removeExtraQuotes(args[1]);
			String[] argsApp = Arrays.copyOfRange(args, 2, args.length);
			
			int startPort = Integer.parseInt(portSt);

			System.err.println(time() + " mainClass  = " + mainClassName);
			System.err.println(time() + " startPort  = " + portSt);
			System.err.println(time() + " other args = " + Arrays.toString(argsApp));
			

			System.err.println(time() + " Creating remote service...");
			IRemoteApplication service = objectFromClassName(mainClassName, IRemoteApplication.class);
			System.err.println(time() + " ... creating complete");

			services.add(service); // to keep strong reference from GC
			
			System.err.println(time() + " Registering remote stub...");

			int port = -1;
			Remote stub = null;
			Registry registry = null;
			Exception lastException = null;

			for (int attempt = 0; attempt < 10; attempt++)
			{
				try
				{
					stub = UnicastRemoteObject.exportObject(service, 0);
					port = firstFreePort(startPort);
					registry = LocateRegistry.createRegistry(port);
					break;
				}
				catch (ExportException e)
				{
					//save the exception and throw, if needed
					lastException = e;
				}
			}

			if (port == -1 || registry == null || stub == null)
			{
				throw lastException;
			}
			System.out.println(CONNECTED_PORT + port);
			System.err.println(time() + " Connected port : " + port);
			registry.rebind(IApplication.serviceName, stub);
			System.err.println(time() + " ... registering complete");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

    @Override
	public final void createLogger(String logName, String serverLogLevel, String serverLogPattern) throws RemoteException
	{
		try 
		{
			exceptionIfNull(logName, 			"logName", "createLogger");
			exceptionIfNull(serverLogLevel, 	"serverLogLevel", "createLogger");
			exceptionIfNull(serverLogPattern, 	"serverLogPattern", "createLogger");
			
			createLoggerDerived(logName, serverLogLevel, serverLogPattern);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_CREATE_LOGGER_EXCEPTION.get(), logName, serverLogLevel, serverLogPattern);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

    @Override
    public void setPluginInfo (PluginInfo info) throws RemoteException
    {
        try 
        {
            exceptionIfNull(info,            "info", "setPluginInfo");
            
            setPluginInfoDerived(info);
        }
        catch (RemoteException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            String msg = String.format(R.REMOTE_APPLICATION_SET_PLUGIN_INFO_EXCEPTION.get(), info);
            throw new ProxyException(msg, e.getMessage(), e);
        }
    }

	@Override
	public final int connect(Map<String, String> args) throws RemoteException
	{
		try 
		{
			exceptionIfNull(args, "args", "run");

			String s = args.get(AbstractApplicationFactory.trimTextName);
			if (!Str.IsNullOrEmpty(s))
			{
				this.useTrimText = Boolean.valueOf(s);
			}

			return connectDerived(args);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_CONNECT_EXCEPTION.get(), args == null ? "null" : Arrays.toString(args.entrySet().toArray()));
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final int run(Map<String, String> args) throws RemoteException
	{
		try 
		{
			exceptionIfNull(args, "args", "run");

			String s = args.get(AbstractApplicationFactory.trimTextName);
			if (!Str.IsNullOrEmpty(s))
			{
				this.useTrimText = Boolean.valueOf(s);
			}

			return runDerived(args);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_RUN_EXCEPTION.get(), args == null ? "null" : Arrays.toString(args.entrySet().toArray()));
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final void stop(boolean needKill) throws RemoteException
	{
		try 
		{
			stopDerived(needKill);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_STOP_ERROR.get());
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}


	@Override
	public final void refresh() throws RemoteException
	{
		try 
		{
			refreshDerived();
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_REFRESH_ERROR.get());
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public String getAlertText() throws RemoteException
	{
		try
		{
			return getAlertTextDerived();
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_GET_ALERT_TEXT_EXCEPTION.get());
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public void navigate(NavigateKind kind) throws RemoteException
	{
		try
		{
			navigateDerived(kind);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_NAVIGATE_EXCEPTION.get(), kind);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public void setAlertText(String text, PerformKind performKind) throws RemoteException
	{
		try
		{
			setAlertTextDerived(text, performKind);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_SET_ALERT_TEXT_EXCEPTION.get(), text, performKind.toString());
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final Collection<String>	titles() throws RemoteException
	{
		try 
		{
			Collection<String> strings = titlesDerived();
			Collection<String> trimmed = new ArrayList<>(strings.size());
			for (String s : strings)
			{
				trimmed.add(trimIfNeeded(s));
			}
			return trimmed;
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_TITLES_EXCEPTION.get());
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final void newInstance(Map<String, String> args) throws Exception
	{
		try
		{
			exceptionIfNull(args, "args", "newInstance");

			newInstanceDerived(args);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_NEW_INSTANCE_EXCEPTION.get(), args == null ? "null" : Arrays.toString(args.entrySet().toArray()));
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final String switchTo(Map<String, String> criteria, boolean softCondition) throws RemoteException
	{
		try 
		{
			exceptionIfNull(criteria, "criteria", "switchTo");

			String switchToDerived = switchToDerived(criteria, softCondition);
			return this.trimIfNeeded(switchToDerived);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_SWITCH_TO_EXCEPTION.get(), criteria);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final void switchToFrame(Locator owner, Locator element) throws RemoteException
	{
		try 
		{
			switchToFrameDerived(owner, element);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_SWITCH_TO_FRAME_EXCEPTION.get(), element);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final void resize(Resize resize, int height, int width) throws RemoteException
	{
		try 
		{
			resizeDerived(resize, height, width);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_RESIZE_EXCEPTION.get(), resize, height, width);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}


	@Override
	public final Collection<String> findAll(Locator owner, Locator element) throws RemoteException
	{
		try 
		{
			exceptionIfNull(element, "element", "findAll");
			
			return findAllDerived(owner, element);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_FIND_ALL_EXCEPTION.get(), owner, element);
			throw new ElementNotFoundException(msg, element);
		}
	}
	
	@Override
	public final ImageWrapper getImage(Locator owner, Locator element) throws RemoteException
	{
		try 
		{
			return getImageDerived(owner, element);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_OPERATE_EXCEPTION.get(), owner, element);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final Rectangle getRectangle(Locator owner, Locator element) throws RemoteException
	{
		try
		{
			return getRectangleDerived(owner, element);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_OPERATE_EXCEPTION.get(), owner, element);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final OperationResult operate(Locator owner, Locator element, Locator rows, Locator header, Operation operation) throws RemoteException
	{
		try 
		{
			exceptionIfNull(element, 	"element", "operate");
			exceptionIfNull(operation, "operation", "operate");

			return operateDerived(owner, element, rows, header, operation);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_OPERATE_ERROR.get(), owner, element, operation);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final CheckingLayoutResult checkLayout(Locator owner, Locator element, Spec spec) throws RemoteException
	{
		try 
		{
			exceptionIfNull(element, 	"element", "checkLayout");
			exceptionIfNull(spec, 		"spec", "checkLayout");

			return checkLayoutDerived(owner, element, spec);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_CHECK_LAYOUT_EXCEPTION.get(), owner, element, spec);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public int closeAll(Locator element, Collection<LocatorAndOperation> operations) throws RemoteException
	{
		try 
		{
			exceptionIfNull(element, 		"element", "closeAll");
			exceptionIfNull(operations, "operations", "closeAll");

			return closeAllDerived(element, operations);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_CLOSE_ALL_EXCEPTION.get(), element, Arrays.toString(operations.toArray()));
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public String closeWindow() throws RemoteException
	{
		try
		{
			return closeWindowDerived();
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_CLOSE_WINDOW_EXCEPTION.get());
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public byte[] getTreeBytes(Locator owner) throws RemoteException
	{
		try
		{
			Document tree = getTreeDerived(owner);
			return Converter.convertXmlDocumentToZipByteArray(tree);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_GET_TREE_BYTES_EXCEPTION.get(), owner);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public void startNewDialog() throws RemoteException
	{
		try
		{
			startNewDialogDerived();
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_START_NEW_DIALOG_EXCEPTION.get());
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public void moveWindow(int x, int y) throws RemoteException
	{
		try
		{
			this.moveWindowDerived(x, y);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_MOVE_WINDOW_EXCEPTION.get(), x, y);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public void moveDialog(Locator owner, int x, int y) throws RemoteException
	{
		try
		{
			this.moveDialogDerived(owner, x, y);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_MOVE_DIALOG_EXCEPTION.get(), owner, x, y);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public void resizeDialog(Locator owner, Resize resize, int height, int width) throws RemoteException
	{
		try
		{
			resizeDialogDerived(owner, resize, height, width);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_RESIZE_DIALOG_EXCEPTION.get(), owner, resize, height, width);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public Dimension getDialogSize(Locator owner) throws RemoteException
	{
		try
		{
			return getDialogSizeDerived(owner);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_GET_DIALOG_SIZE_EXCEPTION.get(), owner);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public Point getDialogPosition(Locator owner) throws RemoteException
	{
		try
		{
			return getDialogPositionDerived(owner);

		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format(R.REMOTE_APPLICATION_GET_DIALOG_POSITION_EXCEPTION.get(), owner);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	//region protected abstract methods
	protected abstract void createLoggerDerived(String logName, String serverLogLevel, String serverLogPattern) throws Exception;

	protected abstract void setPluginInfoDerived(PluginInfo info) throws Exception;

	protected abstract int connectDerived(Map<String, String> args) throws Exception;

	protected abstract int runDerived(Map<String, String> args) throws Exception;

	protected abstract void stopDerived(boolean needKill) throws Exception;
	
	protected abstract void refreshDerived() throws Exception;

	protected abstract String getAlertTextDerived() throws Exception;

	protected abstract void navigateDerived(NavigateKind kind) throws Exception;

	protected abstract void setAlertTextDerived(String text, PerformKind performKind) throws Exception;

	protected abstract Collection<String> titlesDerived() throws Exception;

	protected abstract String switchToDerived(Map<String, String> criteria, boolean softCondition) throws Exception;

	protected abstract void switchToFrameDerived(Locator owner, Locator element) throws Exception;

	protected abstract void resizeDerived(Resize resize, int height, int width) throws Exception;

	protected abstract void resizeDialogDerived(Locator element, Resize resize, int height, int width) throws Exception;

	protected abstract Dimension getDialogSizeDerived(Locator owner) throws Exception;

	protected abstract Point getDialogPositionDerived(Locator owner) throws Exception;

	protected abstract Collection<String> findAllDerived(Locator owner, Locator element) throws Exception;
	
	protected abstract ImageWrapper getImageDerived(Locator owner, Locator element) throws Exception;

	protected abstract Rectangle getRectangleDerived(Locator owner, Locator element) throws Exception;

	protected abstract OperationResult operateDerived(Locator owner, Locator element, Locator rows, Locator header, Operation operation) throws Exception;
	
	protected abstract CheckingLayoutResult checkLayoutDerived(Locator owner, Locator element, Spec spec) throws Exception;

	protected abstract void newInstanceDerived(Map<String,String> args) throws Exception;
	
	protected abstract int closeAllDerived(Locator element, Collection<LocatorAndOperation> operations) throws Exception;

	protected abstract String closeWindowDerived() throws Exception;

	protected abstract Document getTreeDerived(Locator owner) throws Exception;

	protected abstract void startNewDialogDerived() throws Exception;

	protected abstract void moveWindowDerived(int x, int y) throws Exception;

	protected abstract  void moveDialogDerived(Locator owner, int x, int y) throws Exception;
	//endregion

	//region private methods
	private String trimIfNeeded(String s)
	{
		if (s != null && this.useTrimText)
		{
			return s.trim();
		}
		return s;
	}

	private static String removeExtraQuotes(String string)
	{
		if (string != null)
		{
			if (string.startsWith("\""))
			{
				string = string.substring(1);
			}
			
			if (string.endsWith("\""))
			{
				string = string.substring(0, string.length() - 1);
			}

			return string;
		}
			
		return null;
	}

	private static int firstFreePort(int startPort) throws Exception
	{
		int port = 0;
		boolean ok = false;
		for (int count = 0; count <= 1000; count++)
		{
			port = startPort + count;
			if (available(port))
			{
				ok = true;
				break;
			}
		}
		if (!ok)
		{
			throw new Exception(String.format(R.REMOTE_APPLICATION_FIRST_FREE_PORT_EXCEPTION.get(), startPort, (startPort + 1000)));
		}

		return port;
	}

	private static boolean available(int port)
	{
		try (ServerSocket ss = new ServerSocket(port))
		{
			ss.setReuseAddress(true);
		}
		catch (IOException e)
		{
			return false;
		}

		try (DatagramSocket ds = new DatagramSocket(port))
		{
			ds.setReuseAddress(true);
		}
		catch (IOException e)
		{
			return false;
		}

		return true;
	}

	private static String time()
	{
		return new Date().toString();
	}

	@SuppressWarnings("unchecked")
	private static <T > T objectFromClassName(String name, Class<T> baseType) 	throws Exception
	{
		Class<?> type = Class.forName(name);
		
		if (!baseType.isAssignableFrom(type))
		{
			throw new Exception(String.format(R.REMOTE_APPLICATION_CLASS_EXCEPTION.get(), name, baseType.getName()));
		}

		return (T)type.newInstance();
	}

	private void exceptionIfNull(Object object, String message, String methodName)
	{
		if (object == null)
		{
			throw new NullPointerException(String.format(R.REMOTE_APPLICATION_PARAMETER_NULL_EXCEPTION.get(), message, methodName));
		}
	}

	private static List<IRemoteApplication> services = new ArrayList<IRemoteApplication>();
	//endregion

}
