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
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;
import java.util.Map;

@MatrixItemAttribute(
		constantGeneralDescription = R.LET_DESCRIPTION,
		constantExamples = R.LET_EXAMPLE,
		shouldContain 	= { Tokens.Let },
		mayContain 		= { Tokens.Id, Tokens.Off, Tokens.RepOff, Tokens.Global }, 
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= false
	)
public class Let extends MatrixItem
{
	private final Parameter value;

	public Let()
	{
		super();
		this.value = new Parameter(Tokens.Let.get(), null);
	}

	/**
	 * copy constructor
	 */
	public Let(Let let)
	{
		this.value = new Parameter(let.value);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new Let(this);
	}

	//region Interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.value.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.value.saved();
	}
	//endregion

	//region override from MatrixItem
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		
		driver.showComment	(this, layout, 0, 0, getComments());
		driver.showCheckBox	(this, layout, 1, 0, "Global", this.global, this.global);
		driver.showTextBox	(this, layout, 1, 1, this.id::set, this.id::get, this.id::get);
		driver.showTitle	(this, layout, 1, 2, Tokens.Let.get(), context.getFactory().getSettings());
		driver.showExpressionField(this, layout, 1, 3, Tokens.Let.get(), this.value, this.value, null, null, null, null);

		return layout;
	}

    @Override
	public String getItemName()
	{
		return super.getItemName() + " " + (this.value.getExpression() == null ? "" : this.value.getExpression());
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.value.setExpression(systemParameters.get(Tokens.Let));
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		// do not call super.checkItSelf(...) because id may be the same for several Let items.
		super.checkValidId(this.id, listener);
		this.value.prepareAndCheck(evaluator, listener, this);
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			this.value.evaluate(evaluator);
			if (!this.value.isValid())
			{
				ReportTable table = report.addTable("Let", null, true, true, new int[] {50, 50}, "Expression", "Error");

				String msg = "Error in expression #Let";
				table.addValues(this.value.getExpression(), msg);
				table.addValues(this.value.getValueAsString(), " <- Error in here");

				return super.createReturn(msg, listener, start);
			}

			Object val = this.value.getValue();
			Variables vars = super.isGlobal() ? evaluator.getGlobals() : evaluator.getLocals();
			if (!Str.IsNullOrEmpty(super.getId()))
			{
				vars.set(super.getId(), val);
			}

			ReportTable table = report.addTable("Let", null, true, true, new int[] {50, 50}, "Expression", "Value");

			table.addValues(this.value.getExpression(), val);
			report.itemIntermediate(this);

			return new ReturnAndResult(start, Result.Passed, val);
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
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.Let.get(), this.value.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Let.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.value.getExpression(), what, caseSensitive, wholeWord);
	}
	//endregion
}

