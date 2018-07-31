/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.RawTable;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.Tables,
		suffix					   = "TBL",
		constantGeneralDescription = R.TABLE_LOAD_FROM_FILE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.TABLE_LOAD_FROM_FILE_OUTPUT_DESC,
		outputType				   = Table.class,
		constantExamples 		   = R.TABLE_LOAD_FROM_FILE_EXAMPLE
,
		seeAlsoClass = {RawTable.class, TableLoadFromDir.class, TableCreate.class, TableSelect.class}
	)
public class TableLoadFromFile extends AbstractAction 
{
	public static final String fileName      = "File";
	public static final String delimiterName = "Delimiter";

	@ActionFieldAttribute(name = fileName, mandatory = true, constantDescription = R.TABLE_LOAD_FROM_FILE_FILE)
	protected String file;

	@ActionFieldAttribute(name = delimiterName, mandatory = false, def = DefaultValuePool.Semicolon, constantDescription = R.TABLE_LOAD_FROM_FILE_DELIMITER)
	protected String delimiter;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case fileName:
				return HelpKind.ChooseOpenFile;

			case delimiterName:
				return HelpKind.ChooseFromList;
		}

		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case delimiterName:
				list.add(new ReadableValue(context.getEvaluator().createString(",")));
				list.add(new ReadableValue(context.getEvaluator().createString(";")));
				break;

			default:
		}
	}


	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(new Table(this.file, this.delimiter.charAt(0), evaluator));
	}
}
