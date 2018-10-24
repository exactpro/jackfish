/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.actions.ActionsList;
import com.exactprosystems.jf.api.common.i18n.R;
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
		constantGeneralDescription = R.TEMP_ITEM_DESCRIPTION,
		shouldContain = {},
		mayContain = {},
		real = true,
		hasValue = false,
		hasParameters = false,
		hasChildren = false)
public class TempItem extends MatrixItem
{
	private             ArrayList<String> list   = new ArrayList<>();
	private             boolean           isInit = false;
	public static final String            CALL   = "Call ";

	@Override
	protected MatrixItem makeCopy()
	{
		return new TempItem();
	}

	//region override from MatrixItem
	@Override
	protected Object displayYourself(DisplayDriver driver, Context context)
	{
		this.fillList(context);

		Object layout = driver.createLayout(this, 1);
		driver.showAutoCompleteBox(this, layout, 0, 0, () -> list,  () -> "", s -> {
			if (!this.isInit)
			{
				MatrixItem parent = super.getParent();
				int index = parent.index(this);
				MatrixItem newItem;
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
					else if (s.startsWith(CALL))
					{
						String name = s.substring(CALL.length(), s.length());

						newItem = Parser.createItem(Tokens.Call.get(), name);
						((Call) newItem).updateReference(context, name);
					}
					else
					{
						newItem = Parser.createItem(Tokens.Action.get(), s);
					}
					newItem.init(super.getMatrix(), super.getMatrix());
					newItem.createId();
					super.getSource().insert(parent, index, newItem);
					if (newItem instanceof Call)
					{
						((Call) newItem).updateReference(context, "" + newItem.get(Tokens.Call));
						newItem.addKnownParameters();
						super.getMatrix().setupCall(newItem, null, newItem.getParameters());
					}
					driver.setCurrentItem(newItem, super.getMatrix(), false);
				}
				catch (Exception ignored)
				{}
				finally
				{
					super.remove();
					driver.deleteItem(this);
					super.getMatrix().enumerate();
				}

				super.getMatrix().getChangedProperty().accept(true);
				this.isInit = true;
			}
		});
		return layout;
	}
	//endregion

	private void fillList(Context context)
	{
		list.addAll(Arrays.stream(ActionsList.actions).map(Class::getSimpleName).collect(Collectors.toList()));
		list.addAll(Arrays.asList(Tokens.TestCase.get(), Tokens.SubCase.get(), Tokens.Return.get(), Tokens.Call.get(),
				Tokens.If.get(), Tokens.Else.get(), Tokens.For.get(), Tokens.ForEach.get(), Tokens.While.get(),
				Tokens.Continue.get(), Tokens.Break.get(), Tokens.OnError.get(), Tokens.Switch.get(), Tokens.Case.get(),
				Tokens.Default.get(), Tokens.Fail.get(), Tokens.RawTable.get(),
				Tokens.RawMessage.get(), Tokens.RawText.get(), Tokens.NameSpace.get(), Tokens.Let.get(), Tokens.Step.get(),
				Tokens.Assert.get(), Tokens.SetHandler.get()));

		list.addAll(context.subcases(this).stream().map(readableValue -> CALL + readableValue.getValue()).collect(Collectors.toList()));
	}
}
