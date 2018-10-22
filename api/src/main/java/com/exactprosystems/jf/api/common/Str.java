/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
