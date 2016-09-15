////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.DateTime;

import java.io.Serializable;
import java.util.Date;

public class DateCondition extends RelativeCondition  implements Serializable
{

	private static final long serialVersionUID = -713599951095234259L;

	public DateCondition(String name, String ralationStr, Date value, String precision) throws Exception
	{
		super(name, ralationStr);
		
		this.value = value;
		this.precision = Precision.byName(precision);
	}

	public DateCondition(String name, String ralationStr, Date value) throws Exception
	{
		this(name, ralationStr, value, null);
	}

	public DateCondition(String name, Date value) throws Exception
	{
		this(name, "==", value);
	}

	public DateCondition(String name, Date value, String precision) throws Exception
	{
		this(name, "==", value, precision);
	}

	@Override
	public String serialize()
	{
		return start + simpleName() + separator + getName() + separator + this.relation.getSign() + separator + this.value + separator + this.precision + finish;
	}

	@Override
	public String toString()
	{
		return DateCondition.class.getSimpleName() + " [name=" + getName() + " " + this.relation.getSign() + " value=" + DateTime.strDateTime(value) + "]";
	}

	@Override
	protected Integer compare(Object otherValue)
	{
		Date otherDate = convert(otherValue);

		if (this.value == null || otherDate == null)
		{
			return null;
		}
		
		if (equalWithPrecision(this.value, otherDate, this.precision))
		{
			return 0;
		}
		
		if (this.value.before(otherDate))
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}

	private static boolean equalWithPrecision(Date date1, Date date2, Precision precision)
	{
		long ms1 = date1.getTime();
		long ms2 = date2.getTime();
		
		if (precision == Precision.Absolute)
		{
			return ms1 == ms2;
		}
		
		return Math.abs(ms1 - ms2) < precision.getPeriod();
	}

	private Date convert(Object obj)
	{
		Date ret = null; 
		try
		{
			if (obj instanceof Date)
			{
				ret = (Date)obj;
			} 
			else if (obj instanceof String)
			{
				ret = Converter.parseDate(String.valueOf(obj));
			}
		}
		catch (Exception e)
		{
			// nothing to do
		}
		return ret;
	}

	//yyyyMMdd-HH:mm:ss
	private enum Precision 
	{
		OneDay			('d', 24 * 60 * 60 * 1000), 
		OneHour			('H', 60 * 60 * 1000), 
		OneMinute		('m', 60 * 1000), 
		OneSecond		('s', 1000), 
		OneMillisecond	('S', 1), 
		Absolute		('A', 0);
		
		public long getPeriod()
		{
			return this.period;
		}
		
		public static Precision byName(char alias) throws Exception
		{
			for (Precision precision : values())
			{
				if (precision.alias == alias)
				{
					return precision; 
				}
			}
			throw new Exception("Unknown precision: " + alias);
		}
		
		public static Precision byName(String alias) throws Exception
		{
			if (alias != null)
			{
				return byName(alias.charAt(0));
			}
			return Absolute;
		}

		Precision(char alias, long period)
		{
			this.alias = alias;
			this.period = period;
		}
		
		private char alias;
		private long period;
	}
	
	@Override
	protected String valueStr()
	{
		return String.valueOf(this.value);
	}

	private Date value = null;
	private Precision precision = null;

}
