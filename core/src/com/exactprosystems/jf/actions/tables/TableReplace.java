////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
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
		constantGeneralDescription = R.TABLE_REPLACE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.TABLE_REPLACE_EXAMPLE
,
		seeAlsoClass = {TableAddValue.class, TableAddColumns.class, TableConsiderColumnsAs.class, TableColumnRename.class, TableRemoveRow.class}
	)
public class TableReplace extends AbstractAction 
{
	public static final String tableName 		= "Table";
	public static final String colunmsName 		= "Columns";
	public static final String replaceName 		= "Replace";
	public static final String searchName 		= "Search";
	public static final String regexpName 		= "Regexp";
	public static final String matchCellname 	= "MatchCell";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_REPLACE_TABLE)
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = colunmsName, mandatory = true, constantDescription = R.TABLE_REPLACE_COLUMNS)
	protected String[]	columns = new String[] {};

	@ActionFieldAttribute(name = replaceName, mandatory = true, constantDescription = R.TABLE_REPLACE_REPLACE)
	protected Object 	replace = null;
	
	@ActionFieldAttribute(name = searchName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.TABLE_REPLACE_SEARCH)
	protected Object 	search;

	@ActionFieldAttribute(name = regexpName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.TABLE_REPLACE_REGEXP)
	protected String 	regexp;

	@ActionFieldAttribute(name = matchCellname, mandatory = false, def = DefaultValuePool.True, constantDescription = R.TABLE_REPLACE_MATCH_CELL)
	protected Boolean	matchCell;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
	    if (this.regexp != null)
        {
            this.table.replace(this.regexp, this.replace, this.columns);
        }
	    else
		{
			if(this.matchCell == null)
			{
				super.setError("Parameter '" + matchCellname + "' can't be empty.", ErrorKind.EMPTY_PARAMETER);
				return;
			}

			if(!parameters.containsKey(searchName))
			{
				super.setError("Parameter '" + searchName + "' or '" + regexpName + "' can't be empty.", ErrorKind.EMPTY_PARAMETER);
				return;
			}

			this.table.replace(this.search, this.replace, this.matchCell, this.columns);
		}
		
		super.setResult(null);
	}
}

