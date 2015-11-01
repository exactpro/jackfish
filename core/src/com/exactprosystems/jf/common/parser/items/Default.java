package com.exactprosystems.jf.common.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.parser.*;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;

import java.util.List;

@MatrixItemAttribute(
		description 	= "Executed if all cases are not suitable.", 
		shouldContain 	= { Tokens.Default },
		mayContain 		= { Tokens.Off }, 
        closes			= Switch.class,
        real 			= true,
		hasValue 		= false, 
		hasParameters 	= false,
        hasChildren 	= true
	)
public class Default extends MatrixItem
{
    public Default()
    {
        super();
    }

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.Default.get());

		return layout;
	}

    @Override
	protected void docItSelf(Context context, ReportBuilder report)
	{
        ReportTable table;
        table = report.addTable("", 100, new int[] { 30, 70 },
                new String[] { "Chapter", "Description"});

        table.addValues("Destination", "Switch's alternative branch");
        table.addValues("Examples", "<code>#Default</code>");
        table.addValues("See also", "Switch");
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, Tokens.Default.get());
	}

    @Override
    protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
    {
        return SearchHelper.matches(Tokens.Default.get(), what, caseSensitive, wholeWord);
    }
}

