/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;
import java.util.Map;

@MatrixItemAttribute(
		constantGeneralDescription = R.FAIL_DESCRIPTION,
		constantExamples = R.FAIL_EXAMPLE,
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
	private final Parameter failValue;

	public Fail()
	{
		super();
		this.failValue = new Parameter(Tokens.Fail.get(), null);
	}

	/**
	 * copy constructor
	 */
	public Fail(Fail fail)
	{
		this.failValue = new Parameter(fail.failValue);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new Fail(this);
	}

	//region Interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.failValue.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.failValue.saved();
	}

	//endregion

	//region override from MatrixItem
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, super.getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.Fail.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 1, Tokens.Fail.get(), this.failValue, this.failValue, null, null, null, null);
		return layout;
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
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
				ReportTable table = report.addTable("Fail", null, true, true, new int[] { 50, 50 }, "Expression", "Error");

				String msg = String.format(R.COMMON_ERROR_IN_EXPRESSION.get(), this.getClass().getSimpleName());
				table.addValues(this.failValue.getExpression(), msg);
				table.addValues(this.failValue.getValueAsString(), " <- Error in here");

				return new ReturnAndResult(start, Result.Failed, msg, ErrorKind.EXCEPTION, this);
			}

			Object eval = this.failValue.getValue();
			if (eval != null)
			{
				ReportTable table = report.addTable("Fail", null, true, true, new int[] { 50, 50 }, "Expression", "Value");

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
			listener.error(this.owner, super.getNumber(), this, e.getMessage());
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
	//endregion
}
