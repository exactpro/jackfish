////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.client.ClientHelper;
import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@ActionAttribute(
        group                   = ActionGroups.System,
        generalDescription      = "The following action is needed to compare two structured objects that implement interface"
                + " Map, for example: table rows, MapMessages.",
        additionFieldsAllowed   = false,
        outputType              = Boolean.class,
        outputDescription       = "A logical variable, true if matching values are equal, else – false. If there are"
                + " differences between matching values table {{$Mismatched$}} fields that contains information about "
                + "mismatched values, is added to the report.",
        additionalDescription = "Helps to pass values and their names that are needed to be compared.",
        examples = "{{`1. Make a table with 2 rows and columns, add values.`}}"
                + "2. Compare values from the first row of the table with those ones that are specified in Expected parameter of action Compare. "
                + "{{##Id;#RawTable\n"
                + "DATA1;Table\n"
                + "@;Country;Capital\n"
                + "0;Russia;Moscow\n"
                + "1;Germany;Berlin\n"
                + "#EndRawTable\n"
                + "\n"
                + "\n"
                + "#Action;#DoNotFail;#Actual;#Expected\n"
                + "Compare;true;DATA1.get(0); { 'Country':'Russia', 'Capital':'Berlin' } #}}"
    )
public class Compare extends AbstractAction
{
    public final static String    dontFailName = "DoNotFail";
    public final static String    actualName   = "Actual";
    public final static String    expectedName = "Expected";

    @ActionFieldAttribute(name = actualName, mandatory = true, description = "A set of names and values that should be compared as actual value.")
    protected Map<String, Object> actual       = Collections.emptyMap();

    @ActionFieldAttribute(name = dontFailName, mandatory = false, description = "If true, then when identifying"
            + " differences as a result of the comparison, action will still be marked as Passed, otherwise, as "
            + "Failed. By default, a parameter has a false value.")
    protected Boolean             dontFail;

    @ActionFieldAttribute(name = expectedName, mandatory = true, description = "A set of names and values that should be compared as expected value.")
    protected Map<String, Object> expected     = Collections.emptyMap();

    public Compare()
    {
    }

    @Override
    public void initDefaultValues()
    {
        this.dontFail = false;
    }

    @Override
    public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator)
            throws Exception
    {
        ReportTable table = report.addTable("Comparing fields:", null, true, 1, new int[] { 25, 25, 25, 25 }, 
                "Field", "Expected", "Actual", "Result");

        boolean res = true;
        
        for (Entry<String, Object> expectedEntry : this.expected.entrySet())
        {
            String name = expectedEntry.getKey();
            Object expectedValue = expectedEntry.getValue();
            
            boolean found = this.actual.containsKey(name);
            Object actualValue = this.actual.get(name);
            
            if (found)
            {
                boolean comp = false;
                if (expectedValue == null)
                {
                    comp = expectedValue == actualValue;
                }
                else 
                {
                    comp = expectedValue.equals(actualValue);
                }
                
                table.addValues(name, Str.asString(expectedValue), Str.asString(actualValue), comp ? Result.Passed : Result.Failed );
                res = res && comp;
            }
            else
            {
                table.addValues(name, Str.asString(expectedValue), "<not found>", Result.Failed);
                res = false;
            }
        }

        if (!res && !this.dontFail)
        {
            super.setError("Object does not match.", ErrorKind.NOT_EQUAL);
            return;
        }

        super.setResult(res);
    }
    
}
