/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.app.Mutable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Class for represent mutable array list.
 * This class has methods from {@link Mutable} interface.
 * And this class has listeners for notify, that the list is changed
 *
 * @param <T> the type of list ( should be extended on Mutable)
 */
public class MutableArrayList<T extends Mutable> extends ArrayList<T> implements Mutable
{
	private static final long                                         serialVersionUID = -61727654712092442L;
	private              boolean                                      changed          = false;
	private transient    BiConsumer<Integer, Integer>                 changeListener   = null;
	private transient    BiConsumer<Integer, T>                       addListener      = null;
	private transient    BiConsumer<Integer, Collection<? extends T>> addAllListener   = null;
	private transient    BiConsumer<Integer, T>                       setListener      = null;
	private transient    BiConsumer<Integer, T>                       removeListener   = null;

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
		this.clear();
		this.addAll(c);
	}

	//region ArrayList methods
	@Override
	public void add(int index, T element)
	{
		this.changed = true;
		super.add(index, element);
		this.onAdd(index, element);
	}

	@Override
	public boolean add(T e)
	{
		this.changed = true;
		boolean res = super.add(e);
		this.onAdd(size() - 1, e);
		return res;
	}

	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		this.changed = this.changed || c.size() > 0;
		boolean res = super.addAll(c);
		this.onAddAll(size(), c);
		return res;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c)
	{
		this.changed = this.changed || c.size() > 0;
		boolean res = super.addAll(index, c);
		this.onAddAll(index, c);
		return res;
	}

	@Override
	public void clear()
	{
		int before = size();
		this.changed = this.changed || size() > 0;
		super.clear();
		this.onChange(before, size());
	}

	@Override
	public T remove(int index)
	{
		T res = super.remove(index);
		this.changed = this.changed || res != null;
		this.onRemove(index, res);
		return res;
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		int before = size();
		boolean res = super.removeAll(c);
		this.changed = this.changed || res;
		this.onChange(before, size());
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
			this.onRemove(index, value);
		}
		this.changed |= res;
		return res;
	}

	@Override
	public T set(int index, T element)
	{
		this.changed = true;
		T res = super.set(index, element);
		this.onSet(index, element);
		return res;
	}
	//endregion

	//region Mutable methods

	@Override
	public boolean isChanged()
	{
		return this.changed || super.stream().anyMatch(Mutable::isChanged);
	}

	@Override
	public void saved()
	{
		this.changed = false;
		super.forEach(Mutable::saved);
	}

	//endregion

	//region event methods

	/**
	 * Force call on change listener. Pass the current size for the listener
	 */
	public void fire()
	{
		this.onChange(size(), size());
	}

	/**
	 * Set the change listener. This listener will called, when the list was changed ( e.g. {@link MutableArrayList#clear()} or {@link MutableArrayList#removeAll(Collection)}
	 */
	public void setOnChangeListener(BiConsumer<Integer, Integer> listener)
	{
		this.changeListener = listener;
	}

	/**
	 * Set the add listener. This listener will called, when on the list will added a item ( e.g. {@link MutableArrayList#add(Mutable)} or {@link MutableArrayList#add(int, Mutable)})
	 */
	public void setOnAddListener(BiConsumer<Integer, T> listener)
	{
		this.addListener = listener;
	}

	/**
	 * Set the add all listener. This listener will called, when on the list will added many items ( e.g. {@link MutableArrayList#addAll(Collection)} or {@link MutableArrayList#addAll(int, Collection)})
	 */
	public void setOnAddAllListener(BiConsumer<Integer, Collection<? extends T>> listener)
	{
		this.addAllListener = listener;
	}

	/**
	 * Set the remove listener. This listener will called, when a item will removed from the list( e.g. {@link MutableArrayList#remove(Object)} or {@link MutableArrayList#remove(int)}
	 */
	public void setOnRemoveListener(BiConsumer<Integer, T> listener)
	{
		this.removeListener = listener;
	}

	/**
	 * Set the set listener. This listener will called, when a item will set on the list by specified index ( e.g. {@link MutableArrayList#set(int, Mutable)})
	 */
	public void setOnSetListener(BiConsumer<Integer, T> listener)
	{
		this.setListener = listener;
	}
	//endregion

	//region private methods

	/**
	 * Call the change listener ( is the listener is presented)
	 */
	private void onChange(int before, int now)
	{
		Optional.ofNullable(this.changeListener).ifPresent(l -> l.accept(before, now));
	}

	/**
	 * Call the add listener ( is the listener is presented)
	 */
	private void onAdd(int index, T value)
	{
		Optional.ofNullable(this.addListener).ifPresent(l -> l.accept(index, value));
	}

	/**
	 * Call the add all listener ( is the listener is presented)
	 */
	private void onAddAll(int index, Collection<? extends T> collection)
	{
		Optional.ofNullable(this.addAllListener).ifPresent(l -> l.accept(index, collection));
	}

	/**
	 * Call the set listener ( is the listener is presented)
	 */
	private void onSet(int index, T value)
	{
		Optional.ofNullable(this.setListener).ifPresent(l -> l.accept(index, value));
	}

	/**
	 * Call the remove listener ( is the listener is presented)
	 */
	private void onRemove(int index, T value)
	{
		Optional.ofNullable(this.removeListener).ifPresent(l -> l.accept(index, value));
	}
	//endregion
}
