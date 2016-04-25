////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.parser;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.client.IClientFactory;
import com.exactprosystems.jf.api.common.IMatrix;
import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.DocumentInfo;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.common.parser.items.MatrixRoot;
import com.exactprosystems.jf.common.parser.items.NameSpace;
import com.exactprosystems.jf.common.parser.items.TestCase;
import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.tool.AbstractDocument;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@DocumentInfo(newName = "NewMatrix", extentioin = "jf", description = "Matrix")
public class Matrix extends AbstractDocument implements IMatrix, Cloneable
{
	public static final String EMPTY_STRING = "<empty>";

	public Matrix(Matrix matrix, Configuration configuration) throws Exception
	{
		super(matrix.getName(), configuration);

		this.root = matrix.root;
		this.buffer = matrix.buffer;
		this.matrixListener = matrix.matrixListener;
	}

	public Matrix(String matrixName, Configuration configuration, IMatrixListener matrixListener) throws Exception
	{
		super(matrixName, configuration);

		this.root = new MatrixRoot(matrixName);
		this.buffer = new StringBuilder();
		this.matrixListener = matrixListener;

		if (getName() != null)
		{
			if (!matrixListener.isOk())
			{
				String mgs = matrixListener.getExceptionMessage();
				logger.error(mgs);
				throw new Exception("Matrix did not executed cause errors." + mgs);
			}

		}
	}

	public void setListener(IMatrixListener listener)
	{
		this.matrixListener = listener;
	}

	public void enumerate()
	{
		AtomicInteger count = new AtomicInteger(0);
		Optional.ofNullable(this.root).ifPresent(root -> root.bypass(item -> item.setNubmer(count.getAndIncrement())));
	}
	
	// ==============================================================================================================================
	// interface IMatrix
	// ==============================================================================================================================
	@Override
	public void setDefaultApp(String id)
	{
		if (id.equals(EMPTY_STRING))
		{
			this.defaultApp = null;
			return;
		}
		try
		{
			this.defaultApp = getConfiguration().getApplicationPool().loadApplicationFactory(id);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public IApplicationFactory getDefaultApp()
	{
		return this.defaultApp;
	}
	
	@Override
	public void setDefaultClient(String id)
	{
		if (id.equals(EMPTY_STRING))
		{
			this.defaultClient = null;
			return;
		}
		try
		{
			this.defaultClient = getConfiguration().getClientPool().loadClientFactory(id);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	public IClientFactory getDefaultClient()
	{
		return this.defaultClient;
	}

	@Override
	public AppConnection getDefaultApplicationConnection()
	{
		return null;
	}

	// ==============================================================================================================================
	// AbstractDocument
	// ==============================================================================================================================
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
		this.root = new MatrixRoot(getName());
		try (BufferedReader rawReader = new BufferedReader(reader))
		{
			this.buffer.delete(0, this.buffer.length());
			String line = null;
			while ((line = rawReader.readLine()) != null)
			{
				this.buffer.append(line).append('\n');
			}
			Reader str = new StringReader(this.buffer.toString());

			Parser parser = new Parser();
			this.root = parser.readMatrix(this, str, this.matrixListener);
		}
	}

	@Override
	public void create() throws Exception
	{
		super.create();

		this.root = new MatrixRoot(getName());
		this.root.init(this);
		TestCase item = new TestCase("Test case");
		item.createId();
		this.root.insert(0, item);
	}

	@Override
	public boolean canClose() throws Exception
	{
		return true;
	}

	@Override
	public void save(String fileName) throws Exception
	{
		super.save(fileName);

		try (Writer rawWriter = new FileWriter(new File(fileName)))
		{
			Parser parser = new Parser();
			parser.saveMatrix(this.root, rawWriter);
		}
	}

	// ==============================================================================================================================
	// interface Cloneable
	// ==============================================================================================================================
	@Override
	public Matrix clone() throws CloneNotSupportedException
	{
		try
		{
			Matrix clone = ((Matrix) super.clone());
			clone.root = root.clone();
			clone.root.init(clone);
			clone.buffer = buffer;
			clone.matrixListener = matrixListener;
			clone.enumerate();
			return clone;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new InternalError();
		}
	}

	// ==============================================================================================================================
	// interface Mutable
	// ==============================================================================================================================
	@Override
	public final boolean isChanged()
	{
		return this.root.isChanged();
	}

	@Override
	public final void saved()
	{
		super.saved();
		this.root.saved();
	}

	// ==============================================================================================================================

	public char[] getMatrixBuffer()
	{
		try
		{
			Parser parser = new Parser();
			StringWriter stringWriter = new StringWriter();
			parser.saveMatrix(this.root, stringWriter);
			return stringWriter.getBuffer().toString().toCharArray();
		}
		catch (Exception e)
		{
			return this.buffer.toString().toCharArray();
		}
	}

	// ==============================================================================================================================
	// interface to edit Matrix
	// ==============================================================================================================================
	public void addCopyright(String text)
	{
		Optional.ofNullable(this.getRoot().get(0)).ifPresent(first -> first.addCopyright(text));
	}

	public MatrixItem getRoot()
	{
		return this.root;
	}
	
	public List<String> nameSpaces()
	{
		final List<String> res = new ArrayList<>();

		this.getRoot().bypass(item ->
		{
			if (item instanceof NameSpace)
			{
				res.add(((NameSpace)item).getId());
			}
		});

		return res;
	}

	public int count(MatrixItem item)
	{
		if (item == null)
		{
			return this.root.count();
		}
		return item.count();
	}

	public int getIndex(MatrixItem item)
	{
		if (item == null)
		{
			return getIndex(this.root);
		}
		if (item.getParent() != null)
		{
			for (int i = 0; i < item.getParent().count(); i++)
			{
				if (item.getParent().get(i) == item)
				{
					return i;
				}
			}
		}
		
		return -1;
	}
	
	public MatrixItem get(MatrixItem item, int index)
	{
		if (item == null)
		{
			return this.root.get(index);
		}
		return item.get(index);
	}

	public void insert(MatrixItem item, int index, MatrixItem what)
	{
		MatrixItem parent = item;
		if (item == null)
		{
			parent = this.root;
		}
		parent.insert(index, what);
	}

	public void remove(MatrixItem item)
	{
		if (item != null)
		{
			item.remove();
		}
	}

	public void replace(MatrixItem old, String value)
	{

	}

	// ==============================================================================================================================

	public final List<MatrixItem> find(final String what, final boolean caseSensitive, final boolean wholeWord)
	{
		final List<MatrixItem> res = new ArrayList<>();

		this.getRoot().bypass(item ->
		{
			if (item.matches(what, caseSensitive, wholeWord))
			{
				res.add(item);
			}
		});

		return res;
	}

	public int currentItem()
	{
		return this.count;
	}
	
	public int countResult(Result what)
	{
		if (this.root != null)
		{
			return this.root.count(what);
		}
		return 0;
	}

	public void documentation(Context services, ReportBuilder report)
	{
		try
		{
			report.reportStarted(this);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		try
		{
			if (this.root != null)
			{
				for (int i = 0; i < this.root.count(); i++)
				{
					MatrixItem item = this.root.get(i);
					item.documentation(services, report);
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		try
		{
			report.reportFinished(this);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

	}

	public boolean checkMatrix(Context context, AbstractEvaluator evaluator)
	{
		this.matrixListener.reset(this);
		context.getConfiguration().updateLibs();
		this.root.check(context, evaluator, this.matrixListener, null);

		if (!this.matrixListener.isOk())
		{
			logger.error(this.matrixListener.getExceptionMessage());
			return false;
		}
		
		return true;
	}
	
	public void setTracing(boolean b)
	{
		this.tracing = b;
	}
	
	public void start(Context context, AbstractEvaluator evaluator, ReportBuilder report)
	{
		assert (context != null);
		assert (evaluator != null);
		assert (report != null);

		this.matrixListener.matrixStarted(this);

		try
		{
			report.reportStarted(this);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		try
		{
			if (this.root != null)
			{
				for (this.count = 0; this.count < this.root.count(); this.count++)
				{
					MatrixItem item = this.root.get(this.count);
					item.execute(context, this.matrixListener, evaluator, report);
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		try
		{
			report.reportFinished(this);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		this.matrixListener.matrixFinished(this, countResult(Result.Passed), countResult(Result.Failed));
	}

	public boolean checkMonitor(IMatrixListener listener, MatrixItem item)
	{
		if (this.stop)
		{
			return true;
		}

		if (item.isBreakPoint() || this.pause)
		{
			listener.paused(this, item);
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

	// ==============================================================================================================================
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

		private AtomicBoolean	obj	= new AtomicBoolean(true);
	}

	protected Monitor			monitor	= new Monitor();
	protected volatile boolean	pause;
	protected volatile boolean	stop;
	protected volatile boolean	tracing;

	private IClientFactory		defaultClient;
	private IApplicationFactory	defaultApp;

	private int					count   = 0;
	private MatrixItem			root	= null;
	private StringBuilder		buffer;
	private IMatrixListener		matrixListener;

	private static final Logger	logger	= Logger.getLogger(Matrix.class);
}
