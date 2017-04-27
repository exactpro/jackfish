////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.xml;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Xml;

import java.util.List;

@ActionAttribute(
		group					= ActionGroups.XML,
		suffix					= "XMLCMP",
		generalDescription 		= "The purpose of the action is to compare two Xml structures.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "True, if Xml structures are equal.",
		outputType				= Boolean.class,
		examples 				= "{{`1. Create an Xml structure from a file.`}}"
				+ "{{`2. Create two new Xml structures by choosing them using XPath from the uploaded Xml structure.`}}"
				+ "{{`3. Compare two Xml structures.`}}"
				+ "{{`4. Check the results of the comparison.`}}"
				+ "\n"
				+ "{{##Id;#Action;#File\n"
				+ "XML1;XmlLoadFromFile;'pathToTheFile'\n"
				+ "#Id;#Action;#Xpath;#NodeName;#Xml\n"
				+ "XML2;XmlSelect;'//friend';'newParent';XML1.Out\n"
				+ "#Id;#Action;#Expected;#Actual\n"
				+ "XMLCMP1;XmlCompare;XML2.Out.getChildren().get(0);XML2.Out.getChildren().get(1)\n"
				+ "#Assert;#Message\n"
				+ "XMLCMP1.Out;'Xmls does not equals'#}}"
	)
public class XmlCompare extends AbstractAction 
{
	public final static String actualName          = "Actual";
	public final static String expectedName        = "Expected";
	public final static String ignoreNodeOrderName = "IgnoreNodeOrder";

	@ActionFieldAttribute(name = actualName, mandatory = true, description = "An Xml structure is the one that has to undergo comparison.")
	protected Xml actual = null;

	@ActionFieldAttribute(name = expectedName, mandatory = true, description = "An Xml structure is the one that comparison has to be done to.")
	protected Xml expected = null;

	@ActionFieldAttribute(name = ignoreNodeOrderName, mandatory = false, description = "Ignore node order.")
	protected Boolean ignoreNodesOrder;

	public XmlCompare()
	{
	}

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

	@Override
	public void initDefaultValues()
	{
		this.ignoreNodesOrder = false;
	}
}
