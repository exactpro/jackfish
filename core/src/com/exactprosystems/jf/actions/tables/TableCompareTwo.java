////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import java.util.List;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		suffix					= "TBLCMP",
		generalDescription 		= "Compares the given tables. It will fail if the tables are not equal.",
		additionFieldsAllowed 	= false
	)
public class TableCompareTwo extends AbstractAction 
{
	public final static String actualName = "Actual";
	public final static String expectedName = "Expected";
	public final static String excludeName = "ExcludeColumns";
	public final static String ignoreRowsOrderName = "IgnoreRowsOrder";

	@ActionFieldAttribute(name = actualName, mandatory = true, description = "One message, got from any source.")
	protected Table actual = null;

	@ActionFieldAttribute(name = expectedName, mandatory = true, description = "Another message, got from any source.")
	protected Table expected = null;

	@ActionFieldAttribute(name = excludeName, mandatory = false, description = "Fields that will not be compare.")
	protected String[] exclude;

	@ActionFieldAttribute(name = ignoreRowsOrderName, mandatory = false, description = "Rows order will be ignored in comparison.")
	protected Boolean ignoreRowsOrder;


	public TableCompareTwo()
	{
	}
	
	@Override
	public void initDefaultValues() 
	{
		exclude = new String[]{};
		ignoreRowsOrder = true;
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return ignoreRowsOrderName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}
	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case ignoreRowsOrderName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
		}
	}

	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.actual == null)
		{
			super.setError("Actual table is null");
			return;
		}

		if (this.expected == null)
		{
			super.setError("Expected table is null");
			return;
		}
	
		boolean res = this.actual.extendEquals(report, this.expected, this.exclude, this.ignoreRowsOrder);
		
		if (res)
		{
			super.setResult(null);
		}
		else
		{
			super.setError("Tables are not equal.");
		}
	}
}
