////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.MatrixConnectionImpl;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixError;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.RowTable;
import com.exactprosystems.jf.functions.Table;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

@ActionAttribute(
		group 					   = ActionGroups.System,
		suffix 					   = "RESTBL",
		constantGeneralDescription = R.RESULT_TABLE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.RESULT_TABLE_OUTPUT_DESC,
		outputType 				   = Table.class,
		constantExamples 		   = R.RESULT_TABLE_EXAMPLE,
		seeAlsoClass = {ResultTableUserValue.class}
)
public class ResultTable extends AbstractAction
{
	public final static String decoratedName = "Decorated";
	public final static String matrixName = "Matrix";

	@ActionFieldAttribute(name = decoratedName, mandatory = true, constantDescription = R.RESULT_TABLE_DECORATED)
	protected Boolean decorated;

	@ActionFieldAttribute(name = matrixName, mandatory = false, def = DefaultValuePool.Null,  constantDescription = R.RESULT_TABLE_MATRIX)
	protected MatrixConnectionImpl matrix = null;

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
        Map<String, Object> map = new HashMap<String, Object>();
        if (this.matrix == null)
        {
            map.put(Context.resultColumn, Result.Passed);
        }
        
        Table result = this.matrix == null ? context.getTable() : this.matrix.getTable();
		if (result == null)
		{
			super.setError("The result table is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}

		Table copy = new Table(result);
		if (report.reportIsOn()) {		//TODO zzz
			copy.setValue(copy.size() - 1, map);
		}

		if (this.decorated)
		{
			String passed = report.decorateStyle(Result.Passed.name(), Result.Passed.getStyle());
			Set<String> knownColumns = new HashSet<String>();
			knownColumns.addAll(Arrays.asList(Context.resultColumns));

			for (RowTable row : copy)
			{
				Object matrix = row.get(Context.matrixColumn);
				if (matrix != null)
				{
					Path path = Paths.get(matrix.toString());
					Path pathName = path.getFileName();
					if (pathName != null)
					{
						String shortName = pathName.toString();
						String matrixStr = report.decorateExpandingBlock(shortName, matrix.toString());
						replace(row, Context.matrixColumn, 		e -> matrixStr);
					}
				}
				
				Result res = (Result)row.get(Context.resultColumn);
				String str = report.decorateStyle(row.get(Context.resultColumn), res == null ? "" : res.getStyle());
				row.put(Context.resultColumn, str);
				
                String stepIdentity = Str.asString(row.get(Context.stepIdentityColumn));
                row.put(Context.stepIdentityColumn, stepIdentity);
                row.put(Context.stepColumn,         stepIdentity);

				replace(row, Context.testCaseColumn, 		this::spaceIfNull);
				replace(row, Context.testCaseIdColumn, 		this::spaceIfNull);
				replace(row, Context.timeColumn, 			e -> report.decorateStyle(e == null ? "" : (e + " ms"), "ExecutionTime") );
				
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

                Object wrapper = row.get(Context.screenshotColumn);
                if (wrapper instanceof ImageWrapper)
                {
                    ImageWrapper iw = (ImageWrapper)wrapper;
                    
                    String description = iw.getDescription() == null ? iw.toString() : iw.getDescription();
                    String imageStr = report.decorateLink(description, report.getImageDir() + File.separator + iw.getName(report.getReportDir()));
                    replace(row, Context.screenshotColumn,       e -> imageStr);
                }
                else
                {
                    replace(row, Context.screenshotColumn,       this::spaceIfNull);
                }
				
				for (Entry<String, Object> entry : row.entrySet())
				{
					String key = entry.getKey();
					if (knownColumns.contains(key))
					{
						continue;
					}
					Object value = row.get(key);
					row.put(key, value == null ? "" : value);
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


