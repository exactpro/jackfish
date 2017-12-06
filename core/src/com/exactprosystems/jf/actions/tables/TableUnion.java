////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group 					   = ActionGroups.Tables,
		constantGeneralDescription = R.TABLE_UNION_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples           = R.TABLE_UNION_EXAMPLE
)
public class TableUnion extends AbstractAction
{
	public static final String mainTableName   = "MainTable";
	public static final String unitedTableName = "UnitedTable";

	@ActionFieldAttribute(name = mainTableName, mandatory = true, constantDescription = R.TABLE_UNION_MAIN_TABLE)
	protected Table mainTable;

	@ActionFieldAttribute(name = unitedTableName, mandatory = true, constantDescription = R.TABLE_UNION_UNITED_TABLE)
	protected Table unitedTable;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		boolean atLeastOneCoincidence = false;
		for (int mainNum = 0; mainNum < this.mainTable.getHeaderSize(); mainNum++)
		{
			for (int unitedNum = 0; unitedNum < this.unitedTable.getHeaderSize(); unitedNum++)
			{
				if (this.mainTable.getHeader(mainNum).equals(this.unitedTable.getHeader(unitedNum)))
				{
					atLeastOneCoincidence = true;
					break;
				}
			}
		}

		if (atLeastOneCoincidence)
		{
			this.mainTable.addAll(this.unitedTable);
		}

		super.setResult(null);
	}
}
