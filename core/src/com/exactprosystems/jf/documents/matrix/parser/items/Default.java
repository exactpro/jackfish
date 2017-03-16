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
		description 	= "Executed if all cases are not suitable.",
		examples 		= "{{##Default#}}",
		seeAlso 		= "For, While, Break",
		shouldContain 	= { Tokens.Default },
		mayContain 		= { Tokens.Off, Tokens.RepOff }, 
		parents			= { Switch.class },
        closes			= Switch.class,
        real 			= true,
		hasValue 		= false, 
		hasParameters 	= false,
        hasChildren 	= true,
		seeAlsoClass 	= {For.class, While.class, Break.class}
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
		driver.showTitle(this, layout, 1, 0, Tokens.Default.get(), context.getFactory().getSettings());

		return layout;
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

