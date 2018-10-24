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

package com.exactprosystems.jf.tool.documents.csv;

import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.csv.Csv;
import com.exactprosystems.jf.tool.custom.grideditor.DataProvider;
import com.exactprosystems.jf.tool.custom.grideditor.TableDataProvider;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.scene.control.ButtonType;

import java.io.Reader;

public class CsvFx extends Csv
{
	private TableDataProvider provider;

	public CsvFx(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);
		this.provider = new TableDataProvider(super.table, this::addCommand);
	}

	public DataProvider<String> getProvider()
	{
		return provider;
	}

	//region Abstract document
	@Override
	public void display() throws Exception
	{
		super.display();
		this.provider.fire();
	}

	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
		this.provider.setTable(super.table);
	}

	@Override
	public boolean canClose() throws Exception
	{
		if (!super.canClose())
		{
			return false;
		}

		if (isChanged())
		{
			ButtonType desision = DialogsHelper.showSaveFileDialog(getNameProperty().get());
			if (desision == ButtonType.YES)
			{
				save(getNameProperty().get());
			}
			if (desision == ButtonType.CANCEL)
			{
				return false;
			}
		}

		return true;
	}
	//endregion
}
