////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import com.exactprosystems.jf.api.common.i18n.R;

import java.util.Date;
import java.util.Random;

public class Rnd
{
	private Rnd() {}

	@DescriptionAttribute(text = R.RND_TO_NOT_INCLUDE)
	public static int rnd(@FieldParameter(name = "from") int from, @FieldParameter(name = "to") int to, @FieldParameter(name = "step") int step)
	{
		int ret = random.nextInt(to - from) + from;
		return ret - (ret % step);
	}

	@DescriptionAttribute (text = R.RND_FROM_TO)
	public static double rnd(@FieldParameter(name = "from") double from, @FieldParameter(name = "to") double to)
	{
		return rnd(from, to, 2);
	}

	@DescriptionAttribute(text = R.RND_ACCURACY_COUNT)
	public static double rnd(@FieldParameter(name = "from") double from, @FieldParameter(name = "to") double to, @FieldParameter(name = "count") int count)
	{
		double result = from + random.nextDouble() * (to - from);
		return (double) (int) (result*Math.pow(10,count)) / Math.pow(10,count);
	}

	@DescriptionAttribute(text = R.RND_ACCURACY_ONE_DIGIT)
	public static int rnd(@FieldParameter(name = "from") int from, @FieldParameter(name = "to") int to)
	{
		return rnd(from, to, 1);
	}

	@DescriptionAttribute(text = R.RND_STRING)
	public static String generateString (@FieldParameter(name = "donor") String donor, @FieldParameter(name = "length") int length)
	{
		char[] text = new char[length];
		for (int i = 0; i < length; i++)
		{
			text[i] = donor.charAt(random.nextInt(donor.length()));
		}
		return new String(text);
	}

	private static Random random = null;

	static
	{
		random = new Random();
		random.setSeed(new Date().getTime());
	}
}
