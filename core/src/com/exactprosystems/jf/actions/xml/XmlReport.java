////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.xml;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Xml;
import com.exactprosystems.jf.actions.ActionsReportHelper;

import java.util.List;

@ActionAttribute(
		group					= ActionGroups.XML,
		generalDescription 		= "The purpose of this action is to map the content of the Xml object in the report of a matrix run."
				+ "It is used when there is a need to go through the content of the Xml object.",
		additionFieldsAllowed 	= false,
		examples 				= "{{`1. Create an Xml object by downloading it from the file.`}}"
				+ "{{`Contents of an xml file:`}} "
				+ "{{#<note> \n"
				+ "<to>\n"
				+ "<friend>\n"
				+ "<name id=\"first\">Tove</name>\n"
				+ "</friend>\n"
				+ "</to>\n"
				+ "<from>\n"
				+ "<friend>\n"
				+ "<name id=\"second\">Jani</name>\n"
				+ "</friend>\n"
				+ "</from>\n"
				+ "<heading>Reminder</heading>\n"
				+ "<body>Don't forget me this weekend!</body>\n"
				+ "</note>#}}"
				+ "\n"
				+ "{{`2. Reflect the content of the Xml object in the report.`}} "
				+ "{{##Id;#Action;#File\n"
				+ "XML1;XmlLoadFromFile;'/path/Xml.xml'\n"
				+ "\n"
				+ "#Action;#Xml;#Title\n"
				+ "XmlReport;Xml1;’Xml report’#}}"
	)
public class XmlReport extends AbstractAction 
{
    public final static String xmlName            = "Xml";
    public final static String beforeTestCaseName = "BeforeTestCase";
    public final static String titleName          = "Title";
    public final static String toReportName       = "ToReport";

	@ActionFieldAttribute(name=toReportName, mandatory = false, description = 
            "This parameter is used for directing the output from the given object to the external report "
          + "created by the {{$ReportStart$}} action.")
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = xmlName, mandatory = true, description = "The Xml structure that needs to be kept (preserved).")
	protected Xml 	xml 	= null;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, description = "Enables to output the table on the highest level of the report.")
	protected String 	beforeTestCase 	= null;

	@ActionFieldAttribute(name = titleName, mandatory = true, description = "The heading of the output Xml structure.")
	protected String 	title 	= null;

	public XmlReport()
	{
	}

	@Override
	public void initDefaultValues() 
	{
		this.beforeTestCase = null;
		this.toReport = null;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
	    if (this.xml == null)
	    {
	        super.setError(xmlName, ErrorKind.EMPTY_PARAMETER);
	        return;
	    }
	    
	    report = this.toReport == null ? report : this.toReport;
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		this.xml.report(report, this.beforeTestCase, Str.asString(this.title));
		
		super.setResult(null);
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
}

