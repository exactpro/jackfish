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
		generalDescription 		= "This action is used to replace cell values if the value is equal to the given "
				+ "one or complies with a regular expression.",
		additionFieldsAllowed 	= false,
		seeAlso 				= "{{@TableAddValue@}}, {{@TableAddColumns@}}, {{@TableConsiderColumnAs@}}, {{@TableColumnRename@}}, {{@TableRemoveRow@}}",
		examples 				=
				"{{`1. Create a table with columns Name and Age. Add four lines with data about Mike, Anna, John, Bruce.`}}"
				+ "{{`2. Replace all that comply with Regexp with 'passed'.`}}"
				+ "{{`3. Verify if everything was correct.`}} "
				+ "{{##Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name;Mail\n"
				+ "0;John;c0nst@money.simply.net\n"
				+ "1;Mike;somebody@dev.com.ua\n"
				+ "2;Bruce;Name.Sur_name@gmail.com\n"
				+ "3;Anna;user33@somewhere.in.the.net\n"
				+ "#EndRawTable\n"
				+ "#Action;#Regexp;#Replace;#Table;#Columns\n"
				+ "TableReplace;'[a-zA-Z]{1}[a-zA-Z\\\\d\\\\.\\\\_]+@([a-zA-Z]+\\\\.){1,2}((net)|(com)|(org))';'passed';TC;'Mail'\n"
				+ "#Assert;#Message\n"
				+ "TC.get(0).get('Mail') == 'passed'&& TC.get(2).get('Mail') == 'passed';'Replacement was not made'#}}",
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
	
	@ActionFieldAttribute(name = searchName, mandatory = false, description = "If a cell value or a part of a cell is "
			+ "equal to this value, cell will be replaced. It is ignored if you set Regexp. ")
	protected Object 	search;

	@ActionFieldAttribute(name = regexpName, mandatory = false, description = "If a cell value complies with this"
			+ " regular expression it will be replaced.")
	protected String 	regexp;

	@ActionFieldAttribute(name = matchCellname, mandatory = false, description = "if the value is true, the cell will be"
			+ " replaced, otherwise only the one that complies. It is used only when setting Search parameter and is "
			+ "ignored when setting Regexp.")
	protected Boolean	matchCell;

	public TableReplace()
	{
	}
	
	@Override
	public void initDefaultValues() 
	{
		search 	= null;
		regexp 	= null;
		matchCell = true;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.regexp == null)
		{
			this.table.replace(this.search, this.replace, this.matchCell, this.columns);
		}
		else
		{
			this.table.replace(this.regexp, this.replace, this.columns);
		}
		
		super.setResult(null);
	}
}

