////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.functions;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.HistogramMetric;
import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Histogram
{
	private static final AtomicLong uniqueId = new AtomicLong(0);
	private final HistogramMetric metric;
	private final Integer interval;
	private final Integer intervalCount;

	private long id;
	private IRemoteApplication service;

	public Histogram(AppConnection appConnection, HistogramMetric metric, Integer interval, Integer intervalCount) throws Exception
	{
		this.id = uniqueId.incrementAndGet();
		this.metric = metric;
		this.interval = interval;
		this.intervalCount = intervalCount;
		this.service = appConnection.getApplication().service();
		this.service.subscribe(this.id, metric, interval, intervalCount);
	}

	public void start() throws Exception
	{
		try
		{
			this.service.listening(this.id, true);
		}
		catch (RemoteException e)
		{
			throw new Exception("Error on start listening : " + e.getMessage(), e);
		}
	}

	public void stop() throws Exception
	{
		try
		{
			this.service.listening(this.id, false);
		}
		catch (RemoteException e)
		{
			throw new Exception("Error on stop listening : " + e.getMessage(), e);
		}
	}

	public void report(ReportBuilder reportBuilder, String title) throws IOException
	{
		List<Long> metrics = this.service.getMetrics(this.id);
		reportBuilder.reportHistogram(title, this.intervalCount, this.interval, metrics);
	}

	public Table getTable(AbstractEvaluator evaluator)
	{
		return new Table(new String[]{"1", "2", "3"}, evaluator);
	}

	@Override
	public String toString()
	{
		return "Histogram{" +
				"metric=" + metric +
				", interval=" + interval +
				", intervalCount=" + intervalCount +
				'}';
	}
}
