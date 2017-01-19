////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.csv.Csv;
import com.exactprosystems.jf.documents.guidic.GuiDictionary;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.documents.matrix.parser.listeners.RunnerListener;
import com.exactprosystems.jf.documents.msgdic.MessageDictionary;
import com.exactprosystems.jf.documents.text.PlainText;
import com.exactprosystems.jf.documents.vars.SystemVars;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Notifier;
import com.exactprosystems.jf.functions.Table;

import java.util.List;
import java.util.Map;

public abstract class DocumentFactory
{
	public DocumentFactory()
	{
		this.settings = Settings.load(Settings.SettingsPath);
	}

	public final void setConfiguration(Configuration configuration)
	{
		this.configuration = configuration;
	}
	
	public final Configuration getConfiguration()
	{
		return this.configuration;
	}

	public final Settings getSettings()
	{
		return this.settings;
	}

	public final AbstractEvaluator createEvaluator()
	{
		try
		{
			checkConfiguration();
			AbstractEvaluator eval = this.configuration.createEvaluator();
			 
			 return eval;
		}
		catch (Exception e)
		{
			error(null, e);
		}
		return null;
	}

	public final Context 				createContext() throws Exception
	{
		try
		{
			checkConfiguration();
			return createContext(this.configuration, createMatrixListener());
		}
		catch (Exception e)
		{
			error(null, e);
		}
		return null;
	}
	
	public final Configuration 		createConfig(String fileName)
	{
		try
		{
			return createConfig(fileName, this.settings);
		}
		catch (Exception e)
		{
			error(null, e);
		}
		return null;
	}

	public final Matrix 				createLibrary(String fileName)
	{
		try
		{
			checkConfiguration();
			Matrix ret = createLibrary(fileName, this.configuration, createMatrixListener());
			
			return ret;
		}
		catch (Exception e)
		{
			error(null, e);
		}
		return null;
	}

	public final Matrix 				createMatrix(String fileName)
	{
		try
		{
			checkConfiguration();
			Matrix ret = createMatrix(fileName, this.configuration, createMatrixListener());
			
			return ret;
		}
		catch (Exception e)
		{
			error(null, e);
		}
		return null;
	}

	public final MessageDictionary 	createClientDictionary(String fileName)
	{
		try
		{
			checkConfiguration();
			return createClientDictionary(fileName, this.configuration);
		}
		catch (Exception e)
		{
			error(null, e);
		}
		return null;
	}

	public final GuiDictionary 		createAppDictionary(String fileName)
	{
		try
		{
			checkConfiguration();
			return createAppDictionary(fileName, this.configuration);
		}
		catch (Exception e)
		{
			error(null, e);
		}
		return null;
	}

	public final Csv 					createCsv(String fileName)
	{
		try
		{
			checkConfiguration();
			return createCsv(fileName, this.configuration);
		}
		catch (Exception e)
		{
			error(null, e);
		}
		return null;
	}

	public final PlainText 			createPlainText(String fileName)
	{
		try
		{
			checkConfiguration();
			return createPlainText(fileName, this.configuration); 
		}
		catch (Exception e)
		{
			error(null, e);
		}
		return null;
	}

	public final SystemVars 			createVars(String fileName)
	{
		try
		{
			checkConfiguration();
			return createVars(fileName, this.configuration);
		}
		catch (Exception e)
		{
			error(null, e);
		}
		return null;
	}

	public abstract void 					error(String message, Exception exeption);
	
	public abstract void 					popup(String message, Notifier notifier);

	public abstract Object					input(AbstractEvaluator evaluator, String title, Object defaultValue, HelpKind helpKind, List<ReadableValue> dataSource);

    public abstract boolean                 editTable(AbstractEvaluator evaluator, String title, Table table, Map<String, Boolean> columns);

	protected abstract Context 				createContext(Configuration configuration, IMatrixListener matrixListener) throws Exception;

	protected abstract Configuration 		createConfig(String fileName, Settings settings) throws Exception;

	protected abstract Matrix 				createLibrary(String fileName, Configuration configuration, IMatrixListener matrixListener) throws Exception;

	protected abstract Matrix 				createMatrix(String fileName, Configuration configuration, IMatrixListener matrixListener) throws Exception;

	protected abstract MessageDictionary 	createClientDictionary(String fileName, Configuration configuration) throws Exception;

	protected abstract GuiDictionary 		createAppDictionary(String fileName, Configuration configuration) throws Exception;

	protected abstract Csv 					createCsv(String fileName, Configuration configuration) throws Exception;

	protected abstract PlainText 			createPlainText(String fileName, Configuration configuration) throws Exception; 

	protected abstract SystemVars 			createVars(String fileName, Configuration configuration) throws Exception;

	protected abstract IMatrixListener 		createMatrixListener();
	
	public abstract RunnerListener 		getRunnerListener();
	
	private void checkConfiguration() throws EmptyConfigurationException
	{
		if (this.configuration == null)
		{
			throw new EmptyConfigurationException();
		}
	}

	protected Configuration 		configuration;

	protected Settings 				settings;
}
