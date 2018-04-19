/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.common;

import com.exactprosystems.jf.api.common.i18n.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Str
{
	@DescriptionAttribute(
			text = R.STR_GROUPS)
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

	@DescriptionAttribute(text = R.STR_ARE_EQUAL)
    public static boolean areEqual(@FieldParameter(name = "s1") String s1, @FieldParameter(name = "s2") String s2)
    {
    	if (s1 == null)
    	{
    		return s1 == s2;
    	}
    	
    	return s1.equals(s2);
    }

	@DescriptionAttribute(text = R.STR_IS_NULL_OR_EMPTY)
    public static boolean IsNullOrEmpty(@FieldParameter(name = "s") String s)
    {
    	if (s == null)
    	{
    		return true;
    	}
    	
    	return s.isEmpty();
    }

	@DescriptionAttribute(text = R.STR_AS_STRING)
	public static String asString(@FieldParameter(name = "object") Object object)
	{
		if (object == null)
		{
			return "";
		}
		return object.toString();
	}
}
