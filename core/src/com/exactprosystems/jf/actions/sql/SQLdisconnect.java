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
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.sql.SqlConnection;

@ActionAttribute(
		group					= ActionGroups.SQL,
		suffix					= "SQLDCNT",
		generalDescription 		= "Disconnects from connected DB.",
		additionFieldsAllowed 	= false
	)
public class SQLdisconnect extends AbstractAction
{
	public final static  String connectionName = "Connection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "Connection that was given in SQLConnect.")
	protected SqlConnection connection 		= null;

	@Override
	protected void doRealAction(Context context,
			ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null");
		}
		else
		{
			context.getConfiguration().getDataBasesPool().disconnect(this.connection);
			super.setResult(null);
		}
	}

}
