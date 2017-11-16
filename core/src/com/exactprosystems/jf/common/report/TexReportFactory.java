////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.common.documentation.TexReportBuilder;

import java.io.IOException;
import java.util.Date;


public class TexReportFactory extends ReportFactory
{

	@Override
	public ReportBuilder createReportBuilder(String outputPath, String matrixName, Date currentTime) throws IOException
	{
		ReportBuilder result = new TexReportBuilder(outputPath, matrixName, currentTime);
		result.init(new FileReportWriter(result.generateReportName(outputPath, matrixName, ReportBuilder.SUFFIX, currentTime)));
		return result;
	}
}
