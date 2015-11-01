////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Str
{
	@DescriptionAttribute(
			text = "Return array of strings with groups, that were found in string @string with @pattern.\n" + "See also Regular expressions")
	public static String[] groups(String string, String pattern)
	{

		Pattern p = Pattern.compile(pattern);
		Matcher matcher = p.matcher(string);
		if (matcher.matches())
		{
			String[] returnArray = new String[matcher.groupCount()];
			for (int i = 1; i <= matcher.groupCount(); i++)
			{
				returnArray[i - 1] = matcher.group(i);
			}
			return returnArray;
		}
		return new String[0];
	}

	@DescriptionAttribute(text = "Return true, if strings @s1 and @s2 are equivalents")
    public static boolean areEqual(String s1, String s2)
    {
    	if (s1 == null)
    	{
    		return s1 == s2;
    	}
    	
    	return s1.equals(s2);
    }

	@DescriptionAttribute(text = "Return true, if string @s is empty or null, and false otherwise")
    public static boolean IsNullOrEmpty(String s)
    {
    	if (s == null)
    	{
    		return true;
    	}
    	
    	return s.isEmpty();
    }

	@DescriptionAttribute(text = "Return String value of @object")
	public static String asString(Object object)
	{
		if (object == null)
		{
			return "";
		}
		return String.valueOf(object);
	}
}
