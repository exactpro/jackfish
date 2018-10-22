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

package com.exactprosystems.jf.documents.matrix.parser.items.help;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.ReturnAndResult;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.functions.Table;

import java.util.Collections;


public class HelpTable extends MatrixItem
{
	private String str;
	private Table table;
	private int[]  widths;
	private boolean bordered;

    public HelpTable(String str, Table table, boolean bordered, int[] widths)
    {
        this.str = str;
        this.table = table;
        this.widths = widths;
        this.bordered = bordered;
    }

    public HelpTable(String str, Table table, int[] widths)
	{
        this(str, table, false, widths);
	}

	/**
	 * copy constructor
	 */
	public HelpTable(HelpTable helpTable)
	{
		this.str = helpTable.str;
		this.table = new Table(helpTable.table);
		this.widths = new int[helpTable.widths.length];
		System.arraycopy(helpTable.widths, 0, this.widths, 0, helpTable.widths.length);
		this.bordered = helpTable.bordered;
	}

	@Override
	protected MatrixItem makeCopy()
	{
		return new HelpTable(this);
	}

	@Override
	public String getItemName()
	{
		return this.str;
	}
	
	@Override
	protected ReturnAndResult executeItSelf(long start, Context context, IMatrixListener listener, AbstractEvaluator evaluator, ReportBuilder report, Parameters parameters)
	{
        try
        {
            this.table.report(report, this.str, null, false, true, this.bordered, Collections.emptyMap(), this.widths);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            return new ReturnAndResult(start, Result.Failed, e.getMessage(), ErrorKind.EXCEPTION, this);
        }
        return new ReturnAndResult(start, Result.Passed); 
	}
}
