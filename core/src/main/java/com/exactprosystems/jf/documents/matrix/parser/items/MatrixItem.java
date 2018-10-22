/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.IMatrixItem;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.common.MatrixException;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.Notifier;
import com.exactprosystems.jf.functions.RowTable;
import org.apache.log4j.Logger;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class MatrixItem implements IMatrixItem, Mutable, Cloneable
{
	private static final String VALID_IDENTIFIER_REGEXP = "^[a-zA-Z_$][a-zA-Z_$0-9]*$";
	protected static final Logger logger = Logger.getLogger(MatrixItem.class);

	private BiConsumer<Integer, MatrixItem> onAddListener;
	private BiConsumer<Integer, MatrixItem> onRemoveListener;
	private BiConsumer<Integer, MatrixItem> onChangeParameter;
	private BiConsumer<Boolean, MatrixItem> onBreakPointListener;

	protected MutableValue<String>                   id;
	protected MutableValue<Boolean>                  off;
	protected MutableValue<Boolean>                  repOff;
	protected MutableValue<Boolean>                  global;
	protected MutableValue<Boolean>                  ignoreErr;
	protected MutableArrayList<MutableValue<String>> comments;
	protected Parameters                             parameters;
	protected MutableArrayList<MatrixItem>           children;
	protected Object                                 layout;
	protected Matrix                                 source;
	protected Matrix                                 owner;
	protected int                                    number;
	protected MatrixItem                             parent;
	protected ReturnAndResult                        result;
	protected boolean                                breakPoint;
	protected MatrixItemState          matrixItemState = MatrixItemState.None;
	protected MatrixItemExecutingState executingState  = MatrixItemExecutingState.None;
	protected ImageWrapper             screenshot      = null;

	public MatrixItem()
	{
		this.parameters = new Parameters();
		this.id = new MutableValue<>();
		this.off = new MutableValue<>();
		this.repOff = new MutableValue<>();
		this.global = new MutableValue<>();
		this.ignoreErr = new MutableValue<>();
		this.comments = new MutableArrayList<>();
		this.children = new MutableArrayList<>();
	}

	/**
	 * Final method for create copy of the current item.
	 * The copy item contains all copied children
	 */
	public final MatrixItem createCopy()
	{
		MatrixItem matrixItem = this.makeCopy();
		matrixItem.number = this.number;
		matrixItem.id = new MutableValue<>(this.id);
		matrixItem.off = new MutableValue<>(this.off);
		matrixItem.repOff = new MutableValue<>(this.repOff);
		matrixItem.global = new MutableValue<>(this.global);
		matrixItem.ignoreErr = new MutableValue<>(this.ignoreErr);

		matrixItem.source = this.source;
		matrixItem.owner = this.owner;
		matrixItem.comments = this.comments.stream()
				.map(MutableValue::new)
				.collect(Collectors.toCollection(MutableArrayList::new));
		matrixItem.parent = this.parent == null ? null : this.parent.makeCopy();
		matrixItem.children = new MutableArrayList<>(this.children.size());

		this.children.stream()
				.map(MatrixItem ::createCopy)
				.peek(copy -> copy.parent = matrixItem)
				.forEach(matrixItem.children::add);

		matrixItem.result = null;
		matrixItem.parameters = new Parameters(this.parameters);
		return matrixItem;
	}

	/**
	 * Abstract method for copying any items
	 * @return
	 */
	protected abstract MatrixItem makeCopy();

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName();
	}

	public static MatrixItem createMatrixItem(String className) throws Exception
	{
		Class<?> clazz = Class.forName(MatrixItem.class.getPackage().getName() + "." + className);
		return (MatrixItem) clazz.newInstance();
	}

	/**
	 * Display the item via {@link DisplayDriver} ( and all children recursively)
	 *
	 * @param driver the instance of DisplayDriver
	 * @param context a matrix context
	 *
	 * @see DisplayDriver
	 */
	public final void display(DisplayDriver driver, Context context)
	{
		this.layout = this.displayYourself(driver, context);

		this.children.forEach(item -> item.display(driver, context));

		MatrixItemAttribute attribute = this.getClass().getAnnotation(MatrixItemAttribute.class);
		if (attribute != null && attribute.hasChildren())
		{
			new End(this).display(driver, context);
		}
	}

	/**
	 * @return the created layout for the item.
	 * @see MatrixItem#displayYourself(DisplayDriver, Context)
	 */
	public final Object getLayout()
	{
		return this.layout;
	}

	/**
	 * Create a layout for the item.
	 * @param driver a instance of DisplayDriver
	 * @param context a matrix context
	 * @return a layout for the item.
	 *
	 * @see DisplayDriver
	 * @see DisplayDriver#createLayout(MatrixItem, int)
	 */
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		return null;
	}

	//region implements Mutable
	public boolean isChanged()
	{
		return this.id.isChanged()
				|| this.off.isChanged()
				|| this.repOff.isChanged()
				|| this.global.isChanged()
				|| this.ignoreErr.isChanged()
				|| this.comments.isChanged()
				|| this.parameters.isChanged()
				|| this.children.isChanged();
	}

	@Override
	public void saved()
	{
		this.id.saved();
		this.off.saved();
		this.repOff.saved();
		this.global.saved();
		this.ignoreErr.saved();
		this.comments.saved();
		this.parameters.saved();
		this.children.saved();
	}

	//endregion

	//region bypass

	/**
	 * Bypass through all children and apply the passed LevelVisiter for each child item for the item
	 */
	public final void bypass(int startLevel, LevelVisiter visiter)
	{
		if (visiter != null)
		{
			visiter.visit(startLevel, this);
		}

		for (MatrixItem item : this.children)
		{
			item.bypass(startLevel + 1, visiter);
		}
	}

	/**
	 * Bypass through all children and apply the passed Visiter for each child item for the item
	 *
	 * @see Visiter
	 */
	public final void bypass(Visiter visiter)
	{
		if (visiter != null)
		{
			visiter.visit(this);
		}
		this.children.forEach(item -> item.bypass(visiter));
	}

	/**
	 * @return {@link Stream}, contains all descendants for the item
	 */
	public final Stream<MatrixItem> stream()
	{
		List<MatrixItem> list = new ArrayList<>();
		this.bypass(list::add);
		return list.stream();
	}

	//endregion

	//region public Getters / Setters
	/**
	 * Check, can the item be executed.
	 * @return true, if item can be executed. And false otherwise
	 */
	public boolean canExecute()
	{
		if (isTrue(this.off.get()))
		{
			return false;
		}

		return this.parent == null || this.parent.canExecute();
	}

	/**
	 * @return the source matrix for the item.
	 * For a item from a library it will the library
	 *
	 * @see Matrix
	 * @see MatrixItem#init(Matrix, Matrix)
	 */
	public final Matrix getSource()
	{
		return this.source;
	}

	/**
	 * @return the matrix for the item
	 *
	 * @see Matrix
	 * @see MatrixItem#init(Matrix, Matrix)
	 */
	public final Matrix getMatrix()
	{
		return this.owner;
	}

	/**
	 * @return number of the item on matrix tree
	 */
	public final int getNumber()
	{
		return this.number;
	}

	/**
	 * @return id of the item
	 */
	public final String getId()
	{
		return this.id.get();
	}

	public final void setId(String id)
	{
		this.id.accept(id);
	}

	/**
	 * @return true, if item is off (it mean, that the item can't be executed). And false otherwise
	 *
	 * @see MatrixItem#canExecute()
	 */
	public final boolean isOff()
	{
		return isTrue(this.off.get());
	}

	/**
	 * @return true, if item is report off ( it mean, that item will not be in a report). And false otherwise
	 *
	 * @see ReportBuilder
	 */
	public final boolean isRepOff()
	{
		return isTrue(this.repOff.get());
	}

	/**
	 * Return the value of attribute isGlobal.
	 * This attribute mean, that result of the item can be used from another scopes (e.g. another TestCase, Step and etc)
	 * @return true, if the item has attribute Global. And false otherwise
	 */
	public final boolean isGlobal()
	{
		return isTrue(this.global.get());
	}

	/**
	 * Return the value of attribute isIgnoreErr.
	 * If this attribute set is true and the item was failed, the item will has result {@link Result#Ignored}
	 * @return true, if the item has attribute isIgnoreErr. And false otherwise
	 */
	public final boolean isIgnoreErr()
	{
		return isTrue(this.ignoreErr.get());
	}

	/**
	 * @return the comments ( list of strings) for the item
	 */
	public final List<MutableValue<String>> getComments()
	{
		return this.comments;
	}

	/**
	 * @return a parent item for the item. If parent is null, will return null
	 */
	public final MatrixItem getParent()
	{
		return this.parent;
	}

	/**
	 * @return the result of execution the item
	 */
	public final ReturnAndResult getResult()
	{
		return this.result;
	}

	/**
	 * @return the parameters for the item
	 */
	public Parameters getParameters()
	{
		return this.parameters;
	}

	/**
	 * If a item is on breakpoint state, a matrix will paused on the item and wait, while user resume or stop the matrix
	 * @return the item is on breakpoint state or not
	 */
	public boolean isBreakPoint()
	{
		return this.breakPoint;
	}

	/**
	 * Change the number of the item
	 * @param number a new number for the item
	 */
	public void setNumber(int number)
	{
		this.number = number;
	}

	/**
	 * Change the attribute off
	 * @param off is new value of attribute off
	 *
	 * @see MatrixItem#isOff()
	 */
	public void setOff(boolean off)
	{
		this.off.accept(off);
	}

	/**
	 * Change the attribute report off.
	 *
	 * @param off the new value for the attribute report off
	 *
	 * @see MatrixItem#isRepOff()
	 */
	public void setRepOff(boolean off)
	{
		this.repOff.accept(off);
	}

	/**
	 * @return the current state of the item
	 *
	 * @see MatrixItemState
	 */
	public MatrixItemState getItemState()
	{
		return this.matrixItemState;
	}

	/**
	 * @return Get a value by the passed token.
	 * If the item can get a value by the passed token, will return null.
	 */
	public Object get(Tokens key)
	{
		switch (key)
		{
			case Id:
				return this.id.get();
			case Off:
				return this.off.get();
			case RepOff:
				return this.repOff.get();
			case Global:
				return this.global.get();
			case IgnoreErr:
				return this.ignoreErr.get();
			default:
				return null;
		}
	}

	/**
	 * Set to the item passed value by the passed token.
	 * If the item hasn't the passed token, nothing will happens
	 */
	public void set(Tokens key, Object value)
	{
		switch (key)
		{
			case Id:
				this.id.accept((String) value);
				break;
			case Off:
				this.off.accept((Boolean) value);
				break;
			case RepOff:
				this.repOff.accept((Boolean) value);
				break;
			case Global:
				this.global.accept((Boolean) value);
				break;
			case IgnoreErr:
				this.ignoreErr.accept((Boolean) value);
				break;
			default:
				break;
		}
	}

	/**
	 * Return the executing state for the item. It's used only for {@link TestCase} and {@link Step}.
	 * This executing state need for display on GUI
	 * @return the executing state for the item
	 */
	public MatrixItemExecutingState getExecutingState()
	{
		return this.executingState;
	}

	//endregion

	//region Public members

	/**
	 * Init the owner and the source for all descendants for the item
	 * @param owner the new owner
	 * @param source the new source
	 */
	public final void init(Matrix owner, Matrix source)
	{
		this.owner = owner;
		this.source = source;
		this.children.forEach(child -> child.init(owner, source));
	}

	/**
	 * Initialize the item passed parameters
	 */
	public final void init(Matrix owner, List<String> comments, Map<Tokens, String> systemParameters, Parameters userParameters) throws MatrixException
	{
		MatrixItemAttribute annotation = this.getClass().getAnnotation(MatrixItemAttribute.class);
		boolean hasValue = annotation.hasValue();
		this.owner = owner;
		this.source = owner;
		if (comments != null)
		{
			this.comments = comments.stream()
					.map(MutableValue::new)
					.collect(Collectors.toCollection(MutableArrayList::new));
		}

		this.id.accept(systemParameters.get(Tokens.Id));
		this.off.accept(this.isTrue(hasValue, systemParameters, Tokens.Off));
		this.repOff.accept(this.isTrue(hasValue, systemParameters, Tokens.RepOff));
		this.global.accept(this.isTrue(hasValue, systemParameters, Tokens.Global));
		this.ignoreErr.accept(this.isTrue(hasValue, systemParameters, Tokens.IgnoreErr));

		if (userParameters != null)
		{
			this.parameters = userParameters;
		}

		this.initItSelf(systemParameters);
	}

	/**
	 * Create a new unique id for the item by the {@link MatrixItem#itemSuffixSelf()} and set the id to the item.
	 * If the created id is null or empty, nothing happens
	 */
	public final void createId()
	{
		String suffix = this.itemSuffixSelf();
		if (Str.IsNullOrEmpty(suffix))
		{
			return;
		}

		String newId;
		int count = 1;
		while (true)
		{
			newId = suffix + count;

			if (this.owner.getRoot().find(true, null, newId) == null)
			{
				break;
			}
			count++;
		}

		this.id.accept(newId);
	}

	/**
	 * @return string representation of the path for the item from {@link MatrixRoot}
	 * This path contains all parents, separated by {@code /}. All items
	 */
	public final String getPath()
	{
		if (this.parent != null)
		{
			return this.parent.getPath() + this.getItemName() + "/";
		}
		return this.getItemName() + "/";
	}

	/**
	 * Check the item parameters. Is someone of parameter is invalid, the item will notify error to the passed listener
	 * @param context a matrix context
	 * @param evaluator a evaluator
	 * @param checkListener the listener for notify errors
	 */
	public final void check(Context context, AbstractEvaluator evaluator, IMatrixListener checkListener)
	{
		this.checkItSelf(context, evaluator, checkListener, this.parameters);
	}

	/**
	 * Execute the current item.
	 * If item is off, will create and return {@link ReturnAndResult} with result {@link Result#Off}
	 * @param context the matrix context
	 * @param listener the matrix listener for notify events
	 * @param evaluator the evaluator
	 * @param report the report for reporting execution state of the item
	 * @return result of executing the item
	 *
	 * @see ReturnAndResult
	 */
	public final ReturnAndResult execute(Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report)
	{
		long start = System.currentTimeMillis();

		this.result = null;

		if (this.isOff())
		{
			return new ReturnAndResult(start, Result.Off);
		}

		this.changeState(this.isBreakPoint() ? MatrixItemState.ExecutingWithBreakPoint : MatrixItemState.Executing);
		listener.started(this.owner, this);

		if (context.checkMonitor(listener, this))
		{
			this.changeState(this.isBreakPoint() ? MatrixItemState.BreakPoint : MatrixItemState.None);
			return new ReturnAndResult(start, Result.Stopped);
		}

		boolean prev = report.reportIsOn();
		if (this.isRepOff() && prev)
		{
			report.reportSwitch(false);
		}

		this.beforeReport(report, context);
		report.itemStarted(this);
		report.itemIntermediate(this);

		// TODO handling exceptions should be here

		this.result = this.executeItSelf(start, context, listener, evaluator, report, this.parameters);

		long duration = this.result.getTime();

		if (this.result.getResult() == Result.Failed && this.isIgnoreErr())
		{
			this.result = new ReturnAndResult(start, this.result.getError(), Result.Ignored);
		}

		report.itemFinished(this, duration, this.screenshot);
		listener.finished(this.owner, this, this.result.getResult());
		this.changeState(this.isBreakPoint() ? MatrixItemState.BreakPoint : MatrixItemState.None);

		this.afterReport(report);

		report.reportSwitch(prev);

		if (this.isRepOff() && prev)
		{
			report.reportSwitch(true);
		}

		return this.result;
	}

	/**
	 * Write the item ( and all descendants) via {@link CsvWriter}
	 *
	 * @param level level, which used for insert the dot before item. Count of dot equals {@code 4*level}
	 * @param writer the instance of CsvWriter
	 *
	 * @throws IOException if file not found
	 */
	public final void write(int level, CsvWriter writer) throws IOException
	{
		if (this.getClass().isAnnotationPresent(Deprecated.class))
		{
			return;
		}

		StringBuilder indent = new StringBuilder();
		if (this instanceof Else)
		{
			level--;
		}

		IntStream.range(0, level).forEach(i -> indent.append("    "));

		if (this.comments != null)
		{
			for (MutableValue comment : this.comments)
			{
				writer.writeRecord(new String[]{indent.toString() + Parser.commentPrefix + " " + comment}, true);
			}
		}

		List<String> firstLine = new ArrayList<>();
		List<String> secondLine = new ArrayList<>();

		if (!this.id.isNullOrEmpty())
		{
			this.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Id.get(), this.id.get());
		}

		MatrixItemAttribute annotation = this.getClass().getAnnotation(MatrixItemAttribute.class);
		boolean hasValue = annotation.hasValue();
		this.writeBoolean(hasValue, firstLine, secondLine, this.off, Tokens.Off);
		this.writeBoolean(hasValue, firstLine, secondLine, this.repOff, Tokens.RepOff);
		this.writeBoolean(hasValue, firstLine, secondLine, this.ignoreErr, Tokens.IgnoreErr);
		this.writeBoolean(hasValue, firstLine, secondLine, this.global, Tokens.Global);
		this.writePrefixItSelf(writer, firstLine, secondLine);

		this.writeRecord(writer, firstLine, indent.toString());
		this.writeRecord(writer, secondLine, indent.toString());

		for (int index = 0; index < this.count(); index++)
		{
			this.get(index).write(level + 1, writer);
		}

		List<String> line = new ArrayList<>();
		this.writeSuffixItSelf(writer, line, indent.toString());
		if (!line.isEmpty())
		{
			this.writeRecord(writer, line, indent.toString());
		}
		writer.endRecord();
	}

	public List<String> listOfTopIds(Class<? extends MatrixItem> foundClazz, List<Class<? extends MatrixItem>> owners)
	{
		return this.listOfTopItems(owners)
				.stream()
				.filter(item -> item.getClass() == foundClazz)
				.map(MatrixItem::getId)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	public List<MatrixItem> listOfTopItems(List<Class<? extends MatrixItem>> owners)
	{
		MatrixItem thisItem = this;
		List<Class<? extends MatrixItem>> stopClasses = owners == null || owners.isEmpty() ? Collections.singletonList(MatrixRoot.class) : owners;
		List<MatrixItem> allTopItems = new ArrayList<>();
		while (thisItem != null && !stopClasses.contains(thisItem.getClass()))
		{
			allTopItems.addAll(thisItem.topDescendants(thisItem != this));
			thisItem = thisItem.getParent();
		}
		return allTopItems;
	}

	//endregion

	//region Work with children

	/**
	 * @return count of children, which has passed result
	 */
	public final int count(Result result)
	{
		return (int) this.children.stream()
				.filter(item -> !item.isRepOff() && item.result != null && item.result.getResult() == result)
				.count();
	}

	/**
	 * @return count of children for the item
	 */
	public final int count()
	{
		return this.children.size();
	}

	/**
	 * @return true, if the item has passed item on the descendants
	 */
	public boolean contains(MatrixItem item)
	{
		if (item == null)
		{
			return false;
		}
		if (item == this)
		{
			return true;
		}
		for (MatrixItem child : this.children)
		{
			if (child.contains(item))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * @return index from the children for the passed item. If the passed item not found, will return size of children
	 */
	public int index(MatrixItem what)
	{
		int index = 0;
		for (MatrixItem item : this.children)
		{
			if (item == what)
			{
				return index;
			}
			index++;
		}
		return this.children.size();
	}

	/**
	 * Get child by the passed index
	 */
	public final MatrixItem get(int index)
	{
		return this.children.get(index);
	}

	/**
	 * Insert the item into passed index on the children for current item
	 */
	public final void insert(int index, MatrixItem item)
	{
		item.parent = this;
		item.owner = this.owner;
		item.source = this.owner;
		this.children.add(index, item);
		this.callAddListener(item, index);
	}

	/**
	 * Remove the current item from the parent
	 */
	public final void remove()
	{
		if (this.parent != null)
		{
			int index = this.parent.index(this);
			parent.children.remove(index);
			this.callRemoveListener(this, index);
		}
		this.parent = null;
	}

	/**
	 * Find the item from descendants by specified predicate
	 */
	public final Optional<MatrixItem> find(Predicate<MatrixItem> predicate)
	{
		if (predicate.test(this))
		{
			return Optional.of(this);
		}
		for (MatrixItem item : this.children)
		{
			Optional<MatrixItem> found = item.find(predicate);
			if (found.isPresent())
			{
				return found;
			}
		}
		return Optional.empty();
	}

	/**
	 * Find all descendants, which matches by passed predicate, from the item
	 */
	public final List<MatrixItem> findAll(Predicate<MatrixItem> predicate)
	{
		List<MatrixItem> list = new ArrayList<>();
		this.findAll(list, predicate);
		return list;
	}

	/**
	 * Find a item from children of the current item.
	 * If parameter everyWhere is true, a search will into descendants too
	 * @param everyWhere if true, add descendants of the current item. Otherwise search will only on children
	 * @param clazz the class of item for found
	 * @param id the id of item for found
	 * @return a item or null, if item not found
	 */
	public final MatrixItem find(boolean everyWhere, Class<?> clazz, String id)
	{
		for (MatrixItem item : this.children)
		{
			if (item.isOff())
			{
				continue;
			}

			if ((clazz != null && clazz == item.getClass() || clazz == null) && (id != null && id.equals(item.getId()) || id == null))
			{
				return item;
			}
		}

		if (everyWhere)
		{
			for (MatrixItem item : this.children)
			{
				if (item.isOff())
				{
					continue;
				}

				MatrixItem found = item.find(true, clazz, id);
				if (found != null)
				{
					return found;
				}
			}
		}

		return null;
	}

	/**
	 * Find parent for the current item by passed class. If the current item class equals the passed class, will return the current item.
	 * @param clazz class for searching
	 * @return item or {@link MatrixRoot} instance, if parent by passed class not found
	 */
	public final MatrixItem findParent(Class<?> clazz)
	{
		if (this.getClass() == clazz)
		{
			return this;
		}

		if (this.parent != null)
		{
			if (this.parent.getClass() == clazz)
			{
				return parent;
			}
			else
			{
				return this.parent.findParent(clazz);
			}
		}

		return this;
	}

	//endregion

	/**
	 * Set the breakpoint value and change state for the current item
	 * @param breakPoint the new breakpoint value
	 */
	public final void setBreakPoint(boolean breakPoint)
	{
		this.breakPoint = breakPoint;
		this.callBreakPointListener(this, breakPoint);
		MatrixItemState oldState = this.getItemState();
		MatrixItemState newState;
		if (breakPoint)
		{
			newState = oldState == MatrixItemState.Executing ? MatrixItemState.ExecutingWithBreakPoint : MatrixItemState.BreakPoint;
		}
		else
		{
			newState = oldState == MatrixItemState.ExecutingWithBreakPoint ? MatrixItemState.Executing : MatrixItemState.None;
		}
		this.changeState(newState);
	}

	/**
	 * Change state of current item
	 * @param state the new state
	 */
	public final void changeState(MatrixItemState state)
	{
		this.matrixItemState = state;
	}

	/**
	 * Change executing state of current item
	 * @param state the new state
	 */
	public final void changeExecutingState(MatrixItemExecutingState state)
	{
		this.executingState = state;
	}

	public final boolean matches(String what, boolean caseSensitive, boolean wholeWord)
	{
		if (Str.IsNullOrEmpty(what))
		{
			return false;
		}

		String[] parts = what.trim().split(" ");
		return Arrays.stream(parts)
				.allMatch(part -> this.matchesPart(part, caseSensitive, wholeWord));
	}

	//region listeners
	/**
	 * Set listener for breakpoint. This listener will call, when breakpoint value will changed
	 */
	public final void setOnBreakPoint(BiConsumer<Boolean, MatrixItem> breakPointListener)
	{
		this.owner.getRoot().onBreakPointListener = breakPointListener;
	}

	/**
	 * Set listener for add item. This listener will call, when a item will added
	 */
	public final void setOnAddListener(BiConsumer<Integer, MatrixItem> addListener)
	{
		this.owner.getRoot().onAddListener = addListener;
	}

	/**
	 * Set listener for remove. This listener will call, when a item will removed
	 */
	public final void setOnRemoveListener(BiConsumer<Integer, MatrixItem> removeListener)
	{
		this.owner.getRoot().onRemoveListener = removeListener;
	}

	/**
	 * Set listener for change parameters. This listener will call, when the parameters of current item will changed
	 */
	public final void setOnChangeParameter(BiConsumer<Integer, MatrixItem> changeParameter)
	{
		this.owner.getRoot().onChangeParameter = changeParameter;
	}

	/**
	 * Force call change parameters listener
	 */
	public final void parametersFire(int index)
	{
		this.callChangeParametersListener(this, index);
	}

	/**
	 * Force call add listener
	 */
	public final void fire()
	{
		this.callAddListener(this.owner.getRoot(), 0);
	}

	//endregion

	//region Protected members should be overridden

	/**
	 * Process raw data. It's method should be overridden, if the item has attribute raw
	 *
	 * @see MatrixItemAttribute#raw()
	 */
	public void processRawData(String[] str)
	{
	}

	/**
	 * @return name of the item. If the item has attribute isGlobal, for name will added symbol {@code ^}
	 */
	@Override
	public String getItemName()
	{
		return (this.isGlobal() ? "^" : "") + this.getClass().getSimpleName();
	}

	/**
	 * Add all parameters, which are known the item
	 */
	public void addKnownParameters()
	{
	}

	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return false;
	}

	/**
	 * Init the item the passed parameters
	 */
	protected void initItSelf(Map<Tokens, String> systemParameters) throws MatrixException
	{
	}

	/**
	 * @return for suffix for creating a id for the item
	 */
	protected String itemSuffixSelf()
	{
		return null;
	}

	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
	}

	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
	}

	/**
	 * Check the item. If item can't be compiled, the lister will notify error
	 * By default checked the item parameters and all children
	 * <p>
	 * Items, which are off not checked
	 * @param context a matrix context
	 * @param evaluator the evaluator for compile
	 * @param listener the listener for notify
	 * @param parameters the parameter for the item
	 */
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		this.parameters.prepareAndCheck(evaluator, listener, this);

		this.children.stream()
				.filter(item -> !item.isOff())
				.forEach(item -> item.check(context, evaluator, listener));
	}

	/**
	 * Check, that passed id is valid identifier. If is not valid, will notify error to listener
	 * @see MatrixItem#VALID_IDENTIFIER_REGEXP
	 *
	 * @param string checked value
	 * @param listener instance of listener
	 */
	protected void checkValidId(MutableValue<String> string, IMatrixListener listener)
	{
		String checkedId = string.get();
		if (Str.IsNullOrEmpty(checkedId))
		{
			return;
		}
		if (!checkedId.matches(VALID_IDENTIFIER_REGEXP))
		{
			listener.error(this.source, this.number, this, "Invalid identifier : " + checkedId);
		}
	}

	/**
	 * Write something before executing the item
	 * @param report instance of ReportBuilder
	 */
	protected void beforeReport(ReportBuilder report, Context context)
	{
	}

	/**
	 * Default implementation for execute itself. By default execute children of the item
	 * @param start the start time of the item.
	 * @param context the matrix context
	 * @param listener the matrix listener
	 * @param evaluator the matrix evaluator
	 * @param report the report
	 * @param parameters parameters for the item
	 * @return result of executing
	 *
	 * @see ReturnAndResult
	 */
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		return this.executeChildren(start, context, listener, evaluator, report, null);
	}

	/**
	 * Write something after execute the item
	 * @param report instance of ReportBuilder
	 */
	protected void afterReport(ReportBuilder report)
	{
	}

	//endregion

	//region Protected members for using
	public void correctParametersType()
	{
	}

	/**
	 * Execute the all children
	 */
	protected final ReturnAndResult executeChildren(long start, Context context,  IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Class<?>[] executeUntilNot)
	{
		boolean wasError = false;
        boolean wasStepError = false;
		Object out = null;
		MatrixError error = null;
		//clear state
		this.changeState(MatrixItemState.ExecutingParent);
		for (MatrixItem item : this.children)
		{
			if (executeUntilNot != null && Arrays.asList(executeUntilNot).contains(item.getClass()))
			{
				break;
			}

			ReturnAndResult ret = item.execute(context, listener, evaluator, report);
			Result itemResult = ret.getResult();
			out = ret.getOut();
			if (itemResult == Result.Stopped || itemResult == Result.Return || itemResult == Result.Break)
			{
				if (wasStepError)
				{
					return new ReturnAndResult(start, ret.getError(), Result.StepFailed);
				}
				if (wasError)
				{
					return new ReturnAndResult(start, ret.getError(), Result.Failed);
				}
				return new ReturnAndResult(start, itemResult, out);
			}
			else if (itemResult == Result.Continue)
			{
				if (wasStepError)
				{
					return new ReturnAndResult(start, ret.getError(), Result.StepFailed);
				}
				if (wasError)
				{
					return new ReturnAndResult(start, ret.getError(), Result.Failed);
				}
				return new ReturnAndResult(start, Result.Continue, out);
			}

			if (itemResult == Result.Failed)
			{
				wasError = true;
				error = ret.getError();

				if (isTrue(item.ignoreErr.get()))
				{
					itemResult = Result.Ignored;
					wasError = false;
				}
				else
				{
					break;
				}
			}
			else if (itemResult == Result.StepFailed)
			{
				wasStepError = true;
				error = ret.getError();

				if (isTrue(item.ignoreErr.get()))
				{
					itemResult = Result.Ignored;
					wasStepError = false;
				}
			}

		}
		//restore state for current item ( parent for executing)
		this.changeState(MatrixItemState.Executing);

		if (wasStepError)
		{
			return new ReturnAndResult(start, error, Result.StepFailed);
		}
		if (wasError)
		{
			return new ReturnAndResult(start, error, Result.Failed);
		}
		else
		{
			return new ReturnAndResult(start, Result.Passed, out);
		}
	}

	protected final void addParameter(List<String> firstLine, List<String> secondLine, TypeMandatory type, String parameter, String value)
	{
		String prefix = type.getPrefix();
		firstLine.add(prefix + parameter);
		if (value == null)
		{
			secondLine.add("");
		}
		else
		{
			secondLine.add("" + value.replaceAll(String.valueOf(Configuration.matrixDelimiter), Configuration.unicodeDelimiter));
		}
	}

	protected final void addParameter(List<String> firstLine, TypeMandatory type, String parameter)
	{
		String prefix = type.getPrefix();
		firstLine.add(prefix + parameter);
	}

	/**
	 * Show a popup notification
	 * @param showPopups is true, the popup will displayed
	 * @param context a matrix context
	 * @param message the message, which will appear on the popup notification
	 * @param notifier the kind of notifier
	 */
	protected void doShowPopup(boolean showPopups, Context context, String message, Notifier notifier)
	{
		if (showPopups)
		{
			String str = this.getMatrix().getNameProperty().get() + "\n" + this.getItemName() + "\n" + message;
			context.getFactory().popup(str, notifier);
		}
	}

	/**
	 * Create the screenshot depends on screenshot kinds.
	 * If the parameter connection instanceof {@link AppConnection}, the screenshot will from the connection.
	 * Otherwise the screenshot will taken from the desktop.
	 *
	 * @param row the row, which will used for save screenshot
	 * @param connection the object, which will used for get screenshot ( only if this object instance of {@link AppConnection})
	 * @param screenshotKind kinds of screenshot.
	 * @param when place
	 *
	 * @throws Exception if can't create the {@link java.awt.Robot}
	 * @see com.exactprosystems.jf.api.app.IRemoteApplication#getImage(Locator, Locator)
	 */
	protected final void doScreenshot(RowTable row, Object connection, ScreenshotKind screenshotKind, ScreenshotKind... when) throws Exception
	{
		boolean isErrorStage = Arrays.stream(when).anyMatch(a -> ScreenshotKind.OnError == a);
		if (row != null && row.get(Context.screenshotColumn) != null && !isErrorStage)
		{
			return;
		}

		if (Arrays.stream(when).anyMatch(a -> screenshotKind == a))
		{
			if (connection instanceof AppConnection && !isErrorStage)
			{
				try
				{
					this.screenshot = ((AppConnection) connection).getApplication().service().getImage(null, null);
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
				}
			}

			if (this.screenshot == null)
			{
				Rectangle desktopRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
				BufferedImage image = new java.awt.Robot().createScreenCapture(desktopRect);
				this.screenshot = new ImageWrapper(image);
			}

			this.screenshot.setDescription("" + when[0]);
			if (row != null)
			{
				row.put(Context.screenshotColumn, this.screenshot);
			}
		}
	}

	/**
	 * Create the failed ReturnAndResult and return it
	 */
	protected final ReturnAndResult createReturn(String msg, IMatrixListener listener, long start)
	{
		logger.error(msg);
		listener.error(this.owner, this.getNumber(), this, msg);
		return new ReturnAndResult(start, Result.Failed, msg, ErrorKind.EXCEPTION, this);
	}

	//endregion

	//region private methods
	private void findAll(List<MatrixItem> list, Predicate<MatrixItem> predicate)
	{
		if (predicate.test(this))
		{
			list.add(this);
		}
		this.children.forEach(item -> item.findAll(list, predicate));
	}

	private void callAddListener(MatrixItem item, Integer index)
	{
		if (this.parent != null)
		{
			this.parent.callAddListener(item, index);
		}
		Optional.ofNullable(this.onAddListener).ifPresent(addListener -> addListener.accept(index, item));
	}

	private void callChangeParametersListener(MatrixItem item, Integer index)
	{
		if (this.parent != null)
		{
			this.parent.callChangeParametersListener(item, index);
		}
		Optional.ofNullable(this.onChangeParameter).ifPresent(chaneParameterListener -> chaneParameterListener.accept(index, item));
	}

	private void callRemoveListener(MatrixItem item, Integer index)
	{
		if (this.parent != null)
		{
			this.parent.callRemoveListener(item, index);
		}
		Optional.ofNullable(this.onRemoveListener).ifPresent(removeListener -> removeListener.accept(index, item));
	}

	private void callBreakPointListener(MatrixItem item, Boolean newValue)
	{
		if (this.parent != null)
		{
			this.parent.callBreakPointListener(item, newValue);
		}
		Optional.ofNullable(this.onBreakPointListener).ifPresent(breakPointListener -> breakPointListener.accept(newValue, item));
	}

	private List<MatrixItem> topDescendants(boolean include)
	{
		int diff = include ? 1 : 0;
		return Optional.ofNullable(this.parent)
				.map(par ->
						IntStream.range(0, par.index(this) + diff)
								.mapToObj(par::get)
								.collect(Collectors.toList())
				)
				.orElseGet(Collections::emptyList);
	}

	private void writeBoolean(boolean hasValue, List<String> firstLine, List<String> secondLine, MutableValue<Boolean> field, Tokens token)
	{
		if (isTrue(field.get()))
		{
			if (hasValue)
			{
				addParameter(firstLine, secondLine, TypeMandatory.System, token.get(), "1");
			}
			else
			{
				addParameter(firstLine, TypeMandatory.System, token.get());
			}
		}
	}

	private void writeRecord(CsvWriter writer, List<String> line, String indent) throws IOException
	{
		String[] arr = Converter.convertArray(String.class, line.toArray());
		if (arr != null && arr.length > 0)
		{
			arr[0] = indent + arr[0];
		}

		writer.writeRecord(arr, true);
	}

	public void addCopyright(String copyright)
	{
		if (this.parent instanceof MatrixRoot && parent.get(0) == this)
		{
			this.comments.addAll(Arrays.stream(copyright.split(System.lineSeparator())).map(MutableValue::new).collect(Collectors.toList()));
		}
	}

	private boolean matchesPart(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(this.getClass().getSimpleName(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.id.get(), what, caseSensitive, wholeWord)
				|| this.comments.stream().anyMatch(s -> SearchHelper.matches(s.get(), what, caseSensitive, wholeWord))
				|| this.matchesDerived(what, caseSensitive, wholeWord);
	}

	private Boolean isTrue(boolean hasValue, Map<Tokens, String> systemParameters, Tokens token)
	{
		if (hasValue)
		{
			String value = systemParameters.get(token);
			return !(value == null || value.isEmpty() || value.equals("0"));
		}
		return systemParameters.containsKey(token);
	}

	private static boolean isTrue(Boolean value)
	{
		return value != null && value;
	}

	//endregion
}
