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

package com.exactprosystems.jf.actions.sql;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.sql.SqlConnection;

@ActionAttribute(
		group						  = ActionGroups.SQL,
		suffix						  = "SQLEXEC",
		constantGeneralDescription    = R.SQL_EXECUTE_GENERAL_DESC,
		additionFieldsAllowed 		  = true,
		constantAdditionalDescription = R.SQL_EXECUTE_ADDITIONAL_DESC,
		outputType 					  = Boolean.class,
		constantOutputDescription     = R.SQL_EXECUTE_OUTPUT_DESC,
		constantExamples 			  = R.SQL_EXECUTE_EXAMPLE,
		seeAlsoClass 				  = {SQLdisconnect.class, SQLinsert.class, SQLselect.class, SQLtableUpload.class, SQLconnect.class}
	)
public class SQLexecute extends AbstractAction
{
	public static final String connectionName = "Connection";
	public static final String queryName      = "Query";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.SQL_EXECUTE_CONNECTION)
	protected SqlConnection connection;

	@ActionFieldAttribute(name = queryName, mandatory = true, constantDescription = R.SQL_EXECUTE_QUERY)
	protected String query;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return queryName.equals(fieldName) ? HelpKind.BuildQuery : null;
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection.isClosed())
		{
			super.setError("Connection is not established", ErrorKind.SQL_ERROR);
			return;
		}
		boolean result = context.getConfiguration().getDataBasesPool().execute(this.connection, this.query, parameters.select(TypeMandatory.Extra).values().toArray());
		super.setResult(result);
	}
}
