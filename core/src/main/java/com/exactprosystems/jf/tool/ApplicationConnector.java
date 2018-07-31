/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.tool;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.app.IApplicationPool;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.tool.documents.guidic.ApplicationStatus;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApplicationConnector
{
	public interface ApplicationListener
	{
		void update(ApplicationStatus status, AppConnection connection, Throwable throwable);
	}

	public static final java.lang.String startParameters = "StartParameters";
	public static final java.lang.String connectParameters = "ConnectParameters";

	private String idAppEntry;
	private DocumentFactory factory;
	private Task<Void> task;
	private AppConnection appConnection;
	private ApplicationListener applicationListener;

	public ApplicationConnector(DocumentFactory factory)
	{
		this(null, factory);
	}

	public ApplicationConnector(String idAppEntry, DocumentFactory factory)
	{
		this.idAppEntry = idAppEntry;
		this.factory = factory;
	}

	public void startApplication()  throws Exception
	{
		runApplication(true);
	}

	public void connectApplication()  throws Exception
	{
		runApplication(false);
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
			this.factory.getConfiguration().getApplicationPool().stopApplication(this.appConnection, false);
			this.appConnection = null;
		}

		listener().ifPresent(lis -> lis.update(ApplicationStatus.Disconnected, null, null));
	}

	public void setAppConnection(AppConnection appConnection)
	{
		this.appConnection = appConnection;
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

	private void runApplication(boolean isStart) throws Exception
	{
		AbstractEvaluator evaluator = this.factory.createEvaluator();
		if (this.appConnection != null)
		{
			throw new Exception(R.APP_CON_STOP_BEFORE_RUN.get());
		}
		if (idAppEntry == null)
		{
			throw new Exception(R.APP_CON_CLOSE_FIRSTLY.get());
		}
		IApplicationPool applicationPool	= this.factory.getConfiguration().getApplicationPool();
		IApplicationFactory appFactory 		= applicationPool.loadApplicationFactory(idAppEntry);

		String parametersName 		= isStart ? startParameters : connectParameters;
		String title 				= isStart ? R.COMMON_START.get() : R.COMMON_CONNECT.get();
		String[] strings 			= appFactory.wellKnownParameters(isStart ? ParametersKind.START : ParametersKind.CONNECT);

		Settings settings = this.factory.getSettings();
		final Map<String, String> parameters = settings.getMapValues(Settings.APPLICATION + idAppEntry, parametersName, strings);

		ButtonType desision = DialogsHelper.showParametersDialog(title + " "+ idAppEntry, parameters, evaluator, str ->
		{
			if (appFactory.canFillParameter(str))
			{
				return () -> Arrays.stream(appFactory.listForParameter(str))
						.map(evaluator::createString)
						.map(ReadableValue::new)
						.collect(Collectors.toList());
			}
			return null;
		});

		if (desision == ButtonType.CANCEL)
		{
			return;
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
			if(!Str.IsNullOrEmpty(expression))
			{
				try
				{
					Object value = evaluator.evaluate(expression);
					entry.setValue(String.valueOf(value));
				}
				catch (Exception e)
				{
					throw new Exception (R.COMMON_ERROR_IN.get() + " " + name + " = " + expression + " :" + e.getMessage(), e);
				}
			}
		}
		this.task = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				IApplicationPool applicationPool = factory.getConfiguration().getApplicationPool();
				Common.runLater(() -> update(ApplicationStatus.Connecting, null, true));
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
		this.task.setOnFailed(workerStateEvent -> update(ApplicationStatus.Disconnected, task.getException(), false));
		Thread thread = new Thread(this.task);
		thread.setName("Start app " + this.idAppEntry + ", thread id : " + thread.getId());
		thread.setDaemon(true);
		thread.start();
	}

	private void update(ApplicationStatus status, Throwable exception, boolean progressBarVisible)
	{
		listener().ifPresent(lis -> lis.update(status, this.appConnection, exception));
		Common.progressBarVisible(progressBarVisible);
	}
}
