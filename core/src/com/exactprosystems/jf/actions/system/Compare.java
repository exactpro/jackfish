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
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

@ActionAttribute(
        group                   = ActionGroups.System,
        generalDescription      = "The following action is needed to compare two structured objects that implement interface"
                + " Map, for example: table rows, MapMessages.",
        additionFieldsAllowed   = false,
        suffix                  = "CMP",
        outputType              = Table.class,
        outputDescription       = "A table as a resulf of compare.",
        additionalDescription = "Helps to pass values and their names that are needed to be compared.",
        examples = "{{`1. Make a table with 2 rows and columns, add values.`}}"
                + "2. Compare values from the first row of the table with those ones that are specified in Expected parameter of action Compare. "
                + "{{##Id;#RawTable\n"
                + "DATA1;Table\n"
                + "@;Country;Capital\n"
                + "0;Russia;Moscow\n"
                + "1;Germany;Berlin\n"
                + "#EndRawTable\n"
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

    @ActionFieldAttribute(name = expectedName, mandatory = true, description = "A set of names and values that should be compared as expected value.")
    protected Map<String, Object> expected     = Collections.emptyMap();

    @ActionFieldAttribute(name = dontFailName, mandatory = false, def = DefaultValuePool.False, description = "If true, then when identifying"
            + " differences as a result of the comparison, action will still be marked as Passed, otherwise, as "
            + "Failed. By default, a parameter has a false value.")
    protected Boolean             dontFail;

    @Override
    protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
    {
        switch (fieldName)
        {
            case dontFailName:
                return HelpKind.ChooseFromList;
        }
        return null;
    }

    @Override
    protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
    {
        switch (parameterToFill)
        {
            case dontFailName:
                list.add(ReadableValue.TRUE);
                list.add(ReadableValue.FALSE);
                break;
        }
    }

    @Override
    public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator)
            throws Exception
    {
        String[] headers = new String[] { "Field", "Expected", "Actual", "Result" };
        ReportTable table = report.addTable("Comparing fields:", null, true, true, new int[] { 25, 25, 25, 25 }, headers);
        boolean res = true;
        Table resultTable = new Table(headers, evaluator);
        
        for (Entry<String, Object> expectedEntry : this.expected.entrySet())
        {
            String name = expectedEntry.getKey();
            Object expectedValue = expectedEntry.getValue();
            
            boolean found = this.actual.containsKey(name);
            Object actualValue = this.actual.get(name);
            Object[] line = null;
            
            if (found)
            {
                boolean comp =  Objects.equals(expectedValue, actualValue);
                line = new Object[] { name, Str.asString(expectedValue), Str.asString(actualValue), comp ? Result.Passed : Result.Failed };
                res = res && comp;
            }
            else
            {
                line = new Object[] { name, Str.asString(expectedValue), "<not found>", Result.Failed };
                res = false;
            }
            
            table.addValues(line);
            resultTable.addValue(line);
        }

        super.setResult(resultTable);

        if (!res && !this.dontFail)
        {
            super.setError("Object does not match.", ErrorKind.NOT_EQUAL);
        }
    }
    
}
