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
		description 	= "This operator describes a line of the operator Switch, which wil be performed if none of Case has been worked. \n" +
							"This operator is 1 only  and should be necessarily placed after all Case blocks.",
		examples 		= "Variable number is transferred to the field switch. As variable value doesn’t match any Case," +
							" block Default will be performed." +
							"{{# #Id;#Let\n" +
							"number;0\n" +
							"#Switch\n" +
							"number\n" +
							"#Case\n" +
							"1\n" +
							"#Action;#Greeting\n" +
							"Print;'Hello!'\n" +
							"#Case\n" +
							"2\n" +
							"#Action;#Greeting\n" +
							"Print;'Hi!'\n" +
							"#Default\n" +
							"#Action;#Greeting\n" +
							"Print;'Farewell'\n" +
							"#EndSwitch#}}",
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
		super.addParameter(firstLine, TypeMandatory.System, Tokens.Default.get());
	}

    @Override
    protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
    {
        return SearchHelper.matches(Tokens.Default.get(), what, caseSensitive, wholeWord);
    }
}

