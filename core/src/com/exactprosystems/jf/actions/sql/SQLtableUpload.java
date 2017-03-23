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
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.sql.SqlConnection;

@ActionAttribute(
		group					= ActionGroups.SQL,
		generalDescription 		= "The following action is needed to upload the data from the {{$Table$}} object to the"
				+ " database. The data is added using the Union principle (adding to the end of the database)",
		additionFieldsAllowed 	= false,
		examples = "{{` 1. Create a table. `}}" +
				"{{` 2. Create a connection to a database. `}}" +
				"{{` 3. Execute a query to create the Users table. `}}" +
				"{{` 4. Upload data from the Table object to Users table of the current database. `}}\n" +
				"{{##Id;#RawTable\n" +
				"DATA1;Table\n" +
				"@;Name;Age\n" +
				"0;Mike;25\n" +
				"1;Anna;24\n" +
				"#EndRawTable\n" +
				"#Id;#Action;#User;#Server;#Base;#Sql;#Password\n" +
				"SQLCNT1;SQLconnect;'username';'127.0.0.1:3306';'myDatabase';'MySQL';'userpassword'\n" +
				"#Id;#Action;#Query;#Connection\n" +
				"SQLEXEC1;SQLexecute;'CREATE TABLE users (id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,name VARCHAR(30) NOT NULL,age int NOT NULL)';SQLCNT1.Out\n" +
				"#Action;#Table;#Connection;#Data\n" +
				"SQLtableUpload;'users';SQLCNT1.Out;DATA1#}}",
		seeAlsoClass = {SQLdisconnect.class, SQLexecute.class, SQLinsert.class, SQLselect.class, SQLconnect.class}
	)
public class SQLtableUpload extends AbstractAction
{
	public final static String connectionName 	= "Connection";
	public final static String tableName 		= "Table";
	public final static String dataName 		= "Data";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "Connection to the database, output value of the SQLconnect action.")
	protected SqlConnection connection 		= null;

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "The name of the database table which you need to fill in with data.")
	protected String table 	= "";

	@ActionFieldAttribute(name = dataName, mandatory = true, description = "The Table, from which you take the data to add to the database.")
	protected Table data 	= null;

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
				this.data.upload(this.connection, this.table);
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
