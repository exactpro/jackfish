////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.sql;

import java.util.List;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.sql.SqlConnection;

@ActionAttribute(
		group					= ActionGroups.SQL,
		generalDescription 		= "Upload data to desire table of connected database.",
		additionFieldsAllowed 	= false
	)
public class SQLtableUpload extends AbstractAction
{
	public final static String connectionName 	= "Connection";
	public final static String tableName 		= "Table";
	public final static String dataName 		= "Data";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "Connection that was given in SQLConnect.")
	protected SqlConnection connection 		= null;

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "SQL query.")
	protected String table 	= "";

	@ActionFieldAttribute(name = dataName, mandatory = true, description = "Table object wich can be got from csv-file for example.")
	protected Table data 	= null;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return tableName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}
	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		// TODO Auto-generated method stub
		switch (parameterToFill)
		{
			case tableName:
			
		}
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null");
		}
		else
		{
			this.data.upload(this.connection, this.table);
			super.setResult(null);
		}
	}
		

}
