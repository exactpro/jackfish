////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
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

import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@DocumentInfo(
		kind = DocumentKind.MATRIX,
		newName = "NewMatrix",
		extension = "jf",
		description = "Matrix"
)
public class Matrix extends AbstractDocument implements IMatrix
{
	public static final  String EMPTY_STRING = "<empty>";
	private static final Logger logger       = Logger.getLogger(Matrix.class);

	private IClientFactory      defaultClient;
	private IApplicationFactory defaultApp;

	private   boolean         isLibrary;
	private   MatrixItem      root;
	protected IMatrixListener matrixListener;
	private int                       count         = 0;
	private MatrixEngine              engine        = null;
	private MutableValue<MatrixState> stateProperty = new MutableValue<>(MatrixState.Created);

	public Matrix(String matrixName, DocumentFactory factory, IMatrixListener matrixListener, boolean isLibrary)
	{
		super(matrixName, factory);

		if (!isLibrary)
		{
			this.engine = new MatrixEngine(factory.createContext(), this);
		}

		this.isLibrary = isLibrary;
		this.root = new MatrixRoot(matrixName);
		this.matrixListener = matrixListener;
	}

	@Override
	public String toString()
	{
		return super.getNameProperty().toString();
	}

	public Matrix makeCopy()
	{
		Matrix copy = new Matrix(super.getNameProperty().get(), super.getFactory(), this.matrixListener, this.isLibrary);
		copy.root = this.root.createCopy();
		copy.root.init(copy, copy);
		copy.enumerate();
		return copy;
	}

	//region public Setters / Getters
	public MatrixEngine getEngine()
	{
		return this.engine;
	}

	public void setOut(PrintStream out)
	{
		Optional.ofNullable(this.engine).ifPresent(e -> e.getContext().setOut(out));
	}

	public MutableValue<MatrixState> getStateProperty()
	{
		return this.stateProperty;
	}

	public void setListener(IMatrixListener listener)
	{
		this.matrixListener = listener;
	}

	/**
	 * Return the property, which mean, the matrix is library or not.
	 * If the matrix is library, the matrix has not matrix engine, therefore, can't be executed
	 *
	 * @return true, if the matrix is library
	 */
	public boolean isLibrary()
	{
		return this.isLibrary;
	}

	/**
	 * @return a root of the matrix
	 */
	public MatrixItem getRoot()
	{
		return this.root;
	}
	//endregion

	//region interface IMatrix
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
			this.defaultApp = super.getFactory().getConfiguration().getApplicationPool().loadApplicationFactory(id);
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
	public MatrixConnection start(Date time, Object parameter) throws Exception
	{
		MatrixConnection res = new MatrixConnectionImpl(this);
		if (this.engine != null)
		{
			this.engine.start(time, parameter);
		}
		return res;
	}

	@Override
	public void stop()
	{
		if (this.engine != null)
		{
			this.engine.stop();
		}
	}

	//endregion

	//region AbstractDocument
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);

		this.root = new MatrixRoot(super.getNameProperty().get());
		Parser parser = new Parser();
		this.root = parser.readMatrix(this.root, reader);
		this.root.init(this, this);
		this.enumerate();
	}

	@Override
	public void create()
	{
		super.create();

		this.root = new MatrixRoot(super.getNameProperty().get());
		this.root.init(this, this);

		if (this.isLibrary)
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
		if (this.engine != null)
		{
			this.engine.close();
		}
		super.close();
	}

	@Override
	public void save(String fileName) throws Exception
	{
		try (Writer rawWriter = CommonHelper.writerToFileName(fileName))
		{
			Parser parser = new Parser();
			parser.saveMatrix(this.root, rawWriter);
		}
		super.save(fileName);
	}

	@Override
	public void display() throws Exception
	{
		super.display();
		this.stateProperty.fire();
	}

	//endregion

	//region interface Mutable
	@Override
	public final boolean isChanged()
	{
		return this.root.isChanged() || super.isChanged();
	}

	@Override
	public final void saved()
	{
		super.saved();
		this.root.saved();
	}

	//endregion

	//region public methods
	/**
	 * Set a number for all matrix item. The start number is 0, and it set to MatrixRoot
	 */
	public void enumerate()
	{
		AtomicInteger currentNumber = new AtomicInteger(0);
		Optional.ofNullable(this.root)
				.ifPresent(root -> root.bypass(item -> item.setNumber(currentNumber.getAndIncrement())));
	}

	/**
	 * Set the passed text as the comment to the first item from a MatrixRoot ( if this first item is present)
	 */
	public void addCopyright(String text)
	{
		Optional.ofNullable(this.getRoot().get(0)).ifPresent(first -> first.addCopyright(text));
	}

	public List<String> listOfIds(Class<? extends MatrixItem> clazz)
	{
		return this.getRoot().stream()
				.filter(item -> item.getClass() == clazz && !Str.IsNullOrEmpty(item.getId()))
				.map(MatrixItem::getId)
				.collect(Collectors.toList());
	}

	/**
	 * Return count of children for the passed item.
	 * If the passed item is null, the root item will use
	 *
	 * @return count of children for the passed item
	 *
	 * @see MatrixItem#count()
	 */
	public int count(MatrixItem item)
	{
		return item == null ? this.root.count() : item.count();
	}

	/**
	 * Get index of the passed item. If item is MatrixRoot, will return -1
	 *
	 * @return index of the item or -1, if item not found in the tree
	 */
	public int getIndex(MatrixItem item)
	{
		if (item == null)
		{
			return this.getIndex(this.root);
		}
		MatrixItem parent = item.getParent();
		if (parent != null)
		{
			return IntStream.range(0, parent.count())
					.filter(i -> parent.get(i) == item)
					.findFirst()
					.orElse(-1);
		}

		return -1;
	}

	/**
	 * Get a item, which will found from children on the passed item by passed index.
	 * If the passed item is null, the root item will used
	 *
	 * @see MatrixItem#get(int)
	 */
	public MatrixItem get(MatrixItem item, int index)
	{
		if (item == null)
		{
			return this.root.get(index);
		}
		return item.get(index);
	}

	/**
	 * Insert the passed item to the passed what in the passed index.
	 * If the passed what is null, the root item will used
	 *
	 * @param item the parent item for inserting
	 * @param index the index for inserting
	 * @param what the item, which will use for inserting
	 *
	 * @see MatrixItem#insert(int, MatrixItem)
	 */
	public void insert(MatrixItem item, int index, MatrixItem what)
	{
		MatrixItem parent = item;
		if (item == null)
		{
			parent = this.root;
		}
		parent.insert(index, what);
	}

	/**
	 * Remove the passed item ( if item is not null)
	 * @param item the item, which used for removing
	 *
	 * @see MatrixItem#remove()
	 */
	public void remove(MatrixItem item)
	{
		if (item != null)
		{
			item.remove();
		}
	}

	public final List<MatrixItem> find(final String what, final boolean caseSensitive, final boolean wholeWord)
	{
		return this.getRoot().stream()
				.filter(item -> item.matches(what, caseSensitive, wholeWord))
				.collect(Collectors.toList());
	}

	/**
	 * @return index of current executed item
	 */
	public int currentItem()
	{
		return this.count;
	}

	/**
	 * Return count of children, which has the passed result.
	 * If the root of the matrix is null, will return 0
	 * @return the count of children, which have the passed result
	 *
	 * @see MatrixItem#count(Result)
	 */
	public int countResult(Result what)
	{
		if (this.root != null)
		{
			return this.root.count(what);
		}
		return 0;
	}
	//endregion

	/**
	 * Check the matrix.
	 * @param error a builder for collect errors
	 * @return true, if matrix is ok
	 *
	 * @see MatrixItem#check(Context, AbstractEvaluator, IMatrixListener)
	 */
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
		Date startTime = new Date();
		this.matrixListener.matrixStarted(this);

		try
		{
			report.reportStarted(this.getMatrixBuffer(), VersionInfo.getVersion());
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

		try
		{
			report.reportFinished(this.countResult(Result.Failed), this.countResult(Result.Passed), startTime, new Date());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		this.matrixListener.matrixFinished(this, this.countResult(Result.Passed), this.countResult(Result.Failed));
	}

	//region private methods
	private char[] getMatrixBuffer() throws Exception
	{
		Parser parser = new Parser();
		StringWriter stringWriter = new StringWriter();
		parser.saveMatrix(this.root, stringWriter);
		return stringWriter.getBuffer().toString().toCharArray();
	}
	//endregion

}
