////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Str
{
	@DescriptionAttribute(
			text = "Return array of strings with groups, that were found in string @string with @pattern.\n" + "See also Regular expressions")
	public static String[] groups(@FieldParameter(name = "string") String string, @FieldParameter(name = "pattern") String pattern)
	{
		ArrayList<String> res = new ArrayList<>();
		Pattern p = Pattern.compile(pattern);
		Matcher matcher = p.matcher(string);
		while (matcher.find())
		{
			res.add(matcher.group());
		}
		return res.toArray(new String[res.size()]);
	}

	@DescriptionAttribute(text = "Return true, if strings @s1 and @s2 are equivalents")
    public static boolean areEqual(@FieldParameter(name = "s1") String s1, @FieldParameter(name = "s2") String s2)
    {
    	if (s1 == null)
    	{
    		return s1 == s2;
    	}
    	
    	return s1.equals(s2);
    }

	@DescriptionAttribute(text = "Return true, if string @s is empty or null, and false otherwise")
    public static boolean IsNullOrEmpty(@FieldParameter(name = "s") String s)
    {
    	if (s == null)
    	{
    		return true;
    	}
    	
    	return s.isEmpty();
    }

	@DescriptionAttribute(text = "Return String value of @object")
	public static String asString(@FieldParameter(name = "object") Object object)
	{
		if (object == null)
		{
			return "";
		}
		return object.toString();
	}
}
