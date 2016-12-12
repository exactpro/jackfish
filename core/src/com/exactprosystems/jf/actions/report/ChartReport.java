////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.report;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.ChartKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.common.NullParameterException;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.charts.ChartFactory;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.awt.Color;
import java.util.List;
import java.util.Map;

@ActionAttribute(
		group = ActionGroups.Report, 
		generalDescription = "Reports a chart to the report.", 
		additionFieldsAllowed = true)
public class ChartReport extends AbstractAction
{
	public final static String 	titleName 			= "Title";
	public final static String 	tableName 			= "Table";
	public final static String	beforeTestCaseName	= "BeforeTestCase";
	public final static String	typeName			= "Type";
    public final static String  colorsName          = "Colors";
	public final static String	toReportName		= "ToReport";

	@ActionFieldAttribute(name=toReportName, mandatory = false, description = 
            "This parameter is used for directing the output from the given object to the external report "
          + "created by the {{$ReportStart$}} action.")
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = titleName, mandatory = true, description = "Title.")
	protected String 	title 	= null;

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "The table.")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = typeName, mandatory = true, description = "Type of the chart")
	protected ChartKind			 chartType 			=  null;

    @ActionFieldAttribute(name = colorsName, mandatory = false, description = "Color map")
    protected Map<String, Color>    colors           =  null;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, description = "The name of Testcase before witch the table will be put.")
	protected String			beforeTestCase		= null;

	public ChartReport()
	{
	}

	@Override
	public void initDefaultValues()
	{
		this.beforeTestCase = null;
		this.toReport = null;
		this.colors = null;
	}

	@Override
	public void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		parameters.evaluateAll(context.getEvaluator());
		Object kindObj 	= parameters.get(typeName);
		Object tabObj 	= parameters.get(tableName);

		ChartKind kind 	= null;
		Table tab 		= null;

		if (kindObj instanceof ChartKind)
		{
			kind = (ChartKind) kindObj;
		}
		else
		{
			throw new NullParameterException(typeName);
		}

		if (tabObj instanceof Table)
		{
			tab = (Table) tabObj;
		}
		
		ChartBuilder chartBuilder = ChartFactory.createChartBuilder(kind, tab, null, parameters.select(TypeMandatory.Extra));
		chartBuilder.helpToAddParameters(list, context);
	}


	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case typeName:
				return HelpKind.ChooseFromList;
		}
		
		return null;
	}

	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case typeName:
				for (ChartKind kind : ChartKind.values())
				{
					list.add(new ReadableValue(ChartKind.class.getSimpleName() + "." + kind.name(), kind.getDescription()));
				}
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
        if (this.chartType == null)
        {
            super.setError(typeName, ErrorKind.EMPTY_PARAMETER);
            return;
        }
	    
		ChartBuilder chartBuilder = ChartFactory.createChartBuilder(this.chartType, this.table, this.colors, parameters.select(TypeMandatory.Extra));
		report = this.toReport == null ? report : this.toReport;
		report.reportChart(Str.asString(this.title), this.beforeTestCase, chartBuilder);

		super.setResult(null);
	}
}
