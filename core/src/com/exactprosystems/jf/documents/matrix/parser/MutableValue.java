////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser;

import com.exactprosystems.jf.api.app.Mutable;

import java.util.Objects;
import java.util.function.BiConsumer;

public class MutableValue<T> implements Mutable, Setter<T>, MutableListener<T>
{
    private T value = null;
    private boolean changed = false; 
    private BiConsumer<T, T> changeListener = null;

    public MutableValue()
	{
		this.changed = false;
	}

	public MutableValue(T value)
	{
		this();
		this.value = value;
	}

	/**
	 * copy constructor
	 */
	public MutableValue(MutableValue<T> mutableValue)
	{
		this();
		this.value = mutableValue.value;
		this.changed = mutableValue.changed;
	}
	
	@Override
	public int hashCode()
	{
	    return Objects.hashCode(this.value);
	}
	
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof MutableValue))
        {
            return false;
        }
        MutableValue<?> other = (MutableValue<?>) obj;
        return Objects.equals(this.value, other.value);
    }
	
	
	@Override
	public void set(T value)
	{
		this.changed = this.changed || !Objects.equals(this.value, value);
		this.callListener(value);
		this.value = value;
	}
	
	@Override
	public T get()
	{
		return this.value;
	}

	@Override
	public boolean isChanged()
	{
		return this.changed;
	}

	@Override
	public void saved()
	{
		this.changed = false;
	}

	@Override
	public String toString()
	{
		return this.value == null ? "" : this.value.toString();
	}
	
	public void fire()
	{
		this.callListener(this.value);
	}

	@Override
	public void setOnChangeListener(BiConsumer<T, T> listener)
	{
	    this.changeListener = listener;
	}
	
	public boolean isNullOrEmpty()
	{
		return this.value == null || ("" + this.value).isEmpty(); 
	}

	private void callListener(T value)
	{
		if (this.changeListener != null)
		{
			this.changeListener.accept(this.value, value);
		}
	}
}
