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
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixError;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixRoot;
import com.exactprosystems.jf.documents.matrix.parser.items.NameSpace;
import com.exactprosystems.jf.documents.matrix.parser.items.SubCase;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.Table;
import org.apache.log4j.Logger;

import java.io.PrintStream;
import java.io.Reader;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class Context implements IContext, AutoCloseable, Cloneable
{
	public static final String matrixColumn 			= "Matrix";
	public static final String testCaseIdColumn 		= "TestCaseId";
	public static final String testCaseColumn 			= "TestCase";
	public static final String stepColumn 				= "Step";
	public static final String stepIdentityColumn 		= "StepIdentity";
	public static final String timeColumn 				= "Time";
	public static final String resultColumn 			= "Result";
	public static final String errorColumn 				= "Error";
    public static final String screenshotColumn         = "Screenshot";
	public static final String[] resultColumns = new String[]
			{
				matrixColumn, testCaseIdColumn, testCaseColumn, stepIdentityColumn, stepColumn, timeColumn, 
				resultColumn, errorColumn, screenshotColumn
			};

	private Monitor monitor = new Monitor();
	
	public Context(DocumentFactory factory, IMatrixListener matrixListener, PrintStream out, Presenter presenter) throws Exception
	{
		this.factory = factory;
		this.matrixListener = matrixListener;
		this.outStream = out;
		this.evaluator = factory.createEvaluator();
		this.presenter = presenter;

		createResultTable();
	}

	@Override
	public MatrixRunner createRunner(String name, Reader reader, Date startTime, Object parameter) throws Exception
	{
		return new MatrixRunner(this, name, reader, startTime, parameter);
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
			clone.presenter         = this.presenter;

			return clone;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new InternalError();
		}
	}
	
    public void reset() throws Exception
    {
        this.handlers.clear();
        this.evaluator.reset();
    }

	public void createResultTable()
	{
		String headers[] = resultColumns;
		this.resultTable =  new Table(headers, this.evaluator);
	}

	public void setHandler(HandlerKind handlerKind, SubCase subCase)
	{
	    if (handlerKind != null)
	    {
	        this.handlers.put(handlerKind, subCase);
	    }
	}
	
	public ReturnAndResult  runHandler(HandlerKind handlerKind, ReportBuilder report, MatrixError err) 
    {
       if (handlerKind == null)
       {
           return null;
       }
       
       SubCase handler = this.handlers.get(handlerKind);
       if (handler != null)
       {
           if (handlerKind == HandlerKind.OnTestCaseError || handlerKind == HandlerKind.OnStepError)
           {
               Parameters parameters = handler.getParameters();
               if (parameters.size() > 0)
               {
                   Parameter par = parameters.getByIndex(0); 
                   par.setValue(err);
                   handler.setRealParameters(parameters);
               }
               
           }
           else
           {
               handler.setRealParameters(new Parameters());
           }
           
           return handler.execute(this, this.matrixListener, this.evaluator, report);
       }
       return null;
    }
	
    public void showReport(String reportName)
    {
        if (this.presenter != null)
        {
            this.presenter.show(reportName);
        }
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

	public DocumentFactory getFactory()
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

		Matrix matrix = this.libs.get(ns);
		
		if (matrix == null)
		{
            matrix = getConfiguration().getLibs().get(ns);
            if (matrix == null)
            {
                return null;
            }
            
	        try
	        {
	            matrix = matrix.clone();
	            matrix.getRoot().init(item.getMatrix());
	            this.libs.put(ns, matrix);
	            
	        }
	        catch (Exception e)
	        {
	            logger.error(e.getMessage(), e);
	        }
		}

		MatrixItem mitem = matrix.getRoot().find(false, NameSpace.class, ns);
		if(mitem == null) 
		{
			return null;
		}

		return (SubCase) mitem.find(true, SubCase.class, id);
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
				MatrixItem mitem = lib.getRoot().find(false, NameSpace.class, name);
				if(mitem != null)
				{
					mitem.bypass(it ->
					{
						if (it instanceof SubCase)
						{
							res.add(new ReadableValue(name + "." + it.getId(), ((SubCase) it).getName()));
						}
					});
				}
			}
		}

		return res;
	}

	private class Monitor
	{
		public void enter()
		{
			synchronized (this.obj)
			{
				if (this.obj.get())
				{
					try
					{
						this.obj.set(false);
						while (!this.obj.get())
						{
							this.obj.wait();
						}
					}
					catch (InterruptedException e)
					{
						logger.error(e.getMessage(), e);
					}
				}
			}
		}

		public void leave()
		{
			while (!this.obj.get())
			{
				synchronized (this.obj)
				{
					this.obj.set(true);
					this.obj.notifyAll();
				}
			}
		}

		private final AtomicBoolean obj	= new AtomicBoolean(true);
	}

	public void setTracing(boolean value)
	{
		this.tracing = value;
	}

	public boolean checkMonitor(IMatrixListener listener, MatrixItem item)
	{
		if (this.stop)
		{
			return true;
		}

		if (item.isBreakPoint() || this.pause)
		{
			listener.paused(item.getMatrix(), item);
			this.monitor.enter();
		}
		if (this.tracing)
		{
			try
			{
				Thread.sleep(200);
			}
			catch (InterruptedException e)
			{ }
		}

		return false;
	}

	public void prepareMonitor()
	{
		this.pause = false;
		this.stop = false;
	}

	public void stop()
	{
		this.pause = false;
		this.stop = true;
		this.monitor.leave();
	}

	public void pause()
	{
		this.pause = true;
		this.stop = false;
	}

	public void step()
	{
		this.pause = true;
		this.stop = false;
		this.monitor.leave();
	}

	public void resume()
	{
		this.pause = false;
		this.stop = false;
		this.monitor.leave();
	}

	private volatile boolean	pause;
	private volatile boolean	stop;
	private volatile boolean	tracing;

	private DocumentFactory factory;
	private PrintStream outStream;
	private IMatrixListener matrixListener;
	private AbstractEvaluator evaluator;
	private Table resultTable;
	private Presenter presenter;

	private Map<String, Matrix> libs = new HashMap<>();
	private Map<HandlerKind, SubCase> handlers = new HashMap<>();
	
	private static final Logger logger = Logger.getLogger(Context.class);
}
