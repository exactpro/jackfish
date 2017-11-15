////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.sql;

import java.sql.SQLException;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFSQLException;
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
	public final static String connectionName 	= "Connection";
	public final static String queryName 		= "Query";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.SQL_EXECUTE_CONNECTION)
	protected SqlConnection connection 		= null;

	@ActionFieldAttribute(name = queryName, mandatory = true, constantDescription = R.SQL_EXECUTE_QUERY)
	protected String query 	= "";

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return queryName.equals(fieldName) ? HelpKind.BuildQuery : null;
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if(this.connection.isClosed()){
			super.setError("Connection is not established", ErrorKind.SQL_ERROR);
		}
		else
		{
			try
			{
				boolean result = context.getConfiguration().getDataBasesPool().execute(this.connection, this.query, parameters.select(TypeMandatory.Extra).values().toArray());
				super.setResult(result);
			}
			catch (SQLException e)
			{
				super.setError(e.getMessage(), ErrorKind.SQL_ERROR);
			}
			catch (JFSQLException e)
			{
				super.setError(e.getMessage(), ErrorKind.SQL_ERROR);
			}
		}
	}
}
