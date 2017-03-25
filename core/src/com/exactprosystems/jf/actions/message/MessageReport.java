////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.message;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import java.util.List;
import java.util.Map.Entry;

@ActionAttribute(
		group					= ActionGroups.Messages,
		generalDescription 		=
 "The purpose of the action is to display the object content {{$MapMessage$}} in the report on the matrix run. "
+ "The object type {{$MapMessaget$}} is the tree-based representation of message which consists of fields. "
+ "It is applied when it is necessary to send or receive {{$MapMessage$}}.",
		additionFieldsAllowed 	= false,
		examples = "",
		seeAlsoClass = { MessageCreate.class }
	)
public class MessageReport extends AbstractAction 
{
    public final static String messageName        = "MapMessage";
    public final static String beforeTestCaseName = "BeforeTestCase";
    public final static String titleName          = "Title";
    public final static String toReportName       = "ToReport";

	@ActionFieldAttribute(name=toReportName, mandatory = false, description = 
            "This parameter is used for directing the output from the given object to the external report "
          + "created by the {{$ReportStart$}} action.")
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = messageName, mandatory = true, description = "Object {{$MapMessage$}}, which is required to output.")
	protected MapMessage 	message 	= null;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, description = "It accepts id test case before " +
			"which the text will be displayed in the report.")
	protected String 	beforeTestCase 	= null;

	@ActionFieldAttribute(name = titleName, mandatory = true, description = "The title of the text.")
	protected String 	title 	= null;

	
	public MessageReport()
	{
	}

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
	public void initDefaultValues() 
	{
		this.beforeTestCase = null;
		this.toReport = null;
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

        ReportTable table = report.addExplicitTable(Str.asString(this.title), this.beforeTestCase, true, 0,
                new int[] { 50, 50 }, new String[] { "Field", "Value" });

        for (Entry<String, Object> entry : this.message.entrySet())
        {
            table.addValues(entry.getKey(), entry.getValue());
        }
        if (this.message.getSource() != null)
        {
            table.addValues("Source", this.message.getSource());
        }

        super.setResult(null);
    }
}

