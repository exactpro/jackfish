////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.sql;

import java.sql.SQLException;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.sql.SqlConnection;

@ActionAttribute(
		group					= ActionGroups.SQL,
		suffix					= "SQLDCNT",
		generalDescription 		= "The following action is needed to close a database connection.",
		additionFieldsAllowed 	= false,
		examples = "{{` 1. Establish a database connection setting all mandatory parameters. `}}" +
				"{{` 2. Close the database connection. `}}" +
				"{{` 3. Confirm that the database connection is closed. `}}" +
				"\n" +
				"{{# #Id;#Action;#User;#Server;#Base;#Sql;#Password\n" +
				"SQLCNT1;SQLconnect;'username';'127.0.0.1:3306';'myDatabase';'MySQL';'userpassword'\n" +
				"\n" +
				"#Id;#Action;#Connection\n" +
				"SQLDCNT1;SQLdisconnect;SQLCNT1.Out\n" +
				"\n" +
				"\n" +
				"#Assert;#Message\n" +
				"SQLCNT1.Out.isClosed();'connection is not closed'. #}}",
		seeAlso = "{{@ SQLexecute @}}, {{@ SQLinsert @}}, {{@ SQLselect @}}, {{@ SQLtableUpload @}}, {{@ SQLconnect @}}."
	)
public class SQLdisconnect extends AbstractAction
{
	public final static  String connectionName = "Connection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "Database connection established by the SQLconnect action.")
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
			try
			{
				context.getConfiguration().getDataBasesPool().disconnect(this.connection);
				super.setResult(null);
			}
			catch (SQLException e)
			{
				super.setError(e.getMessage(), ErrorKind.SQL_ERROR);
			}
		}
	}

	@Override
	public void initDefaultValues() 
	{
	}

}
