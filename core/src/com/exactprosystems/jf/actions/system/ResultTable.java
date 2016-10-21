////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixError;
import com.exactprosystems.jf.documents.matrix.parser.items.Step;
import com.exactprosystems.jf.documents.matrix.parser.items.TestCase;
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
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(Context.resultColumn, Result.Passed);
		copy.setValue(copy.size() - 1, map);
		if (this.decorated)
		{
			String passed = report.decorateStyle(Result.Passed.name(), Result.Passed.getStyle());

			for (RowTable row : copy)
			{
				Object matrix = row.get(Context.matrixColumn);
				if (matrix != null)
				{
					Path path = Paths.get(matrix.toString());
					String shortName = path.getFileName().toString();
					String matrixStr = report.decorateExpandingBlock(shortName, matrix.toString());
					replace(row, Context.matrixColumn, 		e -> matrixStr);
				}
				
				Result res = (Result)row.get(Context.resultColumn);
				String str = report.decorateStyle(row.get(Context.resultColumn), res == null ? "" : res.getStyle());
				row.put(Context.resultColumn, str);

				replace(row, Context.testCaseColumn, 		e -> spaceIfNull(((TestCase)e).getName()) );
				replace(row, Context.testCaseIdColumn, 		this::spaceIfNull);
				replace(row, Context.stepIdentityColumn, 	this::spaceIfNull);
				replace(row, Context.stepColumn, 			e -> spaceIfNull(row.get(Context.stepIdentityColumn)) );
				
				Object error = row.get(Context.errorColumn);
				if (error instanceof MatrixError)
				{
					MatrixError matrixError = (MatrixError)error;
					String errorStr = report.decorateExpandingBlock(matrixError.Kind.toString(), matrixError.Message);
					replace(row, Context.errorColumn, 		e -> errorStr);
				}
				else
				{
					replace(row, Context.errorColumn, 		e -> passed);
				}
			}
		}
		
		super.setResult(copy);
	}

	private void replace(RowTable row, String columnName, Function<Object, String> func)
	{
		Object value = row.get(columnName);
		row.put(columnName, func.apply(value));
	}
	
	private String spaceIfNull(Object obj)
	{
		return obj == null ? "" : obj.toString();
	}
}


