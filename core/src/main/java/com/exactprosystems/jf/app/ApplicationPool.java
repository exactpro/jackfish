/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.app.NullParameterException;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.MainRunner;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.AppEntry;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Parameter;
import com.exactprosystems.jf.documents.guidic.GuiDictionary;
import com.exactprosystems.jf.exceptions.app.ApplicationWasClosedException;
import org.apache.log4j.Logger;

import java.io.Reader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class ApplicationPool implements IApplicationPool
{
	private static final Logger logger = Logger.getLogger(ApplicationPool.class);

	private final DocumentFactory                  factory;
	private final Map<String, IApplicationFactory> appFactories;
	private final Set<AppConnection>               connections;

	public ApplicationPool(DocumentFactory factory)
	{
		this.factory = factory;
		this.appFactories = new ConcurrentHashMap<>();
		this.connections = new ConcurrentSkipListSet<>(Comparator.comparingInt(AppConnection::getPort));
	}

	//region PoolVersionSupported
	@Override
	public boolean isSupported(String id)
	{
		try
		{
			IApplicationFactory applicationFactory = this.loadFactory(id);
			return applicationFactory != null;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
		}

		return false;
	}

	//endregion

	//region IApplicationPool
	@Override
	public Set<ControlKind> supportedControlKinds(String id) throws Exception
	{
		IApplicationFactory applicationFactory = loadFactory(id);
		return applicationFactory.supportedControlKinds();
	}

	@Override
	public List<String> appNames()
	{
		return this.factory.getConfiguration().getAppEntries()
				.stream()
				.map(AppEntry::toString)
				.collect(Collectors.toList());
	}

	@Override
	public boolean isLoaded(String id)
	{
		return this.appFactories.containsKey(id);
	}

	@Override
	public IApplicationFactory loadApplicationFactory(String id) throws Exception
	{
		return this.loadFactory(id);
	}

	@Override
	public AppConnection connectToApplication(String id, Map<String, String> parameters) throws Exception
	{
		try
		{
			if (id == null)
			{
				throw new NullParameterException("id");
			}

			// prepare initial parameters
			AppEntry entry = this.getEntryById(id);

			Map<String, String> driverParameters = this.getDriverParameters(entry);
			final IApplicationFactory applicationFactory = this.loadFactory(id, entry);
			final IApplication application = applicationFactory.createApplication();
			application.init(this, applicationFactory);
			String remoteClassName = applicationFactory.getRemoteClassName();
			String jarPath = MainRunner.makeDirWithSubstitutions(entry.get(Configuration.appJar)); 
			String work = MainRunner.makeDirWithSubstitutions(entry.get(Configuration.appWorkDir));

			Integer startPort = Integer.parseInt(entry.get(Configuration.appStartPort));
			SerializablePair<Integer, Integer> pair = application.connect(startPort, jarPath, work, remoteClassName, driverParameters, parameters);

			return new AppConnection(application, id, pair.getValue(), applicationFactory, pair.getKey());
		}
		catch (InterruptedException e)
		{
			throw new InterruptedException(e.getMessage());
		}
		catch (Throwable t)
		{
			logger.error("Error in connectToApplication " + id);
			logger.error(t.getMessage(), t);
			throw new Exception(t.getMessage(), t);
		}
	}

	@Override
	public AppConnection startApplication(String id, Map<String, String> parameters) throws Exception
	{
		try
		{
			if (id == null)
			{
				throw new NullParameterException("id");
			}
			
			// prepare initial parameters
			AppEntry entry = this.getEntryById(id);

			Map<String, String> driverParameters = this.getDriverParameters(entry);
			final IApplicationFactory applicationFactory = this.loadFactory(id, entry);
			final IApplication application = applicationFactory.createApplication();
			application.init(this, applicationFactory);
			String remoteClassName = applicationFactory.getRemoteClassName();
			String jarPath = MainRunner.makeDirWithSubstitutions(entry.get(Configuration.appJar));
			String work = MainRunner.makeDirWithSubstitutions(entry.get(Configuration.appWorkDir));

			Integer startPort = Integer.parseInt(entry.get(Configuration.appStartPort));
			SerializablePair<Integer, Integer> pair = application.start(startPort, jarPath, work, remoteClassName, driverParameters, parameters);
			AppConnection connection = new AppConnection(application, id, pair.getValue(), applicationFactory, pair.getKey());

			this.connections.add(connection);
			return connection;
		}
		catch (InterruptedException e)
		{
			throw new InterruptedException(e.getMessage());
		}
		catch (Throwable t)
		{
			logger.error(String.format("Error in loadApplication(%s)", id));
			logger.error(t.getMessage(), t);
			throw new Exception(t.getMessage(), t);
		}
	}

	@Override
	public void reconnectToApplication(AppConnection connection, Map<String, String> parameters) throws Exception
	{
		try
		{
			IApplication application = connection.getApplication();
			int pid = application.reconnect(parameters);
			connection.setProcessId(pid);
		}
		catch (InterruptedException e)
		{
			throw new InterruptedException(e.getMessage());
		}
		catch (Throwable t)
		{
			logger.error(String.format("Error in reConnectToApplication(%s)", connection));
			logger.error(t.getMessage(), t);
			throw new Exception(t.getMessage(), t);
		}
	}

	@Override
	public List<AppConnection> getConnections()
	{
		return new ArrayList<>(this.connections);
	}

	@Override
	public void stopApplication(AppConnection connection, boolean needKill) throws Exception
	{
		try
		{
			if (connection == null || !connection.isGood())
			{
				throw new ApplicationWasClosedException(connection == null ? "unknown" : connection.getId());
			}

			IApplication app = connection.getApplication();
			this.connections.remove(connection);
			app.stop(needKill);
		}
		catch (Exception e)
		{
			logger.error(String.format("Error in stopApplication(%s)", connection));
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void stopAllApplications(boolean needKill)
	{
		for (AppConnection connection : this.connections.toArray(new AppConnection[this.connections.size()]))
		{
			try
			{
				stopApplication(connection, needKill);
			}
			catch (Exception e)
			{
				logger.error("Error in stopAllApplications()");
				logger.error(e.getMessage(), e);
			}
		}
	}
	//endregion

	public GuiDictionary getDictionary(AppEntry entry) throws Exception
	{
		String dictionaryName = entry.get(Configuration.appDicPath);
		dictionaryName = MainRunner.makeDirWithSubstitutions(dictionaryName);

		GuiDictionary dictionary = null;
		if (!Str.IsNullOrEmpty(dictionaryName))
		{
			dictionary = (GuiDictionary) this.factory.createDocument(DocumentKind.GUI_DICTIONARY, dictionaryName);
			try (Reader reader = CommonHelper.readerFromFileName(dictionaryName))
			{
				dictionary.load(reader);
			}
			AbstractEvaluator evaluator = this.factory.createEvaluator();
			dictionary.evaluateAll(evaluator);
		}
		return dictionary;
	}

	//region private methods
	private IApplicationFactory loadFactory(String id) throws Exception
	{
		AppEntry entry = this.getEntryById(id);
		return this.loadFactory(id, entry);
	}

	private AppEntry getEntryById(String id) throws Exception
	{
		return Optional.ofNullable(this.factory.getConfiguration().getAppEntry(id))
				.orElseThrow(() -> new Exception(String.format(R.APP_POOL_ID_NOT_FOUND.get(), id)));
	}

	private IApplicationFactory loadFactory(String id, AppEntry entry) throws Exception
	{
		//check that we loaded a entry before
		IApplicationFactory applicationFactory = this.appFactories.get(id);
		if (applicationFactory == null)
		{
			synchronized (this.appFactories)
			{
				applicationFactory = this.appFactories.get(id);
				if (applicationFactory == null)
				{
					String jarName = entry.get(Configuration.appJar);
					jarName = MainRunner.makeDirWithSubstitutions(jarName);

					applicationFactory = CommonHelper.loadFactory(this.getClass()
							, IApplicationFactory.class
							, jarName
							, () -> new Exception(String.format(R.APP_POOL_LOAD_FACTORY.get(), id))
							, logger);

					GuiDictionary dictionary = this.getDictionary(entry);
					applicationFactory.init(dictionary);
					this.appFactories.put(id, applicationFactory);
				}
			}
		}
		return applicationFactory;
	}
	
	private Map<String, String> getDriverParameters(AppEntry entry)
	{
		return entry.getParameters()
				.stream()
				.collect(Collectors.toMap(Parameter::getKey, par -> MainRunner.makeDirWithSubstitutions(par.getValue())));
	}
	//endregion
}
