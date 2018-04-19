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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;
import java.util.Map;

@MatrixItemAttribute(
		constantGeneralDescription = R.RETURN_DESCRIPTION,
		constantExamples = R.RETURN_EXAMPLE,
		shouldContain 	= { Tokens.Return },
		mayContain 		= { Tokens.Off, Tokens.RepOff }, 
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= false,
		seeAlsoClass 	= {SubCase.class, Call.class}
)
public class Return extends MatrixItem
{
	private final Parameter returnValue;

	public Return()
	{
		super();
		this.returnValue = new Parameter(Tokens.Return.get(), null);
	}

	public Return(Return ret)
	{
		this.returnValue = new Parameter(ret.returnValue);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new Return(this);
	}

	//region Interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.returnValue.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.returnValue.saved();
	}

	//endregion

	//region override from MatrixItem
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, super.getComments());
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
	{
		this.returnValue.setExpression(systemParameters.get(Tokens.Return));
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		super.checkItSelf(context, evaluator, listener, parameters);
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
				ReportTable table = report.addTable("Return", null, true, true, new int[]{50, 50}, "Expression", "Error");

				String msg = String.format(R.COMMON_ERROR_IN_EXPRESSION.get(), this.getClass().getSimpleName());
				table.addValues(this.returnValue.getExpression(), msg);
				table.addValues(this.returnValue.getValueAsString(), " <- Error in here");

				return super.createReturn(msg, listener, start);
			}

			Object eval = this.returnValue.getValue();
			if (eval != null)
			{
				ReportTable table = report.addTable("Return", null, true, true, new int[]{50, 50}, "Expression", "Value");
				table.addValues(this.returnValue.getExpression(), eval);
				report.itemIntermediate(this);
			}

			return new ReturnAndResult(start, Result.Return, eval);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return super.createReturn(e.getMessage(), listener, start);
		}
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Return.get(), this.returnValue.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Return.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.returnValue.getExpression(), what, caseSensitive, wholeWord);
	}
	//endregion

}

