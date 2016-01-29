////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.api.app;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class HistogramTransfer implements Serializable
{
	private static final long serialVersionUID = -4141790456810458507L;

	private boolean isListening = false;
	private String name;
	private int interval;
	private int intervalCount;
	private LinkedHashMap<Integer, Long> map;

	public HistogramTransfer(String name, int interval, int intervalCount)
	{
		this.name = name;
		this.interval = interval;
		this.intervalCount = intervalCount;
		this.map = new LinkedHashMap<>(this.intervalCount);
	}

	public String getName()
	{
		return name;
	}

	public void start()
	{
		this.isListening = true;
	}

	public void stop()
	{
		this.isListening = false;
	}

	public Map<Integer, Long> getData()
	{
		return this.map;
	}

	public void add(Long ms)
	{
		if (!isListening)
		{
			return;
		}

	}

}
