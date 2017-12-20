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
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;
import java.util.Map;

@MatrixItemAttribute(
        constantGeneralDescription = R.SUB_CASE_DESCRIPTION,
		constantExamples = R.SUB_CASE_EXAMPLE,
        shouldContain 	= { Tokens.SubCase },
        mayContain 		= { Tokens.Id, Tokens.Off, Tokens.RepOff },
        parents			= { TestCase.class, NameSpace.class },
        real			= true,
        hasValue 		= true,
        hasParameters 	= true,
        hasChildren 	= true,
		seeAlsoClass 	= {Call.class, Return.class}
)
public final class SubCase extends MatrixItem
{
	private boolean call = false;
	private final MutableValue<String> name;

	public SubCase()
	{
		super();
		this.name = new MutableValue<>();
	}

	public SubCase(SubCase subCase)
	{
		this.name = new MutableValue<>(subCase.name);
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new SubCase(this);
	}

	//region Interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.name.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.name.saved();
	}

	//endregion

	public void setRealParameters(Parameters realParameters)
	{
		this.call = true;
		Parameters parametersSource = super.getParameters();
		realParameters.forEach(parametersSource::replaceIfExists);
	}

	//region public Getters / setters
	public String getName()
	{
		return this.name.get();
	}

	@Override
	public Object get(Tokens key)
	{
		if (key == Tokens.SubCase)
		{
			return this.name.get();
		}
		return super.get(key);
	}

	@Override
	public void set(Tokens key, Object value)
	{
		if (key == Tokens.SubCase)
		{
			this.name.set((String) value);
		}
		else
		{
			super.set(key, value);
		}
	}
	//endregion

	//region override from MatrixItem
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, super.getComments());
		driver.showTextBox(this, layout, 1, 0, super.id, super.id, () -> super.id.get());
		driver.showTitle(this, layout, 1, 1, Tokens.SubCase.get(), context.getFactory().getSettings());
		driver.showTextBox(this, layout, 1, 2, this.name, this.name, null);
		driver.showParameters(this, layout, 1, 3, this.parameters, () -> "", true);

		return layout;
	}

	@Override
	public String getItemName()
	{
		return super.getItemName() + " "
				+ (this.name.isNullOrEmpty() ? "" : "(" + this.name + ")")
				+ (Str.IsNullOrEmpty(super.getId()) ? "" : " ( id : " + super.getId() + " )");
	}

	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters)
	{
		this.name.set(systemParameters.get(Tokens.SubCase));
	}

	@Override
	protected String itemSuffixSelf()
	{
		return "SUB_";
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, TypeMandatory.System, Tokens.SubCase.get(), this.name.get());
		super.getParameters().forEach(entry -> super.addParameter(firstLine, secondLine, TypeMandatory.Extra, entry.getName(), entry.getExpression()));
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.SubCase.get(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(getId(), what, caseSensitive, wholeWord)
				|| SearchHelper.matches(this.name.get(), what, caseSensitive, wholeWord)
				|| super.getParameters().matches(what, caseSensitive, wholeWord);
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, TypeMandatory.System, Tokens.EndSubCase.get());
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		if (!this.call)
		{
			return new ReturnAndResult(start, Result.NotExecuted);
		}
		try
		{
			evaluator.getLocals().clear();
			evaluator.getLocals().set(parameters.makeCopy());

			reportParameters(report, parameters);
			report.itemIntermediate(this);

			ReturnAndResult returnAndResult = super.executeChildren(start, context, listener, evaluator, report, new Class<?>[]{OnError.class});

			Result result = returnAndResult.getResult();

			if (result.isFail())
			{
				MatrixItem branchOnError = super.find(false, OnError.class, null);
				if (branchOnError != null && branchOnError instanceof OnError)
				{
					((OnError) branchOnError).setError(returnAndResult.getError());

					returnAndResult = branchOnError.execute(context, listener, evaluator, report);
					result = returnAndResult.getResult();
				}
			}
			if (result == Result.Return)
			{
				return new ReturnAndResult(start, returnAndResult);
			}
			else
			{
				return new ReturnAndResult(start, returnAndResult.getError(), result);
			}

		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return super.createReturn(e.getMessage(), listener, start);
		}
		finally
		{
			this.call = false;
		}
	}

	//endregion

	//region private methods
	private void reportParameters(ReportBuilder report, Parameters parameters)
	{
		ReportTable table = report.addTable("Input parameters", null, true, true, new int[] { 20, 40, 40 }, "Parameter", "Expression", "Value");
		parameters.forEach(parameter -> table.addValues(parameter.getName(), parameter.getExpression(), parameter.getValue()));
	}

	//endregion
}
