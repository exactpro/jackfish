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

package com.exactprosystems.jf.api.app;

public enum Range
{
	EQUAL ("equal")
	{
		@Override
		public String toString(String a, String b)
		{
			return "" + a;
		}
		
		@Override
		public boolean func(long x, long a, long b)
		{
			return a == x;
		}
		
		@Override
		public boolean hasTwoArguments()
		{
			return false;
		}
	},
	
	LESS ("less")
	{
		@Override
		public String toString(String a, String b)
		{
			return "less(" + a + ")";
		}
		
		@Override
		public boolean func(long x, long a, long b)
		{
			return x < a;
		}

		@Override
		public boolean hasTwoArguments()
		{
			return false;
		}
	},
	
	GREAT ("great")
	{
		@Override
		public String toString(String a, String b)
		{
			return "great(" + a + ")";
		}
		
		@Override
		public boolean func(long x, long a, long b)
		{
			return x > a;
		}

		@Override
		public boolean hasTwoArguments()
		{
			return false;
		}
	},
	
	ABOUT ("about")
	{
		@Override
		public String toString(String a, String b)
		{
			return "about(" + a + ")";
		}
		
		@Override
		public boolean func(long x, long a, long b)
		{
			return x / 10. * 9 <= a && a <= x / 10. * 11;
		}

		@Override
		public boolean hasTwoArguments()
		{
			return false;
		}
	},
	
	BETWEEN ("between")
	{
		@Override
		public String toString(String a, String b)
		{
			return "between(" + a + ", " + b + ")";
		}
		
		@Override
		public boolean func(long x, long a, long b)
		{
			long min = Math.min(a, b);
			long max = Math.max(a, b);
			return min <= x && x <= max;
		}

		@Override
		public boolean hasTwoArguments()
		{
			return true;
		}
	};
	
	private Range(String name)
	{
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}
	
	public static Range findByName(String name)
	{
		for (Range range : values())
		{
			if (range.name.equals(name))
			{
				return range;
			}
		}
		return null;
	}
	
	public abstract String toString(String a, String b);

	public abstract boolean hasTwoArguments();
	
	public abstract boolean func(long x, long a, long b);
	
	private String name;
}
