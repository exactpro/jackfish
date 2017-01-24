////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.report;

import java.util.Date;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					= ActionGroups.Report,
		generalDescription 		= "This action is used to create an object of Report type. It is possible to set it as "
				+ "ToReport parameter in the following actions: {{@TableReport@}}, {{@TextReport@}}, {{@XmlReport@}} and others to fill the created Report object",
		additionFieldsAllowed 	= false,
		suffix                  = "REP",
		outputDescription       = "Created report.",
        outputType              = ReportBuilder.class,
		examples 				= "{{##Id;#Action;#Version;#ReportName\n" +
				"REP1;ReportStart;'3.141592';'MyReport'#}}"
	)
public class ReportStart extends AbstractAction 
{
	public final static String reportNameName  = "ReportName";
	public final static String versionName     = "Version";

	@ActionFieldAttribute(name = reportNameName, mandatory = true, description = "Name of a created report.")
	protected String reportName; 

    @ActionFieldAttribute(name = versionName, mandatory = false, description = "Version of the report. It will be displayed in report")
    protected String version; 

	public ReportStart()
	{
	}
	
	@Override
	public void initDefaultValues() 
	{
	    this.version = null;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
	    if (Str.IsNullOrEmpty(this.reportName))
	    {
	        super.setError(reportNameName, ErrorKind.EMPTY_PARAMETER);
	        return;
	    }
	    
	    Configuration config = context.getConfiguration();
	    ReportBuilder newReport = config.getReportFactory().createReportBuilder(config.getReports().get(), this.reportName, new Date());
	    newReport.reportStarted(null, this.version);
	    newReport.itemStarted(this.owner.getMatrix().getRoot());
	    
		super.setResult(newReport);
	}
}
