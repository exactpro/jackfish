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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.common.WrongSubcaseNameException;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.MatrixEngine;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.items.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.Table;
import org.apache.log4j.Logger;

import java.io.PrintStream;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Context implements IContext, AutoCloseable
{
	public static final String matrixColumn 			= "Matrix";
	public static final String testCaseIdColumn 		= "TestCaseId";
	public static final String testCaseColumn 			= "TestCase";
    public static final String stepIdColumn             = "StepId";
	public static final String stepColumn 				= "Step";
	public static final String stepIdentityColumn 		= "StepIdentity";
	public static final String timeColumn 				= "Time";
	public static final String resultColumn 			= "Result";
	public static final String errorColumn 				= "Error";
    public static final String screenshotColumn         = "Screenshot";
	public static final String[] resultColumns = new String[]
			{
				matrixColumn, testCaseIdColumn, testCaseColumn, stepIdColumn, stepIdentityColumn, stepColumn, 
				timeColumn, resultColumn, errorColumn, screenshotColumn
			};

	public static class EntryPoint
	{
	    public static EntryPoint NULL = new EntryPoint(null, null);
	    
	    public EntryPoint(Matrix matrix, SubCase subCase)
        {
	        this.matrix = matrix;
	        this.subCase = subCase;
        }
	    
	    public Matrix matrix;
	    public SubCase subCase;
	}
	
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

	//region interface IContext
	public Context createCopy()
	{
		return this.getFactory().createContext();
	}
	//endregion

	public IContext setOut(PrintStream out)
	{
		this.outStream = out;
		return this;
	}

    public void reset() throws Exception
    {
        this.handlers.clear();
        this.evaluator.reset("" + getConfiguration().getVersion());
    }
    
	public void createResultTable()
	{
		String headers[] = resultColumns;
		this.resultTable =  new Table(headers, this.evaluator);
	}

    public void setHandler(HandlerKind handlerKind, String name, MatrixItem item) throws Exception
    {
        if (handlerKind != null)
        {
            if (Str.IsNullOrEmpty(name))
            {
                this.handlers.put(handlerKind, null);
            }
            else
            {
                SubCase subCase = referenceToSubcase(name, item).subCase;
                if (subCase != null)
                {
                	//check parameters of subcase.
					if ((handlerKind == HandlerKind.OnStepError || handlerKind == HandlerKind.OnTestCaseError) && subCase.getParameters().size() == 0)
					{
						throw new WrongSubcaseNameException(String.format("Parameters count for handler %s (subCase %s) must be great that 0", handlerKind, name));
					}
					this.handlers.put(handlerKind, name);
				}
                else
                {
                    throw new WrongSubcaseNameException(name);
                }
            }
        }
    }
	
    public ReturnAndResult runHandler(long start, Context context, IMatrixListener listener, MatrixItem item,
            HandlerKind handlerKind, ReportBuilder report, MatrixError err, MatrixItem localHandler)
    {
        Variables locals = this.evaluator.createLocals(); 
        try
        {
            if (localHandler instanceof OnError)
            {
                ((OnError) localHandler).setError(err);

                return new ReturnAndResult(start, localHandler.execute(context, listener, this.evaluator, report));
            }

            String name = this.handlers.get(handlerKind);
            if (name == null)
            {
                GlobalHandler globalHandler = getConfiguration().getGlobalHandler();
                if (globalHandler != null && globalHandler.isEnabled())
                {
                    name = globalHandler.getGlobalHandler(handlerKind).get();
                }
            }

            if (!Str.IsNullOrEmpty(name))
            {
                SubCase handler = referenceToSubcase(name, item).subCase;
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

                    return new ReturnAndResult(start,
                            handler.execute(this, this.matrixListener, this.evaluator, report));
                }
                else
                {
                    return new ReturnAndResult(start, Result.Failed, "Handler " + name + " is not found",
                            ErrorKind.FAIL, item);
                }
            }
            if (err != null)
            {
                return new ReturnAndResult(start, Result.Failed, err.Message, err.Kind, err.Where);
            }
            return new ReturnAndResult(start, Result.Passed, null);
        }
        finally
        {
            this.evaluator.setLocals(locals);
        }
    }
	
    public void showReport(String reportName)
    {
        if (this.presenter != null)
        {
            this.presenter.show(reportName);
        }
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

	public EntryPoint referenceToSubcase(String name, MatrixItem item)
	{
        if (name == null)
        {
            return EntryPoint.NULL;
        }
        String[] parts = name.split("\\.");
        
        if (parts.length == 0)
        {
            return EntryPoint.NULL;
        }
        else if (parts.length == 1)
        {
            if (item != null)
    	    {
        		MatrixItem ref = item.findParent(MatrixRoot.class).find(true, SubCase.class, name);
        
        		if (ref != null && ref instanceof SubCase)
        		{
        			return new EntryPoint(item.getMatrix(), (SubCase) ref);
        		}
    	    }
            return EntryPoint.NULL;
        }
		
		String ns = parts[0];
		String id = parts[1];

		Configuration config = getConfiguration();
		if (this.lastConfigUpdate == null || config.getLastUpdateDate().compareTo(this.lastConfigUpdate) > 0)
		{
		    this.lastConfigUpdate = config.getLastUpdateDate();
		    this.libs.clear();
		}
		Matrix matrix = this.libs.get(ns);
		
		if (matrix == null)
		{
            matrix = getConfiguration().getLibs().get(ns);
            if (matrix == null)
            {
                return EntryPoint.NULL;
            }

            try
	        {
	            matrix = matrix.makeCopy();
	            if (item != null)
	            {
	                matrix.getRoot().init(item.getMatrix(), matrix);
	            }
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
            return EntryPoint.NULL;
		}

		return new EntryPoint(matrix, (SubCase) mitem.find(true, SubCase.class, id));
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
		this.getConfiguration().addSubcaseFromLibs(res);
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

	public boolean isStop()
	{
		return this.stop;
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
	private Map<HandlerKind, String> handlers = new HashMap<>();
	private Date   lastConfigUpdate = null;
	
	private static final Logger logger = Logger.getLogger(Context.class);
}
