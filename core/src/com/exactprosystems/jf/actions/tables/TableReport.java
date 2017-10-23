////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;
import com.jcraft.jsch.Identity;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ActionAttribute(
		group					      = ActionGroups.Tables,
		constantGeneralDescription    = R.TABLE_REPORT_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
        constantAdditionalDescription = R.TABLE_REPORT_ADDITIONAL_DESC,
		constantExamples 			  = R.TABLE_REPORT_EXAMPLE
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
    public final static String widthName          = "Width";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_REPORT_TABLE)
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = titleName, mandatory = true, constantDescription = R.TABLE_REPORT_TITLE)
	protected String 	title 	= null;

    @ActionFieldAttribute(name=toReportName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.TABLE_REPORT_TO_REPORT)
    protected ReportBuilder toReport;

    @ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.TABLE_REPORT_BEFORE_TEST_CASE)
    protected String    beforeTestCase;

	@ActionFieldAttribute(name = numbersName, mandatory = false, def = DefaultValuePool.True, constantDescription = R.TABLE_REPORT_WITH_NUMBERS)
	protected Boolean withNumbers;

	@ActionFieldAttribute(name = columnsName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.TABLE_REPORT_COLUMNS)
	protected Object	columns;

    @ActionFieldAttribute(name = widthName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.TABLE_REPORT_WIDTHS)
    protected Integer[]  widths;
	
	@ActionFieldAttribute(name = reportValuesName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.TABLE_REPORT_REPORT_VALUES)
	protected Boolean	reportValues;

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
		
		Map<String, String> map = new LinkedHashMap<>();
		if (this.columns == null)
		{
            Parameters extra = parameters.select(TypeMandatory.Extra);
            for (Parameter ex : extra)
            {
                map.put(ex.getName(), ex.getValueAsString());
            }
		}
		else
		{
		    if (this.columns.getClass().isArray())
		    {
		        map = Arrays.stream((Object[])this.columns)
                    .map(e -> Str.asString(e))
                    .collect(Collectors.toMap(a -> a, a -> a, (k,v) -> k, LinkedHashMap::new));
		    }
		    else if (this.columns instanceof Iterable<?>)
		    {
		        map = StreamSupport.stream(((Iterable<?>)this.columns).spliterator(), false)
		            .map(e -> Str.asString(e))
		            .collect(Collectors.toMap(a -> a, a -> a, (k,v) -> k, LinkedHashMap::new));
		    }
		    else if (this.columns instanceof Map<?, ?>)
		    {
		        map = ((Map<?, ?>)this.columns).entrySet()
		            .stream()
		            .collect(Collectors.toMap(e -> Str.asString(e.getKey()), e -> Str.asString(e.getValue()), (k,v) -> k, LinkedHashMap::new));
		    }
		    else
		    {
		        String key = Str.asString(this.columns);
		        map.put(key, key);
		    }
		}
		
		int[] width = null;
		if (this.widths != null)
		{
		    width = Arrays.stream(this.widths).mapToInt(Integer::intValue).toArray(); 
		}
		
		report = this.toReport == null ? report : this.toReport;
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		this.table.report(report, Str.asString(this.title), this.beforeTestCase, this.withNumbers, this.reportValues, true, map, width);
		
		super.setResult(null);
	}
}

