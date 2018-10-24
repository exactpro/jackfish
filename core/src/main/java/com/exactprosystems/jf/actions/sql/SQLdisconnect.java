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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.sql.SqlConnection;

@ActionAttribute(
		group					   = ActionGroups.SQL,
		suffix					   = "SQLDCNT",
		constantGeneralDescription = R.SQL_DISCONNECT_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.SQL_DISCONNECT_EXAMPLE,
		seeAlsoClass 			   = {SQLexecute.class, SQLinsert.class, SQLselect.class, SQLtableUpload.class, SQLconnect.class}
	)
public class SQLdisconnect extends AbstractAction
{
	public static final String connectionName = "Connection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.SQL_DISCONNECT_CONNECTION)
	protected SqlConnection connection;

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		context.getConfiguration().getDataBasesPool().disconnect(this.connection);
		super.setResult(null);
	}
}
