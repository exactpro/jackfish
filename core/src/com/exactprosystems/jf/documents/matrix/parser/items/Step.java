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
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.config.HandlerKind;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.ScreenshotKind;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.Notifier;
import com.exactprosystems.jf.functions.RowTable;
import com.exactprosystems.jf.functions.Table;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@MatrixItemAttribute(
		description 	= "Elementary step in the script",
		examples 		= "#Step",
		seeAlso 		= "TestCase",
		shouldContain 	= { Tokens.Step },
		mayContain 		= { Tokens.IgnoreErr, Tokens.Off, Tokens.Kind, Tokens.For, Tokens.RepOff },
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= true
	)
public class Step extends MatrixItem
{
	public Step()
	{
		super();
        this.kind = new MutableValue<String>();
        this.plugin = new Parameter(Tokens.For.get(), null);
		this.identify = new Parameter(Tokens.Step.get(), null); 
	}

	@Override
	public String toString()
	{
		return super.toString() + " " + this.identify.get();
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		Step clone = (Step) super.clone();
		clone.kind = this.kind;
        clone.plugin = this.plugin.clone();
		clone.identify = this.identify;
		return clone;
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.identify.setExpression(systemParameters.get(Tokens.Step));
        this.kind.set(systemParameters.get(Tokens.Kind)); 
        this.plugin.setExpression(systemParameters.get(Tokens.For));
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, Tokens.Step.get(), 	this.identify.getExpression());
        super.addParameter(firstLine, secondLine, Tokens.Kind.get(),    this.kind.get());
        super.addParameter(firstLine, secondLine, Tokens.For.get(),     this.plugin.getExpression());
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, Tokens.EndStep.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Step.get(), what, caseSensitive, wholeWord) 
                || SearchHelper.matches(this.kind.get(), what, caseSensitive, wholeWord)
                || SearchHelper.matches(this.plugin.getExpression(), what, caseSensitive, wholeWord)
                || SearchHelper.matches(this.identify.getExpression(), what, caseSensitive, wholeWord);
	}

	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (this.plugin.isChanged() || this.identify.isChanged() || this.kind.isChanged())
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
    }

	//==============================================================================================
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.Step.get(), context.getFactory().getSettings());
        driver.showExpressionField(this, layout, 1, 1, Tokens.Step.get(), this.identify, this.identify, null, null, null, null);
        driver.showCheckBox(this, layout, 1, 2, Tokens.IgnoreErr.get(), this.ignoreErr, this.ignoreErr);
        driver.showLabel(this, layout, 1, 3, " Screenshot:");
        driver.showComboBox(this, layout, 1, 4, this.kind, this.kind, v -> Arrays.stream(ScreenshotKind.values()).map(Enum::toString).collect(Collectors.toList()));
        driver.showLabel(this, layout, 1, 5, " Plugin:");
        driver.showExpressionField(this, layout, 1, 6, Tokens.For.get(), this.plugin, this.plugin, null, null, null, null);

        return layout;
	}

    @Override
	public String getItemName()
	{
		return super.getItemName() + " " + (this.identify.getExpression() == null ? "" : this.identify.getExpression());
	}
    
    @Override
    protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
    {
        super.checkItSelf(context, evaluator, listener, ids, parameters);
        this.identify.prepareAndCheck(evaluator, listener, this);
    }
    

    @Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		ReturnAndResult ret = null;
		Table table = context.getTable();
		RowTable row = new RowTable();
		int position = -1;

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

            this.identify.evaluate(evaluator);
			Object identifyValue = this.identify.getValue();
			
            if (table != null && !isRepOff())
			{
				position = table.size();
				
				row.put(Context.matrixColumn, 			this.owner.getName());
				MatrixItem parent = findParent(TestCase.class);
				if (parent instanceof TestCase)
				{
					row.put(Context.testCaseIdColumn, 	parent.getId());
					row.put(Context.testCaseColumn, 	parent);
				}

				row.put(Context.stepIdentityColumn, 	identifyValue);
				row.put(Context.stepColumn, 			this);
				
				table.add(row);
			}
            
            this.plugin.evaluate(evaluator);
            doSreenshot(row, this.plugin.getValue(), screenshotKind, ScreenshotKind.OnStart, ScreenshotKind.OnStartOrError);
			doShowPopup(showPopups, context, "started", Notifier.Info);
			
			report.outLine(this, null, String.format("Step %s", identifyValue), null);

            ret = context.runHandler(start, context, listener, this, HandlerKind.OnStepStart, report, null, null);
			
            if (ret.getResult() != Result.Failed)
            {
                ret = executeChildren(start, context, listener, evaluator, report, new Class<?>[] { OnError.class }, null);
            }

            if (ret.getResult() == Result.Failed)
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
            if (table != null && position >= 0 && !isRepOff())
			{
				row.put(Context.timeColumn, 		ret.getTime());
				row.put(Context.resultColumn, 		ret.getResult());
				row.put(Context.errorColumn, 		ret.getError());
				table.updateValue(position, row);
			}
		} 
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());

            if (table != null && table.size() >= 0  && !isRepOff())
			{
				row.put(Context.timeColumn, 		ret.getTime());
				row.put(Context.resultColumn, 		ret.getResult());
				row.put(Context.errorColumn, 		new MatrixError(e.getMessage(), ErrorKind.EXCEPTION, this));
				table.updateValue(position, row);
			}
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}

		return ret;
	}

	private Parameter identify;
    private MutableValue<String> kind;
    private Parameter plugin;
}
