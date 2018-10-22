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

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.RawTable;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.Tables,
		suffix					   = "TBL",
		constantGeneralDescription = R.TABLE_LOAD_FROM_FILE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.TABLE_LOAD_FROM_FILE_OUTPUT_DESC,
		outputType				   = Table.class,
		constantExamples 		   = R.TABLE_LOAD_FROM_FILE_EXAMPLE
,
		seeAlsoClass = {RawTable.class, TableLoadFromDir.class, TableCreate.class, TableSelect.class}
	)
public class TableLoadFromFile extends AbstractAction 
{
	public static final String fileName      = "File";
	public static final String delimiterName = "Delimiter";

	@ActionFieldAttribute(name = fileName, mandatory = true, constantDescription = R.TABLE_LOAD_FROM_FILE_FILE)
	protected String file;

	@ActionFieldAttribute(name = delimiterName, mandatory = false, def = DefaultValuePool.Semicolon, constantDescription = R.TABLE_LOAD_FROM_FILE_DELIMITER)
	protected String delimiter;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case fileName:
				return HelpKind.ChooseOpenFile;

			case delimiterName:
				return HelpKind.ChooseFromList;
		}

		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case delimiterName:
				list.add(new ReadableValue(context.getEvaluator().createString(",")));
				list.add(new ReadableValue(context.getEvaluator().createString(";")));
				break;

			default:
		}
	}


	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(new Table(this.file, this.delimiter.charAt(0), evaluator));
	}
}
