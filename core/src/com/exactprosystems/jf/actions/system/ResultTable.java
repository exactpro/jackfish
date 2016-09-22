////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import java.util.List;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.functions.RowTable;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group 					= ActionGroups.System, 
		suffix 					= "RESTBL", 
		generalDescription 		= "Returns the result table.", 
		additionFieldsAllowed 	= false, 
		outputDescription 		= "Copy of Table which contains rows correspond to each performed TestCase.", 
		outputType 				= Table.class
)
public class ResultTable extends AbstractAction
{
	public final static String decoratedName = "Decoraded";
	public final static String matrixName = "Matrix";

	@ActionFieldAttribute(name = decoratedName, mandatory = true, description = "If true, returns a table with decorated columns")
	protected Boolean decorated;

	@ActionFieldAttribute(name = matrixName, mandatory = false, description = "If it is used then the result table for the matrix will be returned.")
	protected MatrixRunner matrix = null;

	@Override
	public void initDefaultValues()
	{
		this.matrix = null;
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context,
			Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
		case decoratedName:
			return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case decoratedName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
		}
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report,
			Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Table result = this.matrix == null ? context.getTable() : this.matrix.getTable();
		if (result == null)
		{
			super.setError("The result table is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
		
		Table copy = result.clone();
		if (this.decorated)
		{
			for (RowTable row : copy)
			{
				Result res = (Result)row.get(Context.resultColumn);
				String str = report.decorateStyle(row.get(Context.resultColumn), res == null ? "" : res.getStyle());
				row.put(Context.resultColumn, str);
			}
		}
		
		super.setResult(copy);
	}
}
