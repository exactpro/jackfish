////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.app.IApplicationPool;
import com.exactprosystems.jf.api.client.IClientFactory;
import com.exactprosystems.jf.api.client.IClientsPool;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.IMatrixRunner;
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.common.parser.items.MatrixRoot;
import com.exactprosystems.jf.common.parser.items.SubCase;
import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.common.parser.listeners.RunnerListener;
import com.exactprosystems.jf.sql.DataBasePool;

import org.apache.log4j.Logger;

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
		this.defaultClient = this.configuration.getClientPool().loadClientFactory(id);
	}

	public void setDefaultApp(String id) throws Exception
	{
		this.defaultApp = this.configuration.getApplicationPool().loadApplicationFactory(id);
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
	public IClientsPool getClients()
	{
		return this.configuration.getClientPool();
	}

	@Override
	public IServicesPool getServices()
	{
		return this.configuration.getServicesPool();
	}

	@Override
	public IApplicationPool getApplications()
	{
		return this.configuration.getApplicationPool();
	}

	public DataBasePool getDatabases()
	{
		return this.configuration.getDataBasesPool();
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
	}

	public SubCase referenceToSubcase(String name, MatrixItem item)
	{
		return this.configuration.referenceToSubcase(name, item);
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

	private static final Logger	logger			= Logger.getLogger(Context.class);
}
