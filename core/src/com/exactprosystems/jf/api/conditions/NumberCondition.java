////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.common.DescriptionAttribute;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;

@DescriptionAttribute(text = "Returns rows which contains the given number")
public class NumberCondition extends RelativeCondition  implements Serializable
{

	private static final long serialVersionUID = -5222648499271312819L;

	public NumberCondition(String name, Number value) throws Exception
	{
		super(name, "==");
		this.value = value;
	}

	public NumberCondition(String name, String relationStr, Number value) throws Exception
	{
		super(name, relationStr);
		this.value = value;
	}

	@Override
	public String serialize()
	{
		return super.getSerializePrefix(this.getClass()) + start + getName() + separator + this.relation.getSign() + separator + this.value + finish;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + " [name=" + getName() + " " + this.relation.getSign() + " value=" + value + "]";
	}

	@Override
	protected Integer compare(Object otherValue)
	{
		Number otherNumber = convert(otherValue);

		if (this.value == null || otherNumber == null)
		{
			return null;
		}

		if (this.value.doubleValue() < otherNumber.doubleValue())
		{
			return 1;
		}

		if (this.value.doubleValue() > otherNumber.doubleValue())
		{
			return -1;
		}
		
		return 0;
	}

	private BigDecimal convert(Object obj)
	{
		BigDecimal ret = null; 
		try
		{
			if (Number.class.isAssignableFrom(obj.getClass()))
			{
				ret = new BigDecimal(((Number)obj).doubleValue());
			} 
			else if (obj instanceof String)
			{
				ret = new BigDecimal(format.parse((String)obj).doubleValue());
			}
		}
		catch (Exception e)
		{
		}
		return ret;
	}
	
	@Override
	protected String valueStr()
	{
		return String.valueOf(this.value);
	}

	private Number value = null;
	
	private static NumberFormat format = NumberFormat.getInstance();
}
