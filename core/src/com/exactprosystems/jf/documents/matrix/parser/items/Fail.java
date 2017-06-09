////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.api.error.common.MatrixException;
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
		description 		= "This operator generates a failure in the current matrix place. If a failure given can't be proceeded" +
								" by  OnError or by a global error handler, the current TestCase is failed. \n" +
								"Parameter Fail can take a string value then an error with this message is generated and this message is shown" +
								" instead of failure.  An operator Fail can be called in an error handler and a variable err can be passed to him. \n" +
								"(see OnError). In this case an exact failure place can be found.",
		examples 			= "{{#\n" +
								"#Id;#TestCase;#Kind;#Depends;#For\n" +
								"aa;Test case;Never;;\n" +
								"#Fail\n" +
								"'my error'\n" +
								"#OnError\n" +
								"#Action;#Where error was\n" +
								"Print;err.Where#}}",
		shouldContain 		= { Tokens.Fail }, 
		mayContain 			= { Tokens.Off, Tokens.RepOff }, 
		parents				= { Case.class, Else.class, For.class, ForEach.class, If.class,
								OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real				= true,
		hasValue 			= true, 
		hasParameters 		= false, 
		hasChildren 		= false,
		seeAlsoClass 		= {TestCase.class}
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
		driver.showTitle(this, layout, 1, 0, Tokens.Fail.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 1, Tokens.Fail.get(), this.failValue, this.failValue, null, null, null, null);

		return layout;
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters) throws MatrixException
	{
		this.failValue.setExpression(systemParameters.get(Tokens.Fail));
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		super.checkItSelf(context, evaluator, listener, parameters);
		this.failValue.prepareAndCheck(evaluator, listener, this);
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			this.failValue.evaluate(evaluator);
			if (!this.failValue.isValid())
			{
				ReportTable table = report.addTable("Fail", null, true, true, new int[] { 50, 50 }, new String[] { "Expression", "Error" });

				String msg = "Error in expression #Fail";
				table.addValues(this.failValue.getExpression(), msg);
				table.addValues(this.failValue.getValueAsString(), " <- Error in here");

				throw new Exception(msg);
			}

			Object eval = this.failValue.getValue();
			if (this.failValue != null)
			{
				ReportTable table = report.addTable("Fail", null, true, true, new int[] { 50, 50 }, new String[] { "Expression", "Value" });

				table.addValues(this.failValue.getExpression(), eval);

				report.itemIntermediate(this);
				
				if (eval instanceof MatrixError)
				{
					return new ReturnAndResult(start, (MatrixError)eval, Result.Failed);
				}
			}

			return new ReturnAndResult(start, Result.Failed, String.valueOf(eval), ErrorKind.FAIL, this);
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
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Fail.get(), this.failValue.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Fail.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.failValue.getExpression(), what, caseSensitive, wholeWord);
	}

	private Parameter	failValue	= null;
}
