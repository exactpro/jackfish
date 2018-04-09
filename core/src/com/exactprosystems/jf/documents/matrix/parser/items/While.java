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
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;
import java.util.Map;

@MatrixItemAttribute(
		constantGeneralDescription = R.WHILE_DESCRIPTION,
		constantExamples = R.WHILE_EXAMPLE,
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
	private final Parameter condition;
	private       int       loops;

	public While()
	{
		super();
		this.condition = new Parameter(Tokens.While.get(), null);
	}

	public While(While wh)
	{
		this.condition = new Parameter(wh.condition);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new While(this);
	}

	//region Interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.condition.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.condition.saved();
	}

	//endregion

	//region override from MatrixItem
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, super.getComments());
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
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.condition.setExpression(systemParameters.get(Tokens.While));
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.While.get(), this.condition.getExpression());
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, TypeMandatory.System, Tokens.EndWhile.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.While.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.condition.getExpression(), what, caseSensitive, wholeWord);
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			ReturnAndResult ret;
			Result result;

			this.loops = 0;

			while (this.checkCondition(evaluator))
			{
				report.outLine(this, null, "loop", this.loops);

				ret = super.executeChildren(start, context, listener, evaluator, report, new Class<?>[]{OnError.class});
				result = ret.getResult();

				if (result.isFail())
				{
					MatrixItem branchOnError = super.find(false, OnError.class, null);
					if (branchOnError != null && branchOnError instanceof OnError)
					{
						((OnError) branchOnError).setError(ret.getError());
						ret = branchOnError.execute(context, listener, evaluator, report);
						result = ret.getResult();
					}
					else
					{
						return ret;
					}
				}

				if (result == Result.Break)
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
			return super.createReturn(e.getMessage(), listener, start);
		}
	}

	//endregion

	//region private methods
	private boolean checkCondition(AbstractEvaluator evaluator) throws Exception
	{
		this.loops++;

		this.condition.evaluate(evaluator);
		if (!this.condition.isValid())
		{
			throw new Exception(String.format(R.COMMON_ERROR_IN_EXPRESSION.get(), this.getClass().getSimpleName()));
		}

		Object bool = this.condition.getValue();
		if (!(bool instanceof Boolean))
		{
			throw new Exception(R.COMMON_RESULT_IS_NOT_BOOLEAN.get());
		}
		return (Boolean) bool;
	}
	//endregion
}
