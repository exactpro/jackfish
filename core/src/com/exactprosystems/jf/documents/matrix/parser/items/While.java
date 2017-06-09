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
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;
import java.util.Map;

@MatrixItemAttribute(
		description 	= "Operator While is used to organized a cycle.",
		examples 		= "Notify a variable a and set value 1 to it.\n" +
							"While the variable value is a < 10 in  While cycle display its value in console  and increase it by 1." +
							"{{#\n" +
							"#Id;#Let\n" +
							"a;1\n" +
							"#While\n" +
							"a < 10\n" +
							"#Action;#a\n" +
							"Print;a\n" +
							"#Id;#Let\n" +
							"a;a+1\n" +
							"#EndWhile#}}",
		shouldContain 	= { Tokens.While },
		mayContain 		= { Tokens.Off, Tokens.RepOff },
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= true,
		seeAlsoClass 	= {For.class, ForEach.class}
)
public class While extends MatrixItem
{
	public While()
	{
		super();
		this.condition = new Parameter(Tokens.While.get(),	null); 
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		While clone = (While) super.clone();
		clone.condition = condition;
		return clone;
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.condition.setExpression(systemParameters.get(Tokens.While));
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, 	Tokens.While.get(), this.condition.getExpression());
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, TypeMandatory.System, Tokens.EndWhile.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.While.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.condition.getExpression(), what, caseSensitive, wholeWord);
	}

	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (this.condition.isChanged())
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.condition.saved();
    }

	//==============================================================================================
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.While.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 1, Tokens.While.get(), this.condition, this.condition, null, null, null, null);

		return layout;
	}

    @Override
	public String getItemName()
	{
		return super.getItemName() + " " + (this.condition.getExpression() == null ? "" : ": " + this.condition.getExpression());
	}
    
    @Override
    protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
    {
        super.checkItSelf(context, evaluator, listener, parameters);
        this.condition.prepareAndCheck(evaluator, listener, this);
    }
    

    @Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			ReturnAndResult ret = null;
			Result result = null;

			this.loops = 0;
			
			while(conditonResult(evaluator))
			{
				report.outLine(this, null, "loop", this.loops);

				ret = executeChildren(start, context, listener, evaluator, report, new Class<?>[] { OnError.class });
				result = ret.getResult();

				if (result.isFail())
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
                if (result.isFail())
                {
                    return new ReturnAndResult(start, ret.getError(), result);
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

    private boolean conditonResult(AbstractEvaluator evaluator) throws Exception
    {
        this.loops++;

        this.condition.evaluate(evaluator);
        if (!this.condition.isValid())
        {
            throw new Exception("Error in expression #While");
        }
        
        Object bool = this.condition.getValue();
        if (! (bool instanceof Boolean))
        {
            throw new Exception("result is not type of Boolean");
        }
        return (Boolean)bool;
    }
    
	private Parameter condition;
	
	private int loops = 0;
}
