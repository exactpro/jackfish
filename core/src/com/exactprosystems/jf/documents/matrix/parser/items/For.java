////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.MatrixException;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@MatrixItemAttribute(
		description 	= "Operator For is used to organize a cycle which should be performed a given number of times. This cycle can for for increase and reduction." +
							"Fields:\n" +
							"For – a variable is given what iteration will be performed by.\n" +
							"From -  a numeric value which a cycle will be started with is given. \n" +
							"To - a numeric value which is reached and the last iteration will be performed.  \n" +
				"Step – a step, which will change the value in the field from.",
		examples 		= "{{##For;#From;#To;#Step\n" +
							"a;1;10;1\n" +
							"#Action;#a\n" +
							"Print;a\n" +
							"#EndFor#}}",
		seeAlso 		= "While, ForEach",
		shouldContain 	= { Tokens.For, Tokens.From, Tokens.To },
		mayContain 		= { Tokens.Step, Tokens.Off, Tokens.RepOff }, 
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= true,
		seeAlsoClass 	= {While.class, ForEach.class}
	)
public final class For extends MatrixItem
{
	public For()
	{
		super();
		this.var	= new MutableValue<String>();
		this.from	= new Parameter(Tokens.From.get(),	null); 
		this.to		= new Parameter(Tokens.To.get(), 		null); 
		this.step	= new Parameter(Tokens.Step.get(), 	null); 
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		For clone = ((For) super.clone());
		clone.var = var.clone();
		clone.from = from.clone();
		clone.to = to.clone();
		clone.step = step.clone();
		return clone;
	}
	
	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (	this.var.isChanged()
    		||	this.from.isChanged()
    		|| 	this.to.isChanged()
    		|| 	this.step.isChanged() )
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.var.saved();
    	this.from.saved();
    	this.to.saved();
    	this.step.saved();
    }

	//==============================================================================================
	// implements Displayed
	//==============================================================================================
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.For.get(), context.getFactory().getSettings());
		driver.showTextBox(this, layout, 1, 1, this.var, this.var, () -> this.var.get());
		driver.showTitle(this, layout, 1, 2, Tokens.From.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 3, Tokens.From.get(), this.from, this.from, null, null, null, null);
		driver.showTitle(this, layout, 1, 4, Tokens.To.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 5, Tokens.To.get(), this.to, this.to, null, null, null, null);
		driver.showTitle(this, layout, 1, 6, Tokens.Step.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 7, Tokens.Step.get(), this.step, this.step, null, null, null, null);

		return layout;
	}

	//==============================================================================================

	@Override
	public String getItemName()
	{
		return super.getItemName()
				+ " "
				+ (this.var.toString().equals("") ? this.var : this.var + " = ")
				+ (this.from.getExpression() == null ? "" : this.from + " ")
				+ (this.to.getExpression() == null ? "" : this.to + " ")
				+ (this.step.getExpression() == null ? "" : this.step);
	}
	
	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters) 
			throws MatrixException
	{
		super.initItSelf(systemParameters);
		
		this.var.set(systemParameters.get(Tokens.For)); 
		this.from.setExpression(systemParameters.get(Tokens.From)); 
		this.to.setExpression(systemParameters.get(Tokens.To)); 
		this.step.setExpression(systemParameters.get(Tokens.Step)); 
		if (this.step == null)
		{
			this.step.setExpression("1");
		}
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, Tokens.For.get(), 	this.var.get());
		super.addParameter(firstLine, secondLine, Tokens.From.get(), 	this.from.getExpression());
		super.addParameter(firstLine, secondLine, Tokens.To.get(), 		this.to.getExpression());
		super.addParameter(firstLine, secondLine, Tokens.Step.get(), 	this.step.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(this.var.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(Tokens.For.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(Tokens.From.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(Tokens.Step.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.from.getExpression(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.to.getExpression(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.step.getExpression(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, Tokens.EndFor.get());
	}

    @Override
    protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
    {
        super.checkItSelf(context, evaluator, listener, ids, parameters);
        this.from.prepareAndCheck(evaluator, listener, this);
        this.to.prepareAndCheck(evaluator, listener, this);
        this.step.prepareAndCheck(evaluator, listener, this);
    }
    
	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			ReturnAndResult ret = null;
			Result result = null;
			
			if (!this.from.evaluate(evaluator))
			{
				throw new Exception("Error in expression #From");
			}
			if (!this.to.evaluate(evaluator))
			{
				throw new Exception("Error in expression #To");
			}
			if (!this.step.evaluate(evaluator))
			{
				throw new Exception("Error in expression #Step");
			}
			
			Object fromValue 	= this.from.getValue();
			Object toValue 		= this.to.getValue();
			Object stepValue 	= this.step.getValue();
			if (!(fromValue instanceof Number))
			{
				throw new Exception("#From is not type of Number");
			}
			if (!(toValue instanceof Number))
			{
				throw new Exception("#To is not type of Number");
			}
			if (!(stepValue instanceof Number))
			{
				throw new Exception("#Step is not type of Number");
			}
			
			// start value
			AtomicReference<Number> current = new AtomicReference<>((Number)fromValue);
			
			for (setCurrent(current, evaluator);
			     checkCondition(current, toValue, stepValue, evaluator);
			     changeCurrent(current, stepValue, evaluator)
			     )
			{
				report.outLine(this, null, String.format("loop %s = %s", this.var, current.get()), current.get().intValue());
				
				ret = executeChildren(start, context, listener, evaluator, report, new Class<?>[] { OnError.class });
				result = ret.getResult();
				
				if (result == Result.Failed)
				{
					MatrixItem branchOnError = super.find(false, OnError.class, null);
					if (branchOnError != null && branchOnError instanceof OnError)
					{
						((OnError)branchOnError).setError(ret.getError());
						
						ret = branchOnError.execute(context, listener, evaluator, report);
						result = ret.getResult();
					}
					else
					{
						return ret;
					}
				}

                if(result == Result.Break)
                {
                    break;
                }
                if (result == Result.Failed)
                {
                    return new ReturnAndResult(start, ret.getError(), Result.Failed);
                }
                if (result == Result.Stopped || result == Result.Return)
                {
                    return new ReturnAndResult(start, result, ret.getOut());
                }
                if (result == Result.Continue)
                {
                    continue;
                }
            }

            return new ReturnAndResult(start, Result.Passed, null);
		} 
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
	}

    private void setCurrent(AtomicReference<Number> current, AbstractEvaluator evaluator)
    {
        Number currentValue = current.get();
        evaluator.getLocals().set(this.var.get(), currentValue);
    }

    private boolean checkCondition(AtomicReference<Number> current, Object toValue, Object stepValue, AbstractEvaluator evaluator)
	{
	    Number currentValue = current.get();
	    
		return ((Number)stepValue).intValue() > 0 
				? ((Number)currentValue).intValue() <= ((Number)toValue).intValue()
				: ((Number)currentValue).intValue() >= ((Number)toValue).intValue();
	}

    private void changeCurrent(AtomicReference<Number> current, Object stepValue, AbstractEvaluator evaluator)
    {
        Number currentValue = current.get();
        currentValue = currentValue.intValue() + ((Number)stepValue).intValue();
        current.set(currentValue);
        
        setCurrent(current, evaluator);
    }


    private MutableValue<String> var; 
	private Parameter from; 
	private Parameter to; 
	private Parameter step;
}
