////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.HideAttribute;
import com.exactprosystems.jf.api.common.i18n.R;

import java.awt.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@DescriptionAttribute(text = R.CONDITION_DESCRIPTION)
public abstract class Condition implements ICondition, Serializable
{
	private static final long	serialVersionUID	= -7581472488041624617L;
	
	public static final char 	separator			= '|';
	public static final char 	start				= '{';
	public static final char 	finish				= '}';

	private Map<Class<? extends Condition>, String> prefixMap = new HashMap<Class<? extends Condition>, String>(){{
		put(AndCondition.class, 		"&");
		put(ColorCondition.class, 		"C");
		put(DateCondition.class, 		"D");
		put(EmptyCondition.class, 		"E");
		put(NotCondition.class, 		"!");
		put(NumberCondition.class, 		"N");
		put(OrCondition.class,			"^");
		put(RegexpCondition.class, 		"$");
		put(StringCondition.class,		"S");
		put(TrueCondition.class,		"T");
		put(FalseCondition.class,		"F");

	}};

	//region static methods
	@HideAttribute
	public 	static Condition[] convertToCondition(Map<String, Object> expected) throws Exception
	{
		Condition[] conditions = new Condition[expected.size()];
		
		int i = 0;
		for (Entry<String, Object> entry : expected.entrySet())
		{
			String expectedKey = entry.getKey().toString();
			Object expectedValue = entry.getValue();
			Condition condition = Condition.convertToCondition(expectedKey, expectedValue);
			conditions[i++] = condition;
		}

		return conditions;
	}
	
	@HideAttribute
	public 	static Condition convertToCondition(String name, Object expectedValue) throws Exception
	{
		if (expectedValue instanceof Condition)
		{
			return (Condition)expectedValue;
		}
		else if (expectedValue == null)
		{
			return new EmptyCondition(name);
		}
		else if (expectedValue instanceof Number)
		{
			return new NumberCondition(name, (Number)expectedValue);
		}
		else if (expectedValue instanceof Date)
		{
			return new DateCondition(name, (Date)expectedValue);
		}
		else if (expectedValue instanceof Color)
		{
			return new ColorCondition(name, (Color)expectedValue);
		}
		
		return new StringCondition(name, "" + expectedValue);
	}
	
	@DescriptionAttribute(text = R.CONDITION_AND_DESCRIPTION)
	public static AndCondition and(Condition ... cond) throws Exception
	{
		return new AndCondition(cond);
	}
	
    @DescriptionAttribute(text = R.CONDITION_OR_DESCRIPTION)
	public static OrCondition or(Condition ... cond) throws Exception
	{
		return new OrCondition(cond);
	}
	
    @DescriptionAttribute(text = R.CONDITION_NOT_DESCRIPTION)
	public static NotCondition not(Condition cond)
	{
		return new NotCondition(cond);
	}

	@DescriptionAttribute(text = R.CONDITION_TRUE_DESCRIPTION)
	public static TrueCondition True()
	{
		return new TrueCondition();
	}

	@DescriptionAttribute(text = R.CONDITION_FALSE_DESCRIPTION)
	public static FalseCondition False()
	{
		return new FalseCondition();
	}

    @DescriptionAttribute(text = R.CONDITION_EMPTY_DESCRIPTION)
	public static EmptyCondition empty(String name)
	{
	    return new EmptyCondition(name);
	}
	
    @DescriptionAttribute(text = R.CONDITION_REGEXP_DESCRIPTION)
    public static RegexpCondition regexp(String name, String pattern)
    {
        return new RegexpCondition(name, pattern);
    }
	
    @DescriptionAttribute(text = R.CONDITION_STRING_DESCRIPTION)
    public static StringCondition string(String name, String value, boolean ignoreCase)
    {
        return new StringCondition(name, value, ignoreCase);
    }
    
    @DescriptionAttribute(text = R.CONDITION_DATE_DESCRIPTION)
    public static DateCondition date(String name, String ralationStr, Date value, String precision) throws Exception
    {
        return new DateCondition(name, ralationStr, value, precision);
    }
    
    @DescriptionAttribute(text = R.CONDITION_NUMBER_DESCRIPTION)
    public static NumberCondition number(String name, String relationStr, Number value) throws Exception
    {
        return new NumberCondition(name, relationStr, value);
    }
    
    @DescriptionAttribute(text = R.CONDITION_COLOR_DESCRIPTION)
    public static ColorCondition color(String name, Color color)
    {
        return new ColorCondition(name, color);
    }
	//endregion
    
	@HideAttribute
	public Condition(String name)
	{
		this.name = name;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	//region protected methods
	protected boolean isMatchedName(String otherName)
	{
		if (this.name == null)
		{
			return true;
		}

		return this.name.equals(otherName);
	}

	protected final String getSerializePrefix(Class<? extends Condition> clazz)
	{
		return this.prefixMap.get(clazz);
	}
	//endregion

	private String name;
}
