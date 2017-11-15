////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.xml;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Xml;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.XML,
		suffix					   = "XMLCMP",
		constantGeneralDescription = R.XML_COMPARE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.XML_COMPARE_OUTPUT_DESC,
		outputType				   = Boolean.class,
		constantExamples 		   = R.XML_COMPARE_EXAMPLE
	)
public class XmlCompare extends AbstractAction 
{
	public final static String actualName          = "Actual";
	public final static String expectedName        = "Expected";
	public final static String ignoreNodeOrderName = "IgnoreNodeOrder";

	@ActionFieldAttribute(name = actualName, mandatory = true, constantDescription = R.XML_COMPARE_ACTUAL)
	protected Xml actual = null;

	@ActionFieldAttribute(name = expectedName, mandatory = true, constantDescription = R.XML_COMPARE_EXPECTED)
	protected Xml expected = null;

	@ActionFieldAttribute(name = ignoreNodeOrderName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.XML_COMPARE_IGNORE_NODE_ORDER)
	protected Boolean ignoreNodesOrder;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case ignoreNodeOrderName :
				return HelpKind.ChooseFromList;
		}
		return super.howHelpWithParameterDerived(context, parameters, fieldName);
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case ignoreNodeOrderName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(this.actual.compareTo(this.expected, this.ignoreNodesOrder));
	}
}
