////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		generalDescription 		= "This action is determined  to take string values stored in a table to a certain data type. \n"
				+ "Objects Table are filled with string values by default. It is used in case when it is needed to "
				+ "perform an action with data stored in a table not as a line but as a one type of data given: "
				+ "{{$Integer$}}, {{$String$}}, {{$Boolean$}}, {{$Double$}}, {{$BigDecimal$}}, {{$Date$}}, and also {{$Expression$}}"
				+ " which is used to name cells which include expressions in mvel language. These expressions s will be "
				+ "calculated  when actions TableReport (if parameter ReportValues is used), TableSaveToFile (if parameter"
				+ " SaveValues is used), TableCompareTwo are performed.",
		additionFieldsAllowed 	= false,
		examples 				=
				"{{`1. Create a table with columns Name and Age, SalaryPerYear and populate it with values.`}}"
				+ "{{`2. Set data type  Integer for column  Age.`}}"
				+ "{{`3. Using action TableSelect select all rows with value more than 25(int) in column Age.`}}"
				+ "{{`4. Verify that TableConsiderColumnsAs is correct. `}}"
				+ "{{##Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name;Age;SalaryPerYear\n"
				+ "0;Mike;42;12000 * 12\n"
				+ "1;John;32;25000*12\n"
				+ "2;Fred;21;7000*12\n"
				+ "#EndRawTable\n"
				+ "#Action;#Table;#Integer\n"
				+ "TableConsiderColumnsAs;TC;'Age'\n"
				+ "#Id;#Action;#Table;#Age\n"
				+ "TS;TableSelect;TC;new NumberCondition('Age','>',25)\n"
				+ "#Action;#Expression;#Table\n"
				+ "TableConsiderColumnsAs;'SalaryPerYear';TS.Out\n"
				+ "#Assert;#Message\n"
				+ "TS.Out.size() == 2 && TS.Out.get(0).get('SalaryPerYear') == '144000'#}}",
		seeAlsoClass = {TableReplace.class, TableAddColumns.class, TableColumnRename.class, TableAddValue.class}

	)
public class TableConsiderColumnsAs extends AbstractAction 
{
	public final static String tableName = "Table";
	public final static String asStringName = "String";
	public final static String asBooleanName = "Boolean";
	public final static String asIntegerName = "Integer";
	public final static String asDoubleName = "Double";
	public final static String asBigDecimalName = "BigDecimal";
	public final static String asDateName = "Date";
	public final static String asExpressionName = "Expression";
    public final static String asGroupName = "Group";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "A table is needed to be performed.\n")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = asStringName, mandatory = false, description = "Is specified as an array of column names, where it is needed to set data type - String.")
	protected String[]	asString;

	@ActionFieldAttribute(name = asBooleanName, mandatory = false, description = "Is specified as an array of column names, where it is needed to set data type - Boolean.")
	protected String[]	asBoolean;

	@ActionFieldAttribute(name = asIntegerName, mandatory = false, description = "Is specified as an array of column names, where it is needed to set data type - Integer.")
	protected String[]	asInteger;

	@ActionFieldAttribute(name = asDoubleName, mandatory = false, description = "Is specified as an array of column names, where it is needed to set data type - Double.")
	protected String[]	asDouble;

	@ActionFieldAttribute(name = asBigDecimalName, mandatory = false, description = "Is specified as an array of column names, where it is needed to set data type - BigDecimal.")
	protected String[]	asBigDecimal;

	@ActionFieldAttribute(name = asDateName, mandatory = false, description = "Is specified as an array of column names, where it is needed to set data type - Date.")
	protected String[]	asDate;

	@ActionFieldAttribute(name = asExpressionName, mandatory = false, description = "Is specified as an array of column names, where it is needed to set data type - Expression.")
	protected String[]	asExpression;
	
    @ActionFieldAttribute(name = asGroupName, mandatory = false, description = "Is specified as an array of column names, where it is needed to set data type - Group.")
    protected String[]  asGroup;
    
	public TableConsiderColumnsAs()
	{
	}
	
	@Override
	public void initDefaultValues() 
	{
		this.asString 		= new String[] {};
		this.asBoolean 		= new String[] {};
		this.asInteger 		= new String[] {};
		this.asDouble 		= new String[] {};
		this.asBigDecimal 	= new String[] {};
		this.asDate 		= new String[] {};
		this.asExpression 	= new String[] {};
		this.asGroup        = new String[] {};
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.asString.length > 0)
		{
			this.table.considerAsString(this.asString);
		}
		if (this.asBoolean.length > 0)
		{
			this.table.considerAsBoolean(this.asBoolean);
		}
		if (this.asInteger.length > 0)
		{
			this.table.considerAsInt(this.asInteger);
		}
		if (this.asDouble.length > 0)
		{
			this.table.considerAsDouble(this.asDouble);
		}
		if (this.asBigDecimal.length > 0)
		{
			this.table.considerAsBigDecimal(this.asBigDecimal);
		}
		if (this.asDate.length > 0)
		{
			this.table.considerAsDate(this.asDate);
		}
		if (this.asExpression.length > 0)
		{
			this.table.considerAsExpression(this.asExpression);
		}
        if (this.asGroup.length > 0)
        {
            this.table.considerAsGroup(this.asGroup);
        }
		
		super.setResult(null);
	}
}

