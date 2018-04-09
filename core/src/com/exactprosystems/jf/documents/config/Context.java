/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.config;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.common.MatrixConnection;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.common.WrongSubcaseNameException;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.matrix.Matrix;
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

/**
 * A class for support executing a matrix.
 * This class contains some methods for stopping, pausing and etc methods.
 */
public class Context implements IContext, AutoCloseable
{
	public static final String matrixColumn       = "Matrix";
	public static final String testCaseIdColumn   = "TestCaseId";
	public static final String testCaseColumn     = "TestCase";
	public static final String stepIdColumn       = "StepId";
	public static final String stepColumn         = "Step";
	public static final String stepIdentityColumn = "StepIdentity";
	public static final String timeColumn         = "Time";
	public static final String resultColumn       = "Result";
	public static final String errorColumn        = "Error";
	public static final String screenshotColumn   = "Screenshot";
	public static final String[] resultColumns = new String[]
			{
				matrixColumn, testCaseIdColumn, testCaseColumn, stepIdColumn, stepIdentityColumn, stepColumn, 
				timeColumn, resultColumn, errorColumn, screenshotColumn
			};

	private static final Logger  logger  = Logger.getLogger(Context.class);

	private volatile boolean pause;
	private volatile boolean stop;
	private volatile boolean tracing;

	private DocumentFactory   factory;
	private PrintStream       outStream;
	private IMatrixListener   matrixListener;
	private AbstractEvaluator evaluator;
	private Table             resultTable;
	private Presenter         presenter;
	private Map<String, Matrix>      libs             = new HashMap<>();
	private Map<HandlerKind, String> handlers         = new EnumMap<>(HandlerKind.class);
	private Date                     lastConfigUpdate = null;
	private Monitor                  monitor          = new Monitor();

	public static class EntryPoint
	{
		private Matrix  matrix;
		private SubCase subCase;

		public static final EntryPoint NULL = new EntryPoint(null, null);

		public EntryPoint(Matrix matrix, SubCase subCase)
		{
			this.matrix = matrix;
			this.subCase = subCase;
		}

		public Matrix getMatrix()
		{
			return matrix;
		}

		public SubCase getSubCase()
		{
			return subCase;
		}
	}

	public Context(DocumentFactory factory, IMatrixListener matrixListener, PrintStream out, Presenter presenter)
	{
		this.factory = factory;
		this.matrixListener = matrixListener;
		this.outStream = out;
		this.evaluator = factory.createEvaluator();
		this.presenter = presenter;

		this.createResultTable();
	}

	//region interface IContext
	public Context createCopy()
	{
		return this.getFactory().createContext();
	}

	@Override
	public IContext setOut(PrintStream out)
	{
		this.outStream = out;
		return this;
	}

	@Override
	public MatrixConnection startMatrix(String fileName, Reader reader, Object parameter) throws Exception
	{
		Matrix matrix = (Matrix) this.getFactory().createDocument(DocumentKind.MATRIX, fileName);
		matrix.load(reader);
		return matrix.start(new Date(), parameter);
	}
    //endregion

	/**
	 * Reset the current context. It's reset the all handlers and evaluator
	 *
	 * @see HandlerKind
	 * @see GlobalHandler
	 * @see AbstractEvaluator
	 */
	public void reset()
	{
		this.handlers.clear();
		this.evaluator.reset("" + getConfiguration().getVersion());
	}

	/**
	 * Create the result table of executing a matrix
	 * <p>
	 * User can interact with the table via actions {@link com.exactprosystems.jf.actions.system.ResultTable} and {@link com.exactprosystems.jf.actions.system.ResultTableUserValue}
	 */
	public void createResultTable()
	{
		this.resultTable =  new Table(resultColumns, this.evaluator);
	}

	/**
	 * Set the handler for a matrix items ( TestCase and Step)
	 *
	 * @throws Exception if SubCase with passed name not found
	 *
	 * @see Step
	 * @see TestCase
	 * @see HandlerKind
	 */
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
				SubCase subCase = this.referenceToSubcase(name, item).getSubCase();
				if (subCase != null)
				{
					//check parameters of subcase.
					if ((handlerKind == HandlerKind.OnStepError || handlerKind == HandlerKind.OnTestCaseError) && subCase.getParameters().isEmpty())
					{
						throw new WrongSubcaseNameException(String.format(R.CONTEXT_SET_HANDLER_EXCEPTION.get(), handlerKind, name));
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

	/**
	 * Run the stored handler.If passed local handler ( it mean {@link OnError} item from the matrix) is presented, the local handler will invoked.
	 * <p>
	 * If the handler local handler not presented, a handler will find on the global handlers
	 * @param start a time of executing the handler
	 * @param context context for a matrix
	 * @param listener a {@link IMatrixListener} object
	 * @param item a item, which should be handled
	 * @param handlerKind type of handler
	 * @param localHandler a local handler. If local handler is presented, this handler will executing
	 * @return a ReturnAndResult object
	 *
	 * @see IContext
	 * @see IMatrixListener
	 * @see ReportBuilder
	 * @see HandlerKind
	 * @see ReturnAndResult
	 * @see GlobalHandler
	 */
	public ReturnAndResult runHandler(long start, Context context, IMatrixListener listener, MatrixItem item, HandlerKind handlerKind, ReportBuilder report, MatrixError err, MatrixItem localHandler)
	{
		if (localHandler instanceof OnError)
		{
			((OnError) localHandler).setError(err);

			return new ReturnAndResult(start, localHandler.execute(context, listener, this.evaluator, report));
		}

		Variables locals = this.evaluator.createLocals();
		try
		{
			String name = this.handlers.get(handlerKind);
			if (name == null)
			{
				GlobalHandler globalHandler = this.getConfiguration().getGlobalHandler();
				if (globalHandler != null && globalHandler.isEnabled())
				{
					name = globalHandler.getGlobalHandler(handlerKind).get();
				}
			}

			if (!Str.IsNullOrEmpty(name))
			{
				SubCase handler = this.referenceToSubcase(name, item).getSubCase();
				if (handler != null)
				{
					if (handlerKind == HandlerKind.OnTestCaseError || handlerKind == HandlerKind.OnStepError)
					{
						Parameters parameters = handler.getParameters();
						if (!parameters.isEmpty())
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

					return new ReturnAndResult(start, handler.execute(this, this.matrixListener, this.evaluator, report));
				}
				else
				{
					return new ReturnAndResult(start, Result.Failed, String.format(R.CONTEXT_HANDLER_NOT_FOUND.get(), name), ErrorKind.FAIL, item);
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

	//region public getters
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
	//endregion

	@Override
	public void close()
	{
	}

	/**
	 * Find subCase with passed name. If subcase will not found into the matrix, the subcase will find in libraries
	 * @param name id of subcase, which need be founded
	 * @return EntryPoint with the found subcase. This method can return {@link EntryPoint#NULL} if subcase not found
	 */
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

		Configuration config = this.getConfiguration();

		//if current libs are out of date - clear the libraries.
		if (this.lastConfigUpdate == null || config.getLastUpdateDate().compareTo(this.lastConfigUpdate) > 0)
		{
			this.lastConfigUpdate = config.getLastUpdateDate();
			this.libs.clear();
		}
		Matrix matrix = this.libs.get(ns);

		if (matrix == null)
		{
			matrix = config.getLibs().get(ns);
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

		MatrixItem nameSpaceItem = matrix.getRoot().find(false, NameSpace.class, ns);
		if (nameSpaceItem == null)
		{
			return EntryPoint.NULL;
		}

		return new EntryPoint(matrix, (SubCase) nameSpaceItem.find(true, SubCase.class, id));
	}

	/**
	 * Return list of all subcases from a matrix, which is a source for passed MatrixItem
	 * <p>
	 * For the returned list will be added all subcase from the configurations
	 * @param item from will get a Matrix
	 * @return List of all subcases
	 *
	 * @see SubCase
	 * @see MatrixItem
	 */
	public List<ReadableValue> subcases(MatrixItem item)
	{
		final List<ReadableValue> res = new ArrayList<>();

		MatrixItem root = item.findParent(MatrixRoot.class);

		root.stream()
				.filter(mi -> mi instanceof SubCase)
				.map(mi -> (SubCase) mi)
				.map(subCase -> new ReadableValue(subCase.getId(), subCase.getName()))
				.forEach(res::add);

		this.getConfiguration().addSubcaseFromLibs(res);
		return res;
	}

	/**
	 * Set tracing for execute the matrix for the current Context
	 */
	public void setTracing(boolean value)
	{
		this.tracing = value;
	}

	/**
	 * Check the {@link Context#monitor}. If monitor is stopped ( or breakpointed ) executing the matrix will paused.
	 * <p>
	 * If {@link Context#tracing} is on, then before the execute next MatrixItem will small delay ( 200ms)
	 * @param listener a {@link IMatrixListener} for notify changes
	 * @param item a item for checking
	 * @return true, if matrix need be paused and false otherwise
	 */
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

	/**
	 * Set the initial values for the {@link Context#monitor}
	 */
	public void prepareMonitor()
	{
		this.pause = false;
		this.stop = false;
	}

	/**
	 * Stop the {@link Context#monitor} and leaving from it
	 */
	public void stop()
	{
		this.pause = false;
		this.stop = true;
		this.monitor.leave();
	}

	/**
	 * @return true, if the matrix was stopped (via {@link Context#stop()} method) and false otherwise
	 */
	public boolean isStop()
	{
		return this.stop;
	}

	/**
	 * Pause a matrix
	 */
	public void pause()
	{
		this.pause = true;
		this.stop = false;
	}

	/**
	 * Execute the current item on a matrix and stop on the next item
	 */
	public void step()
	{
		this.pause = true;
		this.stop = false;
		this.monitor.leave();
	}

	/**
	 * Running a stopped/paused matrix
	 */
	public void resume()
	{
		this.pause = false;
		this.stop = false;
		this.monitor.leave();
	}

	private static class Monitor
	{
		private final AtomicBoolean obj = new AtomicBoolean(true);

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
	}
}
