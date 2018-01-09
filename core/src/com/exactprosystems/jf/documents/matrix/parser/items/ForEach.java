////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@MatrixItemAttribute(
	constantGeneralDescription = R.FOREACH_DESCRIPTION,
	constantExamples = R.FOREACH_EXAMPLE,
	shouldContain 	= { Tokens.ForEach, Tokens.In },
	mayContain 		= { Tokens.Off, Tokens.RepOff }, 
	parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
						OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
	real			= true,
	hasValue 		= true, 
	hasParameters 	= false,
	hasChildren 	= true,
	seeAlsoClass 	= {For.class, While.class}
)
public final class ForEach extends MatrixItem
{
	private final MutableValue<String> var;
	private final Parameter            in;

	public ForEach()
	{
		super();
		this.var = new MutableValue<>();
		this.in = new Parameter(Tokens.In.get(), null);
	}

	/**
	 * copy constructor
	 */
	public ForEach(ForEach forEach)
	{
		this.var = new MutableValue<>(forEach.var);
		this.in = new Parameter(forEach.in);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new ForEach(this);
	}

	//region Interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.var.isChanged() || this.in.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.var.saved();
		this.in.saved();
	}

	//endregion

	//region override from MatrixItem
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, super.getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.ForEach.get(), context.getFactory().getSettings());
		driver.showTextBox(this, layout, 1, 1, this.var, this.var, this.var::get);
		driver.showTitle(this, layout, 1, 2, Tokens.In.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 3, Tokens.In.get(), this.in, this.in, null, null, null, null);

		return layout;
	}

	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + this.var + (this.in.getExpression() == null ? "" : " In " + this.in.getExpression());
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.var.accept(systemParameters.get(Tokens.ForEach));
		this.in.setExpression(systemParameters.get(Tokens.In));
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.ForEach.get(), this.var.get());
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.In.get(), this.in.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(this.var.get(), what, caseSensitive, wholeWord) 
				|| SearchHelper.matches(Tokens.ForEach.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(Tokens.In.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.in.getExpression(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, TypeMandatory.System, Tokens.EndForEach.get());
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		super.checkItSelf(context, evaluator, listener, parameters);
		super.checkValidId(this.var, listener);
		this.in.prepareAndCheck(evaluator, listener, this);
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			ReturnAndResult returnAndResult;
			Result result;

			if (!this.in.evaluate(evaluator))
			{
				return super.createReturn(String.format(R.FOREACH_EXCEPTION_IN.get(), this.in.getValueAsString()), listener, start);
			}

			Object inValue = this.in.getValue();
			if (!(inValue instanceof Iterable<?>))
			{
				return super.createReturn(R.FOREACH_EXCEPTION.get(), listener, start);
			}

			Iterator<?> iterator = ((Iterable<?>) inValue).iterator();
			int count = 0;
			AtomicReference<Object> current = new AtomicReference<>(null);

			while (this.checkCondition(iterator, current, evaluator))
			{
				report.outLine(this, null, String.format("loop %s = %s", this.var, Str.asString(current.get())), count++);

				returnAndResult = super.executeChildren(start, context, listener, evaluator, report, new Class<?>[]{OnError.class});
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

	private boolean checkCondition(Iterator<?> iterator, AtomicReference<Object> current, AbstractEvaluator evaluator)
	{
		boolean ret = iterator.hasNext();
		if (ret)
		{
			Object currentValue = iterator.next();
			current.set(currentValue);
			evaluator.getLocals().set(this.var.get(), currentValue);
		}
		return ret;
	}
	//endregion
}
