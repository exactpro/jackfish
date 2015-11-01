////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;

import org.apache.log4j.*;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.*;

public class WinRemoteApplication extends RemoteApplication
{
	public static final String listFilesName = "list.txt";
	public static final String binDirName = "bin";

	
	private static Method	connectDerived		= Driver.byName(WinRemoteApplication.class, "connectDerived");
	private static Method	runDerived			= Driver.byName(WinRemoteApplication.class, "runDerived");
	private static Method	stopDerived			= Driver.byName(WinRemoteApplication.class, "stopDerived");
	private static Method	refreshDerived		= Driver.byName(WinRemoteApplication.class, "refreshDerived");
	private static Method	titledDerived		= Driver.byName(WinRemoteApplication.class, "titledDerived");
	private static Method	switchToDerived		= Driver.byName(WinRemoteApplication.class, "switchToDerived");
	private static Method	findAllDerived		= Driver.byName(WinRemoteApplication.class, "findAllDerived");
	private static Method	getLocatorDerived	= Driver.byName(WinRemoteApplication.class, "getLocatorDerived");
	private static Method	getImageDerived		= Driver.byName(WinRemoteApplication.class, "getImageDerived");
	private static Method	newInstanceDerived	= Driver.byName(WinRemoteApplication.class, "newInstanceDerived");
	
	public WinRemoteApplication() throws RemoteException
	{
		try (InputStream stream = getClass().getResourceAsStream(binDirName + File.separator + listFilesName);
				BufferedReader br = new BufferedReader(new InputStreamReader(stream)))
		{
			String fileName = null;
			while ((fileName = br.readLine()) != null)
			{
				InputStream f = getClass().getResourceAsStream(binDirName + File.separator + fileName);
				Files.copy(f, Paths.get(fileName));
			}
		}
		catch (IOException e)
		{
			throw new RemoteException(e.getMessage(), e);
		}
		
		try
		{
			List<String> commandLine = new ArrayList<String>();
			commandLine.add("UIAdapter.exe");

			ProcessBuilder builder = new ProcessBuilder(commandLine);
			builder.redirectInput(Redirect.PIPE);
			builder.redirectOutput(Redirect.PIPE);
			this.process = builder.start();
		}
		catch (IOException e)
		{
			throw new RemoteException(e.getMessage(), e);
		}
	}

	@Override
	protected void createLoggerDerived(String logName, String serverLogLevel, String serverLogPattern) throws Exception
	{
		logger = Logger.getLogger(WinRemoteApplication.class);

		Layout layout = new PatternLayout(serverLogPattern);
		Appender appender = new FileAppender(layout, logName);
		logger.addAppender(appender);
		logger.setLevel(Level.toLevel(serverLogLevel, Level.ALL));

		this.driver = new Driver(this.logger, this.process.getOutputStream(), this.process.getInputStream());
	}

	@Override
	protected void connectDerived(@Name(name = "args") Map<String, String> args) throws Exception
	{
		logger.info("##########################################################################################################");
		logger.info("connectDerived(" + args + ")");
		
		this.operationExecutor = new WindowsOperationExecutor(this.driver, this.logger);

		this.driver.translate(Object.class, connectDerived, args);
	}


	@Override
	protected void runDerived(@Name(name = "args") Map<String, String> args) throws Exception
	{
		logger.info("##########################################################################################################");
		logger.info("derivedStart(" + args + ")");
		
		this.operationExecutor = new WindowsOperationExecutor(this.driver, this.logger);

		this.driver.translate(Object.class, runDerived, args);
	}

	@Override
	protected void stopDerived() throws Exception
	{
		logger.info("derivedStop()");

		this.driver.translate(Object.class, stopDerived);
		this.process.waitFor();
	}

	@Override
	protected void refreshDerived() throws Exception
	{
		this.driver.translate(Object.class, refreshDerived);
	}

	@Override
	protected Collection<String> titlesDerived() throws Exception
	{
		return this.driver.translate(ArrayList.class, titledDerived);
	}

	@Override
	protected String switchToDerived(@Name(name = "title") final String title) throws Exception
	{
		return this.driver.translate(String.class, switchToDerived, title);
	}

	@Override
	protected Collection<String> findAllDerived(@Name(name = "owner") Locator owner, @Name(name = "locator") Locator locator) throws Exception
	{
		return this.driver.translate(ArrayList.class, findAllDerived, owner, locator);
	}
	
	@Override
	protected Locator getLocatorDerived(@Name(name = "owner") Locator owner, @Name(name = "controlKind") ControlKind controlKind, @Name(name = "x") int x, @Name(name = "y") int y) throws Exception
	{
		return this.driver.translate(Locator.class, getLocatorDerived, controlKind, x, y);
	}

	@Override
	protected ImageWrapper getImageDerived(@Name(name = "owner") Locator owner, @Name(name = "locator") Locator locator) throws Exception
	{
		return this.driver.translate(ImageWrapper.class, getImageDerived, owner, locator);
	}

	@Override
	protected void newInstanceDerived(@Name(name = "args") Map<String, String> args) throws Exception
	{
		this.driver.translate(Object.class, newInstanceDerived, args);
	}
	
	
	@Override
	protected OperationResult operateDerived(Locator owner, Locator locator, Locator additional, Locator header, Operation operation) throws Exception
	{
		return operation.operate(this.operationExecutor, owner, locator, additional, header);
	}

	@Override
	protected int closeAllDerived(Locator element, Collection<LocatorAndOperation> operations)
			throws Exception
	{
		List<UIProxy> dialogs = this.operationExecutor.findAll(ControlKind.Any, null, element);
		
		for (UIProxy dialog : dialogs)
		{
			for (LocatorAndOperation pair : operations)
			{
				Locator locator = pair.getLocator();
				
				List<UIProxy> components = this.operationExecutor.findAll(locator.getControlKind(), dialog, locator);
				if (components.size() == 1)
				{
					UIProxy component = components.get(0);
					Operation operation = pair.getOperation();
					operation.operate(this.operationExecutor, locator, component);
					
				}
			}
		}
		
		return dialogs.size();
	}

	@Override
	protected String closeWindowDerived() throws Exception
	{
		//TODO do this method
		return "notSupported";
	}

	@Override
	protected Document getTreeDerived(Locator owner) throws Exception
	{
		//TODO return null
		return null;
	}

	@Override
	protected void startGrabbingDerived() throws Exception
	{
	}

	@Override
	protected void endGrabbingDerived() throws Exception
	{
	}

	@Override
	protected void highlightDerived(Locator owner, String xpath) throws Exception
	{
		// TODO Auto-generated method stub
		
	}

	private Process			process;
	private Driver 			driver;
	public OperationExecutor<UIProxy> operationExecutor;
	private Logger			logger	= null;
}
