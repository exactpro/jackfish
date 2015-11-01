////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
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
		description 		= "Make this TestCase failed.", 
		shouldContain 		= { Tokens.Fail }, 
		mayContain 			= { Tokens.Off }, 
		real				= true,
		hasValue 			= true, 
		hasParameters 		= false, 
		hasChildren 		= false
		)
public class Fail extends MatrixItem
{
	public Fail()
	{
		super();
		this.failValue = new Parameter(Tokens.Fail.get(), null);
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		Fail clone = (Fail) super.clone();
		clone.failValue = failValue;
		return clone;
	}

	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (this.failValue.isChanged())
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.failValue.saved();
    }

	//==============================================================================================
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.Fail.get());
		driver.showExpressionField(this, layout, 1, 1, Tokens.Fail.get(), this.failValue, this.failValue, null, null, null, null);

		return layout;
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters) throws MatrixException
	{
		this.failValue.setExpression(systemParameters.get(Tokens.Fail));
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
		ReportTable table;
		table = report.addTable("", 100, new int[] { 30, 70 }, new String[] { "Chapter", "Description" });

		table.addValues("Destination", "Make this TestCase failed");
		table.addValues("Examples", "<code>#Fail<p>'Error due the condition'</code>");
		table.addValues("See also", "TesCase");
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
	{
		super.checkItSelf(context, evaluator, listener, ids, parameters);
		this.failValue.prepareAndCheck(evaluator, listener, this);
	}

	@Override
	protected ReturnAndResult executeItSelf(Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			this.failValue.evaluate(evaluator);
			if (!this.failValue.isValid())
			{
				ReportTable table = report.addTable("Fail", 1, new int[] { 50, 50 }, new String[] { "Expression", "Error" });

				String msg = "Error in expression #Fail";
				table.addValues(this.failValue.getExpression(), msg);
				table.addValues(this.failValue.getValueAsString(), " <- Error in here");

				throw new Exception(msg);
			}

			Object eval = this.failValue.getValue();
			if (this.failValue != null)
			{
				ReportTable table = report.addTable("Fail", 1, new int[] { 50, 50 }, new String[] { "Expression", "Value" });

				table.addValues(this.failValue.getExpression(), eval);

				report.itemIntermediate(this);
			}

			return new ReturnAndResult(Result.Failed, eval);
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
		super.addParameter(firstLine, secondLine, Tokens.Fail.get(), this.failValue.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Fail.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.failValue.getExpression(), what, caseSensitive, wholeWord);
	}

	private Parameter	failValue	= null;
}
