package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		generalDescription 		= "Replaces all values in table using regular expression or just one value to the given.",
		additionFieldsAllowed 	= false
	)
public class TableReplace extends AbstractAction 
{
	public final static String tableName = "Table";
	public final static String colunmsName = "Columns";
	public final static String replaceName = "Replace";
	public final static String searchName = "Search";
	public final static String regexpName = "Regexp";
	public final static String matchCellname = "MatchCell";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "The table.")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = colunmsName, mandatory = true, description = "Array of columns.")
	protected String[]	columns 	= new String[] {};

	@ActionFieldAttribute(name = searchName, mandatory = true, description = "Value that will be put into the table.")
	protected Object 	search 	= null;

	@ActionFieldAttribute(name = replaceName, mandatory = false, description = "If value of cell equals this value then it will be changed to Replace.")
	protected Object 	replace = null;

	@ActionFieldAttribute(name = regexpName, mandatory = false, description = "If value of cell matches this regexp then it will be changed to Replace.")
	protected String 	regexp 	= null;

	//TODO help me with description
	@ActionFieldAttribute(name = matchCellname, mandatory = false, description = "")
	protected Boolean	matchCell = true;

	public TableReplace()
	{
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.regexp == null)
		{
			this.table.replace(this.search, this.replace, matchCell, this.columns);
		}
		else
		{
			this.table.replace(this.regexp, this.replace, this.columns);
		}
		
		super.setResult(null);
	}
}

