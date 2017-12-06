////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.Tables,
		constantGeneralDescription = R.TABLE_SAVE_TO_FILE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.TABLE_SAVE_TO_FILE_OUTPUT_DESC,
		outputType			 	   = Boolean.class,
		constantExamples           = R.TABLE_SAVE_TO_FILE_EXAMPLE
)
public class TableSaveToFile extends AbstractAction 
{
	public static final String tableName      = "Table";
	public static final String fileNameName   = "File";
	public static final String delimiterName  = "Delimiter";
	public static final String saveValuesName = "SaveValues";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_SAVE_TO_FILE_TABLE)
	protected Table table;

	@ActionFieldAttribute(name = fileNameName, mandatory = true, constantDescription = R.TABLE_SAVE_TO_FILE_FILE_NAME)
	protected String fileName;

	@ActionFieldAttribute(name = delimiterName, mandatory = false, def = DefaultValuePool.Semicolon, constantDescription = R.TABLE_SAVE_TO_FILE_DELIMITER)
	protected String delimiter;

	@ActionFieldAttribute(name = saveValuesName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.TABLE_SAVE_TO_FILE_SAVE_VALUES)
	protected Boolean saveValues;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case fileNameName:
				return HelpKind.ChooseSaveFile;

			case delimiterName:
			case saveValuesName:
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

			case saveValuesName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(this.table.save(this.fileName, this.delimiter.charAt(0), this.saveValues, false));
	}
}
