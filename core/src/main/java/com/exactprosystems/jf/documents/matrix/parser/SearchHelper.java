/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser;

public class SearchHelper
{
	private SearchHelper()
	{

	}

	public static boolean matches(String str, String what, boolean caseSensitive, boolean wholeWord)
	{
		if (str == null || what == null || what.isEmpty())
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
