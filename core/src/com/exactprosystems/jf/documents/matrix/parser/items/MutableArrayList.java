////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.app.Mutable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

public class MutableArrayList<T extends Mutable> extends ArrayList<T> implements Mutable
{
	private static final long                                         serialVersionUID = -61727654712092442L;
	private              boolean                                      changed          = false;
	private              BiConsumer<Integer, Integer>                 changeListener   = null;
	private              BiConsumer<Integer, T>                       addListener      = null;
	private              BiConsumer<Integer, Collection<? extends T>> addAllListener   = null;
	private              BiConsumer<Integer, T>                       setListener      = null;
	private              BiConsumer<Integer, T>                       removeListener   = null;

	public MutableArrayList()
	{
		super();
	}

	public MutableArrayList(Collection<? extends T> c)
	{
		super(c);
	}

	public MutableArrayList(int initialCapacity)
	{
		super(initialCapacity);
	}

	public void from(Collection<? extends T> c)
	{
		clear();
		addAll(c);
	}

	//region ArrayList methods
	@Override
	public void add(int index, T element)
	{
		this.changed = true;
		super.add(index, element);
		onAdd(index, element);
	}

	@Override
	public boolean add(T e)
	{
		this.changed = true;
		boolean res = super.add(e);
		onAdd(size(), e);
		return res;
	}

	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		this.changed = this.changed || c.size() > 0;
		boolean res = super.addAll(c);
		onAddAll(size(), c);
		return res;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c)
	{
		this.changed = this.changed || c.size() > 0;
		boolean res = super.addAll(index, c);
		onAddAll(index, c);
		return res;
	}

	@Override
	public void clear()
	{
		int before = size();
		this.changed = this.changed || size() > 0;
		super.clear();
		onChange(before, size());
	}

	@Override
	public T remove(int index)
	{
		T res = super.remove(index);
		this.changed = this.changed || res != null;
		onRemove(index, res);
		return res;
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		int before = size();
		boolean res = super.removeAll(c);
		this.changed = this.changed || res;
		onChange(before, size());
		return res;
	}

	@Override
	public boolean remove(Object o)
	{
		int index = super.indexOf(o);
		boolean res = index >= 0;
		if (res)
		{
			T value = super.remove(index);
			onRemove(index, value);
		}
		this.changed |= res;
		return res;
	}

	@Override
	public T set(int index, T element)
	{
		this.changed = true;
		T res = super.set(index, element);
		onSet(index, element);
		return res;
	}
	//endregion

	//region Mutable methods

	@Override
	public boolean isChanged()
	{
		if (this.changed)
		{
			return true;
		}
		return this.stream().anyMatch(Mutable::isChanged);
	}

	@Override
	public void saved()
	{
		this.changed = false;
		this.forEach(Mutable::saved);
	}

	//endregion

	//region event methods
	public void fire()
	{
		onChange(size(), size());
	}

	public void setOnChangeListener(BiConsumer<Integer, Integer> listener)
	{
		this.changeListener = listener;
	}

	public void setOnAddListener(BiConsumer<Integer, T> listener)
	{
		this.addListener = listener;
	}

	public void setOnAddAllListener(BiConsumer<Integer, Collection<? extends T>> listener)
	{
		this.addAllListener = listener;
	}

	public void setOnRemoveListener(BiConsumer<Integer, T> listener)
	{
		this.removeListener = listener;
	}

	public void setOnSetListener(BiConsumer<Integer, T> listener)
	{
		this.setListener = listener;
	}
	//endregion

	//region private methods
	private void onChange(int before, int now)
	{
		Optional.ofNullable(this.changeListener).ifPresent(l -> l.accept(before, now));
	}

	private void onAdd(int index, T value)
	{
		Optional.ofNullable(this.addListener).ifPresent(l -> l.accept(index, value));
	}

	private void onAddAll(int index, Collection<? extends T> collection)
	{
		Optional.ofNullable(this.addAllListener).ifPresent(l -> l.accept(index, collection));
	}

	private void onSet(int index, T value)
	{
		Optional.ofNullable(this.setListener).ifPresent(l -> l.accept(index, value));
	}

	private void onRemove(int index, T value)
	{
		Optional.ofNullable(this.removeListener).ifPresent(l -> l.accept(index, value));
	}
	//endregion
}
