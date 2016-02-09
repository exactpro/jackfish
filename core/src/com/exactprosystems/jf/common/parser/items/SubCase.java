////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.parser.*;
import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MatrixItemAttribute(
        description 	= "Subroutine.",
        shouldContain 	= { Tokens.SubCase },
        mayContain 		= { Tokens.Id, Tokens.Off },
        real			= true,
        hasValue 		= true,
        hasParameters 	= true,
        hasChildren 	= true
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
		driver.showTitle(this, layout, 1, 1, Tokens.SubCase.get(), context.getConfiguration().getSettings());
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
		return super.getItemName() + " " + (this.name == null || this.name.isNullOrEmpty() ? "" : "(" + this.name + ")");
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
		report.reportSwitch(true);
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
	{
		//TODO check, that subcase don't contains item call, that execute current subcase ( overwise we will have stackoverflow)
		ids = new HashSet<String>();
		
		super.checkItSelf(context, evaluator, listener, ids, parameters);
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
        ReportTable table;
        table = report.addTable("", 100, new int[] { 30, 70 },
                new String[] { "Chapter", "Description"});

        table.addValues("Destination", "SubCase is needed to make script shorter.");
        table.addValues("Examples", "<code>#Id;#SubCase<p>ID;Description</code>");
        table.addValues("See also", "<code>EndSubCase, Return</code>");
	}

	@Override
	protected ReturnAndResult executeItSelf(Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		if (!this.call)
		{
			return new ReturnAndResult(Result.NotExecuted);
		}

		try
		{
			this.locals = evaluator.createLocals();

			this.locals.set(parameters);

			reportParameters(report, parameters);
			report.itemIntermediate(this);

			Variables oldLocals = evaluator.getLocals();
			ReturnAndResult res = executeChildren(context, listener, evaluator, report, new Class<?>[] { OnError.class }, this.locals);
			evaluator.setLocals(oldLocals);

			return res;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			listener.error(this.owner, getNumber(), this, e.getMessage());
			return new ReturnAndResult(Result.Failed, null, e.getMessage());
		}
		finally
		{
			this.call = false;
		}
	}

	@Override
	protected void afterReport(ReportBuilder report)
	{
		report.reportSwitch(true);
	}

	// ==============================================================================================
	// Private members
	// ==============================================================================================
	private void reportParameters(ReportBuilder report, Parameters parameters)
	{
		ReportTable table = report.addTable("Input parameters", 2, new int[] { 20, 40, 40 }, new String[] { "Parameter", "Expression", "Value" });

		for (Parameter parameter : parameters)
		{
			table.addValues(parameter.getName(), parameter.getExpression(), parameter.getValue());
		}
	}

	private Variables	locals	= null;

	private boolean	call	= false;

	private MutableValue<String> name;
}
