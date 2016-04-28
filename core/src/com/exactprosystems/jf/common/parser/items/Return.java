////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.*;
import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;

import java.util.List;
import java.util.Map;
import java.util.Set;

@MatrixItemAttribute(
		description 	= "Returns value from SubCase.", 
		shouldContain 	= { Tokens.Return },
		mayContain 		= { Tokens.Off }, 
		real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= false
	)
public class Return extends MatrixItem
{
	public Return()
	{
		super();
		this.returnValue = new Parameter(Tokens.Return.get(),	null); 
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		Return clone = (Return) super.clone();
		clone.returnValue = returnValue;
		return clone;
	}

	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (this.returnValue.isChanged())
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.returnValue.saved();
    }

	//==============================================================================================
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.Return.get(), context.getConfiguration().getSettings());
		driver.showExpressionField(this, layout, 1, 1, Tokens.Return.get(), this.returnValue, this.returnValue, null, null, null, null);

		return layout;
	}

    @Override
	public String getItemName()
	{
		return super.getItemName() + " " + this.returnValue;
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
			throws MatrixException
	{
		this.returnValue.setExpression(systemParameters.get(Tokens.Return));
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
        ReportTable table;
        table = report.addTable("", 100, new int[] { 30, 70 },
                new String[] { "Chapter", "Description"});

        table.addValues("Destination", "Returns a value from SubCase to calling action");
        table.addValues("Examples", "<code>#Return<p>123</code>");
        table.addValues("See also", "SubCase");
	}

    @Override
    protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
    {
        super.checkItSelf(context, evaluator, listener, ids, parameters);
        this.returnValue.prepareAndCheck(evaluator, listener, this);
    }
	
	@Override
	protected ReturnAndResult executeItSelf(Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			this.returnValue.evaluate(evaluator);
			if (!this.returnValue.isValid())
			{
				ReportTable table = report.addTable("Return", 1, new int[] {50, 50}, 
						new String[] {"Expression", "Error"});
			
				String msg = "Error in expression #Return";
	        	table.addValues(this.returnValue.getExpression(), msg);
				table.addValues(this.returnValue.getValueAsString(), " <- Error in here");

	        	throw new Exception(msg);
			}
			
			
			Object eval = this.returnValue.getValue();
			if (this.returnValue != null)
			{
				ReportTable table = report.addTable("Return", 1, new int[] {50, 50}, 
						new String[] {"Expression", "Value"});
			
	        	table.addValues(this.returnValue.getExpression(), eval);

				report.itemIntermediate(this);
			}
	
			return new ReturnAndResult(Result.Return, eval);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(Result.Failed, null, e.getMessage());
		}
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, Tokens.Return.get(), this.returnValue.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Return.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.returnValue.getExpression(), what, caseSensitive, wholeWord);
	}

	private Parameter returnValue = null;
}

