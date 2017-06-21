////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group 					= ActionGroups.System, 
		suffix 					= "", 
		generalDescription 		= "The following action is needed to add extra columns and values to the system table "
				+ "that can be acquired by action {{@ResultTable@}}.\n"
				+ "Changes values for the current row (current test case or step).",
		additionFieldsAllowed 	= true,
		additionalDescription = "Names of additional columns are specified in the value of the additional parameter, "
				+ "in the value of the parameter - values are indicated, that will be added to the row that corresponds "
				+ "to that test case or step, where the following action is. {{`If names match, additional columns do not "
				+ "replace columns of the system table: Matrix, TestCaseId, TestCase, StepIdentity, Step, Time, Result, Error, Screenshot.`}}",
		examples 				= "{{#\n" +
				"#Id;#TestCase;#Kind;#Depends;#For\n" +
				"RTUSERVAL;ResultTableUserValue;Never;;\n" +
				"    #Action;UserValue\n" +
				"    ResultTableUserValue;'value'\n" +
				"    #Step;#Kind;#For;#Depends\n" +
				"    ;;;\n" +
				"    #EndStep\n" +
				"    #Id;#Action;$Decoraded\n" +
				"    RESTBL1;ResultTable;false\n" +
				"    #Assert;#Message\n" +
				"    RESTBL1.Out[RESTBL1.Out.size - 1].Result == Result.Passed;'Assert'\n" +
				"    #Id;#Let\n" +
				"    firstTableSize;RESTBL1.Out.size\n" +
				"    #Assert;#Message\n" +
				"    RESTBL1.Out[RESTBL1.Out.size - 2].UserValue == 'value';'Wrong value'\n" +
				"    #Assert;#Message\n" +
				"    RESTBL1.Out[RESTBL1.Out.size - 1].UserValue == null;'Wrong value'\n" +
				"    #Action;OtherValue\n" +
				"    ResultTableUserValue;100\n" +
				"    #Step;#Kind;#For;#Depends\n" +
				"    ;;;\n" +
				"        #Action;UserValue;OtherValue\n" +
				"        ResultTableUserValue;'value1';200\n" +
				"    #EndStep#}}",
		seeAlsoClass = {ResultTable.class}
)
public class ResultTableUserValue extends AbstractAction
{
	public final static String	matrixName		= "Matrix";

	@ActionFieldAttribute(name = matrixName, mandatory = false, def = DefaultValuePool.Null, description = "Object MatrixRunner is indicated (that "
			+ "is an output value of actions {{@MatrixRun@}} and {{@MatrixRunFromText@}}). This action will be used in the summary table. ")
	protected MatrixRunner		matrix			= null;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Table result = this.matrix == null ? context.getTable() : this.matrix.getTable();
		if (result == null)
		{
			super.setError("The result table is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
		int size = result.size();
		if (size == 0)
		{
			super.setError("The result table is empty", ErrorKind.FAIL);
			return;
		}
		Set<String> predefinedColumns = new HashSet<>(Arrays.asList(Context.resultColumns));
		Parameters extraParams = parameters.select(TypeMandatory.Extra);
		
		for (String name : extraParams.keySet())
		{
		    if (predefinedColumns.contains(name))
		    {
    		    super.setError("Parameter '" + name + "' is reserved word.", ErrorKind.WRONG_PARAMETERS);
    		    return;
		    }
		}
		
		for (Parameter parameter : extraParams)
		{
			String name = parameter.getName();
			Object value = parameter.getValue();
			if (predefinedColumns.contains(name))
			{
				continue;
			}

			boolean found = false;
			for (int i = 0; i < result.getHeaderSize(); i++)
			{
				if (result.getHeader(i).equals(name))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				result.addColumns(name);
			}
			
			result.changeValue(name, size - 1, value);
		}

		super.setResult(null);
	}
}
