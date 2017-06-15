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
import com.exactprosystems.jf.api.error.JFSQLException;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.sql.SqlConnection;

@ActionAttribute(
		group					= ActionGroups.SQL,
		suffix					= "SQLEXEC",
		generalDescription 		= "The following action is needed to execute any SQL database query.",
		additionFieldsAllowed 	= true,
		additionalDescription = "The additional parameter is defined by the data that will be used in completed SQL"
				+ " query instead of placeholders -'?'. The parameter name is not used during the execution and in"
				+ " this case can serve as a comment for the user. For example you can use it as an object "
				+ "description set as a parameter value. \n" +
                "{{*Attention!*}} You should mind the order of additional parameters.",
		outputType = Boolean.class,
		outputDescription = "Returns 'true' if the query was for data selection and 'false' if for data change.",
		examples = "{{` 1. Create a connection to a database. `}}" +
				"{{` 2. Execute the query to create Users table. `}}" +
				"{{` 3.-4. Define and initialize Name and Age variables. `}}" +
				"{{` 5. Execute the query to fill in the Users table using placeholders in additional parameters. `}}" +
				"{{` 6. Execute the Update query using placeholders. `}}\n" +
				"{{#\n" +
				"#Id;#Action;#User;#Server;#Base;#Sql;#Password\n" +
				"SQLCNT1;SQLconnect;'username';'127.0.0.1:3306';'database';'MySQL';'password'\n" +
				"#Id;#RawText;#Kind \n" +
				"createTable;Text;None\n" +
				"~;CREATE TABLE users (id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY, \n" +
				"name VARCHAR(30) NOT NULL,age int NOT NULL)\n" +
				"#EndRawText\n" +
				"#Id;#Action;#Query;#Connection\n" +
				"SQLEXEC1;SQLexecute;createTable;SQLCNT1.Out\n" +
				"#Id;#Let\n" +
				"name;'mike'\n" +
				"#Id;#Let\n" +
				"age;23\n" +
				"#Id;#RawText;#Kind \n" +
				"insertQuery;Text;None\n" +
				"~;insert into users (name, age) values (?, ?)\n" +
				"#EndRawText\n" +
				"#Id;#Action;#Query;#Connection;#User name;#User age\n" +
				"SQLINS1;SQLinsert;insertQuery;SQLCNT1.Out;name;age\n" +
				"#Id;#RawText;#Kind \n" +
				"updateQuery;Text;None\n" +
				"~;update users set name=? where ? = 23\n" +
				"#EndRawText\n" +
				"#Id;#Action;#Query;#Connection;#User new name;#User new age\n" +
				"SQLEXEC2;SQLexecute;updateQuery;SQLCNT1.Out;'john';age#}}",
		seeAlsoClass = {SQLdisconnect.class, SQLinsert.class, SQLselect.class, SQLtableUpload.class, SQLconnect.class}
	)
public class SQLexecute extends AbstractAction
{
	public final static String connectionName 	= "Connection";
	public final static String queryName 		= "Query";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "Database connection established by"
			+ " the {{@ SQLconnect action @}}.")
	protected SqlConnection connection 		= null;

	@ActionFieldAttribute(name = queryName, mandatory = true, description = "The SQL database query.")
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
