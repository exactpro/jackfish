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
