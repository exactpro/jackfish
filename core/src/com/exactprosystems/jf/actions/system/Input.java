////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ActionAttribute(
		group					   = ActionGroups.System,
		constantGeneralDescription = R.INPUT_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
        constantOutputDescription  = R.INPUT_OUTPUT_DESC,
		suffix					   = "INP",
        outputType                 = Object.class
		
	)
public class Input extends AbstractAction 
{
    public final static String defaultValueName = "DefaultValue";
    public final static String dataSourceName   = "DataSource";
    public final static String helpKindName     = "HelpKind";
    public static final String titleName		= "Title";
    public final static String timeoutName      = "Timeout";

    @ActionFieldAttribute(name=titleName, mandatory = true, constantDescription = R.INPUT_TITLE)
    protected String title;

	@ActionFieldAttribute(name = defaultValueName, mandatory = true, constantDescription = R.INPUT_DEFAULT_VALUE)
	protected Object defaultValue; 
	
    @ActionFieldAttribute(name = helpKindName, mandatory = true, constantDescription = R.INPUT_HELP_KIND)
    protected HelpKind helpKind;

    @ActionFieldAttribute(name = dataSourceName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.INPUT_DATA_SOURCE)
    protected Object dataSource; 

    @ActionFieldAttribute(name = timeoutName, mandatory = false, def = DefaultValuePool.IntMin, constantDescription = R.INPUT_TIMEOUT)
    protected Integer timeout;
    
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
		List<ReadableValue> list = new ArrayList<>();
		
		if (this.dataSource == null)
		{}
		else if (this.dataSource.getClass().isArray())
		{
		    Arrays.stream((Object[])this.dataSource).forEach(o -> list.add(new ReadableValue(Str.asString(o))));
		}
		else if (this.dataSource instanceof List<?>)
		{
		    ((List<?>)this.dataSource).stream().forEach(o -> list.add(new ReadableValue(Str.asString(o))));
		}
        else if (this.dataSource instanceof Map<?,?>)
        {
            ((Map<?,?>)this.dataSource).entrySet().stream().forEach(o -> list.add(new ReadableValue(Str.asString(o.getKey()), Str.asString(o.getValue()))));
        }
        else
        {
            list.add(new ReadableValue(Str.asString(this.dataSource)));
        }
		
		Object input = context.getFactory().input(evaluator, this.title, this.defaultValue, this.helpKind, list, this.timeout);
		if (input == null)
		{
            super.setError("User cancelled input", ErrorKind.INPUT_CANCELLED);
            return;
		}
		super.setResult(input);
	}
}
