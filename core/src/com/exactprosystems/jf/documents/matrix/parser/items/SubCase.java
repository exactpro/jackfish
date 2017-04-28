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
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
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
        description 	= "This operator describes a subprogram. In parameters the program arguments are described, factual parameters are used with Call. \n" +
							"SubCase is performed by the Call operator only." +
							" SubCase operator can even be located in commented testcases.",
		examples 		= "{{##Id;#SubCase\n" +
							"SUB_1;\n" +
							"#Action;#Greeting\n" +
							"Print;'Hello!'\n" +
							"#EndSubCase#}}",
		seeAlso 		= "Call, Return",
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
	public SubCase()
	{
		super();
		this.name = new MutableValue<String>();
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		SubCase clone = (SubCase) super.clone();
		clone.call = call;
		clone.name = name.clone();
		return clone;
	}

	
	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (this.name.isChanged())
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.name.saved();
    }

	//==============================================================================================

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTextBox(this, layout, 1, 0, this.id, this.id, () -> this.id.get());
		driver.showTitle(this, layout, 1, 1, Tokens.SubCase.get(), context.getFactory().getSettings());
		driver.showTextBox(this, layout, 1, 2, this.name, this.name, null);
		driver.showParameters(this, layout, 1, 3, this.parameters, () -> "", true);

		return layout;
	}

	// ==============================================================================================
	// Getters / setters
	// ==============================================================================================
	public String getName()
	{
		return this.name.get();
	}

	public void setRealParameters(Parameters realParameters)
	{
		this.call = true;

		Parameters parametersSource = getParameters();

		for (Parameter entry : realParameters)
		{
			parametersSource.replaceIfExists(entry);
		}

	}

	// ==============================================================================================
	// Protected members should be overridden
	// ==============================================================================================
	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + (this.name == null || this.name.isNullOrEmpty() ? "" : "(" + this.name + ")") + (super.getId() == null || super.getId().isEmpty() ? "" : " ( id : " +
				super.getId() + " )");
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
		addParameter(firstLine, secondLine, Tokens.SubCase.get(), this.name.get());

		for (Parameter entry : getParameters())
		{
			super.addParameter(firstLine, secondLine, entry.getName(), entry.getExpression());
		}
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.SubCase.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(getId(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.name.get(), what, caseSensitive, wholeWord) ||
				getParameters().matches(what, caseSensitive, wholeWord);
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, Tokens.EndSubCase.get());
	}

	@Override
	protected void beforeReport(ReportBuilder report)
	{
	    super.beforeReport(report);
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Parameters parameters)
	{
		super.checkItSelf(context, evaluator, listener, parameters);
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
			evaluator.getLocals().set(parameters);

			reportParameters(report, parameters);
			report.itemIntermediate(this);

			ReturnAndResult ret = executeChildren(start, context, listener, evaluator, report, new Class<?>[] { OnError.class });

			Result result = ret.getResult();
			
			if (result.isFail())
			{
				MatrixItem branchOnError = super.find(false, OnError.class, null);
				if (branchOnError != null && branchOnError instanceof OnError)
				{
					((OnError)branchOnError).setError(ret.getError());
					
					ret = branchOnError.execute(context, listener, evaluator, report);
					result = ret.getResult();
				}
			}
			return new ReturnAndResult(start, ret);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
		}
		finally
		{
			this.call = false;
		}
	}

	@Override
	protected void afterReport(ReportBuilder report)
	{
	    super.afterReport(report);
	}

	// ==============================================================================================
	// Private members
	// ==============================================================================================
	private void reportParameters(ReportBuilder report, Parameters parameters)
	{
		ReportTable table = report.addTable("Input parameters", null, true, 2, new int[] { 20, 40, 40 }, new String[] { "Parameter", "Expression", "Value" });

		for (Parameter parameter : parameters)
		{
			table.addValues(parameter.getName(), parameter.getExpression(), parameter.getValue());
		}
	}

	private boolean	call	= false;

	private MutableValue<String> name;
}
