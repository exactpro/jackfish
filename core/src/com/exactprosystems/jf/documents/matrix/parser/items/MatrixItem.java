////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.IMatrixItem;
import com.exactprosystems.jf.api.common.Str;
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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class MatrixItem implements IMatrixItem, Mutable, Cloneable
{
	public MatrixItem()
	{
		this.parameters = new Parameters();
		this.id 		= new MutableValue<String>();
		this.off		= new MutableValue<Boolean>();
        this.repOff     = new MutableValue<Boolean>();
		this.global		= new MutableValue<Boolean>();
		this.ignoreErr	= new MutableValue<Boolean>();
		this.comments 	= new MutableArrayList<CommentString>();
		this.children 	= new MutableArrayList<MatrixItem>();
	}

	//==============================================================================================
	//implements Cloneable
	//==============================================================================================
	@SuppressWarnings("unchecked")
	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		try
		{
			MatrixItem clone = ((MatrixItem) super.clone());

			clone.number 	= 0;
			clone.id 		= this.id.clone();
			clone.off 		= this.off.clone();
            clone.repOff    = this.repOff.clone();
			clone.global 	= this.global.clone();
			clone.ignoreErr = this.ignoreErr.clone();

            clone.source    = this.source;
			clone.owner     = this.owner;
			clone.comments 	= (MutableArrayList<CommentString>) comments.clone();
			clone.parent 	= this.parent;
			clone.children 	= new MutableArrayList<MatrixItem>(children.size());
			for (MatrixItem child : children)
			{
				MatrixItem item = child.clone();
				item.parent = clone;
				clone.children.add(item);
			}
			clone.result 	= null;
			clone.parameters = this.parameters.clone();

			return clone;
		}
		catch (Exception e)
		{
			// this exception from ArrayList.clone()
			throw new InternalError();
		}
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

    public static MatrixItem createMatrixItem(String className) throws Exception
    {
        Class<?> clazz = Class.forName(MatrixItem.class.getPackage().getName() + "." +className);
        return (MatrixItem) clazz.newInstance();
    }

	//==============================================================================================
	// implements Displayed
	//==============================================================================================
	public final void display(DisplayDriver driver, Context context)
	{
		this.layout = displayYourself(driver, context);

		for (MatrixItem item : this.children)
		{
			item.display(driver, context);
		}
		MatrixItemAttribute attribute = this.getClass().getAnnotation(MatrixItemAttribute.class);
		if (attribute != null && attribute.hasChildren())
		{
			new End(this).display(driver, context);
		}
	}
	
	public final Object getLayout()
	{
		return this.layout;
	}

	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		return null;
	}
	//==============================================================================================

    public boolean canExecute()
    {
    	if (isTrue(this.off.get()))
    	{
    		return false;
    	}

    	if (this.parent == null)
    	{
    		return true;
    	}

    	return this.parent.canExecute();
    }

	//==============================================================================================
	// implements Mutable
	//==============================================================================================

    public boolean isChanged()
    {
    	if (	this.id.isChanged()
    		||	this.off.isChanged()
            ||  this.repOff.isChanged()
    		||	this.global.isChanged()
    		||	this.ignoreErr.isChanged()
    		||	this.comments.isChanged()
    		||	this.parameters.isChanged()
    		||	this.children.isChanged() )
    	{
    		return true;
    	}
		return false;
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

	//==============================================================================================
	// bypass
	//==============================================================================================
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

    public final void bypass(Visiter visiter)
    {
    	if (visiter != null)
    	{
    		visiter.visit(this);
    	}

		for (MatrixItem item : this.children)
		{
			item.bypass(visiter);
		}
    }

	//==============================================================================================
	// Getters / Setters
	//==============================================================================================
    public final Matrix getSource()
    {
        return this.source;
    }

	public final Matrix getMatrix()
	{
		return this.owner;
	}

	public final int getNumber()
	{
		return this.number;
	}

	public final String getId()
	{
		return this.id.get();
	}

	public final void setId(String id) 
	{
		this.id.set(id);
	}

	public final boolean isOff()
	{
		return isTrue(this.off.get());
	}

    public final boolean isRepOff()
    {
        return isTrue(this.repOff.get());
    }

	public final boolean isGlobal()
	{
		return isTrue(this.global.get());
	}

	public final boolean isIgnoreErr()
	{
		return isTrue(this.ignoreErr.get());
	}

	public final List<CommentString> getComments()
	{
		return this.comments;
	}

	public final MatrixItem getParent()
	{
		return this.parent;
	}

	public final ReturnAndResult getResult()
	{
		return this.result;
	}

	public Parameters getParameters()
	{
		return this.parameters;
	}

    public boolean isBreakPoint()
    {
        return this.breakPoint;
    }

	public void setNubmer(int number)
	{
		this.number = number;
	}

	public void setOff(boolean off)
	{
		this.off.set(off);
	}

    public void setRepOff(boolean off)
    {
        this.repOff.set(off);
    }

	public MatrixItemState getItemState()
	{
		return this.matrixItemState;
	}

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

    public void set(Tokens key, Object value)
    {
        switch (key)
        {
        case Id:
            this.id.set((String)value);
            break;
        case Off:
            this.off.set((Boolean)value);
            break;
        case RepOff:
            this.repOff.set((Boolean)value);
            break;
        case Global:
            this.global.set((Boolean)value);
            break;
        case IgnoreErr:
            this.ignoreErr.set((Boolean)value);
            break;
        default:
            break;
        }
    }

	public MatrixItemExecutingState getExecutingState()
	{
		return this.executingState;
	}

    //==========================================================================================================================
	// Public members
	//==========================================================================================================================
	public final void init(Matrix owner, Matrix source)
	{
		this.owner 			= owner;
		this.source         = source;
		for (MatrixItem child : this.children)
		{
			child.init(owner, source);
		}
	}

	public final void init(Matrix owner, List<String> comments,
			Map<Tokens, String> systemParameters, Parameters userParameters) throws MatrixException
	{
		MatrixItemAttribute annotation = this.getClass().getAnnotation(MatrixItemAttribute.class);
		boolean hasValue = annotation.hasValue();
		this.owner 			= owner;
        this.source         = owner;
		if (comments != null)
		{
			this.comments = new MutableArrayList<CommentString>();
			for (String comment : comments)
			{
				this.comments.add(new CommentString(comment));
			}
		}

		this.id.set(systemParameters.get(Tokens.Id));
		this.off.set(isTrue(hasValue, systemParameters, Tokens.Off));
        this.repOff.set(isTrue(hasValue, systemParameters, Tokens.RepOff));
		this.global.set(isTrue(hasValue, systemParameters, Tokens.Global));
		this.ignoreErr.set(isTrue(hasValue, systemParameters, Tokens.IgnoreErr));

		if (userParameters != null)
		{
			this.parameters = userParameters;
		}

		initItSelf(systemParameters);
	}
	
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

		this.id.set(newId);
	}

	public final String getPath()
	{
		if (this.parent != null)
		{
			return this.parent.getPath() + getItemName() + "/";
		}
		return getItemName() + "/";
	}

	public final void check(Context context, AbstractEvaluator evaluator, IMatrixListener checkListener)
	{
		checkItSelf(context, evaluator, checkListener, this.parameters);
	}

	public final ReturnAndResult execute(Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report)
	{
		long start = System.currentTimeMillis();

		this.result = null;

		if (isOff())
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
		if (isRepOff() && prev)
        {
            report.reportSwitch(false);
        }

		beforeReport(report);
		report.itemStarted(this);
		report.itemIntermediate(this);
		
		// TODO handling exceptions should be here
		
		this.result = executeItSelf(start, context, listener, evaluator, report, this.parameters);
		
		
		
		
		long duration = this.result.getTime();
		
		if (this.result.getResult() == Result.Failed && isIgnoreErr())
		{
			this.result = new ReturnAndResult(start, this.result.getError(), Result.Ignored);
		}
		
		report.itemFinished(this, duration, this.screenshot);
		listener.finished(this.owner, this, this.result.getResult());
		this.changeState(this.isBreakPoint() ? MatrixItemState.BreakPoint : MatrixItemState.None);
		
		afterReport(report);
        
		report.reportSwitch(prev);

		if (isRepOff() && prev)
        {
            report.reportSwitch(true);
        }
        

		return this.result;
	}

	public final void write(int level, CsvWriter writer) throws IOException
	{

		if(this.getClass().isAnnotationPresent(Deprecated.class))
		{
			return;
		}

        String indent = "";
		if (this instanceof Else)
		{
			level--;
		}
		for (int i = 0; i < level; i++)
		{
			indent += "    ";
		}

		if (this.comments != null)
		{
			for (CommentString comment : this.comments)
			{
				writer.writeRecord(new String[] { indent + Parser.commentPrefix + " " + comment }, true);
			}
		}

		List<String> firstLine = new ArrayList<String>();
		List<String> secondLine = new ArrayList<String>();

		if (!this.id.isNullOrEmpty())
		{
			addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Id.get(), this.id.get());
		}

		MatrixItemAttribute annotation = getClass().getAnnotation(MatrixItemAttribute.class);
		boolean hasValue = annotation.hasValue();
		writeBoolean(hasValue, firstLine, secondLine, this.off, Tokens.Off);
        writeBoolean(hasValue, firstLine, secondLine, this.repOff, Tokens.RepOff);
		writeBoolean(hasValue, firstLine, secondLine, this.ignoreErr, Tokens.IgnoreErr);
		writeBoolean(hasValue, firstLine, secondLine, this.global, Tokens.Global);
		writePrefixItSelf(writer, firstLine, secondLine);

		writeRecord(writer, firstLine, indent);
		writeRecord(writer, secondLine, indent);

		if (count() > 0)
		{
			for(int index = 0; index < this.count(); index++)
			{
				MatrixItem children = get(index);
				children.write(level + 1, writer);
			}

		}

		List<String> line = new ArrayList<String>();
		writeSuffixItSelf(writer, line, indent);
		if (line.size() > 0)
		{
			writeRecord(writer, line, indent);
		}
		writer.endRecord();
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
		if (this.getParent() instanceof MatrixRoot && getParent().get(0) == this)
		{
			this.comments.addAll(Arrays.asList(copyright.split(System.lineSeparator())).stream().map(CommentString::new).collect(Collectors.toList()));
		}
	}
	
	//----------------------------------------------------------------------------------------------
	// Work witch children
	//----------------------------------------------------------------------------------------------
	public final int count(Result result)
	{
		int count = 0;
		for(MatrixItem item : this.children)
		{
			if (!item.isRepOff() && item.result != null && item.result.getResult() == result)
			{
				count++;
			}
		}
		return count;
	}

	public final int count()
	{
		return this.children.size();
	}
	
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

	public final MatrixItem get(int index)
	{
		return this.children.get(index);
	}

	public final void insert(int index, MatrixItem item)
	{
		item.parent = this;
		item.owner  = this.owner;
        item.source = this.owner;
		this.children.add(index, item);
	}

	public final void remove()
	{
		if (this.parent != null)
		{
			int index = this.parent.index(this);
			parent.children.remove(index);
		}
		this.parent = null;
	}

    public final Optional<MatrixItem> find(Predicate<MatrixItem> predicate)
    {
        if (predicate.test(this))
        {
            return Optional.ofNullable(this);
        }
        for (MatrixItem item : this.children)
        {
            Optional<MatrixItem> found = item.find(predicate);
            if (found.isPresent())
            {
                return found;
            }
        }
        return Optional.ofNullable(null);
    }
	
    public final List<MatrixItem> findAll(Predicate<MatrixItem> predicate)
    {
        List<MatrixItem> list = new ArrayList<>();
        findAll(list, predicate);
        return list;
    }

	public final MatrixItem find(boolean everyWhere, Class<?> clazz, String id)
	{
		for (MatrixItem item : this.children)
		{
			if (item.isOff())
			{
				continue;
			}
			
			if (	(clazz != null && clazz == item.getClass() || clazz == null)
				&&	(id != null && id.equals(item.getId()) || id == null) )
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

				MatrixItem found = item.find(everyWhere, clazz, id);
				if (found != null)
				{
					return found;
				}
			}
		}

		return null;
	}

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

    public final void setBreakPoint(boolean breakPoint)
    {
        this.breakPoint = breakPoint;
		MatrixItemState oldState = getItemState();
		MatrixItemState newState;
		if (breakPoint)
		{
			newState = oldState == MatrixItemState.Executing ? MatrixItemState.ExecutingWithBreakPoint : MatrixItemState.BreakPoint;
		}
		else
		{
			newState = oldState == MatrixItemState.ExecutingWithBreakPoint ? MatrixItemState.Executing : MatrixItemState.None;
		}
		changeState(newState);
	}

	public final void changeState(MatrixItemState state)
	{
		this.matrixItemState = state;
	}

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
		for (String part : parts)
		{
			if (!matchesPart(part, caseSensitive, wholeWord))
			{
				return false;
			}
		}
		return true;
	}

	private final boolean matchesPart(String what, boolean caseSensitive, boolean wholeWord)
	{
		if (SearchHelper.matches(this.getClass().getSimpleName(), what, caseSensitive, wholeWord))
		{
			return true;
		}

		if (SearchHelper.matches(this.id.get(), what, caseSensitive, wholeWord))
		{
			return true;
		}

		for (CommentString s : this.comments)
		{
			if (SearchHelper.matches(String.valueOf(s), what, caseSensitive, wholeWord))
			{
				return true;
			}
		}

		return matchesDerived(what, caseSensitive, wholeWord);
	}

    //==========================================================================================================================
	// Protected members should be overridden
	//==========================================================================================================================
	public void processRawData(String[] str)
	{
	}

	@Override
	public String getItemName()
	{
		return (isGlobal() ? "^" : "") + getClass().getSimpleName();
	}

	public void addKnownParameters()
	{
	}

	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return false;
	}
	
	protected void initItSelf(Map<Tokens, String> systemParameters)
			throws MatrixException
	{
	}

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


	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		this.parameters.prepareAndCheck(evaluator, listener, this);

		for(MatrixItem item : this.children)
		{
			if (!item.isOff())
			{
				item.check(context, evaluator, listener);
			}
		}
	}

	protected void checkValidId(MutableValue<String> string, IMatrixListener listener)
	{
		String id = string.get();
		if (Str.IsNullOrEmpty(id))
		{
			return;
		}
		boolean matches = id.matches(VALID_IDENTIFIER_REGEXP);
		if (!matches)
		{
			listener.error(this.source, this.number, this, "Invalid identifier : " + id);
		}
	}

	protected void beforeReport(ReportBuilder report)
	{
	}

	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		return executeChildren(start, context, listener, evaluator, report, null);
	}

	protected void afterReport(ReportBuilder report)
	{
	}

	//==========================================================================================================================
	// Protected members for using
	//==========================================================================================================================
	public void correctParametersType()
	{
	}

	protected final ReturnAndResult executeChildren(long start, Context context,  IMatrixListener listener, AbstractEvaluator evaluator,
			ReportBuilder report, Class<?>[] executeUntilNot)
	{
		boolean wasError = false;
        boolean wasStepError = false;
		Object out = null;
		MatrixError error = null;
		//clear state
		this.changeState(MatrixItemState.ExecutingParent);
		for (MatrixItem item : this.children)
		{
			if (executeUntilNot != null)
			{
				if (Arrays.asList(executeUntilNot).contains(item.getClass()))
				{
					break;
				}
			}

			ReturnAndResult ret = item.execute(context, listener, evaluator, report);
			Result result = ret.getResult();
			out = ret.getOut();
			if (result == Result.Stopped || result == Result.Return || result == Result.Break)
			{
                if (wasStepError)
                {
                    return new ReturnAndResult(start, ret.getError(), Result.StepFailed);
                }
				if (wasError)
				{
					return new ReturnAndResult(start, ret.getError(), Result.Failed);
				}
				return new ReturnAndResult(start, result, out);
			}
			else if (result == Result.Continue)
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

			if (result == Result.Failed)
			{
				wasError = true;
				error = ret.getError();

				if (isTrue(item.ignoreErr.get()))
				{
					result = Result.Ignored;
					wasError = false;
				}
				else
				{
					break;
				}
			}
			else if (result == Result.StepFailed)
            {
                wasStepError = true;
                error = ret.getError();

                if (isTrue(item.ignoreErr.get()))
                {
                    result = Result.Ignored;
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

	protected Boolean isTrue(boolean hasValue, Map<Tokens, String> systemParameters, Tokens token)
	{
		if (hasValue)
		{
			String value = systemParameters.get(token);
			return !(value == null || value.isEmpty() || value.equals("0"));
		}
		return systemParameters.containsKey(token);
	}

	protected static final boolean isTrue(Boolean value)
	{
		return value != null && value.booleanValue();
	}

    protected void doShowPopup(boolean showPopups, Context context, String message, Notifier notifier) 
    {
        if (showPopups)
		{
        	String str = this.getMatrix().getNameProperty().get() + "\n" + this.getItemName() + "\n" + message; 
			context.getFactory().popup(str, notifier);
		}
	}

    protected final void doSreenshot(RowTable row, Object connection, ScreenshotKind screenshotKind, ScreenshotKind ... when) throws Exception
    {
        boolean isErrorStage = Arrays.stream(when).anyMatch(a -> ScreenshotKind.OnError == a);
        if (row != null && row.get(Context.screenshotColumn) != null && !isErrorStage)
        {
            return;
        }
        
        if (Arrays.stream(when).anyMatch(a -> screenshotKind == a))
        {
//            ImageWrapper screenshot = null;  
            
            if (connection instanceof AppConnection && !isErrorStage)
            {
                try
                {
                    screenshot = ((AppConnection)connection).getApplication().service().getImage(null, null);
                }
                catch (Exception e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
            
            if (screenshot == null)
            {
                Rectangle desktopRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                BufferedImage image = new java.awt.Robot().createScreenCapture(desktopRect);
                screenshot =  new ImageWrapper(image);
            }
            
            screenshot.setDescription("" + when[0]);
            if (row != null)
            {
                row.put(Context.screenshotColumn,    screenshot);
            }
        }
    }

    private void findAll(List<MatrixItem> list, Predicate<MatrixItem> predicate)
    {
        if (predicate.test(this))
        {
            list.add(this);
        }
        for (MatrixItem item : this.children)
        {
            item.findAll(list, predicate);
        }
    }


	protected static final Logger logger = Logger.getLogger(MatrixItem.class);

	//==============================================================================================
	// Private members
	//==============================================================================================
	// define state of this item
	private static final String VALID_IDENTIFIER_REGEXP = "^[a-zA-Z_$][a-zA-Z_$0-9]*$";

    protected MutableValue<String>            id;
    protected MutableValue<Boolean>           off;
    protected MutableValue<Boolean>           repOff;
    protected MutableValue<Boolean>           global;
    protected MutableValue<Boolean>           ignoreErr;
    protected MutableArrayList<CommentString> comments;
    protected Parameters                      parameters;
    protected MutableArrayList<MatrixItem>    children;

    // do not define state
    protected Object                          layout;
    protected Matrix                          source;
    protected Matrix                          owner;
    protected int                             number;
    protected MatrixItem                      parent;
    protected ReturnAndResult                 result;
    protected boolean                         breakPoint;
    protected MatrixItemState                 matrixItemState         = MatrixItemState.None;
	protected MatrixItemExecutingState        executingState  = MatrixItemExecutingState.None;
    protected ImageWrapper                    screenshot              = null;
}
