////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import java.util.List;
import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.RawTable;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		suffix					= "TBLIDX",
		generalDescription 		= "This action is used for searching a suitable line numbers in the table given. "
				+ "Can be applied when it Is needed to get all indexes of the such lines.",
		additionFieldsAllowed 	= true,
		additionalDescription   = "Columns containing the data which defines the search conditions. Column title is given in"
				+ " the parameter’s value. In the value it is needed to specify the content which defines the search.",
		outputDescription 		= "Outputs the index list matching to conditions.",
		outputType				= List.class,
		examples = "{{#" +
				"#Id;#RawTable\n" +
				"TC;Table\n" +
				"@;Name;Column\n" +
				"0;Mike;2\n" +
				"1;John;32\n" +
				"2;Fred;2\n" +
				"3;Mike;1\n" +
				"4;Angel;10\n" +
				"5;John;40\n" +
				"#EndRawTable\n" +
				"\n" +
				"#Action;$Table;$Integer\n" +
				"TableConsiderColumnsAs;TC;'Column'\n" +
				"\n" +
				"#Id;#Action;$Table;Column;Name\n" +
				"TBLIDX1;TableGetRowIndexes;TC;new NumberCondition('Column','>',2);'John'\n" +
				"\n" +
				"#Assert;#Message\n" +
				"TBLIDX1.Out == [1,5];\n" +
				"\n" +
				"#}}",
		seeAlsoClass = {RawTable.class, TableLoadFromFile.class, TableLoadFromDir.class, TableCreate.class, TableSelect.class}
	)
public class TableGetRowIndexes extends AbstractAction 
{
	public final static String tableName = "Table";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "The table in which row is serached")
	protected Table 	table 	= null;

	public TableGetRowIndexes()
	{
	}
	
	@Override
	public void initDefaultValues() 
	{
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Parameters extra = parameters.select(TypeMandatory.Extra);

		Condition[] conditions = Condition.convertToCondition(extra);
		List<Integer> indexes = this.table.findAllIndexes(conditions);
		
		super.setResult(indexes);
	}
}

