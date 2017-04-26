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
		generalDescription = "The following action is needed to display graphs and diagrams in reports based on data from the tables.",
		additionFieldsAllowed = true,
		additionalDescription = "Depend on the type of the graph/diagram. {{` {{$Labels$}} – it is needed to name a column, "
				+ "which values will be used for creating a diagram.`}} {{` {{$YAxisDescription$}} – it is needed to specify the "
				+ "name that will be assigned to Z-axis.`}} {{` {{$Values$}} – the name of the column, which values will be used "
				+ "for creating Pie type diagram.`}}",
		examples = "1. Make a table\n"
				+ "2. Create a report by ReportStart action\n"
				+ "3. Create a graph based on the table. Specify a newly created report in parameter Report.\n"
				+ "4-5. Display a report.\n"
				+ "{{##Id;#TestCase;#Kind;#Depends;#For\n"
				+ "Chart;;;;\n"
				+ "    #Id;#RawTable\n"
				+ "    DATA1;Table\n"
				+ "    @;Labels;Mike salary;John Salary\n"
				+ "    0;2001;1;2\n"
				+ "    1;2002;2;3\n"
				+ "    2;2003;3;4\n"
				+ "    3;2004;3;4\n"
				+ "    4;2005;3;5\n"
				+ "    5;2006;4;5\n"
				+ "    #EndRawTable\n"
				+ "    #Id;#Action;#ReportName\n"
				+ "    REP1;ReportStart;'My report'\n"
				+ "    #Action;#ToReport;#Type;#Table;#Title;#Labels\n"
				+ "    ChartReport;REP1.Out;ChartKind.Line;DATA1;'Chart title';'Labels'\n"
				+ "    #Action;#Passed;#Report;#Failed\n"
				+ "    ReportFinish;0;REP1.Out;0\n"
				+ "    #Action;#Report\n"
				+ "    ReportShow;REP1.Out.getReportName()#}}"
)
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

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "Table that contains data for a graph.")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = typeName, mandatory = true, description = "Type of graph.")
	protected ChartKind			 chartType 			=  null;

    @ActionFieldAttribute(name = colorsName, mandatory = false, description = "Color map.")
    protected Map<String, Color>    colors           =  null;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, description = "Allows to display a graph at the top of the report.")
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
		
		ChartBuilder chartBuilder = ChartFactory.createStubChartBuilder(kind);
		chartBuilder.helpToAddParameters(list, context);
	}


	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case beforeTestCaseName:
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
        if (this.chartType == null)
        {
            super.setError(typeName, ErrorKind.EMPTY_PARAMETER);
            return;
        }
	    
		ChartBuilder chartBuilder = ChartFactory.createChartBuilder(this.chartType, this.table, this.colors, parameters.select(TypeMandatory.Extra));
		report = this.toReport == null ? report : this.toReport;
		// TODO perform explicit report chart
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		report.reportChart(Str.asString(this.title), this.beforeTestCase, chartBuilder);

		super.setResult(null);
	}
}
