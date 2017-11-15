////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import java.util.List;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

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
	public final static String tableName = "Table";
	public final static String fileNameName = "File";
	public final static String delimiterName = "Delimiter";
	public final static String saveValuesName = "SaveValues";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_SAVE_TO_FILE_TABLE)
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = fileNameName, mandatory = true, constantDescription = R.TABLE_SAVE_TO_FILE_FILE_NAME)
	protected String 	fileName 	= null;

	@ActionFieldAttribute(name = delimiterName, mandatory = false, def = DefaultValuePool.Semicolon, constantDescription = R.TABLE_SAVE_TO_FILE_DELIMITER)
	protected String	delimiter;

	@ActionFieldAttribute(name = saveValuesName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.TABLE_SAVE_TO_FILE_SAVE_VALUES)
	protected Boolean	saveValues;

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
		if (saveValues == null)
		{
			super.setError("SaveValues is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}

		if (delimiter == null)
		{
			super.setError("Delimiter is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}

		super.setResult(this.table.save(this.fileName, this.delimiter.charAt(0), this.saveValues, false));
	}
}
