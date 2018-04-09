/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LimitedArrayList<E> extends CopyOnWriteArrayList<E>
{
	private static final long	serialVersionUID	= -4604339590295118791L;

	public LimitedArrayList(int limit)
	{
		super();
		this.limit = limit;
	}

	@Override
	public boolean add(E e)
	{
		if (this.size() >= this.limit)
		{
			this.remove(0);
		}
		return super.add(e);
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c)
	{
		if (this.size() > this.limit + c.size())
		{
			List<E> newlist = new ArrayList<E>(this.limit); 
			newlist.addAll(this.size() + c.size() - this.limit, this);
			clear();
			addAll(newlist);
		}
		return super.addAll(c);
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends E> c)
	{
		if (this.size() > this.limit + c.size())
		{
			List<E> newlist = new ArrayList<E>(this.limit); 
			newlist.addAll(this.size() + c.size() - this.limit, this);
			clear();
			addAll(newlist);
		}
		return super.addAll(index, c);
	}
	
	private int limit;
}
