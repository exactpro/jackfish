/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions.system;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.client.ClientHelper;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@ActionAttribute(
        group                         = ActionGroups.System,
        constantGeneralDescription    = R.CHECK_GENERAL_DESC,
        additionFieldsAllowed         = true,
        outputType                    = Boolean.class,
        constantOutputDescription     = R.CHECK_OUTPUT_DESC,
        constantAdditionalDescription = R.CHECK_ADDITIONAL_DESC,
        constantExamples              = R.CHECK_EXAMPLE
    )
public class Check extends AbstractAction 
{
	public static final String dontFailName = "DoNotFail";
	public static final String actualName   = "Actual";

	@ActionFieldAttribute(name = actualName, mandatory = true, constantDescription = R.CHECK_ACTUAL)
	protected Map<String, Object> actual;

	@ActionFieldAttribute(name = dontFailName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.CHECK_DONT_FAIL)
	protected Boolean dontFail;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		if (dontFailName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (dontFailName.equals(parameterToFill))
		{
			list.add(ReadableValue.TRUE);
			list.add(ReadableValue.FALSE);
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Map<String, String> diff = ClientHelper.difference(new MapMessage(this.actual, null), Condition.convertToCondition(parameters.select(TypeMandatory.Extra).makeCopy()));

		if (diff == null)
		{
			super.setResult(true);
		}
		else
		{
			ReportTable table = report.addTable("Mismatched fields:", null, true, true, new int[]{20, 80}, "Name", "Expected + Actual");
			for (Entry<String, String> entry : diff.entrySet())
			{
				table.addValues(entry.getKey(), entry.getValue());
			}

			if (this.dontFail)
			{
				super.setResult(false);
			}
			else
			{
				super.setError("Object does not match.", ErrorKind.NOT_EQUAL);
			}
		}
	}

}
