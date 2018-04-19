/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.functions;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Text implements List<String>, Mutable, Cloneable
{
	private static final Logger logger = Logger.getLogger(Text.class);

	private       boolean           changed;
	private       Consumer<Boolean> changeListener;
	private final List<String>      list;

	public Text()
	{
		this.changed = false;
		this.list = new ArrayList<>();
	}

	public Text(Reader reader) throws IOException
	{
		this();
		this.read(reader);
	}

	public Text(String fileName) throws IOException
	{
		this();
		try (Reader reader = CommonHelper.readerFromFileName(fileName))
		{
			this.read(reader);
		}
	}

	/**
	 * copy constructor
	 */
	public Text(Text text)
	{
		this();
		this.list.addAll(text.list);
	}

	//region Interface Mutable
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

	//endregion

	public void setChangeListener(Consumer<Boolean> changeListener)
	{
		this.changeListener = changeListener;
	}

	/**
	 * Save the text to the file by passed file name
	 * @return true, if save was successful
	 */
	public boolean save(String fileName)
	{
		try (Writer writer = CommonHelper.writerToFileName(fileName))
		{
			this.save(writer);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	/**
	 * Report the text
	 */
	public void report(ReportBuilder report, String beforeTestcase, String title)
	{
		if (beforeTestcase != null || report.reportIsOn())
		{
			ReportTable table = report.addExplicitTable(title, beforeTestcase, true, true, new int[0]);
			this.list.forEach(table::addValues);
		}
	}

	/**
	 * Evaluate the text. Each line will evaluate via {@link AbstractEvaluator#templateEvaluate(String)}
	 *
	 * @return a new {@link Text} object, which has all evaluated lines
	 *
	 * @throws Exception if evaluated some of lines was failed
	 */
	public Text perform(AbstractEvaluator evaluator) throws Exception
	{
		List<String> evaluatedLines = new ArrayList<>();
		for (String line : this.list)
		{
			evaluatedLines.add(evaluator.templateEvaluate(line));
		}
		Text result = new Text();
		result.list.addAll(evaluatedLines);
		return result;
	}

	@Override
	public String toString()
	{
		return this.list.stream().collect(Collectors.joining(System.lineSeparator()));
	}

	//region interface List
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
		this.changed(true);
		return this.list.add(e);
	}

	@Override
	public boolean remove(Object o)
	{
		this.changed(true);
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
		this.changed(true);
		return this.list.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends String> c)
	{
		this.changed(true);
		return this.list.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		this.changed(true);
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
		this.changed(true);
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
		this.changed(true);
		return this.list.set(index, element);
	}

	@Override
	public void add(int index, String element)
	{
		this.changed(true);
		this.list.add(index, element);
	}

	@Override
	public String remove(int index)
	{
		this.changed(true);
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

	//endregion

	//region private methods
	private void changed(boolean flag)
	{
		this.changed = true;
		Optional.ofNullable(this.changeListener).ifPresent(c -> c.accept(true));
	}

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
			String line;
			while ((line = buffReader.readLine()) != null)
			{
				this.list.add(line);
			}
		}
	}
	//endregion
}
