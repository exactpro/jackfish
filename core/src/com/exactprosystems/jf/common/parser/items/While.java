////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.*;
import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@MatrixItemAttribute(
		description 	= "Loop while condition is true.", 
		shouldContain 	= { Tokens.While },
		mayContain 		= { Tokens.Off },
		real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= true
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
		super.addParameter(firstLine, secondLine, Tokens.While.get(), 	this.condition.getExpression());
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, Tokens.EndWhile.get());
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
		driver.showTitle(this, layout, 1, 0, Tokens.While.get(), context.getConfiguration().getSettings());
		driver.showExpressionField(this, layout, 1, 1, Tokens.While.get(), this.condition, this.condition, null, null, null, null);

		return layout;
	}

    @Override
	public String getItemName()
	{
		return super.getItemName() + " " + this.condition;
	}

    @Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
        ReportTable table;
        table = report.addTable("", 100, new int[] { 30, 70 },
                new String[] { "Chapter", "Description"});

        table.addValues("Destination", "To organize a loop with precondition");
        table.addValues("Examples", "<code>#While<p>true</code>");
        table.addValues("See also", "For, Break, Continue");
	}
    
    @Override
    protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
    {
        super.checkItSelf(context, evaluator, listener, ids, parameters);
        this.condition.prepareAndCheck(evaluator, listener, this);
    }
    

    @Override
	protected ReturnAndResult executeItSelf(Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			ReturnAndResult ret = new ReturnAndResult(Result.Passed);
			Result result = ret.getResult();

			this.loops = 0;
			
			this.condition.evaluate(evaluator);
			if (!this.condition.isValid())
			{
				throw new Exception("Error in expression #While");
			}
			
			Object bool = this.condition.getValue();
			if (bool instanceof Boolean)
			{
				while((Boolean)bool)
				{
					report.outLine(this, "loop", this.loops);

					ret = executeChildren(context, listener, evaluator, report, new Class<?>[] { OnError.class }, null);
					result = ret.getResult();

					this.loops++;
					this.condition.evaluate(evaluator);
					if (!this.condition.isValid())
					{
						throw new Exception("Error in expression #While");
					}
					bool = this.condition.getValue();

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

					if (result == Result.Failed || result == Result.Stopped || result == Result.Break || result == Result.Return)
					{
						break;
					}
					if (result == Result.Continue)
					{
						continue;
					}
				}

				return new ReturnAndResult(result == Result.Continue ? Result.Passed : result, ret.getOut());
			}
					
			throw new Exception("result is not type of Boolean");
		} 
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(Result.Failed, null, e.getMessage());
		}
	}

	private Parameter condition;
	
	private int loops = 0;
}
