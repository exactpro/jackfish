////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
			return x/10*9 < a && a < x/10*11;
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
			return min < x && x < max;
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
	
	public abstract String toString(String a, String b);
	
	public abstract boolean func(long x, long a, long b);
	
	private String name;
}
