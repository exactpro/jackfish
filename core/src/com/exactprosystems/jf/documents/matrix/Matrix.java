////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix;

import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.client.IClientFactory;
import com.exactprosystems.jf.api.common.IMatrix;
import com.exactprosystems.jf.api.common.MatrixConnection;
import com.exactprosystems.jf.api.common.MatrixState;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.AbstractDocument;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixRoot;
import com.exactprosystems.jf.documents.matrix.parser.items.NameSpace;
import com.exactprosystems.jf.documents.matrix.parser.items.TestCase;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@DocumentInfo(
        kind = DocumentKind.MATRIX,
        newName = "NewMatrix", 
        extentioin = "jf", 
        description = "Matrix"
)
public class Matrix extends AbstractDocument implements IMatrix
{
    public static final String           EMPTY_STRING     = "<empty>";
    private static final Logger          logger           = Logger.getLogger(Matrix.class);

    private IClientFactory               defaultClient;
    private IApplicationFactory          defaultApp;

    private boolean                      isLibrary;
    private int                          count            = 0;
    private MatrixItem                   root             = null;
    private IMatrixListener              matrixListener   = null;
    private MatrixEngine                 engine           = null;
    private MutableValue<MatrixState>    stateProperty    = new MutableValue<>(MatrixState.Created);

	public Matrix(String matrixName, DocumentFactory factory, IMatrixListener matrixListener, boolean isLibrary) throws Exception
	{
		super(matrixName, factory); 

        if (!isLibrary)
        {
            this.engine = new MatrixEngine(factory.createContext(), this);
        }
        
		this.isLibrary = isLibrary;
		this.root = new MatrixRoot(matrixName);
		this.matrixListener = matrixListener;

		if (!getNameProperty().isNullOrEmpty())
		{
			if (!matrixListener.isOk())
			{
				String mgs = matrixListener.getExceptionMessage();
				logger.error(mgs);
				throw new Exception("Matrix did not executed cause errors." + mgs);
			}

		}
	}

	public MatrixEngine getEngine()
	{
	    return this.engine;
	}
	
	public MutableValue<MatrixState> getStateProperty()
	{
	    return this.stateProperty;
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
		return getNameProperty().toString();
	}
	
     public Matrix makeCopy() throws Exception
      {
          Matrix copy = new Matrix(getNameProperty().get(), getFactory(), this.matrixListener, this.isLibrary);
          copy.root = this.root.clone();
          copy.root.init(copy, copy);
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

	// ==============================================================================================================================
	// AbstractDocument
	// ==============================================================================================================================
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
		this.root = new MatrixRoot(getNameProperty().get());
		StringBuffer buffer = new StringBuffer();
		try (BufferedReader rawReader = new BufferedReader(reader))
		{
			String line = null;
			while ((line = rawReader.readLine()) != null)
			{
				buffer.append(line).append('\n');
			}
			Reader stringReader = CommonHelper.readerFromString(buffer.toString());

			Parser parser = new Parser();
			this.root = parser.readMatrix(this.root, stringReader);
			this.root.init(this, this);
			enumerate();
		}
	}

	@Override
	public void create() throws Exception
	{
		super.create();

		this.root = new MatrixRoot(getNameProperty().get());
		this.root.init(this, this);

		if (this.isLibrary())
		{
			NameSpace item = new NameSpace();
			this.root.insert(0, item);
		}
		else
		{
			TestCase item = new TestCase("Test case");
			item.createId();
			this.root.insert(0, item);
		}
	}

	@Override
	public boolean canClose() throws Exception
	{
		return true;
	}
	
    @Override
    public void close() throws Exception
    {
        super.close();
        
        if (this.engine != null)
        {
            this.engine.close();
        }
    }

	@Override
	public void save(String fileName) throws Exception
	{
		super.save(fileName);
		
		try (Writer rawWriter = CommonHelper.writerToFileName(fileName))
		{
			Parser parser = new Parser();
			parser.saveMatrix(this.root, rawWriter);
		}
	}
	
	@Override
	public void display() throws Exception
	{
	    super.display();
	    
	    this.stateProperty.fire();
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

	public char[] getMatrixBuffer() throws Exception
	{
		Parser parser = new Parser();
		StringWriter stringWriter = new StringWriter();
		parser.saveMatrix(this.root, stringWriter);
		return stringWriter.getBuffer().toString().toCharArray();
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

	public MatrixConnection start(Date time, Object parameter) throws Exception
	{
	    MatrixConnection res = new MatrixConnectionImpl(this);
	    if (getEngine() != null)
	    {
	        getEngine().start(time, parameter);
	    }
	    
	    return res;
	}

    public void stop()
    {
        if (getEngine() != null)
        {
            getEngine().stop();
        }
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

	protected boolean checkMatrix(Context context, AbstractEvaluator evaluator, StringBuilder error)
	{
		this.matrixListener.reset(this);
		this.root.check(context, evaluator, this.matrixListener);

		if (!this.matrixListener.isOk())
		{
			logger.error(this.matrixListener.getExceptionMessage());
			error.append(this.matrixListener.getExceptionMessage());
			return false;
		}
		
		return true;
	}
	
	protected void start(Context context, AbstractEvaluator evaluator, ReportBuilder report)
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

}
