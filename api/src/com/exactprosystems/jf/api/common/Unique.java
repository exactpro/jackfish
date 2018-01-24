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

public class Unique
{
	private Unique() {}

	@DescriptionAttribute(text = R.UNIQUE_STRING_DESCRIPTION)
	public static String string()
	{
		char[] alphabet = 
			{ 	
				'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 
				'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 
				'U', 'V', 'W', 'X', 'Y', 'Z', 
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9' 
			};
		
		int base = alphabet.length;
		
		long num = System.currentTimeMillis() * 100 + count;
		count = (count + 1) % 100;
		
		String ret = "";
		while(num != 0)
		{
			int oneChar = (int)(num % base);
			num = num / base;
			ret = alphabet[oneChar] + ret;
		}
		
		return ret;
	}
	
	private static int count = 0;

}
