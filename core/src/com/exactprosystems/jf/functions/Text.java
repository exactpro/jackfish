////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.functions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;

public class Text implements List<String>, Mutable
{
	public Text()
	{
		this.changed = false;
		this.list = new ArrayList<String>();
	}
	
	public Text(Reader reader) throws IOException
	{
		this();
		read(reader);
	}

	public Text(String fileName) throws FileNotFoundException, IOException
	{
		this();
		try (Reader reader = new FileReader(fileName))
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

	//==============================================================================================
	
	public boolean save(String fileName)
	{
		try (Writer writer = new FileWriter(fileName))
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
	
	public void report(ReportBuilder report, String title) throws Exception
	{
		ReportTable table = report.addTable(title, true, 0, new int[] {});
		
		for(String list : this.list)
		{
			table.addValues(list);
		}
	}

	public Text perform(AbstractEvaluator evaluator) throws Exception
	{
		List<String> res = new ArrayList<String>();
		for (String line : this.list)
		{
			res.add(evaluator.templateEvaluate(line));
		}
		Text result = new Text();
		result.list = res;
		this.changed = true;
		
		return result;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (String line : this.list)
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
	public Iterator<String> iterator()
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
	public boolean add(String e)
	{
		this.changed = true;
		return this.list.add(e);
	}

	@Override
	public boolean remove(Object o)
	{
		this.changed = true;
		return this.list.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return this.list.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends String> c)
	{
		this.changed = true;
		return this.list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends String> c)
	{
		this.changed = true;
		return this.list.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		this.changed = true;
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
		this.changed = true;
		this.list.clear();
	}

	@Override
	public String get(int index)
	{
		return this.list.get(index);
	}

	@Override
	public String set(int index, String element)
	{
		this.changed = true;
		return this.list.set(index, element);
	}

	@Override
	public void add(int index, String element)
	{
		this.changed = true;
		this.list.add(index, element);
	}

	@Override
	public String remove(int index)
	{
		this.changed = true;
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
	public ListIterator<String> listIterator()
	{
		return this.list.listIterator();
	}

	@Override
	public ListIterator<String> listIterator(int index)
	{
		return this.list.listIterator(index);
	}

	@Override
	public List<String> subList(int fromIndex, int toIndex)
	{
		return this.list.subList(fromIndex, toIndex);
	}

	//------------------------------------------------------------------------------------------------------------------
	private void save(Writer writer) throws IOException
	{
		try (BufferedWriter buffWriter = new BufferedWriter(writer))
		{
			for (String line : this.list)
			{
				buffWriter.write(line);
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
				this.list.add(line);
			}
		}
	}
	
	private boolean changed;
	private List<String> list;
	private static final Logger logger = Logger.getLogger(Text.class);
}
