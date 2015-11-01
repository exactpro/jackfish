////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.client.IClient;
import com.exactprosystems.jf.api.client.IClientFactory;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.IMatrixRunner;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.app.ApplicationPool;
import com.exactprosystems.jf.client.ClientsPool;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.common.parser.items.MatrixRoot;
import com.exactprosystems.jf.common.parser.items.SubCase;
import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.common.parser.listeners.RunnerListener;
import com.exactprosystems.jf.common.xml.gui.GuiDictionary;
import com.exactprosystems.jf.common.xml.messages.MessageDictionary;
import com.exactprosystems.jf.service.ServicePool;
import com.exactprosystems.jf.sql.DataBasePool;

import org.apache.log4j.Logger;

import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.*;
import java.util.Map.Entry;

public class Context implements IContext, AutoCloseable, Cloneable
{
	public Context(IMatrixListener matrixListener, RunnerListener runnerListener, PrintStream out, Configuration configuration) throws Exception
	{
		this.configuration = configuration;
		this.evaluator = configuration.createEvaluator();

		this.matrixListener = matrixListener;
		this.outStream = out;
		this.clients = new ClientsPool(configuration);
		this.services = new ServicePool(configuration);
		this.applications = new ApplicationPool(configuration);
		this.databases = new DataBasePool(configuration);

		this.libs = new HashMap<String, Matrix>();

		this.defaultClient = null;
		this.defaultApp = null;
		this.runnerListener = runnerListener;
	}

	@Override
	public IMatrixRunner createRunner(Reader reader, Date startTime, Object parameter) throws Exception
	{
		return new MatrixRunner(this, reader, startTime, parameter);
	}

	@Override
	public Context clone() throws CloneNotSupportedException
	{
		try
		{
			Context clone = ((Context) super.clone());

			clone.configuration = this.configuration;
			clone.matrixListener = this.matrixListener;
			clone.outStream = this.outStream;

			clone.evaluator = this.configuration.createEvaluator();

			clone.clients = new ClientsPool(this.configuration);
			clone.services = new ServicePool(this.configuration);
			clone.applications = new ApplicationPool(this.configuration);
			clone.databases = new DataBasePool(this.configuration);

			return clone;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new InternalError();
		}
	}

	public void setDefaultClient(String id) throws Exception
	{
		this.defaultClient = this.clients.loadClientFactory(id);
	}

	public void setDefaultApp(String id) throws Exception
	{
		this.defaultApp = this.applications.loadApplicationFactory(id);
	}

	public Configuration getConfiguration()
	{
		return this.configuration;
	}

	public AbstractEvaluator getEvaluator()
	{
		return this.evaluator;
	}

	@Override
	public ClientsPool getClients()
	{
		return this.clients;
	}

	@Override
	public ServicePool getServices()
	{
		return this.services;
	}

	@Override
	public ApplicationPool getApplications()
	{
		return this.applications;
	}

	public DataBasePool getDatabases()
	{
		return this.databases;
	}

	public PrintStream getOut()
	{
		return this.outStream;
	}

	public IMatrixListener getMatrixListener()
	{
		return this.matrixListener;
	}

	public RunnerListener getRunnerListner()
	{
		return this.runnerListener;
	}

	@Override
	public IClientFactory getDefaultClient()
	{
		return this.defaultClient;
	}

	@Override
	public IApplicationFactory getDefaultApp()
	{
		return this.defaultApp;
	}

	@Override
	public void close() throws Exception
	{
		this.services.stopAllServices();
		this.applications.stopAllApplications();
	}

	public SubCase referenceToSubcase(String name, MatrixItem item)
	{
		MatrixItem ref = item.findParent(MatrixRoot.class).find(true, SubCase.class, name);

		if (ref != null && ref instanceof SubCase)
		{
			return (SubCase) ref;
		}
		if (name == null)
		{
			return null;
		}
		String[] parts = name.split("\\.");
		if (parts.length < 2)
		{
			return null;
		}
		String ns = parts[0];
		String id = parts[1];

		Matrix matrix = this.libs.get(ns);
		if (matrix == null)
		{
			matrix = this.configuration.getLib(ns);

			if (matrix == null)
			{
				return null;
			}
			try
			{
				matrix = matrix.clone();
			}
			catch (CloneNotSupportedException e)
			{
				logger.error(e.getMessage(), e);
			}
			this.libs.put(ns, matrix);
		}

		return (SubCase) matrix.getRoot().find(true, SubCase.class, id);
	}

	public List<ReadableValue> subcases(MatrixItem item)
	{
		final List<ReadableValue> res = new ArrayList<ReadableValue>();

		MatrixItem root = item.findParent(MatrixRoot.class);

		root.bypass(it ->
		{
			if (it instanceof SubCase)
			{
				res.add(new ReadableValue(it.getId(), ((SubCase) it).getName()));
			}
		});
		this.configuration.updateLibs();
		for (Entry<String, Matrix> entry : this.configuration.getLibs().entrySet())
		{
			final String name = entry.getKey();
			Matrix lib = entry.getValue();

			if (lib != null)
			{
				lib.getRoot().bypass(it ->
				{
					if (it instanceof SubCase)
					{
						res.add(new ReadableValue(name + "." + it.getId(), ((SubCase) it).getName()));
					}
				});
			}
		}

		return res;
	}

	private IClientFactory		defaultClient;
	private IApplicationFactory	defaultApp;

	private Configuration		configuration;
	private AbstractEvaluator	evaluator;
	private RunnerListener		runnerListener;
	private IMatrixListener		matrixListener	= null;
	private PrintStream			outStream		= null;
	private ClientsPool			clients			= null;
	private ServicePool			services		= null;
	private ApplicationPool		applications	= null;
	private DataBasePool		databases		= null;
	private Map<String, Matrix>	libs;

	private static final Logger	logger			= Logger.getLogger(Context.class);
}
