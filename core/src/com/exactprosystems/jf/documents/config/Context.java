////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.config;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixRoot;
import com.exactprosystems.jf.documents.matrix.parser.items.SubCase;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.Table;
import org.apache.log4j.Logger;

import java.io.PrintStream;
import java.io.Reader;
import java.util.*;
import java.util.Map.Entry;

public class Context implements IContext, AutoCloseable, Cloneable
{
	public static final String matrixColumn 			= "Matrix";
	public static final String testCaseIdColumn 		= "TestCaseId";
	public static final String testCaseColumn 			= "TestCase";
	public static final String stepColumn 				= "Step";
	public static final String stepIdentityColumn 		= "StepIdentity";
	public static final String resultColumn 			= "Result";
	public static final String errorColumn 				= "Error";
	public static final String[] resultColumns = new String[]
			{
				matrixColumn, testCaseIdColumn, testCaseColumn, resultColumn, errorColumn
			};
	
	public Context(DocumentFactory factory, IMatrixListener matrixListener, PrintStream out) throws Exception
	{
		this.factory 		= factory;
		this.matrixListener = matrixListener;
		this.outStream 		= out;
		this.evaluator 		= factory.createEvaluator();

		createResultTable();
	}

	@Override
	public MatrixRunner createRunner(Reader reader, Date startTime, Object parameter) throws Exception
	{
		return new MatrixRunner(this, reader, startTime, parameter);
	}

	@Override
	public Context clone() throws CloneNotSupportedException
	{
		try
		{
			Context clone = ((Context) super.clone());

			clone.outStream 		= this.outStream;
			clone.matrixListener 	= this.matrixListener == null ? null : this.matrixListener.clone();
			clone.factory			= this.factory;
			clone.evaluator 		= this.factory.createEvaluator();
			clone.libs 				= new HashMap<String, Matrix>();
			clone.resultTable		= this.resultTable.clone();

			return clone;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new InternalError();
		}
	}

	public void createResultTable()
	{
		String headers[] = new String[] 
				{ 
					matrixColumn, testCaseIdColumn, testCaseColumn, stepColumn, stepIdentityColumn,  
					resultColumn, errorColumn
				};
		this.resultTable =  new Table(headers, this.evaluator);
	}

	public Context setOut(PrintStream out)
	{
		this.outStream = out;
		return this;
	}
	
	public AbstractEvaluator getEvaluator()
	{
		return this.evaluator;
	}

	public DocumentFactory	getFactory()
	{
		return this.factory;
	}

	public Configuration getConfiguration()
	{
		return this.factory.getConfiguration();
	}

	public PrintStream getOut()
	{
		return this.outStream;
	}

	public IMatrixListener getMatrixListener()
	{
		return this.matrixListener;
	}

	public Table getTable()
	{
		return this.resultTable;
	}
	
	@Override
	public void close() throws Exception
	{
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

		Matrix matrix = getConfiguration().getLibs().get(ns);
		try {
			matrix = matrix.clone();
		} catch (CloneNotSupportedException e) {
			logger.error(e.getMessage(), e);
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
		for (Entry<String, Matrix> entry : getConfiguration().getLibs().entrySet())
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

	private DocumentFactory			factory;
	private PrintStream				outStream;
	private IMatrixListener			matrixListener;
	private AbstractEvaluator		evaluator;
	private Table 					resultTable;

	//TODO need to remove it, cause this the same as Configuration.libs
	private Map<String, Matrix>		libs 			= new HashMap<>();

	private static final Logger	logger				= Logger.getLogger(Context.class);
}
