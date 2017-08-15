////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.MatrixConnectionImpl;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixError;
import com.exactprosystems.jf.functions.RowTable;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group 					= ActionGroups.System, 
		suffix 					= "RESTBL", 
		generalDescription 		= "This action is needed to get a copy of the system table that contains information about running current matrix. \n"
				+ "This table is made during running a matrix, each row of the table contains information about one {{@TestCase@}} or {{@Step@}}.\n"
				+ "Amendments made to the table do not influence the system table. To add columns and values there is action {{@ResultTableUserValue@}}.\n"
				+ "It is needed to use an optional parameter if applying to another matrix.",
		additionFieldsAllowed 	= false, 
		outputDescription 		= "A copy if the system table that has columns Matrix, TestCaseId, TestCase, StepIdentity, Step, Time, Result, Error, Screenshot.",
		outputType 				= Table.class,
		examples 				= "{{#\n#Id;#TestCase;#Kind\n"
				+ "First;;\n"
				+ "    #Action;#name\n"
				+ "    Print;'value'\n"
				+ "    #Action;#Time\n"
				+ "    Wait;1000\n"
				+ "#Id;#TestCase;#Kind\n"
				+ "Second;;\n"
				+ "    #Action;#Text;#Notifier\n"
				+ "    Show;'Some text for print';Notifier.Info\n"
				+ "    #Fail\n"
				+ "    'Fail'\n"
				+ "#Id;#TestCase;#Kind\n"
				+ "Third;;\n"
				+ "    #Id;#Action;#Decoraded\n"
				+ "    RESTBL1;ResultTable;true\n"
				+ "    #Action;#BeforeTestCase;#Table;#Title\n"
				+ "    TableReport;'First';RESTBL1.Out;'Result table'#}}",
		seeAlsoClass = {ResultTableUserValue.class}
)
public class ResultTable extends AbstractAction
{
	public final static String decoratedName = "Decoraded";
	public final static String matrixName = "Matrix";

	@ActionFieldAttribute(name = decoratedName, mandatory = true, description = "If you set true, a table has a"
			+ " human-readable kind. When indicating false, values that are in the table will be objects  which can be used later.")
	protected Boolean decorated;

	@ActionFieldAttribute(name = matrixName, mandatory = false, def = DefaultValuePool.Null, description = "Object MatrixRunner is indicated "
			+ "(that is an output value of actions {{@MatrixRun@}} and {{@MatrixRunFromText@}}). This action will be used in the summary table.")
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
		
		Table copy = result.clone();
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
					String shortName = path.getFileName().toString();
					String matrixStr = report.decorateExpandingBlock(shortName, matrix.toString());
					replace(row, Context.matrixColumn, 		e -> matrixStr);
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


