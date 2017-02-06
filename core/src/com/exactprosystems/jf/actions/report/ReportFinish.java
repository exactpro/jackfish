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
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					= ActionGroups.Report,
		generalDescription 		= "This action is used as a final step in building a report taken from {{@ReportStart@}} "
				+ "action. It will be impossible to add new elements to Report after after using this action.",
		additionFieldsAllowed 	= false,
		examples 				= "{{`1. Create an object of Report type.`}}"
				+ "{{`2. Create an object of Table type.`}}"
				+ "{{`3. Move the object Table using {{@TableReport@}} action to the report created in the first step.`}}"
				+ "{{`4. Finish building Report object.`}} "
				+ "{{##Id;#Action;#Version;#ReportName\n"
				+ "REP1;ReportStart;'3.141592';'MyReport'\n"
				+ "\n"
				+ "#Id;#RawTable\n"
				+ "DATA1;Table\n"
				+ "@;newH\n"
				+ "0;newR\n"
				+ "#EndRawTable\n"
				+ "\n"
				+ "#Action;#ToReport;#Table;#Title\n"
				+ "TableReport;REP1.Out;DATA1;'Report title'\n"
				+ "\n"
				+ "#Action;#Passed;#Report;#Failed\n"
				+ "ReportFinish;0;REP1.Out;0#}}"
	)
public class ReportFinish extends AbstractAction 
{
    public final static String passedName = "Passed";
    public final static String failedName = "Failed";
    public final static String reportName = "Report";
	public final static String startTimeName 	= "StartTime";
	public final static String finishTimeName 	= "FinishTime";

    @ActionFieldAttribute(name = reportName, mandatory = true, description = "Report which should be finished.")
    protected ReportBuilder    report;

    @ActionFieldAttribute(name = passedName, mandatory = true, description = "A number of passed actions is specified.")
    protected Integer          passed;

    @ActionFieldAttribute(name = failedName, mandatory = true, description = "A number of failed actions is specified.")
    protected Integer          failed;

	@ActionFieldAttribute(name = startTimeName, mandatory = false, description = "Time when the report starts to be built. If the parameter is not set then the current time is used.")
	protected Date startTime; 

	@ActionFieldAttribute(name = finishTimeName, mandatory = false, description = "The time when the report is finished. If the parameter is not set then the curent time is used.")
	protected Date finishTime; 

	public ReportFinish()
	{
	}
	
	@Override
	public void initDefaultValues() 
	{
		this.startTime = null;
		this.finishTime = null;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
	    
	    if (this.report == null)
	    {
	        super.setError(reportName, ErrorKind.EMPTY_PARAMETER);
	        return;
	    }
		
	    this.report.itemFinished(this.owner.getMatrix().getRoot(), 0, null);
	    this.report.reportFinished(this.failed, this.passed, this.startTime, this.finishTime);
	    
		super.setResult(null);
	}
}
