////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Converter
{
	public static void setFormats(String formats)
	{
		if (formats != null)
		{
			for(String s : formats.split("\\|"))
			{
				if (s != null && !s.isEmpty())
				{
					additionFormats.add(new SimpleDateFormat(s.trim()));
	            }
			}
		}
	}

	public static byte[] convertToByteArray(Object object)
	{
		if (object == null)
		{
			return null;
		}
		Object[] array = (Object[])object; 
		
		byte[] ret = new byte[array.length];
		for (int i = 0; i < array.length; i++)
		{
			ret[i] = (byte) array[i];
		}
		
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] convertByteArray(Class<T> clazz, Object object) throws Exception
	{
		if (object == null)
		{
			return null;
		}
		byte[] array = (byte[])object; 
		
		T[] ret = (T[])Array.newInstance(clazz, array.length);
		for (int i = 0; i < array.length; i++)
		{
			ret[i] = (T)convertToType(array[i], clazz);
		}
		
		
		return ret;
	}
	

	@SuppressWarnings("unchecked")
	public static <T> T[] convertArray(Class<T> clazz, Object object)
	{
		if (object == null)
		{
			return null;
		}
		Object[] array = (Object[])object; 
		
		T[] ret = (T[])Array.newInstance(clazz, array.length);
		for (int i = 0; i < array.length; i++)
		{
			ret[i] = (T) array[i];
		}
		
		
		return ret;
	}
	
	public static Map<String, String> toStringMap(Map<String, Object> map)
	{
		Map<String, String> ret = new HashMap<String, String>(map.size());
		for(Entry<String, Object> entry : map.entrySet())
		{
			ret.put(entry.getKey(), "" + entry.getValue());
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T convertToType(Object object, Class<T> type) throws Exception
	{
		if (object == null)
		{
			return null;
		}
		
		if (type.isAssignableFrom(object.getClass()))
		{
			return (T)object;
		}
		
		if (type.isAssignableFrom(Character.class))
		{
			return (T)new Character(String.valueOf(object).charAt(0));
		}
		else if (type.isAssignableFrom(String.class))
		{
			return (T)String.valueOf(object);
		}
		else if (type.isAssignableFrom(Date.class))
		{
			return (T)parseDate(String.valueOf(object));
		}
		else if (type.isAssignableFrom(Integer.class))
		{
			if (object instanceof String)
			{
				return (T)new Integer(Integer.parseInt(String.valueOf(object)));
			}
			else if (object instanceof Number)
			{
				return (T)new Integer(((Number)object).intValue());
			}
		}
		else if (type.isAssignableFrom(Double.class))
		{
			if (object instanceof String)
			{
				return (T)new Double(Double.parseDouble(String.valueOf(object)));
			}
			else if (object instanceof Number)
			{
				return (T)new Double(((Number)object).doubleValue());
			}
		}
		else if (type.isAssignableFrom(BigInteger.class))
		{
			if (object instanceof String)
			{
				return (T)new BigInteger(String.valueOf(object));
			}
			else if (object instanceof Number)
			{
				return (T)new BigInteger(String.valueOf(((Number)object).intValue()));
			}
		}
		else if (type.isAssignableFrom(BigDecimal.class))
		{
			if (object instanceof String)
			{
				return (T)new BigDecimal(String.valueOf(object));
			}
			else if (object instanceof Number)
			{
				return (T)new BigDecimal(String.valueOf(((Number)object).intValue()));
			}
		}

		throw new Exception("Can not convert " + object + " to type " + type);
	}
	
	public static Date parseDate(String date) throws ParseException
	{
		Date ret = null;

		for (DateFormat formatter :  additionFormats)
		{
			try
			{
				ret = formatter.parse(date);
				if (ret != null)
				{
					return ret;
				}
			}
			catch (ParseException e)
			{
				// in real nothing to do 
			}
		}
			
		throw new ParseException("Can not parse date from " + date, 0);
	}	
	
	private static List<DateFormat> additionFormats = new ArrayList<DateFormat>();
	
}
