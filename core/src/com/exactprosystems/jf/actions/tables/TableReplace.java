package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		generalDescription 		= "This action is used to replace cell values if the value is equal to the given "
				+ "one or complies with a regular expression.",
		additionFieldsAllowed 	= false,
		examples 				=
				"{{`1. Create a table with columns Name and Age. Add four lines with data about Mike, Anna, John, Bruce.`}}"
				+ "{{`2. Replace all that comply with Regexp with 'passed'.`}}"
				+ "{{`3. Verify if everything was correct.`}} "
				+ "{{#\n" +
				"#Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name;Mail\n"
				+ "0;John;c0nst@money.simply.net\n"
				+ "1;Mike;somebody@dev.com.ua\n"
				+ "2;Bruce;Name.Sur_name@gmail.com\n"
				+ "3;Anna;user33@somewhere.in.the.net\n"
				+ "#EndRawTable\n" +
				"#Id;#Let\n" +
				"rgxp;'[a-zA-Z]{1}[a-zA-Z\\\\d\\\\.\\\\_]+@([a-zA-Z]+\\\\.){1,2}((net)|(com)|(org))'\n"
				+ "#Action;#Regexp;#Replace;#Table;#Columns\n"
				+ "TableReplace;rgxp;'passed';TC;'Mail'\n"
				+ "#Assert;#Message\n"
				+ "TC.get(0).get('Mail')=='passed' && TC.get(2).get('Mail')=='passed';'Assert!'#}}",
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

