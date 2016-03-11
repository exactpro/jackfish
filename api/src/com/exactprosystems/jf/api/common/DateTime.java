
////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTime extends Date
{
	private static final long	serialVersionUID	= 3588757755816729420L;

	@DescriptionAttribute(text = "Sets formats for convert string to date")
	public static void SetFormats(String timeFormat, String dateFormat, String dateTimeFormat)
	{
		timeFormater 		= new SimpleDateFormat(timeFormat);
		dateFormater		= new SimpleDateFormat(dateFormat);
		dateTimeFormater	= new SimpleDateFormat(dateTimeFormat);
	}
	
	public DateTime()
	{
		super();
	}

	public DateTime(Date date)
	{
		super();
		setTime(date.getTime());
	}

	@DescriptionAttribute(text = "Set time (hours, minutes and seconds) from @str.\n If @str dosen't fit converters, will be ParseException")
	public DateTime setTime(String str) throws ParseException
	{
		DateTime time = time(str);
		setTime(time.hours(), time.minutes(), time.seconds());
		return this;
	}

	@DescriptionAttribute(text = "Set @hours, @minutes and @seconds to current date")
	public DateTime setTime(int hours, int minutes, int seconds)
	{
		Calendar c = new GregorianCalendar();
		c.setTime(this);
		c.set(Calendar.HOUR, hours);
		c.set(Calendar.MINUTE, minutes);
		c.set(Calendar.SECOND, seconds);
		setDate(c.getTime());
		return this;
	}

	@DescriptionAttribute(text = "Set date (years, months and days) from @str.\n If @str dosen't fit converters, will be ParseException")
	public DateTime setDate(String str) throws ParseException
	{
		DateTime date = date(str);
		setDate(date.years(), date.months(), date.days());
		return this;
	}

	@DescriptionAttribute(text = "Set @year, @month and @day to current date")
	public DateTime setDate(int year, int month, int day)
	{
		Calendar c = new GregorianCalendar();
		c.setTime(this);
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		setDate(c.getTime());
		return this;
	}

	@DescriptionAttribute(text = "Shift current time right @hours, @minutes and @seconds")
	public DateTime shiftTime(int hours, int minutes, int seconds)
	{
		Calendar c = new GregorianCalendar();
		c.setTime(this);
		c.add(Calendar.HOUR, hours);
		c.add(Calendar.MINUTE, minutes);
		c.add(Calendar.SECOND, seconds);
		setDate(c.getTime());
		return this;
	}

	@DescriptionAttribute(text = "Shift current date right @year, @month and @day")
	public DateTime shiftDate(int year, int month, int day)
	{
		Calendar c = new GregorianCalendar();
		c.setTime(this);
		c.add(Calendar.YEAR, year);
		c.add(Calendar.MONTH, month);
		c.add(Calendar.DAY_OF_MONTH, day);
		setDate(c.getTime());
		return this;
	}

	@DescriptionAttribute(text = "Return current year")
	public int years()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);
		return calendar.get(Calendar.YEAR);
	}

	@DescriptionAttribute(text = "Return current month (number)")
	public int months()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);
		return calendar.get(Calendar.MONTH);
	}

	@DescriptionAttribute(text = "Return current day")
	public int days()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	@DescriptionAttribute(text = "Return current hour")
	public int hours()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	@DescriptionAttribute(text = "Return current minute")
	public int minutes()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);
		return calendar.get(Calendar.MINUTE);
	}

	@DescriptionAttribute(text = "Return current second")
	public int seconds()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);
		return calendar.get(Calendar.SECOND);
	}

	@DescriptionAttribute(text = "Return current millisecond")
	public int milliseconds()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);
		return calendar.get(Calendar.MILLISECOND);
	}


	public DateTime date()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);

		calendar = new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH), 0,0,0);
		return new DateTime(calendar.getTime());
	}

	public DateTime time()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);

		calendar = new GregorianCalendar(1970,0,1, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
				calendar.get(Calendar.SECOND));
		return new DateTime(calendar.getTime());
	}

	public String str(String format)
	{
		return new SimpleDateFormat(format).format(this);
	}

	//------------------------------------------------------------------------------------------------------------------
	// fabric methods
	//------------------------------------------------------------------------------------------------------------------
	@DescriptionAttribute(text = "Return current date")
	public static DateTime current()
	{
		return new DateTime();
	}

	@DescriptionAttribute(text = "Return current time")
	public static DateTime currentTime()
	{
		return getTime(new Date());
	}

	@DescriptionAttribute(text = "Return current date from @str. If @str dosen't fit converters, will be ParseException")
	public static DateTime date(String str) throws ParseException
	{
		return new DateTime(Converter.parseDate(str));
	}

	@DescriptionAttribute(text = "Return date with @year, @month and @day")
	public static DateTime date(int year, int month, int day)
	{
		return new DateTime(new GregorianCalendar(year, month, day).getTime());
	}

	@DescriptionAttribute(text = "Return date with @year, @month, @day, @hour, @minute and @second")
	public static DateTime date(int year, int month, int day, int hour, int minute, int second)
	{
		return new DateTime(new GregorianCalendar(year, month, day, hour, minute, second).getTime());
	}

	@DescriptionAttribute(text = "Return time from @str. If @str dosen't fit converters, will be ParseException")
	public static DateTime time(String str) throws ParseException
	{
		return DateTime.getTime(date(str));
	}

	@DescriptionAttribute(text = "Return time with @hours, @minutes and @seconds")
	public static DateTime time(int hours, int minutes, int seconds)
	{
		Calendar current = new GregorianCalendar();
		
		return new DateTime(new GregorianCalendar(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH),
				hours, minutes, seconds).getTime());
	}

	//------------------------------------------------------------------------------------------------------------------
	// operations relative current date/time
	//------------------------------------------------------------------------------------------------------------------
	@DescriptionAttribute(text = "Add @days to date @d and return resulting date")
	public static DateTime add(Date d, int days)
	{
		return new DateTime(d).shiftDate(0, 0, days);
	}

	@DescriptionAttribute(text = "Add @hours, @minutes and @seconds to date @d and return resulting date")
	public static DateTime add(Date d, int hours, int minutes, int seconds)
	{
		return new DateTime(d).shiftTime(hours, minutes, seconds);
	}

	@DescriptionAttribute(text = "Add @days to current date")
	public static DateTime addDays(int days)
	{
		return add(new Date(), days);
	}

	@DescriptionAttribute(text = "Add @hours, @minutes and @seconds to current date")
	public static DateTime addTime(int hours, int minutes, int seconds)
	{
		return add(new Date(), hours, minutes, seconds);
	}

	//------------------------------------------------------------------------------------------------------------------
	// formatters
	//------------------------------------------------------------------------------------------------------------------
	@DescriptionAttribute(text = "Convert @date to string with date converter")
	public static String strDate(Date date)
	{
		if (date == null)
		{
			return "null";
		}
		synchronized (dateFormater)
		{
			return dateFormater.format(date);
		}
	}

	@DescriptionAttribute(text = "Convert @date to string with time converter")
	public static String strTime(Date date)
	{
		if (date == null)
		{
			return "null";
		}
		synchronized (timeFormater)
		{
			return timeFormater.format(date);
		}
	}

	@DescriptionAttribute(text = "Convert @date to string with date-time converter")
	public static String strDateTime(Date date)
	{
		if (date == null)
		{
			return "null";
		}
		synchronized (dateTimeFormater)
		{
			return dateTimeFormater.format(date);
		}
	}
	
	//------------------------------------------------------------------------------------------------------------------
	// parts of date/time
	//------------------------------------------------------------------------------------------------------------------
	@DescriptionAttribute(text = "Get years from @date")
	public static int getYears(Date date)
	{
		return new DateTime(date).years();
	}

	@DescriptionAttribute(text = "Get months (number) from @date")
	public static int getMonths(Date date)
	{
		return new DateTime(date).months();
	}

	@DescriptionAttribute(text = "Get days from @date")
	public static int getDays(Date date)
	{
		return new DateTime(date).days();
	}

	@DescriptionAttribute(text = "Get hours from @date")
	public static int getHours(Date date)
	{
		return new DateTime(date).hours();
	}

	@DescriptionAttribute(text = "Get minutes from @date")
	public static int getMinutes(Date date)
	{
		return new DateTime(date).minutes();
	}

	@DescriptionAttribute(text = "Get seconds from @date")
	public static int getSeconds(Date date)
	{
		return new DateTime(date).seconds();
	}

	@DescriptionAttribute(text = "Get miliseconds from @date")
	public static int getMilliseconds(Date date)
	{
		return new DateTime(date).milliseconds();
	}

	public static DateTime getDate(Date date)
	{
		return new DateTime(date).date();
	}
	
	public static DateTime getTime(Date date)
	{
		return new DateTime(date).time();
	}

	//------------------------------------------------------------------------------------------------------------------
	// private methods
	//------------------------------------------------------------------------------------------------------------------
	private void setDate(Date date)
	{
		setTime(date.getTime());
	}
	
	private static DateFormat timeFormater 		=  new SimpleDateFormat("HH:mm:ss");
	private static DateFormat dateFormater 		=  new SimpleDateFormat("dd/MM/yyyy");
	private static DateFormat dateTimeFormater	 	=  new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
} 
