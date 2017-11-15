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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import java.util.List;

@MatrixItemAttribute(
		description 	= "This operator is used to break cycles  for, forEach and while.",
		examples 		= "Create cycle from 1 to 10 using an operator For, cycle when variable is a = 9 break a cycle using an operator Break." +
							"{{#\n" +
							"#For;#From;#To;#Step \n" +
							"a;1;10;1 \n" +
							"#If \n" +
							"a == 9 \n" +
							"#Break \n" +
							"#EndIf \n" +
							"#EndFor#}}",
		shouldContain 	= { Tokens.Break },
		mayContain 		= { Tokens.Off, Tokens.RepOff },
		parents			= { Case.class, Else.class, For.class, ForEach.class, If.class,
							OnError.class, Step.class, SubCase.class, TestCase.class, While.class },
		real			= true,
		hasValue 		= false,
		hasParameters 	= false,
        hasChildren 	= false,
		seeAlsoClass 	= {For.class, While.class, Continue.class}
)
public class Break extends MatrixItem
{
    public Break()
    {
        super();
    }

	@Override
	protected MatrixItem makeCopy()
	{
		return new Break();
	}

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.Break.get(), context.getFactory().getSettings());

		return layout;
	}

	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
		return new ReturnAndResult(start, Result.Break);
	}

    @Override
    protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
    {
        super.addParameter(firstLine, TypeMandatory.System, Tokens.Break.get());
    }

    @Override
    protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
    {
        return SearchHelper.matches(Tokens.Break.get(), what, caseSensitive, wholeWord);
    }
}
