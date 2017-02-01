////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.ProxyException;
import org.w3c.dom.Document;

import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.List;

public abstract class RemoteApplication implements IRemoteApplication
{
	public static void main(String[] args)
	{
		try
		{
			if (args.length < 2)
			{
				throw new Exception("Too few arguments :" + args.length);
			}
			System.err.println("Starting remote");

			String mainClassName = removeExtraQuotes(args[0]);
			String portSt = removeExtraQuotes(args[1]);
			String[] argsApp = Arrays.copyOfRange(args, 2, args.length);
			
			int port = Integer.parseInt(portSt);

			System.err.println("mainClass  = " + mainClassName);
			System.err.println("port       = " + portSt);
			System.err.println("other args = " + Arrays.toString(argsApp));
			

			System.err.println("Creating remote service...");
			IRemoteApplication service = objectFromClassName(mainClassName, IRemoteApplication.class);
			System.err.println("... creating complete");

			services.add(service); // to keep strong reference from GC
			
			System.err.println("Registering remote stub...");
			Remote stub = UnicastRemoteObject.exportObject(service, 0);
			final Registry registry = LocateRegistry.createRegistry(port);
			registry.rebind(IApplication.serviceName, stub);
			System.err.println("... registering complete");
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
			String msg = String.format("Error createLogger(%s, %s, %s)", logName, serverLogLevel, serverLogPattern);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}



	@Override
	public final int connect(Map<String, String> args) throws RemoteException
	{
		try 
		{
			exceptionIfNull(args, "args", "run");

			return connectDerived(args);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format("Error connect(%s)", args == null ? "null" : Arrays.toString(args.entrySet().toArray()));
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final int run(Map<String, String> args) throws RemoteException
	{
		try 
		{
			exceptionIfNull(args, "args", "run");

			return runDerived(args);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format("Error run(%s)", args == null ? "null" : Arrays.toString(args.entrySet().toArray()));
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final void stop() throws RemoteException
	{
		try 
		{
			stopDerived();
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format("Error stop()");
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
			String msg = String.format("Error refresh()");
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public SerializablePair<String, Boolean> getAlertText() throws RemoteException
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
			String msg = String.format("Error getAlertText()");
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
			String msg = String.format("Error navigate(%s)", kind);
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
			String msg = String.format("Error setAlertText(%s, %s)", text, performKind.toString());
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final Collection<String>	titles() throws RemoteException
	{
		try 
		{
			return titlesDerived();
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format("Error titles()");
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
			String msg = String.format("Error newInstance(%s)", args == null ? "null" : Arrays.toString(args.entrySet().toArray()));
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final String switchTo(String title, boolean softCondition) throws RemoteException
	{
		try 
		{
			exceptionIfNull(title, "title", "switchTo");

			return switchToDerived(title, softCondition);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format("Error switchTo(%s)", title);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final void switchToFrame(Locator owner) throws RemoteException
	{
		try 
		{
			switchToFrameDerived(owner);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format("Error switchToFrame(%s)", owner);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public final void resize (int height, int width, boolean maximize, boolean minimize) throws RemoteException
	{
		try 
		{
			resizeDerived(height, width, maximize, minimize);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format("Error resize(%d, %d, %b, %b)", height, width, maximize, minimize);
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
			String msg = String.format("Error findAll(%s, %s)", owner, element);
//			throw new ProxyException(msg, e.getMessage(), e);
			System.err.println("~~~~~~ " + msg);
			throw new ElementNotFoundException(msg, element);
		}
	}
	
	@Override
	public final Locator getLocator (Locator owner, ControlKind controlKind, int x, int y) throws RemoteException
	{
		try 
		{
			exceptionIfNull(controlKind, 	"controlKind", "getLocator");

			return getLocatorDerived(owner, controlKind, x, y);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format("Error getLocator(%s, %d, %d)", controlKind, x, y);
			throw new ProxyException(msg, e.getMessage(), e);
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
			String msg = String.format("Error operate(%s, %s)", owner, element);
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
			String msg = String.format("Error operate(%s, %s)", owner, element);
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
			String msg = String.format("Error operate(%s, %s, %s)", owner, element, operation);
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
			String msg = String.format("Error checkLayout(%s, %s, %s)", owner, element, spec);
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
			String msg = String.format("Error closeAll(%s, %s)", element, Arrays.toString(operations.toArray()));
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
			String msg = String.format("Error closeWindow");
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public Document getTree(Locator owner) throws RemoteException
	{
		try
		{
			return getTreeDerived(owner);
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format("Error getTree(%s)", owner);
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public void startGrabbing() throws RemoteException
	{
		try
		{
			startGrabbingDerived();
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format("Error start grabbing");
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
			String msg = String.format("Error start new dialog");
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}

	@Override
	public void endGrabbing() throws RemoteException
	{
		try
		{
			endGrabbingDerived();
		}
		catch (RemoteException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			String msg = String.format("Error stop grabbing");
			throw new ProxyException(msg, e.getMessage(), e);
		}
	}


	protected abstract void createLoggerDerived(String logName, String serverLogLevel, String serverLogPattern) throws Exception;

	protected abstract int connectDerived(Map<String, String> args) throws Exception;

	protected abstract int runDerived(Map<String, String> args) throws Exception;

	protected abstract void stopDerived() throws Exception;
	
	protected abstract void refreshDerived() throws Exception;

	protected abstract SerializablePair<String,Boolean> getAlertTextDerived() throws Exception;

	protected abstract void navigateDerived(NavigateKind kind) throws Exception;

	protected abstract void setAlertTextDerived(String text, PerformKind performKind) throws Exception;

	protected abstract Collection<String> titlesDerived() throws Exception;

	protected abstract String switchToDerived(String title, boolean softCondition) throws Exception;

	protected abstract void switchToFrameDerived(Locator owner) throws Exception;

	protected abstract void resizeDerived (int height, int width, boolean maximize, boolean minimize) throws Exception;

	protected abstract Collection<String> findAllDerived(Locator owner, Locator element) throws Exception;
	
	protected abstract Locator getLocatorDerived (Locator owner, ControlKind controlKind, int x, int y) throws Exception;

	protected abstract ImageWrapper getImageDerived(Locator owner, Locator element) throws Exception;

	protected abstract Rectangle getRectangleDerived(Locator owner, Locator element) throws Exception;

	protected abstract OperationResult operateDerived(Locator owner, Locator element, Locator rows, Locator header, Operation operation) throws Exception;
	
	protected abstract CheckingLayoutResult checkLayoutDerived(Locator owner, Locator element, Spec spec) throws Exception;

	protected abstract void newInstanceDerived(Map<String,String> args) throws Exception;
	
	protected abstract int closeAllDerived(Locator element, Collection<LocatorAndOperation> operations) throws Exception;

	protected abstract String closeWindowDerived() throws Exception;

	protected abstract Document getTreeDerived(Locator owner) throws Exception;

	protected abstract void startNewDialogDerived() throws Exception;

	protected abstract void startGrabbingDerived() throws Exception;

	protected abstract void endGrabbingDerived() throws Exception;

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

	@SuppressWarnings("unchecked")
	private static <T > T objectFromClassName(String name, Class<T> baseType) 	throws Exception
	{
		Class<?> type = Class.forName(name);
		
		if (!baseType.isAssignableFrom(type))
		{
			throw new Exception("class '" + name + "' is not assignable from " + baseType.getName());
		}

		return (T)type.newInstance();
	}

	private void exceptionIfNull(Object object, String message, String methodName)
	{
		if (object == null)
		{
			throw new NullPointerException("Parameter '" + message + "' is null in call '" + methodName + "'");
		}
	}

	private static List<IRemoteApplication> services = new ArrayList<IRemoteApplication>();

}
