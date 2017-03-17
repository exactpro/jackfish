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
		generalDescription 		= "This action is used for searching a suitible line numbers in the table given. "
				+ "Can be applied when it Is needed to get all indexes of the such lines.",
		additionFieldsAllowed 	= true,
		additionalDescription   = "Columns containing the data which defines the search conditions. Column title is given in"
				+ " the parameter’s value. In the value it is needed to specify the content which defines the search.",
		outputDescription 		= "Outputs the index list mathching to condtions.",
		outputType				= List.class,
		seeAlso 				= "{{@RawTable@}}, {{@TableLoadFromDir@}}, {{@TableLoadFromFile@}}, {{@TableCreate@}}, {{@TableSelect@}},  ",
		examples = "", // TODO make the examples, pls
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

