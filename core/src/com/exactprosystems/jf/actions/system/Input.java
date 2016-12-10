////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ExecuteResult;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem.HelpKind;

@ActionAttribute(
		group					= ActionGroups.System,
		generalDescription 		= "Input a value via interaction with a user",
		additionFieldsAllowed 	= false,
        outputDescription       = "Result of choosing a value by user, or default value if the timeout expired",
        outputType              = Object.class
		
	)
public class Input extends AbstractAction 
{
    public final static String defaultValueName = "DefaultValue";
    public final static String timeoutName      = "Timeout";
    public final static String dataSourceName   = "DataSource";
    public final static String helpKindName     = "HelpKind";
    
	@ActionFieldAttribute(name = defaultValueName, mandatory = true, description = "Default value if the timout expiered.")
	protected Object defaultValue; 
	
    @ActionFieldAttribute(name = timeoutName, mandatory = true, description = "Timeout im milliseconds.")
    protected Integer timeout; 

    @ActionFieldAttribute(name = timeoutName, mandatory = true, description = "Collection to choice value.")
    protected Collection<?> dataSource; 

    @ActionFieldAttribute(name = helpKindName, mandatory = true, description = "How to help user enter or choose a value.")
    protected ActionItem.HelpKind helpKind; 

    
    public Input()
	{
	}
	
	@Override
	public void initDefaultValues() 
	{
		this.helpKind = HelpKind.Expression;
	}
	
    @Override
    protected ActionItem.HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
    {
        switch (fieldName)
        {
            case helpKindName:
                return ActionItem.HelpKind.ChooseFromList;
        }
        return null;
    }

    @Override
    protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
    {
        switch (parameterToFill)
        {
            case helpKindName:
                Arrays.stream(ActionItem.HelpKind.values()).forEach(a -> list.add(new ReadableValue(a.name())));;
                break;
        }
    }
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		// TODO implement logic here
		
		super.setResult(null);
	}

	@Override
	protected boolean reportAllDetail()
	{
		return false;
	}
}
