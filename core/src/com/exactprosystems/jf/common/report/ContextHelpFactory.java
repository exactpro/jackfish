////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import java.io.IOException;
import java.util.Date;


public class ContextHelpFactory extends ReportFactory
{

	@Override
	public ReportBuilder createReportBuilder(String outputPath, String matrixName, Date currentTime) throws IOException
	{
		ReportBuilder result = new ContextHelpBuilder(currentTime);
		result.init(new StringWriter());
		return result;
	}
}
