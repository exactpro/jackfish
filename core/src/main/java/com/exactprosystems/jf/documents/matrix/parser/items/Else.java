/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;

import java.util.List;

@MatrixItemAttribute(
		constantGeneralDescription = R.ELSE_DESCRIPTION,
		constantExamples = R.ELSE_EXAMPLE,
		shouldContain 	= { Tokens.Else },
		mayContain 		= { Tokens.Off, Tokens.RepOff }, 
		parents			= { If.class }, 
		real			= true,
		hasValue 		= false, 
		hasParameters 	= false,
		hasChildren 	= true,
		seeAlsoClass 	= {If.class}
	)
public class Else extends MatrixItem
{
	public Else()
	{
		super();
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new Else();
	}

	//region override from MatrixItem
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 2);
		driver.showComment(this, layout, 0, 0, super.getComments());
		driver.showTitle(this, layout, 1, 0, Tokens.Else.get(), context.getFactory().getSettings());
		return layout;
	}

	@Override
	protected void writePrefixItSelf(CsvWriter writer, List<String> firstLine, List<String> secondLine)
	{
		super.addParameter(firstLine, TypeMandatory.System, Tokens.Else.get());
	}

	@Override
	protected boolean matchesDerived(String what, boolean caseSensitive, boolean wholeWord)
	{
		return SearchHelper.matches(Tokens.Else.get(), what, caseSensitive, wholeWord);
	}
	//endregion
}
