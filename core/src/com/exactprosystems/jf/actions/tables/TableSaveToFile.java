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
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		generalDescription 		= "This action is used to save a table to the file with csv structure."
				+ "{{`CSV (Comma-Separated Values) is a text format, used for displaying data from the table."
				+ "Specification: Each line in the file is one line from the table. The first line contains column titles.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "True if saved successfully.",
		outputType				= Boolean.class,
		examples = "{{` 1. Create a table with columns Name and Age. `}}"
				+ "{{` 2. Add values to the first line of the table. `}}"
				+ "{{` 3. Save the table into the file dest.csv. `}}"
				+ "{{##Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name;Age\n"
				+ "0;Mike;25\n"
				+ "#EndRawTable\n"
				+ "#Id;#Action;#Table;#File\n"
				+ "TSTF;TableSaveToFile;TC;’Path/dest.csv'\n"
				+ "#Assert;#Message\n"
				+ "TSTF.Out;'Table was not saved'#}}"
	)
public class TableSaveToFile extends AbstractAction 
{
	public final static String tableName = "Table";
	public final static String fileNameName = "File";
	public final static String delimiterName = "Delimiter";
	public final static String saveValuesName = "SaveValues";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "A table which is needed to be saved to a file")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = fileNameName, mandatory = true, description = "Path name to a target file where the "
			+ "table will be saved. The name of the file needs to be given with the name suffix")
	protected String 	fileName 	= null;

	@ActionFieldAttribute(name = delimiterName, mandatory = false, description = "Any character that separates the columns in the file. The default is ',' .\n")
	protected String	delimiter;

	@ActionFieldAttribute(name = saveValuesName, mandatory = false, description = "If the value is false , the value"
			+ " from the cell is saved, if the value is true the expression result is saved. Applicable for the cells"
			+ " of Expression type, see {{@TableConsiderColumnAs@}}.")
	protected Boolean	saveValues;

	public TableSaveToFile()
	{
	}
	
	@Override
	public void initDefaultValues()
	{
		delimiter 	= ";";
		saveValues 	= false;
	}
	
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
