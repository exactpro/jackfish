////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

import java.util.List;

@ActionAttribute(
		group					= ActionGroups.Tables,
		generalDescription 		= "This action is used to output the table to report.",
		additionFieldsAllowed 	= true,
		examples = "{{` 1. Create a test case with id Test.`}}"
				+ "{{` 2. Create a table with columns Name and Age. Add values to the first line of the table.`}}"
				+ "{{` 3. Output the table into the report only with the column Age, before the test case with id Test.`}}" +
				"{{##Id;#TestCase\n" +
				"Test;\n" +
				"    #Id;#RawTable\n" +
				"    TC;Table\n" +
				"    @;Name;Age\n" +
				"    0;Mike;25\n" +
				"    #EndRawTable\n" +
				"    #Action;#BeforeTestCase;#Table;#Title;#Columns\n" +
				"    TableReport;'Test';TC;'Table title';{'Age'}#}}"
	)
public class TableReport extends AbstractAction 
{
    public final static String tableName          = "Table";
    public final static String beforeTestCaseName = "BeforeTestCase";
    public final static String titleName          = "Title";
    public final static String numbersName        = "Numbers";
    public final static String columnsName        = "Columns";
    public final static String reportValuesName   = "ReportValues";
    public final static String toReportName       = "ToReport";

	@ActionFieldAttribute(name=toReportName, mandatory = false, description = 
            "This parameter is used for directing the output from the given object to the external report "
          + "created by the {{@ReportStart@}} action.")
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "A table which is needed to to be output into the report.")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, description = "Enables to output the table on the highest level of the report.")
	protected String 	beforeTestCase 	= null;

	@ActionFieldAttribute(name = titleName, mandatory = true, description = "The title of the output table.")
	protected String 	title 	= null;

	@ActionFieldAttribute(name = numbersName, mandatory = false, description = "If the value is true the column with the lines numbers is output.")
	protected Boolean withNumbers;

	@ActionFieldAttribute(name = columnsName, mandatory = false, description = "Array of column titles which is needed to be output into the report.")
	protected String[]	columns;

	@ActionFieldAttribute(name = reportValuesName, mandatory = false, description = "If the value is false, the value"
			+ " from the cell is output, if the value is true the expression result is output. "
			+ "Applicable for the cells of Expression type, see {{@TableConsiderColumnAs@}}.")
	protected Boolean	reportValues;

	public TableReport()
	{
	}

	@Override
	public void initDefaultValues() 
	{
		this.beforeTestCase = null;
		this.withNumbers 	= true;
		this.columns 		= new String[] {};
		this.reportValues 	= false;
		this.toReport = null;
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case beforeTestCaseName:
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
			case beforeTestCaseName:
				ActionsReportHelper.fillListForParameter(super.owner.getMatrix(),  list, context.getEvaluator());
				break;
			default:
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.table == null)
		{
			super.setError(tableName, ErrorKind.EMPTY_PARAMETER);
			return;
		}
		Parameters columns = parameters.select(TypeMandatory.Extra);
		if (columns.isEmpty())
		{
			columns = null;
		}
		report = this.toReport == null ? report : this.toReport;
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		this.table.report(report, Str.asString(this.title), this.beforeTestCase, this.withNumbers, this.reportValues, columns, this.columns);
		
		super.setResult(null);
	}
}

