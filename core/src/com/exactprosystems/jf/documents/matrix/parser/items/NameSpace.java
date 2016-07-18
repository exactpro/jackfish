////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@MatrixItemAttribute(
        description 	= "Namespace.",
        shouldContain 	= { Tokens.NameSpace },
        mayContain 		= { Tokens.Id, Tokens.Off },
        real			= true,
        hasValue 		= true,
        hasParameters 	= false,
        hasChildren 	= true
)
public final class NameSpace extends MatrixItem
{
	public NameSpace()
	{
		super();
		this.name = new MutableValue<String>();
	}

	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		NameSpace clone = (NameSpace) super.clone();
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
		driver.showTitle(this, layout, 1, 1, Tokens.NameSpace.get(), context.getFactory().getSettings());
		driver.showTextBox(this, layout, 1, 2, this.name, this.name, null);

		return layout;
	}

	// ==============================================================================================
	// Getters / setters
	// ==============================================================================================
	public String getName()
	{
		return this.name.get();
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
		this.name.set(systemParameters.get(Tokens.NameSpace));
	}

	@Override
	protected String itemSuffixSelf()
	{
		return "";
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		addParameter(firstLine, secondLine, Tokens.NameSpace.get(), this.name.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.NameSpace.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(getId(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.name.get(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void writeSuffixItSelf(CsvWriter writer, List<String> line, String indent)
	{
		super.addParameter(line, Tokens.EndNameSpace.get());
	}

	@Override
	protected void beforeReport(ReportBuilder report)
	{
	}

	@Override
	protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
	{
		//TODO check, that NameSpace don't contains item call, that execute current NameSpace ( overwise we will have stackoverflow)
		ids = new HashSet<String>();
		
		super.checkItSelf(context, evaluator, listener, ids, parameters);
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
        ReportTable table;
        table = report.addTable("", true, 100,
                new int[] { 30, 70 }, new String[] { "Chapter", "Description"});

        table.addValues("Destination", "NameSpace is needed to make a library.");
        table.addValues("Examples", "<code>#Id;#NameSpace<p>NS;Description</code>");
        table.addValues("See also", "<code>EndNameSpace, SubCase, Call</code>");
	}

	@Override
	protected ReturnAndResult executeItSelf(Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		try
		{
			this.locals = evaluator.createLocals();

			this.locals.set(parameters);

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
	}

	@Override
	protected void afterReport(ReportBuilder report)
	{
	}

	// ==============================================================================================
	// Private members
	// ==============================================================================================

	private Variables	locals	= null;

	private MutableValue<String> name;
}
