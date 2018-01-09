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
		constantGeneralDescription = R.IF_DESCRIPTION,
		constantExamples = R.IF_EXAMPLE,
		shouldContain 	= { Tokens.If },
		mayContain 		= { Tokens.Off, Tokens.RepOff },
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real			= true,
		hasValue 		= true,
		hasParameters 	= false,
		hasChildren 	= true,
		seeAlsoClass 	= {Else.class}
)
public class If extends MatrixItem
{
	private final Parameter condition;

	public If()
	{
		super();
		this.condition = new Parameter(Tokens.If.get(), null);
	}

	public If(If oldIf)
	{
		this.condition = new Parameter(oldIf.condition);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new If(this);
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
		driver.showTitle(this, layout, 1, 0, Tokens.If.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 1, Tokens.If.get(), this.condition, this.condition, null, null, null, null);

		return layout;
	}

	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + (this.condition.getExpression() == null ? "" : this.condition);
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.condition.setExpression(systemParameters.get(Tokens.If));
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.If.get(), this.condition.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.If.get(), what, caseSensitive, wholeWord) || SearchHelper.matches(this.condition.getExpression(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, TypeMandatory.System, Tokens.EndIf.get());
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		super.checkItSelf(context, evaluator, listener, parameters);
		this.condition.prepareAndCheck(evaluator, listener, this);
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			this.condition.evaluate(evaluator);
			if (!this.condition.isValid())
			{
				ReportTable table = report.addTable("If", null, true, true, new int[]{50, 50}, "Expression", "Error");

				String msg = String.format(R.IF_EXPRESSION_EXCEPTION.get(), this.condition.getValueAsString());
				table.addValues(this.condition.getExpression(), msg);
				return super.createReturn(msg, listener, start);
			}

			Object eval = this.condition.getValue();
			if (eval instanceof Boolean)
			{
				Boolean bool = (Boolean) eval;
				if (bool)
				{
					return super.executeChildren(start, context, listener, evaluator, report, new Class<?>[]{Else.class});
				}
				else
				{
					MatrixItem branchElse = super.find(false, Else.class, null);
					if (branchElse != null)
					{
						ReturnAndResult elseResult = branchElse.execute(context, listener, evaluator, report);
						return new ReturnAndResult(start, elseResult);
					}
					return new ReturnAndResult(start, Result.Passed);
				}
			}
			return super.createReturn(R.COMMON_RESULT_IS_NOT_BOOLEAN.get(), listener, start);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return super.createReturn(e.getMessage(), listener, start);
		}
	}

	//endregion
}