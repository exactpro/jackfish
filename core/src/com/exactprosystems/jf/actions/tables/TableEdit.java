////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ActionAttribute(
		group						  = ActionGroups.Tables,
		constantGeneralDescription 	  = R.TABLE_EDIT_GENERAL_DESK,
		additionFieldsAllowed 		  = true,
		constantAdditionalDescription = R.TABLE_EDIT_ADDITIONAL_DESC,
        constantOutputDescription 	  = R.TABLE_EDIT_OUTPUT_DESC,
		suffix						  = "TBEDT",
        outputType              	  = Boolean.class,
		constantExamples 			  = R.TABLE_EDIT_EXAMPLE
	)
public class TableEdit extends AbstractAction 
{
    public static final String titleName          = "Title";
    public final static String tableName          = "Table";

    @ActionFieldAttribute(name=titleName, mandatory = true, description = "Title of user input")
    protected String title;

    @ActionFieldAttribute(name = tableName, mandatory = true, description = "A table which is needed to to be edited.")
    protected Table     table   = null;
	
    @Override
    protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
    {
        switch (fieldName)
        {
            case titleName:
            case tableName:
                break;

            default:
                return HelpKind.ChooseFromList;
        }
        return null;
    }

    @Override
    protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
    {
        switch (parameterToFill)
        {
            case titleName:
            case tableName:
                break;
    
            default:
                list.add(ReadableValue.TRUE);
                list.add(ReadableValue.FALSE);
                break;
        }
    }
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.title == null)
		{
			super.setError("Title is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
		
		Map<String, Boolean> columns = new LinkedHashMap<>();
		for (Parameter param : parameters.select(TypeMandatory.Extra))
		{
		    String name = param.getName();
		    Object value = param.getValue();
		    if (value == null)
		    {
		        columns.put(name, false);
		    }
		    else if (value instanceof Boolean)
		    {
		        columns.put(name, (Boolean)value);
		    }
		    else
		    {
		        super.setError("Parameter " + name, ErrorKind.WRONG_PARAMETERS);
		        return;
		    }
		}
		
        boolean input = context.getFactory().editTable(context.getEvaluator(), this.title, this.table, columns);
		super.setResult(input);
	}
}
