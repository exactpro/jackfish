////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.actions.ActionsList;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@MatrixItemAttribute(
		description = "Temp action",
		shouldContain = {},
		mayContain = {},
		real = true,
		hasValue = false,
		hasParameters = false,
		hasChildren = false)
public class TempItem extends MatrixItem
{
	private static ArrayList<String> list = new ArrayList<>();

	static
	{
		list.addAll(Arrays.stream(ActionsList.actions).map(Class::getSimpleName).collect(Collectors.toList()));
		list.addAll(Arrays.asList(Tokens.TestCase.get(), Tokens.SubCase.get(), Tokens.Return.get(), Tokens.Call.get(), 
				Tokens.If.get(), Tokens.Else.get(), Tokens.For.get(), Tokens.ForEach.get(), Tokens.While.get(), 
				Tokens.Continue.get(), Tokens.Break.get(), Tokens.OnError.get(), Tokens.Switch.get(), Tokens.Case.get(), 
				Tokens.Default.get(), Tokens.ReportOff.get(), Tokens.ReportOn.get(), Tokens.Fail.get(), Tokens.RawTable.get(), 
				Tokens.RawMessage.get(), Tokens.RawText.get(), Tokens.NameSpace.get(), Tokens.Let.get() ));
	}

	private boolean isInit = false;

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 1);
		driver.showAutoCompleteBox(this, layout, 0, 0, list, s -> {
			if (!isInit)
			{
				getMatrix().replace(this, s);
				this.isInit = true;
			}
		});
		return layout;
	}

	@Override
	public boolean isChanged()
	{
		return false;
	}
}
