/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
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
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.sql.SqlConnection;

@ActionAttribute(
		group					   = ActionGroups.SQL,
		constantGeneralDescription = R.SQL_TABLE_UPLOAD_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.SQL_TABLE_UPLOAD_EXAMPLE,
		seeAlsoClass = {SQLdisconnect.class, SQLexecute.class, SQLinsert.class, SQLselect.class, SQLconnect.class}
	)
public class SQLtableUpload extends AbstractAction
{
	public static final String connectionName = "Connection";
	public static final String tableName      = "Table";
	public static final String dataName       = "Data";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.SQL_TABLE_UPLOAD_CONNECTION)
	protected SqlConnection connection;

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.SQL_TABLE_UPLOAD_TABLE)
	protected String table;

	@ActionFieldAttribute(name = dataName, mandatory = true, constantDescription = R.SQL_TABLE_UPLOAD_DATA)
	protected Table data;

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection.isClosed())
		{
			super.setError("Connection is not established", ErrorKind.SQL_ERROR);
			return;
		}
		this.data.upload(this.connection, this.table);
		super.setResult(null);
	}
}
