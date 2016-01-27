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
import java.util.List;

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
		int index = range;
		if (range >= list.size())
		{
			index = list.size() - 1;
		}
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
		int max = getMax();
		for (int i = 0; i < list.size(); i++)
		{
			createRow(builder, max, i * STEP, list.get(i));
		}
		logger.debug(builder.toString());
	}

	private void createRow(StringBuilder builder, int max, int startRange, int number)
	{
		builder.append("[").append(String.format("%5d", startRange == 0 ? 0 : startRange + 1)).append(" ]")
				//					.append(" - ")
				//					.append(String.format("%7s", endRange == STEP * RANGE_COUNT ? "+inf )" : endRange+" ]"))
				.append(String.format(" %7d\t\t", number)).append(star((int) ((((double) number / max)) * MAX_START_COUNT))).append("\n");
	}

	private int getMax()
	{
		int max = list.get(0);
		for (int i = 1; i < list.size(); i++)
		{
			Integer newMax = list.get(i);
			if (newMax > max)
			{
				max = newMax;
			}
		}
		return max;
	}

	private String star(int starCount)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < starCount; i++)
		{
			sb.append("*");
		}
		return sb.toString();
	}
}
