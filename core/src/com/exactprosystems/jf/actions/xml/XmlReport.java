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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Xml;

@ActionAttribute(
		group					= ActionGroups.XML,
		generalDescription 		= "Reports XML object to the report.",
		additionFieldsAllowed 	= false
	)
public class XmlReport extends AbstractAction 
{
	public final static String xmlName 	= "Xml";
	public final static String beforeTestCaseName = "BeforeTestCase";
	public final static String titleName = "Title";
	public final static String	toReportName		= "ToReport";

	@ActionFieldAttribute(name=toReportName, mandatory = false, description = "Rerouting report")
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = xmlName, mandatory = true, description = "XML object.")
	protected Xml 	xml 	= null;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, description = "The name of Testcase before witch the xml will be put.")
	protected String 	beforeTestCase 	= null;

	@ActionFieldAttribute(name = titleName, mandatory = true, description = "Title.")
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
		this.xml.report(report, this.beforeTestCase, Str.asString(this.title));
		
		super.setResult(null);
	}
}

