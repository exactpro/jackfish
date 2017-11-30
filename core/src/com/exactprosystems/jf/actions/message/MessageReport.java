////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.message;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@ActionAttribute(
		group					   = ActionGroups.Messages,
		constantGeneralDescription = R.MESSAGE_REPORT_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
        constantExamples            = R.MESSAGE_REPORT_EXAMPLE,
		seeAlsoClass               = { MessageCreate.class }
	)
public class MessageReport extends AbstractAction 
{
    public final static String columnsField       = "Field";
    public final static String columnValue        = "Value";
    
    public final static String messageName        = "MapMessage";
    public final static String beforeTestCaseName = "BeforeTestCase";
    public final static String titleName          = "Title";
    public final static String toReportName       = "ToReport";

	@ActionFieldAttribute(name = messageName, mandatory = true, constantDescription = R.MESSAGE_REPORT_MESSAGE)
	protected MapMessage 	message 	= null;

	@ActionFieldAttribute(name = titleName, mandatory = true, constantDescription = R.MESSAGE_REPORT_TITLE)
	protected String 	title 	= null;

    @ActionFieldAttribute(name=toReportName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.MESSAGE_REPORT_TO_REPORT)
    protected ReportBuilder toReport;

    @ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.MESSAGE_REPORT_BEFORE_TESTCASE)
    protected String    beforeTestCase  = null;

    @Override
    protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
    {
        switch (fieldName)
        {
            case beforeTestCaseName:
                return HelpKind.ChooseFromList;
        }

        return null;
    }

    @Override
    protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
    {
        switch (parameterToFill)
        {
            case beforeTestCaseName:
                ActionsReportHelper.fillListForParameter(super.owner.getMatrix(),  list, context.getEvaluator());
                break;
            default:
        }
    }

    @Override
    public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator)
            throws Exception
    {
        if (this.message == null)
        {
            super.setError(messageName, ErrorKind.EMPTY_PARAMETER);
            return;
        }

        report = this.toReport == null ? report : this.toReport;
        this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());

        Table table = new Table(new String[] { columnsField, columnValue }, evaluator);
        table.considerAsGroup(columnsField);

        outMessage(table, this.message, "");

        if (this.message.getSource() != null)
        {
            addRow(table, "Source", this.message.getSource());
        }

        table.report(report, this.title, this.beforeTestCase, false, true);
        super.setResult(null);
    }
    
    private void outMessage(Table table, MapMessage message, String path)
    {
        for (Entry<String, Object> entry : message.entrySet())
        {
            String name = entry.getKey();
            Object value = entry.getValue();
            
            if (value.getClass().isArray())
            {
                int count = 0;
                Object[] array = (Object[])value;
				String oldPath = path;
				path = makePath(path, name + "( " + array.length + " )");
				addRow(table, path+"/*", "");

				for (Object group : array)
                {
                    if (group instanceof MapMessage)
                    {
                        addRow(table, makePath(path, name + "[" + count + "]/*"), "");
                        outMessage(table, (MapMessage)group, makePath(path, name));
                    }
                    count++;
                }
				path = oldPath;
			}
			else if (value instanceof MapMessage)
			{
				addRow(table, makePath(path, name), "");
				outMessage(table, (MapMessage)value, makePath(path, name));
			}
			else
			{
				addRow(table, makePath(path, name), Str.asString(value));
			}
		}
    }
    
    private String makePath(String path, String addon)
    {
        if (Str.IsNullOrEmpty(path))
        {
            return addon;
        }
        return path + "/" + addon;
    }
    
    private void addRow(Table table, String field, String value)
    {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put(columnsField, field);
        row.put(columnValue,  value);
        table.addValue(table.size(), row);
    }
    
   
}

