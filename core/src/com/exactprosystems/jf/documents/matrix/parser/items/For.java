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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@MatrixItemAttribute(
		constantGeneralDescription = R.FOR_DESCRIPTION,
		constantExamples = R.FOR_EXAMPLE,
		shouldContain 	= { Tokens.For, Tokens.From, Tokens.To },
		mayContain 		= { Tokens.Step, Tokens.Off, Tokens.RepOff }, 
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
		hasChildren 	= true,
		seeAlsoClass 	= {While.class, ForEach.class}
	)
public final class For extends MatrixItem
{
	private final MutableValue<String> var;
	private final Parameter from;
	private final Parameter to;
	private final Parameter step;

	public For()
	{
		super();
		this.var = new MutableValue<>();
		this.from = new Parameter(Tokens.From.get(), null);
		this.to = new Parameter(Tokens.To.get(), null);
		this.step = new Parameter(Tokens.Step.get(), null);
	}

	/**
	 * copy constructor
	 */
	public For(For f)
	{
		this.var = new MutableValue<>(f.var);
		this.from = new Parameter(f.from);
		this.to = new Parameter(f.to);
		this.step = new Parameter(f.step);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new For(this);
	}

	//region Interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.var.isChanged()
				|| this.from.isChanged()
				|| this.to.isChanged()
				|| this.step.isChanged()
				|| super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.var.saved();
		this.from.saved();
		this.to.saved();
		this.step.saved();
	}
	//endregion

	//region override from MatrixItem
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, super.getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.For.get(), context.getFactory().getSettings());
		driver.showTextBox(this, layout, 1, 1, this.var, this.var, this.var::get, null);
		driver.showTitle(this, layout, 1, 2, Tokens.From.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 3, Tokens.From.get(), this.from, this.from, null, null, null, null);
		driver.showTitle(this, layout, 1, 4, Tokens.To.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 5, Tokens.To.get(), this.to, this.to, null, null, null, null);
		driver.showTitle(this, layout, 1, 6, Tokens.Step.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 7, Tokens.Step.get(), this.step, this.step, null, null, null, null);

		return layout;
	}

	@Override
	public String getItemName()
	{
		return super.getItemName()
				+ " "
				+ (this.var.get().equals("") ? this.var : this.var + " = ")
				+ (this.from.getExpression() == null ? "" : this.from + " ")
				+ (this.to.getExpression() == null ? "" : this.to + " ")
				+ (this.step.getExpression() == null ? "" : this.step);
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.var.accept(systemParameters.get(Tokens.For));
		this.from.setExpression(systemParameters.get(Tokens.From)); 
		this.to.setExpression(systemParameters.get(Tokens.To)); 
		this.step.setExpression(systemParameters.get(Tokens.Step)); 
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.For.get(), this.var.get());
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.From.get(), this.from.getExpression());
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.To.get(), this.to.getExpression());
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Step.get(), this.step.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(this.var.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(Tokens.For.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(Tokens.From.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(Tokens.Step.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.from.getExpression(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.to.getExpression(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.step.getExpression(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, TypeMandatory.System, Tokens.EndFor.get());
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		super.checkItSelf(context, evaluator, listener, parameters);
		super.checkValidId(this.var, listener);
		this.from.prepareAndCheck(evaluator, listener, this);
		this.to.prepareAndCheck(evaluator, listener, this);
		this.step.prepareAndCheck(evaluator, listener, this);
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			ReturnAndResult returnAndResult;
			Result result;

			if (!this.from.evaluate(evaluator))
			{
				return super.createReturn("Error in expression #From", listener, start);
			}
			if (!this.to.evaluate(evaluator))
			{
				return super.createReturn("Error in expression #To", listener, start);
			}
			this.setDefaultStep();
			if (!this.step.evaluate(evaluator))
			{
				return super.createReturn("Error in expression #Step", listener, start);
			}

			Object fromValue = this.from.getValue();
			Object toValue = this.to.getValue();
			Object stepValue = this.step.getValue();
			if (!(fromValue instanceof Number))
			{
				return super.createReturn("#From is not type of Number", listener, start);
			}
			if (!(toValue instanceof Number))
			{
				return super.createReturn("#To is not type of Number", listener, start);
			}
			if (!(stepValue instanceof Number))
			{
				return super.createReturn("#Step is not type of Number", listener, start);
			}
			
			// start value
			AtomicReference<Number> current = new AtomicReference<>((Number)fromValue);
			
			for (this.setCurrent(current, evaluator);
				this.checkCondition(current, toValue, stepValue, evaluator);
				this.changeCurrent(current, stepValue, evaluator)
				)
			{
				report.outLine(this, null, String.format("loop %s = %s", this.var, current.get()), current.get().intValue());

				returnAndResult = super.executeChildren(start, context, listener, evaluator, report, new Class<?>[] { OnError.class });
				result = returnAndResult.getResult();

				if (result.isFail())
				{
					MatrixItem branchOnError = super.find(false, OnError.class, null);
					if (branchOnError != null && branchOnError instanceof OnError)
					{
						((OnError) branchOnError).setError(returnAndResult.getError());

						returnAndResult = branchOnError.execute(context, listener, evaluator, report);
						result = returnAndResult.getResult();
					}
					else
					{
						return returnAndResult;
					}
				}

				if (result == Result.Break)
				{
					break;
				}
				if (result.isFail())
				{
					return new ReturnAndResult(start, returnAndResult.getError(), result);
				}
				if (result == Result.Stopped || result == Result.Return)
				{
					return new ReturnAndResult(start, result, returnAndResult.getOut());
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
	private void setCurrent(AtomicReference<Number> current, AbstractEvaluator evaluator)
	{
		Number currentValue = current.get();
		evaluator.getLocals().set(this.var.get(), currentValue);
	}

	private boolean checkCondition(AtomicReference<Number> current, Object toValue, Object stepValue, AbstractEvaluator evaluator)
	{
		Number currentValue = current.get();

		return ((Number) stepValue).intValue() > 0 ? currentValue.intValue() <= ((Number) toValue).intValue() : currentValue.intValue() >= ((Number) toValue).intValue();
	}

	private void changeCurrent(AtomicReference<Number> current, Object stepValue, AbstractEvaluator evaluator)
	{
		Number currentValue = current.get();
		currentValue = currentValue.intValue() + ((Number) stepValue).intValue();
		current.set(currentValue);

		this.setCurrent(current, evaluator);
	}

	private void setDefaultStep()
	{
		if (Str.IsNullOrEmpty(this.step.getExpression()))
		{
			this.step.setExpression("1");
		}
	}
	//endregion
}
