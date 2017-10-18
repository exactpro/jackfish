////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.sql;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.config.SqlEntry;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.sql.SqlConnection;

import java.sql.SQLException;
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
	public final static String sqlName 		= "Sql";
	public final static String serverName 	= "Server";
	public final static String baseName 	= "Base";
	public final static String userName 	= "User";
	public final static String passwordName = "Password";



	@ActionFieldAttribute(name = sqlName, mandatory = true, description = "SQL Entry name.")
	protected String sql 		= "";

	@ActionFieldAttribute(name = serverName, mandatory = true, description = "Database ip address. The value of this "
			+ "parameter replaces ${SERVER} in the sqlConnection line in the selected SQL entry parameters.")
	protected String server 	= "";

	@ActionFieldAttribute(name = baseName, mandatory = true, description = "Database name. The value of this parameter "
			+ "replaces ${BASE} in the sqlConnection line in the selected SQL entry parameters.")
	protected String base 		= "";

	@ActionFieldAttribute(name = userName, mandatory = true, description = "Database user name.")
	protected String user 		= "";

	@ActionFieldAttribute(name = passwordName, mandatory = true, description = "Database password.")
	protected String password 	= "";

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		return sqlName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		Configuration configuration = context.getConfiguration();
		AbstractEvaluator evaluator = context.getEvaluator();
		switch (parameterToFill)
		{
			case sqlName :
				for (SqlEntry sqlEntry : configuration.getSqlEntries())
				{
					String quoted = evaluator.createString(sqlEntry.toString());
					list.add(new ReadableValue(quoted));
				}
				break;
		}
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		SqlConnection result;
		try
		{
			result = context.getConfiguration().getDataBasesPool().connect(this.sql, this.server, this.base, this.user, this.password);

			if (result != null && !result.isClosed())
			{
				super.setResult(result);
			}
			else
			{
				super.setError("Can not connect to the data base", ErrorKind.SQL_ERROR);
			}
		}
		catch (SQLException e)
		{
			super.setError(e.getMessage(), ErrorKind.SQL_ERROR);
		}
	}
}
