////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import java.util.ArrayList;

public class SimpleArrayBuilder<T>
{
	private ArrayList<T> list;

	public SimpleArrayBuilder()
	{
		this.list = new ArrayList<>();
	}

	public static <T> SimpleArrayBuilder<T> create()
	{
		return new SimpleArrayBuilder<>();
	}

	public SimpleArrayBuilder<T> add(T t)
	{
		this.list.add(t);
		return this;
	}

	public ArrayList<T> build()
	{
		return this.list;
	}
}
