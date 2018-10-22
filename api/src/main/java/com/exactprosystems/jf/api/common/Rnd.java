/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
