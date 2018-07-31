/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.common.report;

import java.io.IOException;
import java.util.Date;

public class HelpBuilderFactory extends ReportFactory
{
	@Override
	public ReportBuilder createReportBuilder(String outputPath, String matrixName, Date currentTime) throws IOException
	{
		ReportBuilder result = new HelpBuilder(currentTime);
		result.init(new StringWriter());
		return result;
	}
}
