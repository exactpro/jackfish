////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.client.IClientFactory;
import com.exactprosystems.jf.api.common.IMatrix;
import com.exactprosystems.jf.api.common.IMatrixRunner;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.AbstractDocument;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixRoot;
import com.exactprosystems.jf.documents.matrix.parser.items.TestCase;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@DocumentInfo(newName = "NewMatrix", extentioin = "jf", description = "Matrix")
public class Matrix extends AbstractDocument implements IMatrix
{
	public static final String EMPTY_STRING = "<empty>";

	public Matrix(String matrixName, DocumentFactory factory, IMatrixRunner runner, IMatrixListener matrixListener, boolean isLibrary) throws Exception
	{
		super(matrixName, factory);

		this.runner = (MatrixRunner)runner;
		this.isLibrary = isLibrary;
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
	
	@Override
	public String toString()
	{
		return getName();
	}
	
     public Matrix makeCopy() throws Exception
      {
          Matrix copy = new Matrix(getName(), getFactory(), this.runner, this.matrixListener, this.isLibrary);
          copy.root.init(copy);
          copy.buffer = new StringBuilder(this.buffer);
          copy.enumerate();
          return copy;
      }

	
	// ==============================================================================================================================
	// interface IMatrix
	// ==============================================================================================================================
	@Override
	public void setDefaultApp(String id)
	{
		if (id == null || id.equals(EMPTY_STRING))
		{
			this.defaultApp = null;
			return;
		}
		try
		{
			this.defaultApp = getFactory().getConfiguration().getApplicationPool().loadApplicationFactory(id);
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
		if (id == null || id.equals(EMPTY_STRING))
		{
			this.defaultClient = null;
			return;
		}
		try
		{
			this.defaultClient = getFactory().getConfiguration().getClientPool().loadClientFactory(id);
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

	@Override
	public IMatrixRunner getMatrixRunner()
	{
	    return this.runner;
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
    public void close(Settings settings) throws Exception
    {
        super.close(settings);
        
        if (this.runner != null)
        {
            this.runner.close();
        }
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
	public boolean isLibrary()
	{
		return this.isLibrary;
	}
	
	public void addCopyright(String text)
	{
		Optional.ofNullable(this.getRoot().get(0)).ifPresent(first -> first.addCopyright(text));
	}

	public MatrixItem getRoot()
	{
		return this.root;
	}
	
	public List<String> listOfIds(Class<? extends MatrixItem> clazz)
	{
		final List<String> res = new ArrayList<>();

		this.getRoot().bypass(item ->
		{
			if (item.getClass() == clazz && !Str.IsNullOrEmpty(item.getId()))
			{
				res.add(item.getId());
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
			report.reportStarted(null, null);
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
			report.reportFinished(0, 0, null, null);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

	}

	public boolean checkMatrix(Context context, AbstractEvaluator evaluator, StringBuilder error)
	{
		this.matrixListener.reset(this);
		this.root.check(context, evaluator, this.matrixListener, null);

		if (!this.matrixListener.isOk())
		{
			logger.error(this.matrixListener.getExceptionMessage());
			error.append(this.matrixListener.getExceptionMessage());
			return false;
		}
		
		return true;
	}
	
	public void start(Context context, AbstractEvaluator evaluator, ReportBuilder report)
	{
		assert (context != null);
		assert (evaluator != null);
		assert (report != null);
		Date startTime = new Date();

		this.matrixListener.matrixStarted(this);

		try
		{
			report.reportStarted(getMatrixBuffer(), VersionInfo.getVersion());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		try
		{
			if (this.root != null)
			{
				this.root.bypass(MatrixItem::correctParametersType);
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

		Date finishTime = new Date();
		try
		{
			report.reportFinished(getRoot().count(Result.Failed), getRoot().count(Result.Passed), startTime, finishTime);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		this.matrixListener.matrixFinished(this, countResult(Result.Passed), countResult(Result.Failed));
	}

	private IClientFactory		defaultClient;
	private IApplicationFactory	defaultApp;

	private boolean				isLibrary;
	private int					count   = 0;
	private MatrixItem			root	= null;
	private StringBuilder		buffer;
	private IMatrixListener		matrixListener;
    private MatrixRunner        runner;

	private static final Logger	logger	= Logger.getLogger(Matrix.class);
}
