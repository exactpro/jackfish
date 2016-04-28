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
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		generalDescription 		= "Consider pointed columns as columns with type.",
		additionFieldsAllowed 	= false
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

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "The table.")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = asStringName, mandatory = false, description = "Array of columns.")
	protected String[]	asString 	= new String[] {};

	@ActionFieldAttribute(name = asBooleanName, mandatory = false, description = "Array of columns.")
	protected String[]	asBoolean 	= new String[] {};

	@ActionFieldAttribute(name = asIntegerName, mandatory = false, description = "Array of columns.")
	protected String[]	asInteger 	= new String[] {};

	@ActionFieldAttribute(name = asDoubleName, mandatory = false, description = "Array of columns.")
	protected String[]	asDouble 	= new String[] {};

	@ActionFieldAttribute(name = asBigDecimalName, mandatory = false, description = "Array of columns.")
	protected String[]	asBigDecimal 	= new String[] {};

	@ActionFieldAttribute(name = asDateName, mandatory = false, description = "Array of columns.")
	protected String[]	asDate 	= new String[] {};

	@ActionFieldAttribute(name = asExpressionName, mandatory = false, description = "Array of columns.")
	protected String[]	asExpression 	= new String[] {};
	
	public TableConsiderColumnsAs()
	{
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
		
		super.setResult(null);
	}
}

