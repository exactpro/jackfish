////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.api.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class HistogramTransfer implements Serializable
{
	private static final long serialVersionUID = -4141790456810458507L;

	private boolean isListening;
	private HistogramMetric metric;
	private int interval;
	private int intervalCount;

	private List<Long> list;

	public HistogramTransfer(HistogramMetric metric, int interval, int intervalCount)
	{
		this.metric = metric;
		this.interval = interval;
		this.intervalCount = intervalCount;
		this.list = new ArrayList<>(this.intervalCount);
		this.isListening = false;
		reset();
	}

	public void listening(boolean flag)
	{
		this.isListening = flag;
	}

	public void add(Long ms)
	{
		if (!isListening)
		{
			return;
		}
		int range = (int) (ms / this.interval);
		int index = Math.min(range, this.list.size() - 1);
		this.list.set(index, this.list.get(index) + 1);
	}

	public int getInterval()
	{
		return interval;
	}

	public int getIntervalCount()
	{
		return intervalCount;
	}

	public List<Long> getCopyDate()
	{
		return new ArrayList<>(this.list);
	}

	public HistogramMetric getMetric()
	{
		return metric;
	}

	private void reset()
	{
		this.list.clear();
		for (int i = 0; i < this.intervalCount; i++)
		{
			this.list.add(0L);
		}
	}
}
