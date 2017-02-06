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
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.sql.SqlConnection;

@ActionAttribute(
		group					= ActionGroups.SQL,
		suffix					= "SQLSLCT",
		generalDescription 		= "The following action is needed to execute the Select SQLquery.",
		additionFieldsAllowed 	= true,
		additionalDescription = "The additional parameter is defined by the data that will be used in completed SQL"
				+ " query instead of placeholders -'?'. The parameter name is not used during the execution and in"
				+ " this case can serve as a comment for the user. For example you can use it as an object "
				+ "description set as a parameter value. {{$ Attention! You should mind the order of additional parameters. $}}",
		outputDescription 		= "Select SQL query returns a Table object. The returned Table column names coincide"
				+ " with the Table column names we get as a result of a query.",
		outputType				= Table.class,
		seeAlso = "{{@ SQLdisconnect@}}, {{@ SQLexecute @}}, {{@ SQLinsert @}}, {{@ SQLtableUpload @}}, {{@ SQLconnect @}}.",
		examples = "{{` 1. Create a connection to a database. `}}" +
				"{{` 2. Execute the query to create Users table `}}" +
				"{{` 3.-4. Define and initialize Name and Age variables `}}" +
				"{{` 5. Execute the query to fill in the Users table using placeholders in additional parameters `}}" +
				"{{` 6. Execute SQLselect action using placeholders in additional parameters `}}" +
				"{{` 7. Add the table to the report using the Table Report action `}}\n" +
				"{{##Id;#Action;#User;#Server;#Base;#Sql;#Password\n" +
				"SQLCNT1;SQLconnect;'username';'127.0.0.1:3306';'myDatabase';'MySQL';'userpassword'\n" +
				"\n" +
				"#Id;#Action;#Query;#Connection\n" +
				"SQLEXEC1;SQLexecute;'CREATE TABLE users (id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,name VARCHAR(30) NOT NULL,age int NOT NULL)';SQLCNT1.Out\n" +
				"\n" +
				"#Id;#Let\n" +
				"name;'mike'\n" +
				"\n" +
				"#Id;#Let\n" +
				"age;23\n" +
				"\n" +
				"#Id;#Action;#Query;#Connection;#User name we get earlier ;#User age we get earlier\n" +
				"SQLEXEC3;SQLexecute;'insert into users (name, age) values (?, ?)';SQLCNT1.Out;name;age\n" +
				"\n" +
				"#Id;#Action;#Query;#Connection;#User name we get earlier ;#User age we get earlier\n" +
				"SQLSLCT1;SQLselect;'select name, age from test.users where name = ? and age = ?';SQLCNT1.Out;name;age\n" +
				"\n" +
				"#Action;#Table;#Title\n" +
				"TableReport;SQLSLCT1.Out;'Report title'#}}"
	)
public class SQLselect extends AbstractAction
{
	public final static String connectionName 	= "Connection";
	public final static String queryName 		= "Query";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "Connection to the database, output "
			+ "value of the SQLconnect action.")
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
		if (this.connection == null)
		{
			super.setError("Connection is null", ErrorKind.EMPTY_PARAMETER);
		}
		else
		{
			try
			{
				Table result = context.getConfiguration().getDataBasesPool().select(this.connection, this.query, parameters.select(TypeMandatory.Extra).values().toArray());
				super.setResult(result);
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
