////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.app.Mutable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;

public class MutableArrayList<T extends Mutable> extends ArrayList<T> implements Mutable
{
    private static final long            serialVersionUID = -61727654712092442L;
    private boolean                      changed;
    private BiConsumer<Integer, Integer> changeListener   = null;
    private BiConsumer<Integer, T>       addListener      = null;
    private BiConsumer<Integer, T>       setListener      = null;
    private BiConsumer<Integer, T>       removeListener   = null;

	public MutableArrayList()
	{
		super();
		this.changed = false;
	}

	public MutableArrayList(Collection<? extends T> c)
	{
		super(c);
		this.changed = false;
	}

	public MutableArrayList(int initialCapacity)
	{
		super(initialCapacity);
		this.changed = false;
	}

	public void from(Collection<? extends T> c)
	{
		clear();
		addAll(c);
	}
	
	//==============================================================================================
	// implements ArrayList
	//==============================================================================================
    @Override
    public void add(int index, T element)
    {
        int before = size();
        this.changed = true;
        super.add(index, element);
        onChange(before, size());
    }

	@Override
	public boolean add(T e)
	{
        int before = size();
		this.changed = true;
		boolean res = super.add(e);
		onAdd(size(), e);
        onChange(before, size());
        return res; 
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c)
	{
        int before = size();
		this.changed = this.changed || c.size() > 0;
		boolean res = super.addAll(c);
        onChange(before, size());
        return res; 
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> c)
	{
        int before = size();
		this.changed = this.changed || c.size() > 0;
		boolean res = super.addAll(index, c);
        onChange(before, size());
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
        int before = size();
		T res = super.remove(index);
		this.changed = this.changed || res != null;
        onRemove(index, res);
        onChange(before, size());
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
        int before = size();
        int index = super.indexOf(o);
        boolean res = index > 0;
        T value = null;
        if (res)
        {
            value = super.remove(index);
        }
		this.changed |= res;
		onRemove(index, value);
        onChange(before, size());
        return res; 
	}
	
	@Override
	public T set(int index, T element)
	{
        int before = size();
		this.changed = true;
		T res = super.set(index, element);
		onSet(index, element);
        onChange(before, size());
        return res; 
	}
	
	//==============================================================================================
	// implements Mutable
	//==============================================================================================
	@Override
	public boolean isChanged()
	{
		if (this.changed)
		{
			return true;
		}
		for (T element : this)
		{
			if (element.isChanged())
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void saved()
	{
		this.changed = false;
		for (T element : this)
		{
			element.saved();
		}
	}

    //==============================================================================================
    public void setOnChangeListener(BiConsumer<Integer, Integer> listener)
    {
        this.changeListener = listener;
    }
    
    public void setOnAddListener(BiConsumer<Integer, T> listener)
    {
        this.addListener = listener;
    }
    
    public void setOnRemoveListener(BiConsumer<Integer, T> listener)
    {
        this.removeListener = listener;
    }
    
    public void setOnSetListener(BiConsumer<Integer, T> listener)
    {
        this.setListener = listener;
    }
    
    //==============================================================================================
    private void onChange(int before, int now)
    {
        if (this.changeListener != null)
        {
            this.changeListener.accept(before, now);
        }
    }
    
    private void onAdd(int index, T value)
    {
        if (this.addListener != null)
        {
            this.addListener.accept(index, value);
        }
    }

    private void onSet(int index, T value)
    {
        if (this.setListener != null)
        {
            this.setListener.accept(index, value);
        }
    }

    private void onRemove(int index, T value)
    {
        if (this.removeListener != null)
        {
            this.removeListener.accept(index, value);
        }
    }
}
