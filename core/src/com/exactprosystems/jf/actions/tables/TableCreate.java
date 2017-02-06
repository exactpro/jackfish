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
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		suffix					= "TBL",
		generalDescription 		= "This action is determined to create a table (object type {{$Table$}}). Object Table is set"
				+ " as the two-dimensional structure consisting of rows and columns. Ordering of Rows starts with 0, columns are named. "
				+ "Object Table can be created with  {{@RawTable@}}, with actions {{@TableCreate@}} , {{@TableLoadFromFile@}} ,"
				+ " {{@TableLoadFromDir@}} , {{@TableSelect@}} and method {{@Do.getTable()@}}. "
				+ "{{`|header1|header2|`}} "
				+ "{{`|value1 |value2 |`}} "
				+ "{{`|value1 |value2 |`}} ",
		additionFieldsAllowed 	= true,
		additionalDescription 	= "Additional parameters are used to assign column titles. The value field of parameter is left empty.",
		outputDescription 		= "Table structure.",
		outputType				= Table.class,
		seeAlso 				= "{{@RawTable@}}, {{@TableLoadFromDir@}}, {{@TableLoadFromFile@}}, {{@TableSelect@}}",
		examples 				= "{{`1.Create a table with columns Name and Age.`}}"
				+ "{{`2. Verify that the created object has columns  set initially.`}}\n"
				+ "{{##Id;#Action;#Name;#Age\n"
				+ "TC;TableCreate;;\n"
				+ "\n"
				+ "#Assert;#Message\n"
				+ "TC.Out.getHeader(0) == 'Name' && TC.Out.getHeader(1) == 'Age';'Table is not correct'#}}\n"
	)
public class TableCreate extends AbstractAction 
{
	public TableCreate()
	{
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		String[] headers = parameters.select(TypeMandatory.Extra).keySet().toArray(new String[] {});
		
		super.setResult(new Table(headers, evaluator));
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
