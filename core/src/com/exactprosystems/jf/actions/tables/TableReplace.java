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
	public final static String tableName 		= "Table";
	public final static String colunmsName 		= "Columns";
	public final static String replaceName 		= "Replace";
	public final static String searchName 		= "Search";
	public final static String regexpName 		= "Regexp";
	public final static String matchCellname 	= "MatchCell";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "Table where it is needed to replace values.")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = colunmsName, mandatory = true, description = "Array of column titles where it is needed to change values.")
	protected String[]	columns = new String[] {};

	@ActionFieldAttribute(name = replaceName, mandatory = true, description = "Value which replaces.")
	protected Object 	replace = null;
	
	@ActionFieldAttribute(name = searchName, mandatory = false, def = DefaultValuePool.Null, description = "If a cell value or a part of a cell is "
			+ "equal to this value, cell will be replaced. It is ignored if you set Regexp. ")
	protected Object 	search;

	@ActionFieldAttribute(name = regexpName, mandatory = false, def = DefaultValuePool.Null, description = "If a cell value complies with this"
			+ " regular expression it will be replaced.")
	protected String 	regexp;

	@ActionFieldAttribute(name = matchCellname, mandatory = false, def = DefaultValuePool.True, description = "if the value is true, the cell will be"
			+ " replaced, otherwise only the one that complies. It is used only when setting Search parameter and is "
			+ "ignored when setting Regexp.")
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

