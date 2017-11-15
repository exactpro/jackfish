////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser;

public class SearchHelper
{
	public static boolean matches(String str, String what, boolean caseSensitive, boolean wholeWord)
	{
		if (str == null)
		{
			return false;
		}

		if (what == null)
		{
			return false;
		}

		if (what.isEmpty())
		{
			return false;
		}

		if (wholeWord)
		{
			if (caseSensitive)
			{
				return str.equals(what);
			}
			else 
			{
				return str.equalsIgnoreCase(what);
			}
		}
		
		if (caseSensitive)
		{
			return str.contains(what);
		}
		else 
		{
			return str.toLowerCase().contains(what.toLowerCase());
		}
	}
}
