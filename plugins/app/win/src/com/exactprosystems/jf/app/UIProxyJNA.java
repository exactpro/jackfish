////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
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
