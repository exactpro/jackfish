////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.csv.Csv;
import com.exactprosystems.jf.documents.guidic.GuiDictionary;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.documents.msgdic.MessageDictionary;
import com.exactprosystems.jf.documents.text.PlainText;
import com.exactprosystems.jf.documents.vars.SystemVars;

public abstract class DocumentFactory
{
	public DocumentFactory(Consumer<String> errorListener)
	{
		this.errorListener = errorListener;
	}

	public void setConfiguration(Configuration configuration)
	{
		this.configuration = configuration;
	}

	public void setSettings(Settings settings)
	{
		this.settings = settings;
	}

	
	public Context 				createContext(IMatrixListener matrixListener, PrintStream out) throws Exception
	{
//		return new Context(matrixListener, out, this);
		return null;
	}
	
	public Configuration 		createConfig(String fileName)
	{
		try
		{
			return createConfig(fileName, this.settings);
		}
		catch (Exception e)
		{
			printStackTrace(e);
		}
		return null;
	}

	public Matrix 				createMatrix(String fileName)
	{
		try
		{
			return createMatrix(fileName, this.configuration);
		}
		catch (Exception e)
		{
			printStackTrace(e);
		}
		return null;
	}

	public MessageDictionary 	createClientDictionary(String fileName)
	{
		try
		{
			return createClientDictionary(fileName, this.configuration);
		}
		catch (Exception e)
		{
			printStackTrace(e);
		}
		return null;
	}

	public GuiDictionary 		createAppDictionary(String fileName)
	{
		try
		{
			return createAppDictionary(fileName, this.configuration);
		}
		catch (Exception e)
		{
			printStackTrace(e);
		}
		return null;
	}

	public Csv 					createCsv(String fileName)
	{
		try
		{
			return createCsv(fileName, this.configuration);
		}
		catch (Exception e)
		{
			printStackTrace(e);
		}
		return null;
	}

	public PlainText 			createPlainText(String fileName)
	{
		try
		{
			return createPlainText(fileName, this.configuration); 
		}
		catch (Exception e)
		{
			printStackTrace(e);
		}
		return null;
	}

	public SystemVars 			createVars(String fileName)
	{
		try
		{
			return createVars(fileName, this.configuration);
		}
		catch (Exception e)
		{
			printStackTrace(e);
		}
		return null;
	}


	protected abstract Configuration 		createConfig(String fileName, Settings settings) throws Exception;

	protected abstract Matrix 				createMatrix(String fileName, Configuration configuration) throws Exception;

	protected abstract MessageDictionary 	createClientDictionary(String fileName, Configuration configuration) throws Exception;

	protected abstract GuiDictionary 		createAppDictionary(String fileName, Configuration configuration) throws Exception;

	protected abstract Csv 					createCsv(String fileName, Configuration configuration) throws Exception;

	protected abstract PlainText 			createPlainText(String fileName, Configuration configuration) throws Exception; 

	protected abstract SystemVars 			createVars(String fileName, Configuration configuration) throws Exception;

	
	private void printStackTrace(Exception e)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		e.printStackTrace(ps);
		
		this.errorListener.accept(baos.toString());
	}

	protected Configuration 		configuration;

	protected Settings 			settings;
	
	protected Consumer<String>	errorListener;  
}
