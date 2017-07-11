////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@MatrixItemAttribute(
		description 	= "This is an analogue of TestCase block , it is used to divide TestCase. \n" +
							"Each Step keeps its result in a separate line in a system table  (see action ResultTable).\n" +
							"Step which contains an action ResultTable is always Passed.",
		examples 		= "{{#\n" +
                            "#Step;#Kind\n" +
							";\n" +
							"#EndStep#}}",
		shouldContain 	= { Tokens.Step },
		mayContain 		= { Tokens.IgnoreErr, Tokens.Id, Tokens.RepOff, Tokens.Off, Tokens.Kind, Tokens.For, Tokens.Depends },
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= true,
		seeAlsoClass 	= {TestCase.class}
	)
public class Step extends MatrixItem
{
	public Step()
	{
		super();
        this.kind = new MutableValue<String>();
        this.plugin = new Parameter(Tokens.For.get(), null);
		this.identify = new Parameter(Tokens.Step.get(), null); 
        this.depends = new MutableValue<String>();
	}

	@Override
	public String toString()
	{
        String s = this.identify.getExpression();
        return Str.IsNullOrEmpty(s) ? super.toString() : s;
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		Step clone = (Step) super.clone();
		clone.kind = this.kind;
        clone.plugin = this.plugin.clone();
		clone.identify = this.identify;
		clone.depends = this.depends;
		return clone;
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.identify.setExpression(systemParameters.get(Tokens.Step));
        this.kind.set(systemParameters.get(Tokens.Kind)); 
        this.plugin.setExpression(systemParameters.get(Tokens.For));
        this.depends.set(systemParameters.get(Tokens.Depends)); 
        
        this.ignoreErr.set(false); // TODO we need to exclude this parameter
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, 	Tokens.Step.get(), this.identify.getExpression());
        super.addParameter(firstLine, secondLine, TypeMandatory.System,    Tokens.Kind.get(), this.kind.get());
        super.addParameter(firstLine, secondLine, TypeMandatory.System,     Tokens.For.get(), this.plugin.getExpression());
        super.addParameter(firstLine, secondLine, TypeMandatory.System,   Tokens.Depends.get(), this.depends.get());
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, TypeMandatory.System, Tokens.EndStep.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Step.get(), what, caseSensitive, wholeWord) 
                || SearchHelper.matches(this.depends.get(), what, caseSensitive, wholeWord)
                || SearchHelper.matches(this.kind.get(), what, caseSensitive, wholeWord)
                || SearchHelper.matches(this.plugin.getExpression(), what, caseSensitive, wholeWord)
                || SearchHelper.matches(this.identify.getExpression(), what, caseSensitive, wholeWord);
	}

	
    @Override
    public Object get(Tokens key)
    {
        switch (key)
        {
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
    	if (this.plugin.isChanged() || this.identify.isChanged() || this.kind.isChanged() || this.depends.isChanged())
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.identify.saved();
    	this.kind.saved();
    	this.plugin.saved();
    	this.depends.saved();
    }

	//==============================================================================================
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		
        driver.showTextBox(this, layout, 1, 0, this.id, this.id, () -> this.id.get());
		driver.showTitle(this, layout, 1, 1, Tokens.Step.get(), context.getFactory().getSettings());
        driver.showExpressionField(this, layout, 1, 2, Tokens.Step.get(), this.identify, this.identify, null, null, null, null);
        
        driver.showLabel(this, layout, 2, 0, Tokens.Depends.get() + ":");
        driver.showComboBox(this, layout, 2, 1, this.depends, this.depends, () ->
        {
            List<String> list = this.owner.listOfIds(Step.class);
            list.add(0, "");
            return list;
        }, (str) -> true);
        driver.showLabel(this, layout, 2, 2, " Screenshot:");
        driver.showComboBox(this, layout, 2, 3, this.kind, this.kind,() ->
        {   
            return Arrays.stream(ScreenshotKind.values()).map(Enum::toString).collect(Collectors.toList());
        }, (str) -> true);
        driver.showLabel(this, layout, 2, 4, " Plugin:");
        driver.showExpressionField(this, layout, 2, 5, Tokens.For.get(), this.plugin, this.plugin, null, null, null, null);
        
        driver.showToggleButton(this, layout, 1, 3, 
                b -> driver.hide(this, layout, 2, b),
                b -> (b ? "Hide" : "Show") + " additional",
                !((this.kind.isNullOrEmpty() || this.kind.get().equals(ScreenshotKind.Never.name())) && this.depends.isNullOrEmpty() && this.plugin.isExpressionNullOrEmpty()));

        return layout;
	}

    @Override
	public String getItemName()
	{
		return super.getItemName() + " " + Str.asString(this.identify.getExpression());
	}
    
    @Override
    protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
    {
        super.checkItSelf(context, evaluator, listener, parameters);
        this.identify.prepareAndCheck(evaluator, listener, this);
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
        Variables locals = evaluator.createLocals(); 
		ReturnAndResult ret = null;
		Table table = context.getTable();
        int position = -1;
        RowTable row =  null;
        if (!isRepOff())
        {
            position = table.size();
            row =  table.addNew();
        }
        super.screenshot = null;
		try
		{
			Settings settings = getMatrix().getFactory().getSettings();
			String kindStr = this.kind.get();
			if (Str.IsNullOrEmpty(kindStr))
			{
		        kindStr = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_DEFAULT_SCREENSHOT, ScreenshotKind.Never.name()).getValue();	        		
			}
	        ScreenshotKind screenshotKind = ScreenshotKind.valueByName(kindStr);
	        String str = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_POPUPS, "" + false).getValue();
	        boolean showPopups = Boolean.parseBoolean(str);

            if (row != null)
			{
				row.put(Context.matrixColumn, 			this.owner.getName());
				MatrixItem parent = findParent(TestCase.class);
				if (parent instanceof TestCase)
				{
					row.put(Context.testCaseIdColumn, 	parent.getId());
					row.put(Context.testCaseColumn, 	parent);
				}
                row.put(Context.stepIdColumn,           this.getId());
                row.put(Context.stepColumn,             this);
			}
            
            if (!this.identify.evaluate(evaluator))
            {
                ret = new ReturnAndResult(start, Result.StepFailed, this.identify.getValueAsString(), ErrorKind.WRONG_PARAMETERS, this);
                updateTable(table, position, row, ret, ret.getError());
                return ret;
            }
            Object identifyValue = this.identify.getValue();
            if (row != null)
            {
                row.put(Context.stepIdentityColumn,     "" + identifyValue);
            }
            
            if (!Str.IsNullOrEmpty(this.depends.get()))
            {
                MatrixItem step = this.owner.getRoot().find(true, Step.class, this.depends.get());
                if (step != null && step.result != null && step.result.getResult().isFail())
                {
                    ret = new ReturnAndResult(start, Result.StepFailed, "Fail due the Step " + this.depends.get() + " is failed", ErrorKind.FAIL, this);
                    updateTable(table, position, row, ret, ret.getError());
                    return ret;
                }
            }
            
            this.plugin.evaluate(evaluator);
            doSreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnStart, ScreenshotKind.OnStartOrError);
			doShowPopup(showPopups, context, "started", Notifier.Info);
			
			report.outLine(this, null, String.format("Step %s", identifyValue), null);

            ret = context.runHandler(start, context, listener, this, HandlerKind.OnStepStart, report, null, null);
			
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
                
                ret = context.runHandler(start, context, listener, this, HandlerKind.OnStepError, report, 
                        error, super.find(false, OnError.class, null));
            }
            else
            {
                ret = context.runHandler(start, context, listener, this, HandlerKind.OnStepFinish, report, null, null);
            }

            this.plugin.evaluate(evaluator);
            doSreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnFinish, ScreenshotKind.OnFinishOrError);
            updateTable(table, position, row, ret, ret.getError());
		} 
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			updateTable(table, position, row, ret, new MatrixError(e.getMessage(), ErrorKind.EXCEPTION, this));
			return new ReturnAndResult(start, Result.StepFailed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
		finally
		{
		    evaluator.setLocals(locals);
		}

		return ret.getResult() == Result.Failed ? new ReturnAndResult(start, ret, Result.StepFailed): ret;
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
    
    private Parameter identify;
    private MutableValue<String> kind;
    private Parameter plugin;
    private MutableValue<String> depends;
}
