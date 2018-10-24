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
