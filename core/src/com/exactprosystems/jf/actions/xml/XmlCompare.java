////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.xml;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Xml;

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
				+ "XML1;XmlLoadFromFile;'/home/victor.krasnovid/Desktop/Xml.xml'\n"
				+ "\n"
				+ "#Id;#Action;#Xpath;#NodeName;#Xml\n"
				+ "XML2;XmlSelect;'//friend';'newParent';XML1.Out\n"
				+ "\n"
				+ "#Id;#Action;#Expected;#Actual\n"
				+ "XMLCMP1;XmlCompare;XML2.Out.getChildren().get(0);XML2.Out.getChildren().get(1)\n"
				+ "\n"
				+ "#Assert;#Message\n"
				+ "XMLCMP1.Out;'Xmls does not equals'#}}"
	)
public class XmlCompare extends AbstractAction 
{
	public final static String actualName = "Actual";
	public final static String expectedName = "Expected";

	@ActionFieldAttribute(name = actualName, mandatory = true, description = "An Xml structure is the one that has to undergo comparison.")
	protected Xml actual = null;

	@ActionFieldAttribute(name = expectedName, mandatory = true, description = "An Xml structure is the one that comparison has to be done to.")
	protected Xml expected = null;

	public XmlCompare()
	{
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.actual == null)
		{
			super.setError("Actual XML object is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}

		if (this.expected == null)
		{
			super.setError("Expected XML object is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
	
		boolean res = this.actual.equals(this.expected);
		
		super.setResult(res);
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
