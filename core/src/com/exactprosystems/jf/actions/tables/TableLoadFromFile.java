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
		suffix					= "TBL",
		generalDescription 		= "Load data from a file as a table.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Table structure.",
		outputType				= Table.class
	)
public class TableLoadFromFile extends AbstractAction 
{
	public final static String fileName 		= "File";
	public final static String delimiterName 	= "Delimiter";

	@ActionFieldAttribute(name = fileName, mandatory = true, description = "File name.")
	protected String 	file 	= null;

	@ActionFieldAttribute(name = delimiterName, mandatory = false, description = "Delimiter of fields in the file.")
	protected String	delimiter;

	@Override
	public void initDefaultValues() 
	{
		delimiter 	= ";";
	}
	
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
		}
	}
	
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(new Table(this.file, this.delimiter.charAt(0), evaluator));
	}
}
