////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.config.HandlerKind;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.Notifier;
import com.exactprosystems.jf.functions.RowTable;
import com.exactprosystems.jf.functions.Table;

import java.util.List;
import java.util.Map;

@MatrixItemAttribute(
		description 	= "The main operator in matrix directory. It is used to logically divide matrix in steps." +
							" This is the high level operator, can't be put in any other actions or operators including TestCase. \n" +
							"All TestCase are performed one by one, independently of the performed results in previous testcases." +
							" Nevertheless, the dependence can be set with the field Depends.  If TestCase from the parameter Depends is failed,the dependend one will be failed too.\n" +
							"Each TestCase also contains Screenshot parameter which is used to specify when in the TestCase given a screenshot is made.  \n" +
							"TestCase ID is used to shortly identify and link to it in the parameter BeforeTestCase of actions TableReport, TextReport etc.\n" +
							"Each TestCase keeps the performed result in a separate line in a system table (see action ResultTable).\n" +
							"TestCase which has an action ResultTable is always Passed.\n" +
							"EachTestCase introduces its name space, that means actions and variables from one TestCase aren't accessible from another one.\n" +
							"To get the access  from the different TestCase is essentially to enter the parameter  Global for an action.(mark checkbox with G)",
		examples 		= "{{#\n" +
							"#TestCase;#Kind;#Depends;#For\n" +
							";;;#}}",
		shouldContain 	= { Tokens.TestCase },
		mayContain 		= { Tokens.Id, Tokens.RepOff, Tokens.Off, Tokens.Kind, Tokens.For, Tokens.Depends },
		parents			= { MatrixRoot.class },
		closes			= MatrixRoot.class,
        real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= true,
		seeAlsoClass 	= {Step.class}
	)
public final class TestCase extends MatrixItem 
{
	private Parameter plugin;
	private MutableValue<String> name;
	private MutableValue<String> kind;
	private MutableValue<String> depends;

	public TestCase()
	{
		super();
        this.plugin = new Parameter(Tokens.For.get(), null);
        this.name = new MutableValue<String>();
        this.kind = new MutableValue<String>();
        this.depends = new MutableValue<String>();
	}

    public TestCase(String name)
	{
    	this();
		this.name.set(name);
        this.kind.set(ScreenshotKind.Never.name());
	}

	public TestCase(TestCase testCase)
	{
		this.plugin = new Parameter(testCase.plugin);
		this.name = new MutableValue<>(testCase.name);
		this.kind = new MutableValue<>(testCase.kind);
		this.depends = new MutableValue<>(testCase.depends);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new TestCase(this);
	}

	@Override
    public String toString()
    {
        String s = getName();
        return Str.IsNullOrEmpty(s) ? super.toString() : s;
    }

	
    @Override
    public Object get(Tokens key)
    {
        switch (key)
        {
        case TestCase:
            return this.name.get();
        case Kind:
            return this.kind.get();
        case Depends:
            return this.depends.get();
        default:
            return super.get(key);
        }
    }
    
    @Override
    public void set(Tokens key, Object value)
    {
        switch (key)
        {
        case TestCase:
            this.name.set((String)value);
        case Kind:
            this.kind.set((String)value);
        case Depends:
            this.depends.set((String)value);
        default:
            super.set(key, value);
        }
    }
	
	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (this.plugin.isChanged() || this.name.isChanged() || this.kind.isChanged() || this.depends.isChanged())
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.plugin.saved();
    	this.name.saved();
        this.kind.saved();
        this.depends.saved();
    }

	//==============================================================================================

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTextBox(this, layout, 1, 0, this.id, this.id, () -> this.id.get());
		driver.showTitle(this, layout, 1, 1, Tokens.TestCase.get(), context.getFactory().getSettings());
		driver.showTextBox(this, layout, 1, 2, this.name, this.name, null);
        driver.showLabel(this, layout, 2, 0, Tokens.Depends.get() + ":");
        driver.showComboBox(this, layout, 2, 1, this.depends, this.depends, () ->
        {
            List<String> list = this.owner.listOfIds(TestCase.class);
            list.add(0, "");
            return list;
        }, (str) -> true);
        driver.showLabel(this, layout, 2, 2, "Screenshot:");
        driver.showComboBox(this, layout, 2, 3, this.kind, this.kind, () ->
        {
        	List<String> list = ScreenshotKind.names();
        	list.add(0, "");
        	return list;
        }, (str) -> true);
        driver.showLabel(this, layout, 2, 4, "Plugin:");
        driver.showExpressionField(this, layout, 2, 5, Tokens.For.get(), this.plugin, this.plugin, null, null, null, null);
		driver.showToggleButton(this, layout, 1, 3, 
		        b -> driver.hide(this, layout, 2, b),
				b -> (b ? "Hide" : "Show") + " additional",
				!((this.kind.isNullOrEmpty() || this.kind.get().equals(ScreenshotKind.Never.name())) && this.depends.isNullOrEmpty() && this.plugin.isExpressionNullOrEmpty()));

		return layout;
	}

	public String getName()
	{
		return this.name.get();
	}
	
	@Override
	public String getItemName()
	{
		return super.getItemName() + "  " + (this.name.get() == null ? "" : "(" + this.name + ")")
				+  (this.kind.get() == null ? "" : (" Screenshot: " + this.kind + "") );
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
        this.plugin.setExpression(systemParameters.get(Tokens.For));
		this.name.set(systemParameters.get(Tokens.TestCase)); 
        this.kind.set(systemParameters.get(Tokens.Kind)); 
        this.depends.set(systemParameters.get(Tokens.Depends)); 
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
	    super.addParameter(firstLine, secondLine, TypeMandatory.System,  Tokens.TestCase.get(), this.name.get());
		super.addParameter(firstLine, secondLine, TypeMandatory.System,      Tokens.Kind.get(), this.kind.get());
        super.addParameter(firstLine, secondLine, TypeMandatory.System,   Tokens.Depends.get(), this.depends.get());
        super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.For.get(), this.plugin.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.TestCase.get(), what, caseSensitive, wholeWord) 
                || SearchHelper.matches(this.plugin.getExpression(), what, caseSensitive, wholeWord)
		        || SearchHelper.matches(this.name.get(), what, caseSensitive, wholeWord)
		        || SearchHelper.matches(this.kind.get(), what, caseSensitive, wholeWord)
                || SearchHelper.matches(this.depends.get(), what, caseSensitive, wholeWord)
		        ;
	}
	
	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		super.checkItSelf(context, evaluator, listener, parameters);
	}

	@Override
	protected void beforeReport(ReportBuilder report)
	{
	    super.beforeReport(report);
		try
		{
			report.putMark(this.id.get());
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		super.changeExecutingState(MatrixItemExecutingState.Executing);
        Variables locals = evaluator.createLocals(); 
		ReturnAndResult ret = null;
		Table table = context.getTable();
        int position = -1;
		RowTable row = null;
		if(!isRepOff())
		{
    		position = table.size();
            row = table.addNew();
		}

		super.screenshot = null;
		try
		{
			Settings settings = getMatrix().getFactory().getSettings();
			String kindStr = this.kind.get();
			if (Str.IsNullOrEmpty(kindStr))
			{
		        kindStr = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_DEFAULT_SCREENSHOT).getValue();
			}
	        ScreenshotKind screenshotKind = ScreenshotKind.valueByName(kindStr);
	        String str = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_POPUPS).getValue();
	        boolean showPopups = Boolean.parseBoolean(str);
	        
	        if (row != null)
			{
				row.put(Context.matrixColumn, 			this.owner.getNameProperty().get());
				row.put(Context.testCaseIdColumn, 		this.getId());
				row.put(Context.testCaseColumn, 		this);
			}

	        if (!Str.IsNullOrEmpty(this.depends.get()))
	        {
	            MatrixItem testcase = this.owner.getRoot().find(true, TestCase.class, this.depends.get());
	            if (testcase != null && testcase.result != null && testcase.result.getResult().isFail())
	            {
	                ret = new ReturnAndResult(start, Result.Failed, "Fail due the TestCase " + this.depends.get() + " is failed", ErrorKind.FAIL, this);
	                updateTable(table, position, row, ret, ret.getError());
	                super.changeExecutingState(MatrixItemExecutingState.Failed);
	                return ret;
	            }
	        }
	        
            this.plugin.evaluate(evaluator);
	        doSreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnStart, ScreenshotKind.OnStartOrError);
			doShowPopup(showPopups, context, "started", Notifier.Info);

			ret = context.runHandler(start, context, listener, this, HandlerKind.OnTestCaseStart, report, null, null);

			if (!ret.getResult().isFail())
			{
    			ret = executeChildren(start, context, listener, evaluator, report, new Class<?>[] { OnError.class });
			}
			
			if (ret.getResult().isFail())
			{
	            this.plugin.evaluate(evaluator);
	            doSreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnError, ScreenshotKind.OnStartOrError, ScreenshotKind.OnFinishOrError);
	            MatrixError error = ret.getError();
			    
				doShowPopup(showPopups, context, "error: " + error, Notifier.Error);

				ret = context.runHandler(start, context, listener, this, HandlerKind.OnTestCaseError, report, 
			            error, super.find(false, OnError.class, null));
			}
			else
			{
			    ret = context.runHandler(start, context, listener, this, HandlerKind.OnTestCaseFinish, report, null, null);
			}
			
            this.plugin.evaluate(evaluator);
            doSreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnFinish, ScreenshotKind.OnFinishOrError);
            updateTable(table, position, row, ret, ret.getError());
		} 
		catch (Exception e)
		{
		    logger.error(e.getMessage(), e);
            updateTable(table, position, row, ret, new MatrixError(e.getMessage(), ErrorKind.EXCEPTION, this));
            changeExecutingState(MatrixItemExecutingState.Failed);
            return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
		finally
		{
		    evaluator.setLocals(locals);
		}
		super.changeExecutingState(ret.getResult().isFail() ? MatrixItemExecutingState.Failed : MatrixItemExecutingState.Passed);
		return ret;
	}

    private void updateTable(Table table, int position, RowTable row, ReturnAndResult ret, MatrixError error)
    {
        if (table != null && position >= 0 && row != null)
        {
            row.put(Context.timeColumn,         ret.getTime());
            row.put(Context.resultColumn,       ret.getResult().isFail() ? Result.Failed : ret.getResult());
            row.put(Context.errorColumn,        error);
            table.updateValue(position, row);
        }
    }

    @Override
	protected void afterReport(ReportBuilder report)
	{
        super.afterReport(report);
	}
}
