////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplicationPool;
import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.dictionary.ApplicationStatus;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class ApplicationConnector
{
	public interface ApplicationListener
	{
		void update(ApplicationStatus status, AppConnection connection, Throwable throwable);
	}

	public static final java.lang.String startParameters = "StartParameters";
	public static final java.lang.String connectParameters = "ConnectParameters";

	private String idAppEntry;
	private Configuration configuration;
	private Task<Void> task;
	private AppConnection appConnection;
	private ApplicationListener applicationListener;

	public ApplicationConnector(Configuration configuration)
	{
		this(null, configuration);
	}

	public ApplicationConnector(String idAppEntry, Configuration config)
	{
		this.idAppEntry = idAppEntry;
		this.configuration = config;
	}

	public Optional<AppConnection> startApplication()  throws Exception
	{
		return runApplication(true);
	}

	public Optional<AppConnection> connectApplication()  throws Exception
	{
		return runApplication(false);
	}

	public void stopApplication() throws Exception
	{
		if (this.task != null && this.task.isRunning() && !this.task.isDone())
		{
			this.task.cancel();
			this.task = null;
		}
		if (this.appConnection != null)
		{
			this.appConnection.close();
			this.appConnection = null;
		}

		listener().ifPresent(lis -> lis.update(ApplicationStatus.Disconnected, null, null));
	}

	public AppConnection getAppConnection()
	{
		return appConnection;
	}

	public void setApplicationListener(ApplicationListener applicationListener)
	{
		this.applicationListener = applicationListener;
	}

	public void setIdAppEntry(String idAppEntry)
	{
		this.idAppEntry = idAppEntry;
	}

	public String getIdAppEntry()
	{
		return idAppEntry;
	}

	private Optional<ApplicationListener> listener()
	{
		return Optional.ofNullable(this.applicationListener);
	}

	private Optional<AppConnection> runApplication(boolean isStart) throws Exception
	{
		AbstractEvaluator evaluator = this.configuration.createEvaluator();
		if (this.appConnection != null)
		{
			throw new Exception("You need to stop old application, before run new");
		}
		if (idAppEntry == null)
		{
			throw new Exception("You should choose app entry at first.");
		}
		IApplicationPool applicationPool	= this.configuration.getApplicationPool();

		String parametersName 		= isStart ? startParameters : connectParameters;
		String title 				= isStart ? "Start " : "Connect ";
		String[] strings 			= isStart ? applicationPool.wellKnownStartArgs(idAppEntry) : applicationPool.wellKnownConnectArgs(idAppEntry);

		Settings settings = this.configuration.getSettings();
		final Map<String, String> parameters = settings.getMapValues(Settings.APPLICATION + idAppEntry, parametersName, strings);

		ButtonType desision = DialogsHelper.showParametersDialog(title + idAppEntry, parameters, evaluator);

		if (desision == ButtonType.CANCEL)
		{
			return Optional.empty();
		}

		settings.setMapValues(Settings.APPLICATION + idAppEntry, parametersName, parameters);
		settings.saveIfNeeded();

		// evaluate parameters
		Iterator<Map.Entry<String, String>> iterator = parameters.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<String, String> entry = iterator.next();
			String name = entry.getKey();
			String expression = entry.getValue();
			try
			{
				Object value = evaluator.evaluate(expression);
				entry.setValue(String.valueOf(value));
			}
			catch (Exception e)
			{
				throw new Exception ("Error in " + name + " = " + expression + " :" + e.getMessage(), e);
			}
		}
		this.task = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				IApplicationPool applicationPool = configuration.getApplicationPool();
				update(ApplicationStatus.Connecting, null, true);
				if (isStart)
				{
					appConnection = applicationPool.startApplication(idAppEntry, parameters);
				}
				else
				{
					appConnection = applicationPool.connectToApplication(idAppEntry, parameters);
				}
				return null;
			}
		};

		this.task.setOnSucceeded(workerStateEvent -> update(ApplicationStatus.Connected, null, false));
		this.task.setOnFailed(workerStateEvent -> update(ApplicationStatus.Connected, task.getException(), false));
		Thread thread = new Thread(this.task);
		thread.setName("Start app " + this.idAppEntry + ", thread id : " + thread.getId());
		thread.setDaemon(true);
		thread.start();
		return Optional.empty();
	}

	private void update(ApplicationStatus status, Throwable exception, boolean progressBarVisible)
	{
		listener().ifPresent(lis -> lis.update(status, this.appConnection, exception));
		Common.progressBarVisible(progressBarVisible);
	}
}
