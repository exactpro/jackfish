////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.functions.Table.TableCompareResult;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@ActionAttribute(
		group					= ActionGroups.Tables,
		suffix					= "TBLCMP",
		generalDescription 		= "This action is determined to compare two tables.",
		additionFieldsAllowed 	= false,
		outputType              = Table.class,
		outputDescription       = "A table as a resulf of compare.",
		examples 				=
				"{{`1 Create a table with columns Name and Age. The first table line is applied with values Mike and 42 accordingly.`}}"
				+ "{{`2. Create a table similar to the previous. The first table line is applied with values Mike and 42 accordingly.`}}"
				+ "{{`3. Compare two tables.`}}"
				+ "Information about distinctions ( line number and columns titles which values do not match) is reported as a result of this action in form of:"
				+ "{{`Differences`}}"
				+ "{{`# Expected Actual`}}"
				+ "{{`0 [ Name : Mike, Age : 42 ] [ Name : Mike, Age : 41 ]`}}"
				+ "{{`Age 42 41`}}"
				+ "{{`If tables have different columns numbers, distinctions in cells will be ignored and information about all columns titles is displayed.`}} "
				+ "{{##Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name;Age\n"
				+ "0;Mike;42\n"
				+ "#EndRawTable\n"
				+ "#Id;#RawTable\n"
				+ "TC1;Table\n"
				+ "@;Name;Age\n"
				+ "0;Mike;41\n"
				+ "#EndRawTable\n"
				+ "#Action;#Expected;#Actual\n"
				+ "TableCompareTwo;TC;TC1#}}"
	)
public class TableCompareTwo extends AbstractAction
{
	public final static String actualName = "Actual";
	public final static String expectedName = "Expected";
	public final static String excludeName = "ExcludeColumns";
	public final static String ignoreRowsOrderName = "IgnoreRowsOrder";
    public final static String compareValuesName = "CompareValues";

	@ActionFieldAttribute(name = actualName, mandatory = true, description = "A table which is to be compared. .")
	protected Table actual = null;

	@ActionFieldAttribute(name = expectedName, mandatory = true, description = "A table which is to be compared with.")
	protected Table expected = null;

	@ActionFieldAttribute(name = excludeName, mandatory = false, def = DefaultValuePool.EmptyArrString, description = "An array of column names, which are excluded out of comparison.")
	protected String[] exclude;

	@ActionFieldAttribute(name = ignoreRowsOrderName, mandatory = false, def = DefaultValuePool.False, description = "Ignore row order.")
	protected Boolean ignoreRowsOrder;

    @ActionFieldAttribute(name = compareValuesName, mandatory = false, def = DefaultValuePool.False, description = "If true compare values otherwise compare string representation of values.")
    protected Boolean compareValues;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return ignoreRowsOrderName.equals(fieldName) || compareValuesName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case ignoreRowsOrderName:
			case compareValuesName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
		}
	}


	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.exclude == null)
		{
			super.setError("ExcludeColumns is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}

		if (this.ignoreRowsOrder == null)
		{
			super.setError("IgnoreRowsOrder is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
		
		
		Set<String> actualColumns = this.actual.names(this.exclude);
        Set<String> expectedColumns = this.expected.names(this.exclude);
		
		if (!Objects.equals(actualColumns, expectedColumns))
		{
            super.setError("Actual columns " + actualColumns + " doesn't match expected columns " + expectedColumns, 
                ErrorKind.WRONG_PARAMETERS);
            return;
		}
		
		Table differences = new Table(new String[] { "Description", "Expected", "Actual" }, evaluator);
		TableCompareResult res = Table.extendEquals(report, differences, this.actual, this.expected, this.exclude, 
		      this.ignoreRowsOrder, this.compareValues);

        super.setResult(differences);
		if (!res.equal)
		{
            String message = String.format("Tables are not equal.\n %d - matched\n %d - extra actual\n %d - extra expected", 
                res.matched, res.extraActual, res.extraExpected);
		
			super.setError(message, ErrorKind.NOT_EQUAL);
		}
	}
}
