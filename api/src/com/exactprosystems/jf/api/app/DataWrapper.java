////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.awt.Point;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.Date;

public class DataWrapper implements Serializable
{

	private static final long serialVersionUID = 7636187827543071522L;

	public boolean containsNotNul()
	{
		return this.contains != null;
	}
	
	@Override
	public String toString()
	{
		if (this.contains == null)
		{
			return "";
		}
		
		String str = DataWrapper.class.getSimpleName() + "[" + this.contains + " : ";
		switch (this.contains)
		{
		case Boolean:
			str += this.boolValue;
			break;

		case Long:
			str += this.longValue;
			break;

		case Double:
			str += this.doubleValue;
			break;

		case String:
			str += this.strValue;
			break;

		case Date:
			str += this.dateValue;
			break;

		case Point:
			str += this.pointValue;
			break;
			
		case BigDecimal:
			str += this.bigDecimalValue;
			break;

		case BigInteger:
			str += this.bigIntegerValue;
			break;

		default:
			break;
		}
				
		str += "]";
		return str;
	}

	public DataWrapper(Object value) throws Exception
	{
		if (value == null)
		{
			return;
		}
		
		if (value instanceof Boolean)
		{
			this.boolValue = (Boolean)value;
			this.contains = Contains.Boolean;
		}
		else if (value instanceof Long)
		{
			this.longValue = (Long)value;
			this.contains = Contains.Long;
		}
		else if (value instanceof Integer)
		{
			this.longValue = (Integer)value;
			this.contains = Contains.Long;
		}
		else if (value instanceof Double)
		{
			this.doubleValue = (Double)value;
			this.contains = Contains.Double;
		}
		else if (value instanceof BigDecimal)
		{
			this.bigDecimalValue = (BigDecimal)value;
			this.contains = Contains.BigDecimal;
		}
		else if (value instanceof BigInteger)
		{
			this.bigIntegerValue = (BigInteger)value;
			this.contains = Contains.BigInteger;
		}
		else if (value instanceof String)
		{
			this.strValue = (String)value;
			this.contains = Contains.String;
		}
		else if (value instanceof Date)
		{
			this.dateValue = (Date)value;
			this.contains = Contains.Date;
		}
		else if (value instanceof Point)
		{
			this.pointValue = (Point)value;
			this.contains = Contains.Point;
		}
		else 
		{
			throw new Exception("unknown type " + value + " : " + value.getClass().getSimpleName());
		}
	}

	public DataWrapper(boolean value)
	{
		this.boolValue = value;
		this.contains = Contains.Boolean;
	}
	
	public DataWrapper(long value)
	{
		this.longValue = value;
		this.contains = Contains.Long;
	}
	
	public DataWrapper(BigDecimal value)
	{
		this.bigDecimalValue = value;
		this.contains = Contains.BigDecimal;
	}
	
	public DataWrapper(BigInteger value)
	{
		this.bigIntegerValue = value;
		this.contains = Contains.BigInteger;
	}
	
	public DataWrapper(double value)
	{
		this.doubleValue = value;
		this.contains = Contains.Double;
	}
	
	public DataWrapper(String value)
	{
		this.strValue = value;
		this.contains = Contains.String;
	}
	
	public DataWrapper(Date value)
	{
		this.dateValue = value;
		this.contains = Contains.Date;
	}
	
	public DataWrapper(int x, int y)
	{
		this.pointValue = new Point(x, y);
		this.contains = Contains.Point;
	}
	
	public boolean getBoolean() throws RemoteException
	{
		if (this.contains == Contains.Boolean)
		{
			return this.boolValue;
		}
		else
		{
			throw new RemoteException("" + this.toString() + " doesn't contain boolean.");
		}
	}

	public long getLong() throws RemoteException
	{
		if (this.contains == Contains.Long)
		{
			return this.longValue;
		}
		else
		{
			throw new RemoteException("" + this.toString() + " doesn't contain long.");
		}
	}

	public double getDouble() throws RemoteException
	{
		if (this.contains == Contains.Double)
		{
			return this.doubleValue;
		}
		else
		{
			throw new RemoteException("" + this.toString() + " doesn't contain double.");
		}
	}

	public BigDecimal getBigDecimal() throws RemoteException
	{
		if (this.contains == Contains.BigDecimal)
		{
			return this.bigDecimalValue;
		}
		else
		{
			throw new RemoteException("" + this.toString() + " doesn't contain BigDecimal.");
		}
	}

	public BigInteger getBigInteger() throws RemoteException
	{
		if (this.contains == Contains.BigInteger)
		{
			return this.bigIntegerValue;
		}
		else
		{
			throw new RemoteException("" + this.toString() + " doesn't contain BigInteger.");
		}
	}

	public Date getDate() throws RemoteException
	{
		if (this.contains == Contains.Date)
		{
			return this.dateValue;
		}
		else
		{
			throw new RemoteException("" + this.toString() + " doesn't contain date.");
		}
	}

	public Point getPoint() throws RemoteException
	{
		if (this.contains == Contains.Point)
		{
			return this.pointValue;
		}
		else
		{
			throw new RemoteException("" + this.toString() + " doesn't contain point.");
		}
	}

	public String getString() throws RemoteException
	{
		if (this.contains == null)
		{
			return "";
		}
		
		switch (this.contains)
		{
		case Boolean:
			return "" + this.boolValue;

		case Long:
			return "" + this.longValue;

		case Double:
			return new DecimalFormat("#####################.#####################").format(this.doubleValue);  

		case String:
			return  this.strValue;

		case Date:
			return  this.dateValue.toString();

		case Point:
			return  this.pointValue.toString();
		
		case BigDecimal:
			return new DecimalFormat("#####################.#####################").format(this.bigDecimalValue);  

		case BigInteger:
			return  this.bigIntegerValue.toString();

		default:
			break;
		}
		
		return "";
	}
	

	private BigDecimal bigDecimalValue = null;
	private BigInteger bigIntegerValue = null;
	private boolean boolValue = false;
	private long longValue = 0L;
	private double doubleValue = 0.0d;
	private Date dateValue = null;
	private Point pointValue = null;
	private String strValue = null;
	
	private enum Contains { Boolean, Long, Double, String, Date, Point, BigDecimal, BigInteger };
	private Contains contains = null;
}
