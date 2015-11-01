////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

public class Unique
{
	private Unique() {}

	@DescriptionAttribute(text = "Get random string, which depends of current time")
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
