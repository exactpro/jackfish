////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import java.io.IOException;
import java.util.Date;

public abstract class ReportFactory
{
	public abstract ReportBuilder createReportBuilder(String outputPath, String matrixName, Date currentTime) throws IOException;
}
