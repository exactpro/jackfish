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
	EQUAL
	{
		@Override
		public String toString(long a, long b)
		{
			return "" + a;
		}
		
		@Override
		public boolean func(long x, long a, long b)
		{
			return a == x;
		}
	},
	
	LESS
	{
		@Override
		public String toString(long a, long b)
		{
			return "less(" + a + ")";
		}
		
		@Override
		public boolean func(long x, long a, long b)
		{
			return x < a;
		}
	},
	
	GREAT
	{
		@Override
		public String toString(long a, long b)
		{
			return "great(" + a + ")";
		}
		
		@Override
		public boolean func(long x, long a, long b)
		{
			return x > a;
		}
	},
	
	ABOUT
	{
		@Override
		public String toString(long a, long b)
		{
			return "about(" + a + ")";
		}
		
		@Override
		public boolean func(long x, long a, long b)
		{
			return x/10*9 < a && a < x/10*11;
		}
	},
	
	BETWEEN
	{
		@Override
		public String toString(long a, long b)
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
	
	
	public abstract String toString(long a, long b);
	
	public abstract boolean func(long x, long a, long b);
}
