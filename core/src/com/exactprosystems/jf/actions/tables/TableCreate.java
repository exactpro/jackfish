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
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.RawTable;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					      = ActionGroups.Tables,
		suffix					      = "TBL",
		constantGeneralDescription    = R.TABLE_CREATE_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
		constantAdditionalDescription = R.TABLE_CREATE_ADDITIONAL_DESC,
		constantOutputDescription 	  = R.TABLE_CREATE_OUTPUT_DESC,
		outputType				      = Table.class,
		constantExamples 			  = R.TABLE_CREATE_EXAMPLE,
		seeAlsoClass = {RawTable.class, TableLoadFromDir.class, TableLoadFromFile.class, TableSelect.class}
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

		for (String columnName : headers)
		{
			if(columnName.isEmpty())
			{
				super.setError("The column name does not have to contain an empty value.", ErrorKind.EMPTY_PARAMETER);
				return;
			}
		}
		
		super.setResult(new Table(headers, evaluator));
	}
}
