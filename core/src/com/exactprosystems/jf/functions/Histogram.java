////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.functions;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.HistogramTransfer;
import com.exactprosystems.jf.common.report.ReportBuilder;

public class Histogram
{
	private HistogramTransfer histogramTransfer;

	public Histogram(AppConnection appConnection, String parameterKind, Integer interval, Integer intervalCount) throws Exception
	{
		this.histogramTransfer = new HistogramTransfer(parameterKind, interval, intervalCount);
		appConnection.getApplication().service().subscribe(this.histogramTransfer);
	}

	public void start()
	{
		this.histogramTransfer.start();
	}

	public void stop()
	{
		this.histogramTransfer.stop();
	}

	public void report(ReportBuilder reportBuilder, String title)
	{

	}

	public Table getTable()
	{
		return new Table(new String[]{"1", "2", "3"});
	}
}
