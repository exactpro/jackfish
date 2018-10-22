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

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.RawTable;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					      = ActionGroups.Tables,
		suffix					      = "TBL",
		constantGeneralDescription    = R.TABLE_CREATE_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
		constantAdditionalDescription = R.TABLE_CREATE_ADDITIONAL_DESC,
		constantOutputDescription 	  = R.TABLE_CREATE_OUTPUT_DESC,
		outputType				      = Table.class,
		constantExamples 			  = R.TABLE_CREATE_EXAMPLE,
		seeAlsoClass = {RawTable.class, TableLoadFromDir.class, TableLoadFromFile.class, TableSelect.class}
	)
public class TableCreate extends AbstractAction 
{
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		String[] headers = parameters.select(TypeMandatory.Extra).keySet().toArray(new String[] {});

		for (String columnName : headers)
		{
			if(columnName.isEmpty())
			{
				super.setError("The column name does not have to contain an empty value.", ErrorKind.EMPTY_PARAMETER);
				return;
			}
		}
		
		super.setResult(new Table(headers, evaluator));
	}
}
