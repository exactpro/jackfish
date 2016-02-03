////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.api.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsCounter
{
	private Map<HistogramMetric, Long> mapTime;

	private HashMap<Long, HistogramTransfer> mapHistogramId = new HashMap<>();
	private HashMap<HistogramMetric, List<HistogramTransfer>> mapHistogramMetric = new HashMap<>();


	public MetricsCounter()
	{
		this.mapTime = new HashMap<>();
	}

	public void before(HistogramMetric metric)
	{
		this.mapTime.put(metric, System.currentTimeMillis());
	}

	public void after(HistogramMetric metric)
	{
		Long before = this.mapTime.get(metric);
		long time = System.currentTimeMillis() - before;
		List<HistogramTransfer> hsts = this.mapHistogramMetric.get(metric);
		if (hsts != null)
		{
			for (HistogramTransfer hst : hsts)
			{
				hst.add(time);
			}
		}
	}

	void subscribe(long id, HistogramMetric metric, int interval, int intervalCount)
	{
		HistogramTransfer transfer = new HistogramTransfer(metric, interval, intervalCount);
		this.mapHistogramId.put(id, transfer);
		List<HistogramTransfer> histogramList = this.mapHistogramMetric.remove(metric);
		if (histogramList == null)
		{
			histogramList = new ArrayList<>();
		}
		histogramList.add(transfer);
		this.mapHistogramMetric.put(metric, histogramList);
	}

	List<Long> getCopyDate(long id) throws Exception
	{
		return getHistogram(id).getCopyDate();
	}

	void listening(long id, boolean isStart) throws Exception
	{
		getHistogram(id).listening(isStart);
	}

	private HistogramTransfer getHistogram(long id) throws Exception
	{
		HistogramTransfer histogramTransfer = this.mapHistogramId.get(id);
		if (histogramTransfer == null)
		{
			throw new Exception(String.format("Histogram with id %s not subscribed", id));
		}
		return histogramTransfer;
	}
}
