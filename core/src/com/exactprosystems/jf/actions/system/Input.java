////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixError;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.tool.Common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@ActionAttribute(
		group					= ActionGroups.System,
		generalDescription 		= "Input a value via interaction with a user",
		additionFieldsAllowed 	= false,
        outputDescription       = "Result of choosing a value by user, or default value if the timeout expired",
		suffix					= "INP",
        outputType              = Object.class
		
	)
public class Input extends AbstractAction 
{
    public final static String defaultValueName = "DefaultValue";
    public final static String dataSourceName   = "DataSource";
    public final static String helpKindName     = "HelpKind";
    public static final String titleName		= "Title";
    public final static String timeoutName      = "Timeout";

    @ActionFieldAttribute(name=titleName, mandatory = true, description = "Title of user input")
    protected String title;

	@ActionFieldAttribute(name = defaultValueName, mandatory = true, description = "Default value if the timout expiered.")
	protected Object defaultValue; 
	
    @ActionFieldAttribute(name = dataSourceName, mandatory = false, description = "Collection (a list or a map) to choice value.")
    protected Object dataSource; 

    @ActionFieldAttribute(name = helpKindName, mandatory = true, description = "How to help user enter or choose a value.")
    protected HelpKind helpKind;

    @ActionFieldAttribute(name = timeoutName, mandatory = false, description = "If this timeout expires default value will be used.")
    protected Integer timeout;
    
    public Input()
	{
	}
	
	@Override
	public void initDefaultValues() 
	{
		this.dataSource = null;
		this.timeout = 10000;
	}
	
    @Override
    protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
    {
        switch (fieldName)
        {
            case helpKindName:
                return HelpKind.ChooseFromList;
        }
        return null;
    }

    @Override
    protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
    {
        switch (parameterToFill)
        {
            case helpKindName:
				Arrays.stream(HelpKind.values()).forEach(a -> list.add(new ReadableValue(HelpKind.class.getSimpleName() + "." + a.name())));;
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
		if (this.defaultValue == null)
		{
			super.setError("Default value is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
		
		List<ReadableValue> list = new ArrayList<>();
		
		if (this.dataSource == null)
		{}
		else if (this.dataSource.getClass().isArray())
		{
		    Arrays.stream((Object[])this.dataSource).forEach(o -> list.add(new ReadableValue(Common.createLiteral(o, evaluator))));
		}
		else if (this.dataSource instanceof List<?>)
		{
		    ((List<?>)this.dataSource).stream().forEach(o -> list.add(new ReadableValue(Common.createLiteral(o, evaluator))));
		}
        else if (this.dataSource instanceof Map<?,?>)
        {
            ((Map<?,?>)this.dataSource).entrySet().stream().forEach(o -> list.add(new ReadableValue(Common.createLiteral(o.getKey(), evaluator), Str.asString(o.getValue()))));
        }
        else
        {
            list.add(new ReadableValue(Str.asString(this.dataSource)));
        }
		
		Object input = context.getFactory().input(context.getEvaluator(), this.title, this.defaultValue, this.helpKind, list, this.timeout);
		if (input instanceof MatrixError)
		{
			super.setError(((MatrixError) input).Message, ((MatrixError) input).Kind);
			return;
		}
		super.setResult(input);
	}
}
