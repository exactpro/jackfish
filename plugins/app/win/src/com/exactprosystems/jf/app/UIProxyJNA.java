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

	public UIProxyJNA(int[] id)
	{
		this.id = id;
	}

	public int[] getId()
	{
		return id;
	}

	public String getIdString()
	{
		if (this.id == null)
		{
			return null;
		}
		StringBuilder b = new StringBuilder();
		for (int i : this.id)
		{
			b.append(i).append(SEPARATOR);
		}
		return b.deleteCharAt(b.length() - 1).toString();
	}

	@Override
	public String toString()
	{
		return "UIProxyJNA{" + "id=" + Arrays.toString(id) + '}';
	}
}
