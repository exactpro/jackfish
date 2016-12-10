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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.config.HandlerKind;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.RowTable;
import com.exactprosystems.jf.functions.Table;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@MatrixItemAttribute(
		description 	= "Test case.", 
		shouldContain 	= { Tokens.TestCase },
		mayContain 		= { Tokens.Id, Tokens.RepOff, Tokens.Off, Tokens.Kind, Tokens.For, Tokens.Depends },
		closes			= MatrixRoot.class,
        real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= true
	)
public final class TestCase extends MatrixItem 
{	
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


	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		TestCase clone = (TestCase) super.clone();
		clone.plugin = this.plugin.clone();
		clone.name = this.name.clone();
        clone.kind = this.kind.clone();
        clone.depends = this.depends.clone();
		return clone;
	}

	@Override
	public String toString()
	{
		return super.toString() + " " + getName();
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
        driver.showToggleButton(this, layout, 1, 3, "Show additional", b ->
        {
            driver.hide(this, layout, 2, b);
            return null;
        }, !(this.kind.isNullOrEmpty() && this.depends.isNullOrEmpty() && this.plugin.isExpressionNullOrEmpty()));

        driver.showLabel(this, layout, 2, 0, "Screenshot:");
        driver.showComboBox(this, layout, 2, 1, this.kind, this.kind, v -> ScreenshotKind.names() );
        driver.showLabel(this, layout, 2, 2, "Plugin:");
        driver.showLabel(this, layout, 2, 3, Tokens.Depends.get() + ":");
        driver.showComboBox(this, layout, 2, 4, this.depends, this.depends, v -> this.owner.listOfIds(TestCase.class));
        driver.showExpressionField(this, layout, 2, 5, Tokens.For.get(), this.plugin, this.plugin, null, null, null, null);

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
		addParameter(firstLine, secondLine, Tokens.TestCase.get(),  this.name.get());
        addParameter(firstLine, secondLine, Tokens.Kind.get(),      this.kind.get());
        addParameter(firstLine, secondLine, Tokens.Depends.get(),   this.depends.get());
        super.addParameter(firstLine, secondLine, Tokens.For.get(), this.plugin.getExpression());
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
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
	{
		ids = new HashSet<String>();
		
		super.checkItSelf(context, evaluator, listener, ids, parameters);
	}

	@Override
	protected void beforeReport(ReportBuilder report)
	{
	    super.beforeReport(report);
		try
		{
			report.putMark(this.id.get());
			report.reportSwitch(true);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
        ReportTable table;
        table = report.addTable("", null, true, 100,
                new int[] { 30, 70 }, new String[] { "Chapter", "Description"});

        table.addValues("Destination", "TestCase is needed to make script shorter.");
        table.addValues("Examples", "<code>#Id;#TestCase<p>ID;Description</code>");
        table.addValues("See also", "");
	}
	
	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		Result res = null;
		ReturnAndResult ret = null;
		Table table = context.getTable();
		RowTable row = new RowTable();
		int position = -1;
		
		try
		{
	        ScreenshotKind screenshotKind = ScreenshotKind.valueByName(this.kind.get());
	        String dependsOnTestCase = this.depends.get();

	        if (table != null)
			{
				position = table.size();

				row.put(Context.matrixColumn, 			this.owner.getName());
				row.put(Context.testCaseIdColumn, 		this.getId());
				row.put(Context.testCaseColumn, 		this);
				
				table.add(row);
			}

	        if (!Str.IsNullOrEmpty(depends.get()))
	        {
//	            this.owner.
	        }
	        
            this.plugin.evaluate(evaluator);
	        doSreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnStart, ScreenshotKind.OnStartOrError);
			
			this.locals = evaluator.createLocals();
			
			context.runHandler(HandlerKind.OnTestCaseStart, report, null);

			ret = executeChildren(start, context, listener, evaluator, report, new Class<?>[] { OnError.class }, this.locals);
			res = ret.getResult();
			
			if (res == Result.Failed)
			{
	            this.plugin.evaluate(evaluator);
	            doSreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnError, ScreenshotKind.OnStartOrError, ScreenshotKind.OnFinishOrError);

	            MatrixError error = ret.getError();
			    
			    ReturnAndResult errorRet = context.runHandler(HandlerKind.OnTestCaseError, report, error);
	            if (errorRet != null)
	            {
	                ret = errorRet;
	                res = ret.getResult();
	            }
	            else
	            {
        		    MatrixItem branchOnError = super.find(false, OnError.class, null);
        			if (branchOnError != null && branchOnError instanceof OnError)
        			{
        				((OnError)branchOnError).setError(error);
        				
        				ret = branchOnError.execute(context, listener, evaluator, report);
        				res = ret.getResult();
        			}
	            }
			}
	        context.runHandler(HandlerKind.OnTestCaseFinish, report, null);
			
            this.plugin.evaluate(evaluator);
            doSreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnFinish, ScreenshotKind.OnFinishOrError);
            if (table != null && position >= 0)
			{
				row.put(Context.timeColumn, 		ret.getTime());
				row.put(Context.resultColumn, 		res);
				row.put(Context.errorColumn, 		ret.getError());
				table.updateValue(position, row);
			}

            outScreenshot(report, row);
		} 
		catch (Exception e)
		{
		    logger.error(e.getMessage(), e);
		    
			if (table != null && table.size() >= 0)
			{
				row.put(Context.timeColumn, 		ret.getTime());
				row.put(Context.resultColumn, 		res);
				row.put(Context.errorColumn, 		new MatrixError(e.getMessage(), ErrorKind.EXCEPTION, this));
				table.updateValue(position, row);
			}
		}

		return ret;
	}

    @Override
	protected void afterReport(ReportBuilder report)
	{
        super.afterReport(report);
	}

	
	private Variables locals = null;
    private Parameter plugin;
	private MutableValue<String> name;
    private MutableValue<String> kind;
    private MutableValue<String> depends;
}
