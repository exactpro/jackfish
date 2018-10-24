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

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.config.SqlEntry;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.sql.SqlConnection;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.SQL,
		suffix					   = "SQLCNT",
		constantGeneralDescription = R.SQL_CONNECT_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.SQL_CONNECT_OUTPUT_DESC,
		outputType				   = SqlConnection.class,
		constantExamples 		   = R.SQL_CONNECT_EXAMPLE,
		seeAlsoClass 			   = {SQLexecute.class, SQLinsert.class, SQLselect.class, SQLtableUpload.class, SQLdisconnect.class}
	)
public class SQLconnect  extends AbstractAction
{
	public static final String sqlName      = "Sql";
	public static final String serverName   = "Server";
	public static final String baseName     = "Base";
	public static final String userName     = "User";
	public static final String passwordName = "Password";

	@ActionFieldAttribute(name = sqlName, mandatory = true, constantDescription = R.SQL_CONNECT_SQL)
	protected String sql;

	@ActionFieldAttribute(name = serverName, mandatory = true, constantDescription = R.SQL_CONNECT_SERVER)
	protected String server;

	@ActionFieldAttribute(name = baseName, mandatory = true, constantDescription = R.SQL_CONNECT_BASE)
	protected String base;

	@ActionFieldAttribute(name = userName, mandatory = true, constantDescription = R.SQL_CONNECT_USER)
	protected String user;

	@ActionFieldAttribute(name = passwordName, mandatory = true, constantDescription = R.SQL_CONNECT_PASSWORD)
	protected String password;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		return sqlName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		AbstractEvaluator evaluator = context.getEvaluator();
		if (sqlName.equals(parameterToFill))
		{
			context.getConfiguration().getSqlEntries().stream()
					.map(SqlEntry::toString)
					.map(evaluator::createString)
					.map(ReadableValue::new)
					.forEach(list::add);
		}
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		SqlConnection result = context.getConfiguration().getDataBasesPool().connect(this.sql, this.server, this.base, this.user, this.password);

		if (result != null && !result.isClosed())
		{
			super.setResult(result);
		}
		else
		{
			super.setError("Can not connect to the data base", ErrorKind.SQL_ERROR);
		}
	}
}
