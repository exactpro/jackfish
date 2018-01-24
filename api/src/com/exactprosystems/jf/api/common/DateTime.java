
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
	@DescriptionAttribute(text = R.DATE_TIME_SET_FORMATS)
	public static void setFormats(String timeFormat, String dateFormat, String dateTimeFormat)
	{
		timeFormater 		= new SimpleDateFormat(timeFormat);
		dateFormater		= new SimpleDateFormat(dateFormat);
		dateTimeFormater	= new SimpleDateFormat(dateTimeFormat);
	}
	
	@DescriptionAttribute(text = R.DATE_TIME_SET_TIME_FROM_STRING)
	public DateTime setTime(@FieldParameter(name = "str") String str) throws ParseException
	{
		DateTime time = time(str);
		setTime(time.hours(), time.minutes(), time.seconds());
		return this;
	}

	@DescriptionAttribute(text = R.DATE_TIME_SET_TIME_TO_CURRENT_DATE)
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

	@DescriptionAttribute(text = R.DATE_TIME_SET_DATE_FROM_STRING)
	public DateTime setDate(@FieldParameter(name = "str") String str) throws ParseException
	{
		DateTime date = date(str);
		setDate(date.years(), date.months(), date.days());
		return this;
	}

	@DescriptionAttribute(text = R.DATE_TIME_SET_DATE_TO_CURRENT_DATE)
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

	@DescriptionAttribute(text = R.DATE_TIME_SHIFT_TIME)
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

	@DescriptionAttribute(text = R.DATE_TIME_SHIFT_DATE)
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

	@DescriptionAttribute(text = R.DATE_TIME_YEARS)
	public int years()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);
		return calendar.get(Calendar.YEAR);
	}

	@DescriptionAttribute(text = R.DATE_TIME_MONTH)
	public int months()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);
		return calendar.get(Calendar.MONTH);
	}

    @DescriptionAttribute(text = R.DATE_TIME_DAY_OF_WEEK)
    public int dayOfWeek()
    {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(this);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    @DescriptionAttribute(text = R.DATE_TIME_DAYS)
	public int days()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	@DescriptionAttribute(text = R.DATE_TIME_HOURS)
	public int hours()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);
		return calendar.get(Calendar.HOUR_OF_DAY);
	}

	@DescriptionAttribute(text = R.DATE_TIME_MINUTES)
	public int minutes()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);
		return calendar.get(Calendar.MINUTE);
	}

	@DescriptionAttribute(text = R.DATE_TIME_SECONDS)
	public int seconds()
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);
		return calendar.get(Calendar.SECOND);
	}

	@DescriptionAttribute(text = R.DATE_TIME_MILLISECONDS)
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

   @DescriptionAttribute(text = R.DATE_TIME_CONVERT_TO_STRING)
    public String str()
    {
        synchronized (dateTimeFormater)
        {
            return dateTimeFormater.format(this);
        }
    }
	
	@DescriptionAttribute(text= R.DATE_TIME_CONVERT_TO_STRING_VIA_FORMAT)
	public String str(@FieldParameter(name = "format") String format)
	{
		return new SimpleDateFormat(format).format(this);
	}
	
    @DescriptionAttribute(text= R.DATE_TIME_CONVERT_TO_STRING_VIA_FORMAT_AND_ZONEID)
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
	@DescriptionAttribute(text = R.DATE_TIME_RETURN_CURRENT_DATE)
	public static DateTime current()
	{
		return new DateTime();
	}

    @DescriptionAttribute(text = R.DATE_TIME_RETURN_CURRENT_DATE_FOR_ZONEID)
    public static DateTime current(String zoneId)
    {
        return new DateTime(zoneId);
    }

    @DescriptionAttribute(text = R.DATE_TIME_RETURN_CURRENT_TIME)
	public static DateTime currentTime()
	{
		return getTime(new Date());
	}

	@DescriptionAttribute(text = R.DATE_TIME_RETURN_DATE_FROM_STRING)
	public static DateTime dateTime(@FieldParameter(name = "str") String str, @FieldParameter(name = "format") String format) throws ParseException
	{
		return new DateTime(new SimpleDateFormat(format).parse(str));
	}

    @DescriptionAttribute(text = R.DATE_TIME_RETURN_DATE_FROM_STRING_ZONEID)
    public static DateTime dateTime(@FieldParameter(name = "str") String str, @FieldParameter(name = "format") String format, String zoneId) throws ParseException
    {
        ZoneId zone = ZoneId.of(zoneId);
        TimeZone tz = TimeZone.getTimeZone(zone);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(tz);
        return new DateTime(sdf.parse(str));
    }
	
	@DescriptionAttribute(text = R.DATE_TIME_RETURN_DATE_FROM_STR)
	public static DateTime date(@FieldParameter(name = "str") String str) throws ParseException
	{
		return new DateTime(Converter.parseDate(str));
	}

	@DescriptionAttribute(text = R.DATE_TIME_RETURN_DATE_YEAR_MONTH_DAY)
	public static DateTime date(@FieldParameter(name = "year") int year, @FieldParameter(name = "month") int month, @FieldParameter(name = "day") int day)
	{
		return new DateTime(new GregorianCalendar(year, month, day).getTime());
	}

	@DescriptionAttribute(text = R.DATE_TIME_RETURN_DATE_WITH_ALL_VALUES)
	public static DateTime date(int year, int month, int day, int hour, int minute, int second)
	{
		return new DateTime(new GregorianCalendar(year, month, day, hour, minute, second).getTime());
	}

	@DescriptionAttribute(text = R.DATE_TIME_RETURN_TIME_FROM_STRING)
	public static DateTime time(@FieldParameter(name = "str") String str) throws ParseException
	{
		return DateTime.getTime(date(str));
	}

	@DescriptionAttribute(text = R.DATE_TIME_RETURN_TIME_HOURS_MINUTES_SECONDS)
	public static DateTime time(@FieldParameter(name = "hours") int hours, @FieldParameter(name = "minutes") int minutes, @FieldParameter(name = "seconds") int seconds)
	{
		Calendar current = new GregorianCalendar();
		
		return new DateTime(new GregorianCalendar(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH),
				hours, minutes, seconds).getTime());
	}

	//------------------------------------------------------------------------------------------------------------------
	// operations relative current date/time
	//------------------------------------------------------------------------------------------------------------------
	@DescriptionAttribute(text = R.DATE_TIME_ADD_DAYS_TO_DATE)
	public static DateTime add(@FieldParameter(name = "d") Date d, @FieldParameter(name = "days") int days)
	{
		return new DateTime(d).shiftDate(0, 0, days);
	}

	@DescriptionAttribute(text = R.DATE_TIME_ADD_HOURS_MINUTES_SECONDS_TO_DATE)
	public static DateTime add(@FieldParameter(name = "d") Date d, @FieldParameter(name = "hours") int hours, @FieldParameter(name = "minutes") int minutes, @FieldParameter(name = "seconds") int seconds)
	{
		return new DateTime(d).shiftTime(hours, minutes, seconds);
	}

	@DescriptionAttribute(text = R.DATE_TIME_ADD_DAYS_TO_CURRENT_DATE)
	public static DateTime addDays(@FieldParameter(name = "days") int days)
	{
		return add(new Date(), days);
	}

	@DescriptionAttribute(text = R.DATE_TIME_ADD_HOURS_MINUTES_SECONDS_TO_CURRENT_DATE)
	public static DateTime addTime(@FieldParameter(name = "hours") int hours, @FieldParameter(name = "minutes") int minutes, @FieldParameter(name = "seconds") int seconds)
	{
		return add(new Date(), hours, minutes, seconds);
	}

	//------------------------------------------------------------------------------------------------------------------
	// formatters
	//------------------------------------------------------------------------------------------------------------------
	@DescriptionAttribute(text = R.DATE_TIME_CONVERT_DATE_TO_STRING_VIA_CONVERTER)
	public static String strDate(Date date, String format)
	{
		if (date == null)
		{
			return "";
		}
		return new SimpleDateFormat(format).format(date);
	}

    @DescriptionAttribute(text = R.DATE_TIME_CONVERT_DATE_TO_STRING_VIA_CONVERTER_ZONEID)
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

    @DescriptionAttribute(text = R.DATE_TIME_CONVERT_DATE_TO_STRING_VIA_DATE_CONVERTER)
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

	@DescriptionAttribute(text = R.DATE_TIME_CONVERT_DATE_TO_STRING_VIA_TIME_CONVERTER)
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

	@DescriptionAttribute(text = R.DATE_TIME_CONVERT_DATE_TO_STRING_VIA_DATETIME_CONVERTER)
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
	@DescriptionAttribute(text = R.DATE_TIME_GET_YEAR_FROM_DATE)
	public static int getYears(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).years();
	}

	@DescriptionAttribute(text = R.DATE_TIME_GET_MONTH_NUMBER_FROM_DATE)
	public static int getMonths(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).months();
	}

    @DescriptionAttribute(text = R.DATE_TIME_GET_DAY_OF_WEEK_FROM_DATE)
    public static int getDayOfWeek(@FieldParameter(name = "date") Date date)
    {
        return new DateTime(date).dayOfWeek();
    }

	@DescriptionAttribute(text = R.DATE_TIME_GET_DAYS_FROM_DATE)
	public static int getDays(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).days();
	}

	@DescriptionAttribute(text = R.DATE_TIME_GET_HOURS_FROM_DATE)
	public static int getHours(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).hours();
	}

	@DescriptionAttribute(text = R.DATE_TIME_GET_MINUTES_FROM_DATE)
	public static int getMinutes(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).minutes();
	}

	@DescriptionAttribute(text = R.DATE_TIME_GET_SECONDS_FROM_DATE)
	public static int getSeconds(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).seconds();
	}

	@DescriptionAttribute(text = R.DATE_TIME_GET_MILLISECONDS_FROM_DATE)
	public static int getMilliseconds(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).milliseconds();
	}

	@DescriptionAttribute(text = R.DATE_TIME_GET_DATE_FROM_JAVA_UTIL_DATE)
	public static DateTime getDate(@FieldParameter(name = "date") Date date)
	{
		return new DateTime(date).date();
	}

	@DescriptionAttribute(text = R.DATE_TIME_GET_TIME_FROM_JAVA_UTIL_DATE)
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
