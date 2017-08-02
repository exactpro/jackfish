////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.functions;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.report.ReportBuilder;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

public class Content implements List<ContentItem>, Mutable, Cloneable
{
	public Content()
	{
		this.changed = false;
		this.list = new ArrayList<ContentItem>();
	}
	
	public Content(Reader reader) throws IOException
	{
		this();
		read(reader);
	}

	public Content(String fileName) throws FileNotFoundException, IOException
	{
		this();
		try (Reader reader = CommonHelper.readerFromFileName(fileName))
		{
			read(reader);
		}
	}
	
	//==============================================================================================
	// Interface Mutable
	//==============================================================================================
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

	public void setChangeListener(Consumer<Boolean> changeListener)
	{
		this.changeListener = changeListener;
	}

	//==============================================================================================
    // Interface Cloneable
    //==============================================================================================
    @Override
    public Content clone() throws CloneNotSupportedException
    {
        Content clone = (Content)super.clone();
        
        clone.list = new ArrayList<ContentItem>();
        clone.list.addAll(this.list);
        
        return clone;
    }

	//==============================================================================================
	
	public boolean save(String fileName)
	{
		try (Writer writer = CommonHelper.writerToFileName(fileName))
		{
			save(writer);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (ContentItem line : this.list)
		{
			sb.append(line).append('\n');
		}
		
		return sb.toString();
	}
	

	//------------------------------------------------------------------------------------------------------------------
	// interface List
	//------------------------------------------------------------------------------------------------------------------
	@Override
	public int size()
	{
		return this.list.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.list.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return this.list.contains(o);
	}

	@Override
	public Iterator<ContentItem> iterator()
	{
		return this.list.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return this.list.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return this.list.toArray(a);
	}

	@Override
	public boolean add(ContentItem e)
	{
		changed(true);
		return this.list.add(e);
	}

	@Override
	public boolean remove(Object o)
	{
		changed(true);
		return this.list.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return this.list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends ContentItem> c)
	{
		changed(true);
		return this.list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends ContentItem> c)
	{
		changed(true);
		return this.list.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		changed(true);
		return this.list.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return this.list.retainAll(c);
	}

	@Override
	public void clear()
	{
		changed(true);
		this.list.clear();
	}

	@Override
	public ContentItem get(int index)
	{
		return this.list.get(index);
	}

	@Override
	public ContentItem set(int index, ContentItem element)
	{
		changed(true);
		return this.list.set(index, element);
	}

	@Override
	public void add(int index, ContentItem element)
	{
		changed(true);
		this.list.add(index, element);
	}

	private void changed(boolean flag)
	{
		this.changed = true;
		Optional.ofNullable(this.changeListener).ifPresent(c -> c.accept(this.changed));
	}

	@Override
	public ContentItem remove(int index)
	{
		changed(true);
		return this.list.remove(index);
	}

	@Override
	public int indexOf(Object o)
	{
		return this.list.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o)
	{
		return this.list.lastIndexOf(o);
	}

	@Override
	public ListIterator<ContentItem> listIterator()
	{
		return this.list.listIterator();
	}

	@Override
	public ListIterator<ContentItem> listIterator(int index)
	{
		return this.list.listIterator(index);
	}

	@Override
	public List<ContentItem> subList(int fromIndex, int toIndex)
	{
		return this.list.subList(fromIndex, toIndex);
	}

	//------------------------------------------------------------------------------------------------------------------
	private void save(Writer writer) throws IOException
	{
		try (BufferedWriter buffWriter = new BufferedWriter(writer))
		{
			for (ContentItem line : this.list)
			{
				buffWriter.write(line.toString());
				buffWriter.newLine();
			}
		}
	}

	private void read(Reader reader) throws IOException
	{
		this.list.clear();
		try (BufferedReader buffReader = new BufferedReader(reader))
		{
			String line = null;
			while((line = buffReader.readLine()) != null)
			{
				this.list.add(new ContentItem(line));
			}
		}
	}
	
	private boolean changed;
	private Consumer<Boolean> changeListener;
	private List<ContentItem> list;
	private static final Logger logger = Logger.getLogger(Content.class);
}
