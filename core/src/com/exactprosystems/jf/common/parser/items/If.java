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
        description 	= "Condition executing.",
        shouldContain 	= { Tokens.If },
        mayContain 		= { Tokens.Off },
        real			= true,
        hasValue 		= true,
        hasParameters 	= false,
        hasChildren 	= true
)
public class If extends MatrixItem
{
	public If()
	{
		super();
		this.condition	= new Parameter(Tokens.If.get(),	null); 
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		If clone = (If) super.clone();
		clone.condition = condition.clone();
		return clone;
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
		driver.showTitle(this, layout, 1, 0, Tokens.If.get(), context.getConfiguration().getSettings());
		driver.showExpressionField(this, layout, 1, 1, Tokens.If.get(), this.condition, this.condition, null, null, null, null);

		return layout;
	}

	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + this.condition;
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters) throws MatrixException
	{
		this.condition.setExpression(systemParameters.get(Tokens.If));
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, Tokens.If.get(), this.condition.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.If.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.condition.getExpression(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, Tokens.EndIf.get());
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
        ReportTable table;
        table = report.addTable("", 100, new int[] { 30, 70 },
                new String[] { "Chapter", "Description"});

        table.addValues("Destination", "To check a condition and execute one or another branch of script");
        table.addValues("Examples", "<code>#If</code>");
        table.addValues("See also", "Else");
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
			
			this.condition.evaluate(evaluator);
			if (!this.condition.isValid())
			{
				ReportTable table = report.addTable("If", 1, new int[] {50, 50}, 
						new String[] {"Expression", "Error"});
			
				String msg = "Error in expression #If.\n"+this.condition.getValueAsString();
	        	table.addValues(this.condition.getExpression(), msg);

	        	throw new Exception(msg);
			}

			Object eval = this.condition.getValue();
			if (eval instanceof Boolean)
			{
				Boolean	bool = (Boolean) eval;
				if (bool)
				{
					ret = executeChildren(context, listener, evaluator, report, new Class<?>[] { Else.class }, null);
				}
				else
				{
					MatrixItem branchElse = super.find(false, Else.class, null);
					if (branchElse != null)
					{
						return branchElse.execute(context, listener, evaluator, report);
					}
				}

				return ret;
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
}
