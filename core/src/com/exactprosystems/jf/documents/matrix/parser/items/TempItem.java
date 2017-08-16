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
import com.exactprosystems.jf.documents.guidic.controls.Table;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.functions.Text;

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
				Tokens.Default.get(), Tokens.Fail.get(), Tokens.RawTable.get(), 
				Tokens.RawMessage.get(), Tokens.RawText.get(), Tokens.NameSpace.get(), Tokens.Let.get(), Tokens.Step.get(),
				Tokens.Assert.get(), Tokens.SetHandler.get() ));
	}

	private boolean isInit = false;

	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		Object layout = driver.createLayout(this, 1);
		driver.showAutoCompleteBox(this, layout, 0, 0, list, s -> {
			if (!isInit)
			{
		        MatrixItem parent = this.getParent();
		        int index = parent.index(this);
		        MatrixItem newItem = null;
		        try
		        {
		            if (Tokens.containsIgnoreCase(s))
		            {
		                if (s.equalsIgnoreCase(Tokens.RawTable.get()))
		                {
		                    newItem = Parser.createItem(Tokens.RawTable.get(), Table.class.getSimpleName());
		                }
		                else if (s.equalsIgnoreCase(Tokens.RawMessage.get()))
		                {
		                    newItem = Parser.createItem(Tokens.RawMessage.get(), "none");
		                }
		                else if (s.equalsIgnoreCase(Tokens.RawText.get()))
		                {
		                    newItem = Parser.createItem(Tokens.RawText.get(), Text.class.getSimpleName());
		                }
		                else
		                {
		                    newItem = Parser.createItem(s, null);
		                }
		            }
		            else
		            {
		                newItem = Parser.createItem(Tokens.Action.get(), s);
		            }
		            newItem.init(getMatrix(), getMatrix());
		            newItem.createId();
		            this.getParent().insert(index, newItem);
		            newItem.display(driver, context);
		        }
		        catch (Exception e)
		        {
		            //          DialogsHelper.showError(e.getMessage());
		        }
		        finally
		        {
		            this.remove();
		            driver.deleteItem(this);
		            getMatrix().enumerate();
		        }

		        getMatrix().getChangedProperty().set(true);
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
