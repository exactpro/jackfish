////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;

import java.util.List;

@MatrixItemAttribute(
		description 	= "Executed if condition is false.", 
		shouldContain 	= { Tokens.Else },
		mayContain 		= { Tokens.Off, Tokens.RepOff }, 
		parents			= { If.class }, 
        real			= true,
		hasValue 		= false, 
		hasParameters 	= false,
        hasChildren 	= true
	)
public class Else extends MatrixItem
{
    public Else()
    {
        super();
    }

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.Else.get(), context.getFactory().getSettings());

		return layout;
	}

    @Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
        ReportTable table;
        table = report.addTable("", null, true, 100,
                new int[] { 30, 70 }, new String[] { "Chapter", "Description"});

        table.addValues("Destination", "If's alternative branch");
        table.addValues("Examples", "<code>#Else</code>");
        table.addValues("See also", "If");
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, Tokens.Else.get());
	}

    @Override
    protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
    {
        return SearchHelper.matches(Tokens.Else.get(), what, caseSensitive, wholeWord);
    }
}
