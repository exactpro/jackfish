////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
	public final static  String connectionName = "Connection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.SQL_DISCONNECT_CONNECTION)
	protected SqlConnection connection 		= null;

	@Override
	protected void doRealAction(Context context,
			ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null", ErrorKind.EMPTY_PARAMETER);
		}
		else
		{
			context.getConfiguration().getDataBasesPool().disconnect(this.connection);
			super.setResult(null);
		}
	}
}
