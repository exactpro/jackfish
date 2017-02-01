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
		description 	= "Returns value from SubCase.",
		examples 		= "#Return",
		seeAlso 		= "SubCase, Call",
		shouldContain 	= { Tokens.Return },
		mayContain 		= { Tokens.Off, Tokens.RepOff }, 
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
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
		driver.showTitle(this, layout, 1, 0, Tokens.Return.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 1, Tokens.Return.get(), this.returnValue, this.returnValue, null, null, null, null);

		return layout;
	}

    @Override
	public String getItemName()
	{
		return super.getItemName() + " " + (this.returnValue.getExpression() == null ? "" : ": " + this.returnValue.getExpression());
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
			throws MatrixException
	{
		this.returnValue.setExpression(systemParameters.get(Tokens.Return));
	}

    @Override
    protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
    {
        super.checkItSelf(context, evaluator, listener, ids, parameters);
        this.returnValue.prepareAndCheck(evaluator, listener, this);
    }
	
	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			this.returnValue.evaluate(evaluator);
			if (!this.returnValue.isValid())
			{
				ReportTable table = report.addTable("Return", null, true, 1, 
						new int[] {50, 50}, new String[] {"Expression", "Error"});
			
				String msg = "Error in expression #Return";
	        	table.addValues(this.returnValue.getExpression(), msg);
				table.addValues(this.returnValue.getValueAsString(), " <- Error in here");

	        	throw new Exception(msg);
			}
			
			
			Object eval = this.returnValue.getValue();
			if (this.returnValue != null)
			{
				ReportTable table = report.addTable("Return", null, true, 1, 
						new int[] {50, 50}, new String[] {"Expression", "Value"});
			
	        	table.addValues(this.returnValue.getExpression(), eval);

				report.itemIntermediate(this);
			}
	
			return new ReturnAndResult(start, Result.Return, eval);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
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

