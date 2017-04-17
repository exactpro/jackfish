////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.util.Date;
import java.util.Random;

public class Rnd
{
	private Rnd() {}

	@DescriptionAttribute(text = "Generates random integer in range @from - @to. To isn't include.\n" + "Result can be divided on the @step without a rest.")
	public static int rnd(@FieldParameter(name = "from") int from, @FieldParameter(name = "to") int to, @FieldParameter(name = "step") int step)
	{
		int ret = random.nextInt(to - from) + from;
		return ret - (ret % step);
	}

	@DescriptionAttribute (text = "Generates random double in range @from - @to.")
	public static double rnd(@FieldParameter(name = "from") double from, @FieldParameter(name = "to") double to)
	{
		return rnd(from, to, 2);
	}

	@DescriptionAttribute(text = "Generates random double with an accuracy of @count digits in range @from - @to")
	public static double rnd(@FieldParameter(name = "from") double from, @FieldParameter(name = "to") double to, @FieldParameter(name = "count") int count)
	{
		double result = from + random.nextDouble() * (to - from);
		return (double) (int) (result*Math.pow(10,count)) / Math.pow(10,count);
	}

	@DescriptionAttribute(text = "Generates random double with an accuracy of 1 digit in range @from - @to")
	public static int rnd(@FieldParameter(name = "from") int from, @FieldParameter(name = "to") int to)
	{
		return rnd(from, to, 1);
	}

	private static Random random = null;

	static
	{
		random = new Random();
		random.setSeed(new Date().getTime());
	}
}
