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
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.MatrixException;
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

@MatrixItemAttribute(
		description 	= "Check the assertion.", 
		shouldContain 	= { Tokens.Assert },
		mayContain 		= { Tokens.Off, Tokens.Message }, 
		real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= false
	)
public class Assert extends MatrixItem
{
	public Assert()
	{
		super();
		this.assertion = new Parameter(Tokens.Assert.get(),	null); 
		this.message = new Parameter(Tokens.Message.get(),	null); 
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		Assert clone = (Assert) super.clone();
		clone.assertion = this.assertion;
		clone.message = this.message;
		return clone;
	}

	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (this.assertion.isChanged() || this.message.isChanged())
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.assertion.saved();
    	this.message.saved();
    }

	//==============================================================================================
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment			(this, layout, 0, 0, getComments());
		driver.showTitle			(this, layout, 1, 0, Tokens.Assert.get(), context.getFactory().getSettings());
		driver.showExpressionField	(this, layout, 1, 1, Tokens.Assert.get(), this.assertion, this.assertion, null, null, null, null);
		driver.showLabel			(this, layout, 1, 2, Tokens.Message.get());
		driver.showExpressionField	(this, layout, 1, 3, Tokens.Message.get(), this.message, this.message, null, null, null, null);

		return layout;
	}

    @Override
	public String getItemName()
	{
		return super.getItemName() + " " + this.assertion + " " + this.message;
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
			throws MatrixException
	{
		this.assertion.setExpression(systemParameters.get(Tokens.Assert));
		this.message.setExpression(systemParameters.get(Tokens.Message));
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
        ReportTable table;
        table = report.addTable("", null, true, 100,
                new int[] { 30, 70 }, new String[] { "Chapter", "Description"});

        table.addValues("Destination", "Check the assertion and if it is false throws an Exception with Message");
        table.addValues("Examples", "<code>#Assert;#Message<p>a == 10;'The value of ' + a + ' doesn match 10'</code>");
        table.addValues("See also", "Action assert");
	}

    @Override
    protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
    {
        super.checkItSelf(context, evaluator, listener, ids, parameters);
        this.assertion.prepareAndCheck(evaluator, listener, this);
        this.message.prepareAndCheck(evaluator, listener, this);
    }
	
	@Override
	protected ReturnAndResult executeItSelf(Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			this.assertion.evaluate(evaluator);
			this.message.evaluate(evaluator);
			if (!this.assertion.isValid() || !this.message.isValid())
			{
				ReportTable table = report.addTable("Assert", null, true, 1, 
						new int[] {50, 50}, new String[] {"Expression", "Error"});

				String msg = "Error in expression ";
				if (!this.assertion.isValid())
				{
					msg += " #Assert";
		        	table.addValues(this.assertion.getExpression(), this.assertion.getValueAsString());
				}
				if (!this.message.isValid())
				{
					msg += " #Message";
		        	table.addValues(this.message.getExpression(), this.message.getValueAsString());
				}

				return new ReturnAndResult(Result.Failed, msg, ErrorKind.EXCEPTION, this);
			}
			
			
			Object eval = this.assertion.getValue();
			if (eval instanceof Boolean)
			{
				ReportTable table = report.addTable("Assert", null, true, 1, 
						new int[] {50, 50}, new String[] {"Expression", "Value"});

				boolean bool = (Boolean)eval;
				if (bool)
				{
		        	table.addValues(this.assertion.getExpression(), bool);
					report.itemIntermediate(this);
					
					return new ReturnAndResult(Result.Passed, null); 
				}
				else
				{
		        	table.addValues(this.assertion.getExpression(), bool);
		        	table.addValues(this.message.getExpression(), this.message.getValueAsString());
					report.itemIntermediate(this);
					
					return new ReturnAndResult(Result.Failed, this.message.getValueAsString(), ErrorKind.ASSERT, this); 
				}
			}
	
			return new ReturnAndResult(Result.Failed, "Assert expression should be boolean", ErrorKind.EXPRESSION_ERROR, this);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, Tokens.Assert.get(), 	this.assertion.getExpression());
		super.addParameter(firstLine, secondLine, Tokens.Message.get(), this.message.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Assert.get(), what, caseSensitive, wholeWord) 
				|| SearchHelper.matches(this.assertion.getExpression(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.message.getExpression(), what, caseSensitive, wholeWord);
	}

	private Parameter assertion = null;
	private Parameter message = null;
}

