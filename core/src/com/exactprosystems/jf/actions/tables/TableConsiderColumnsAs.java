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
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					   = ActionGroups.Tables,
		constantGeneralDescription = R.TABLE_CONSIDER_COLUMN_AS_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples		   = R.TABLE_CONSIDER_COLUMN_AS_EXAMPLE
,
		seeAlsoClass = {TableReplace.class, TableAddColumns.class, TableColumnRename.class, TableAddValue.class}

	)
public class TableConsiderColumnsAs extends AbstractAction
{
    public final static String tableName        = "Table";
    public final static String asStringName     = "String";
    public final static String asBooleanName    = "Boolean";
    public final static String asIntegerName    = "Integer";
    public final static String asDoubleName     = "Double";
    public final static String asBigDecimalName = "BigDecimal";
    public final static String asDateName       = "Date";
    public final static String asExpressionName = "Expression";
    public final static String asGroupName      = "Group";
    public final static String asHyperlinkName  = "Hyperlink";
    public final static String asColoredName    = "Colored";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_TABLE)
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = asStringName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_STRING)
	protected String[]	asString;

	@ActionFieldAttribute(name = asBooleanName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_BOOLEAN)
	protected String[]	asBoolean;

	@ActionFieldAttribute(name = asIntegerName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_INTEGER)
	protected String[]	asInteger;

	@ActionFieldAttribute(name = asDoubleName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_DOUBLE)
	protected String[]	asDouble;

	@ActionFieldAttribute(name = asBigDecimalName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_BIG_DECIMAL)
	protected String[]	asBigDecimal;

	@ActionFieldAttribute(name = asDateName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_DATE)
	protected String[]	asDate;

	@ActionFieldAttribute(name = asExpressionName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_EXPRESSION)
	protected String[]	asExpression;

    @ActionFieldAttribute(name = asGroupName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_GROUP)
    protected String[]  asGroup;

    @ActionFieldAttribute(name = asHyperlinkName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_HYPERLINK)
    protected String[]  asHyperlink;

    @ActionFieldAttribute(name = asColoredName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_COLORED)
    protected String[]  asColored;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if(this.asString == null
		|| this.asBoolean == null
		|| this.asInteger == null
		|| this.asDouble == null
		|| this.asBigDecimal == null
		|| this.asDate == null
		|| this.asExpression == null
		|| this.asGroup == null
		|| this.asHyperlink == null
		|| this.asColored == null)
		{
			super.setError("Columns of considering can't be empty.", ErrorKind.EMPTY_PARAMETER);
			return;
		}

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
        if (this.asHyperlink.length > 0)
        {
            this.table.considerAsHyperlink(this.asHyperlink);
        }
        if (this.asColored.length > 0)
        {
            this.table.considerAsColored(this.asColored);
        }

		super.setResult(null);
	}
}

