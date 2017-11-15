
////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateTime extends Date
{
	private static final long	serialVersionUID	= 3588757755816729420L;

	public DateTime()
	{
		super();
	}

    public DateTime(String zoneId)
    {
        super();
        ZoneId zone = ZoneId.of(zoneId);
        setDate(Date.from(Clock.system(zone).instant()));
    }

    public DateTime(Date date)
	{
		super();
		setTime(date.getTime());
	}

	@HideAttribute
	@DescriptionAttribute(text = "Sets formats for convert string to date")
	public static void setFormats(String timeFormat, String dateFormat, String dateTimeFormat)
	{
		timeFormater 		= new SimpleDateFormat(timeFormat);
		dateFormater		= new SimpleDateFormat(dateFormat);
		dateTimeFormater	= new SimpleDateFormat(dateTimeFormat);
	}
	
	@DescriptionAttribute(text = "Set time (hours, minutes and seconds) from @str.\n If @str dosen't fit converters, will be ParseException")
	public DateTime setTime(@FieldParameter(name = "str") String str) throws ParseException
	{
		DateTime time = time(str);
		setTime(time.hours(), time.minutes(), time.seconds());
		return this;
	}

	@DescriptionAttribute(text = "Set @hours, @minutes and @seconds to current date")
	public DateTime setTime(@FieldParameter(name = "hours") int hours, @FieldParameter(name = "minutes") int minutes, @FieldParameter(name = "seconds") int seconds)
	{
		Calendar c = new GregorianCalendar();
		c.setTime(this);
		c.set(Calendar.HOUR_OF_DAY, hours);
		c.set(Calendar.MINUTE, minutes);
		c.set(Calendar.SECOND, seconds);
		setDate(c.getTime());
		return this;
	}

	@DescriptionAttribute(text = "Set date (years, months and days) from @str.\n If @str dosen't fit converters, will be ParseException")
	public DateTime setDate(@FieldParameter(name = "str") String str) throws ParseException
	{
		DateTime date = date(str);
		setDate(date.years(), date.months(), date.days());
		return this;
	}

	@DescriptionAttribute(text = "Set @year, @month and @day to current date")
	public DateTime setDate(@FieldParameter(name = "year") int year, @FieldParameter(name = "month") int month, @FieldParameter(name = "day") int day)
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
	public DateTime shiftTime(@FieldParameter(name = "hours") int hours, @FieldParameter(name = "minutes") int minutes, @FieldParameter(name = "seconds") int seconds)
	{
	    DateTime other = new DateTime(this); 
		Calendar c = new GregorianCalendar();
		c.setTime(other);
		c.add(Calendar.HOUR, hours);
		c.add(Calendar.MINUTE, minutes);
		c.add(Calendar.SECOND, seconds);
		other.setDate(c.getTime());
		return other;
	}

	@DescriptionAttribute(text = "Shift current date right @year, @month and @day")
	public DateTime shiftDate(@FieldParameter(name = "year") int year, @FieldParameter(name = "month") int month, @FieldParameter(name = "day") int day)
	{
        DateTime other = new DateTime(this); 
		Calendar c = new GregorianCalendar();
		c.setTime(other);
		c.add(Calendar.YEAR, year);
		c.add(Calendar.MONTH, month);
		c.add(Calendar.DAY_OF_MONTH, day);
		other.setDate(c.getTime());
        return other;
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

    @DescriptionAttribute(text = "Return current day of week")
    public int dayOfWeek()
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(this);
        return calendar.get(Calendar.DAY_OF_WEEK);
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

   @DescriptionAttribute(text = "Convert this date to string with date-time converter")
    public String str()
    {
        synchronized (dateTimeFormater)
        {
            return dateTimeFormater.format(this);
        }
    }
	
	@DescriptionAttribute(text="Convert current instance of date to String via @format")
	public String str(@FieldParameter(name = "format") String format)
	{
		return new SimpleDateFormat(format).format(this);
	}
	
    @DescriptionAttribute(text="Convert current instance of date to String via @format using @zoneId")
    public String str(@FieldParameter(name = "format") String format, String zoneId)
    {
        ZoneId zone = ZoneId.of(zoneId);
        TimeZone tz = TimeZone.getTimeZone(zone);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(tz);
        return sdf.format(this);
    }
	
	//------------------------------------------------------------------------------------------------------------------
	// fabric methods
	//------------------------------------------------------------------------------------------------------------------
	@DescriptionAttribute(text = "Return current date")
	public static DateTime current()
	{
		return new DateTime();
	}

    @DescriptionAttribute(text = "Return current date for given @zoneId")
    public static DateTime current(String zoneId)
    {
        return new DateTime(zoneId);
    }

    @DescriptionAttribute(text = "Return current time")
	public static DateTime currentTime()
	{
		return getTime(new Date());
	}

	@DescriptionAttribute(text = "Return date from @str using @format for date. If @str dosen't fit converters, will be ParseException")
	public static DateTime dateTime(@FieldParameter(name = "str") String str, @FieldParameter(name = "format") String format) throws ParseException
	{
		return new DateTime(new SimpleDateFormat(format).parse(str));
	}

    @DescriptionAttribute(text = "Return date from @str using @format for date using @zoneId. If @str dosen't fit converters, will be ParseException")
    public static DateTime dateTime(@FieldParameter(name = "str") String str, @FieldParameter(name = "format") String format, String zoneId) throws ParseException
    {
        ZoneId zone = ZoneId.of(zoneId);
        TimeZone tz = TimeZone.getTimeZone(zone);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(tz);
        return new DateTime(sdf.parse(str));
    }
	
	@DescriptionAttribute(text = "Return date from @str. If @str dosen't fit converters, will be ParseException")
	public static DateTime date(@FieldParameter(name = "str") String str) throws ParseException
	{
		return new DateTime(Converter.parseDate(str));
	}

	@DescriptionAttribute(text = "Return date with @year, @month and @day")
	public static DateTime date(@FieldParameter(name = "year") int year, @FieldParameter(name = "month") int month, @FieldParameter(name = "day") int day)
	{
		return new DateTime(new GregorianCalendar(year, month, day).getTime());
	}

	@DescriptionAttribute(text = "Return date with @year, @month, @day, @hour, @minute and @second")
	public static DateTime date(int year, int month, int day, int hour, int minute, int second)
	{
		return new DateTime(new GregorianCalendar(year, month, day, hour, minute, second).getTime());
	}

	@DescriptionAttribute(text = "Return time from @str. If @str dosen't fit converters, will be ParseException")
	public static DateTime time(@FieldParameter(name = "str") String str) throws ParseException
	{
		return DateTime.getTime(date(str));
	}

	@DescriptionAttribute(text = "Return time with @hours, @minutes and @seconds")
	public static DateTime time(@FieldParameter(name = "hours") int hours, @FieldParameter(name = "minutes") int minutes, @FieldParameter(name = "seconds") int seconds)
	{
		Calendar current = new GregorianCalendar();
		
		return new DateTime(new GregorianCalendar(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH),
				hours, minutes, seconds).getTime());
	}

	//------------------------------------------------------------------------------------------------------------------
	// operations relative current date/time
	//------------------------------------------------------------------------------------------------------------------
	@DescriptionAttribute(text = "Add @days to date @d and return resulting date")
	public static DateTime add(@FieldParameter(name = "d") Date d, @FieldParameter(name = "days") int days)
	{
		return new DateTime(d).shiftDate(0, 0, days);
	}

	@DescriptionAttribute(text = "Add @hours, @minutes and @seconds to date @d and return resulting date")
	public static DateTime add(@FieldParameter(name = "d") Date d, @FieldParameter(name = "hours") int hours, @FieldParameter(name = "minutes") int minutes, @FieldParameter(name = "seconds") int seconds)
	{
		return new DateTime(d).shiftTime(hours, minutes, seconds);
	}

	@DescriptionAttribute(text = "Add @days to current date")
	public static DateTime addDays(@FieldParameter(name = "days") int days)
	{
		return add(new Date(), days);
	}

	@DescriptionAttribute(text = "Add @hours, @minutes and @seconds to current date")
	public static DateTime addTime(@FieldParameter(name = "hours") int hours, @FieldParameter(name = "minutes") int minutes, @FieldParameter(name = "seconds") int seconds)
	{
		return add(new Date(), hours, minutes, seconds);
	}

	//------------------------------------------------------------------------------------------------------------------
	// formatters
	//------------------------------------------------------------------------------------------------------------------
	@DescriptionAttribute(text = "Convert @date to string with @format converter")
	public static String strDate(Date date, String format)
	{
		if (date == null)
		{
			return "";
		}
		return new SimpleDateFormat(format).format(date);
	}

    @DescriptionAttribute(text = "Convert @date to string with @format converter")
    public static String strDate(Date date, String format, String zoneId)
    {
        if (date == null)
        {
            return "";
        }
        ZoneId zone = ZoneId.of(zoneId);
        TimeZone tz = TimeZone.getTimeZone(zone);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(tz);
        return sdf.format(date);
    }

    @DescriptionAttribute(text = "Convert @date to string with date converter")
	public static String strDate(@FieldParameter(name = "date") Date date)
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
	public static String strTime(@FieldParameter(name = "date") Date date)
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
	public static String strDateTime(@FieldParameter(name = "date") Date date)
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
	public static int getYears(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).years();
	}

	@DescriptionAttribute(text = "Get months (number) from @date")
	public static int getMonths(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).months();
	}

    @DescriptionAttribute(text = "Get day of week from @date")
    public static int getDayOfWeek(@FieldParameter(name = "date") Date date)
    {
        return new DateTime(date).dayOfWeek();
    }

	@DescriptionAttribute(text = "Get days from @date")
	public static int getDays(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).days();
	}

	@DescriptionAttribute(text = "Get hours from @date")
	public static int getHours(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).hours();
	}

	@DescriptionAttribute(text = "Get minutes from @date")
	public static int getMinutes(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).minutes();
	}

	@DescriptionAttribute(text = "Get seconds from @date")
	public static int getSeconds(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).seconds();
	}

	@DescriptionAttribute(text = "Get milliseconds from @date")
	public static int getMilliseconds(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).milliseconds();
	}

	@DescriptionAttribute(text = "Get date from instance of type java.util.Date @date")
	public static DateTime getDate(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).date();
	}

	@DescriptionAttribute(text = "Get time from instance of type java.util.Date @date")
	public static DateTime getTime(@FieldParameter(name = "date") Date date)
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
