////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.util.List;

@ActionAttribute(
		group					= ActionGroups.Tables,
		generalDescription 		= "Reports the table to the report.",
		additionFieldsAllowed 	= true
	)
public class TableReport extends AbstractAction 
{
	public final static String tableName = "Table";
	public final static String titleName = "Title";
	public final static String numbersName = "Numbers";
	public final static String columnsName = "Columns";
	public final static String reportValuesName = "ReportValues";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "The table.")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = titleName, mandatory = true, description = "Title.")
	protected String 	title 	= null;

	@ActionFieldAttribute(name = numbersName, mandatory = false, description = "If true then outputs row numbers.")
	protected Boolean withNumbers;

	@ActionFieldAttribute(name = columnsName, mandatory = false, description = "Columns printed in the report.")
	protected String[]	columns;

	@ActionFieldAttribute(name = reportValuesName, mandatory = false, description = "Report values instead expressions.")
	protected Boolean	reportValues;

	public TableReport()
	{
	}

	@Override
	public void initDefaultValues() 
	{
		withNumbers 	= true;
		columns 		= new String[] {};
		reportValues 	= false;
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case numbersName:
			case reportValuesName:
				return HelpKind.ChooseFromList;
		}
		
		return null;
	}

	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case numbersName:
			case reportValuesName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
			default:
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.table == null)
		{
			super.setError("Table is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
		this.table.report(report, this.title, this.withNumbers, this.reportValues, this.columns);
		
		super.setResult(null);
	}

	@Override
	protected boolean reportAllDetail()
	{
		return false;
	}
}

