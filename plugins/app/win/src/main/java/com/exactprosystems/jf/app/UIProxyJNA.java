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
package com.exactprosystems.jf.app;

import java.util.Arrays;

public class UIProxyJNA
{
	//TODO think about it pls
	public static final UIProxyJNA DUMMY = new UIProxyJNA(new int[0]);

	public static final String SEPARATOR = ",";

	private int[] id;

	public UIProxyJNA()
	{
		this.id = null;
	}

	public UIProxyJNA(int[] id)
	{
		this.id = id;
	}

	public UIProxyJNA(String stringId)
	{
		this.id = stringToIntArray(stringId);
	}

	public String getIdString()
	{
		if (this.id == null)
		{
			return null;
		}
		StringBuilder b = new StringBuilder();
		String sep = "";
		for (int i : this.id)
		{
			b.append(sep).append(i);
			sep = SEPARATOR;
		}
		return b.toString();
	}

	private static int[] stringToIntArray(String s)
	{
		if (s == null)
		{
			return null;
		}
		String[] temp = s.split(SEPARATOR);
		int[] id = new int[temp.length];
		for (int i = 0; i < temp.length; ++i)
		{
			id[i] = Integer.parseInt(temp[i]);
		}
		return id;
	}

	@Override
	public String toString()
	{
		return "UIProxyJNA{" + "id=" + Arrays.toString(id) + '}';
	}
}
