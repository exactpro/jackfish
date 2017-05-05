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

import java.util.List;

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

	@ActionFieldAttribute(name = actualName, mandatory = true, description = "A table which is to be compared. .")
	protected Table actual = null;

	@ActionFieldAttribute(name = expectedName, mandatory = true, description = "A table which is to be compared with.")
	protected Table expected = null;

	@ActionFieldAttribute(name = excludeName, mandatory = false, description = "An array of column names, which are excluded out of comparison.")
	protected String[] exclude;

	@ActionFieldAttribute(name = ignoreRowsOrderName, mandatory = false, description = "Ignore row order.")
	protected Boolean ignoreRowsOrder;


	public TableCompareTwo()
	{
	}

	@Override
	public void initDefaultValues()
	{
		this.exclude = new String[]{};
		this.ignoreRowsOrder = false;
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

		Table differences = new Table(new String[] { "Description", "Expected", "Actual" }, evaluator);
		boolean res = Table.extendEquals(report, differences, this.actual, this.expected, this.exclude, this.ignoreRowsOrder);

        super.setResult(differences);
		if (!res)
		{
			super.setError("Tables are not equal.", ErrorKind.NOT_EQUAL);
		}
	}

}
