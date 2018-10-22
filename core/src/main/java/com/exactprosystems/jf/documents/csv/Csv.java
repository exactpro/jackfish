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

package com.exactprosystems.jf.documents.csv;

import com.exactprosystems.jf.documents.AbstractDocument;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.functions.Table;

import java.io.Reader;

/**
 * Class for Csv documents.
 */
@DocumentInfo(
            kind = DocumentKind.CSV,
            newName = "Csv", 
            extension = "csv",
            description = "CSV"
        )
public class Csv extends AbstractDocument
{
	protected Table table;
	protected char tableDelimiter = ';';

	public Csv(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);

		this.table = new Table(new String[][]
				{
					new String[] { "<none>" },
					new String[] { "" 		},
				}, null);
		this.table.saved();
	}

	//region AbstractDocument
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
		this.table.fillFromTable(new Table(reader, this.tableDelimiter, null));
		this.table.saved();
	}

	@Override
	public boolean canClose() throws Exception
	{
		return true;
	}

	@Override
	public void save(String fileName) throws Exception
	{
		this.table.save(fileName, tableDelimiter, false, false);
		super.save(fileName);
	}

	//endregion

	//region interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.table.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		this.table.saved();
		super.saved();
	}

	//endregion

	public void setDelimiter(char delimiter)
	{
		this.tableDelimiter = delimiter;
	}
}
