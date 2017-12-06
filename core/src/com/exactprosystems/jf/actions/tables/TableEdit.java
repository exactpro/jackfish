////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ActionAttribute(
		group						  = ActionGroups.Tables,
		constantGeneralDescription 	  = R.TABLE_EDIT_GENERAL_DESC,
		additionFieldsAllowed 		  = true,
		constantAdditionalDescription = R.TABLE_EDIT_ADDITIONAL_DESC,
        constantOutputDescription 	  = R.TABLE_EDIT_OUTPUT_DESC,
		suffix						  = "TBEDT",
        outputType              	  = Boolean.class,
		constantExamples 			  = R.TABLE_EDIT_EXAMPLE
	)
public class TableEdit extends AbstractAction 
{
	public static final String titleName = "Title";
	public static final String tableName = "Table";

	@ActionFieldAttribute(name = titleName, mandatory = true, constantDescription = R.TABLE_EDIT_TITLE)
	protected String title;

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_EDIT_TABLE)
	protected Table table;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case titleName:
			case tableName:
				break;

			default:
				return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case titleName:
			case tableName:
				break;

			default:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Map<String, Boolean> columns = new LinkedHashMap<>();
		for (Parameter param : parameters.select(TypeMandatory.Extra))
		{
			String name = param.getName();
			Object value = param.getValue();
			if (value == null)
			{
				columns.put(name, false);
			}
			else if (value instanceof Boolean)
			{
				columns.put(name, (Boolean) value);
			}
			else
			{
				super.setError(String.format("Parameter for column %s should be empty or true/false", name), ErrorKind.WRONG_PARAMETERS);
				return;
			}
		}

		boolean input = context.getFactory().editTable(context.getEvaluator(), this.title, this.table, columns);
		super.setResult(input);
	}
}
