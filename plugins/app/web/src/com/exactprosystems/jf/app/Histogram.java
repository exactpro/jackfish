////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.app;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated
public class Histogram
{
	private final int STEP = 25;
	private final int RANGE_COUNT = 100;
	private final long MAX_TIME = 60000; // 1 minute
	private final int MAX_START_COUNT = 30;

	private String name;
	private long lastUpdate;
	private Logger logger;

	private List<Integer> list = new ArrayList<>(RANGE_COUNT);

	/**
	 * use this method in public static void main only for testing
	 */
	private static void test()
	{
		Logger logger = Logger.getLogger(Histogram.class);
		Histogram histogram = new Histogram("Find by", logger);
		for (int i = 0; i < 15000; i++)
		{
			int o = (int) (Math.random() * 2500);
			if (o < 100)
			{
				o = 0;
			}
			int time = o;
			histogram.add(time);
		}
		histogram.report();
	}

	public Histogram(String name, Logger logger)
	{
		this.name = name;
		this.logger = logger;
		reset();
		lastUpdate = System.currentTimeMillis();
	}

	public void add(double time)
	{
		int range = (int) (time / STEP);
		int index = Math.min(range, list.size() - 1);
		list.set(index, list.get(index) + 1);
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastUpdate > MAX_TIME)
		{
			report();
			lastUpdate = currentTime;
		}

	}

	public void reset()
	{
		list.clear();
		for (int i = 0; i < RANGE_COUNT; i++)
		{
			list.add(0);
		}
	}

	public void report()
	{
		StringBuilder builder = new StringBuilder("\nHistogram for " + name);
		builder.append("\n");
		int max = Collections.max(this.list);
		for (int i = 0; i < list.size(); i++)
		{
			createRow(builder, max, i * STEP, list.get(i));
		}
		logger.debug(builder.toString());
	}

	private void createRow(StringBuilder builder, int max, int startRange, int number)
	{
		builder.append("[").append(String.format("%5d", startRange == 0 ? 0 : startRange + 1)).append(" ]")
				//.append(" - ")
				//.append(String.format("%7s", endRange == STEP * RANGE_COUNT ? "+inf )" : endRange+" ]"))
				.append(String.format(" %7d\t\t", number));
		int maxStartCount = (int) ((((double) number / (max == 0 ? 1 : max))) * MAX_START_COUNT);
		for (int i = 0; i < maxStartCount; i++)
		{
			builder.append("*");
		}
		builder.append("\n");
	}
}
