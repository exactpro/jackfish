////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.*;
import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;

import java.util.List;
import java.util.Map;
import java.util.Set;

@MatrixItemAttribute(
		description 	= "Choices one variant in a switch.", 
		shouldContain 	= { Tokens.Case },
		mayContain 		= { Tokens.Off }, 
        closes			= Switch.class,
        real			= true,
		hasValue 		= true, 
		hasParameters 	= false,
        hasChildren 	= true
	)
public class Case extends MatrixItem
{
	public Case()
	{
		super();
		this.variant = new Parameter(Tokens.Case.get(),	null); 
	}
	
	@Override
	public MatrixItem clone() throws CloneNotSupportedException
	{
		Case clone = (Case) super.clone();
		clone.variant = variant.clone();
		return clone;
	}

	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
    @Override
    public boolean isChanged()
    {
    	if (this.variant.isChanged())
    	{
    		return true;
    	}
    	return super.isChanged();
    }

    @Override
    public void saved()
    {
    	super.saved();
    	this.variant.saved();
    }

    //==============================================================================================
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.Case.get(), context.getConfiguration().getSettings());
		driver.showExpressionField(this, layout, 1, 1, Tokens.Case.get(), this.variant, this.variant, null, null, null, null);

		return layout;
	}

	@Override
	public String getItemName()
	{
		return super.getItemName() + " " + this.variant;
	}
	
	@Override
	protected void initItSelf(Map<Tokens, String> systemParameters) throws MatrixException
	{
		this.variant.setExpression(systemParameters.get(Tokens.Case));
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, secondLine, Tokens.Case.get(), this.variant.getExpression());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Case.get(), what, caseSensitive, wholeWord) ||
				SearchHelper.matches(this.variant.getExpression(), what, caseSensitive, wholeWord);
	}

	@Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
        ReportTable table;
        table = report.addTable("", 100, new int[] { 30, 70 },
                new String[] { "Chapter", "Description"});

        table.addValues("Destination", "Switch's branch");
        table.addValues("Examples", "<code>#Case</code>");
        table.addValues("See also", "Switch");
	}

    @Override
    protected void checkItSelf(Context context, AbstractEvaluator evaluator, IMatrixListener listener, Set<String> ids, Parameters parameters)
    {
        super.checkItSelf(context, evaluator, listener, ids, parameters);
        this.variant.prepareAndCheck(evaluator, listener, this);
    }
    
	public Parameter getVariant()
	{
		return this.variant;
	}

	private Parameter variant;
}
