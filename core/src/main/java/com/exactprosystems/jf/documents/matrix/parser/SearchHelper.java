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
