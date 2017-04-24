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
				+ "in the value of the parameter – values are indicated, that will be added to the row that corresponds "
				+ "to that test case or step, where the following action is. {{`If names match, additional columns do not "
				+ "replace columns of the system table: Matrix, TestCaseId, TestCase, StepIdentity, Step, Time, Result, Error, Screenshot.`}}",
		examples 				= "{{##Id;#TestCase;#Kind;#For\n"
				+ "First;;;\n"
				+ "#Id;#Action;#Content\n"
				+ "TXT1;TextCreate;'#TestCase;#Kind;#For\\n;;\\n    #Action;#Time\\n  Wait;1\\n\\n    #Fail \\n  "
				+ " \\'Failed\\'\\n\\n #TestCase;#Kind;#For\\n;;\\n    #Action;#Time\\n    Wait;1'\n"
				+ "    #Id;#Action;#Text\n"
				+ "    MXRN1;MatrixRunFromText;TXT1.Out\n"
				+ "    #Id;#Action;#Matrix\n"
				+ "    MXWT1;MatrixWait;MXRN1.Out\n"
				+ "    #Action;#Matrix\n"
				+ "    ResultTableUserValue;MXRN1.Out\n"
				+ "    #Id;#Action;#Matrix;#Decoraded\n"
				+ "    RESTBL1;ResultTable;MXRN1.Out;true\n"
				+ "    #Action;#BeforeTestCase;#Table;#Title\n"
				+ "    TableReport;'First';RESTBL1.Out;'Result table'#}}",
		seeAlsoClass = {ResultTable.class}
)
public class ResultTableUserValue extends AbstractAction
{
	public final static String	matrixName		= "Matrix";

	@ActionFieldAttribute(name = matrixName, mandatory = false, description = "Object MatrixRunner is indicated (that "
			+ "is an output value of actions {{@MatrixRun@}} and {{@MatrixRunFromText@}}). This action will be used in the summary table. ")
	protected MatrixRunner		matrix			= null;

	@Override
	public void initDefaultValues()
	{
	}

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
