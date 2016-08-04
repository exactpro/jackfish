////////////////////////////////////////////////////////////////////////////////
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.functions;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportHelper;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.exceptions.ColumnIsPresentException;
import com.exactprosystems.jf.sql.SqlConnection;

import org.apache.log4j.Logger;

import java.io.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Table implements List<RowTable>, Mutable, Cloneable
{
	private Table(AbstractEvaluator evaluator)
	{
		this.evaluator = evaluator;
		this.innerList = new ArrayList<>();
	}

	public Table(Table table, AbstractEvaluator evaluator)
	{
		this(evaluator);
		this.addColumns(Arrays.stream(table.headers).map(h -> h.name).toArray(String[]::new));
		for (int i = 0; i < table.innerList.size(); i++)
		{
			Map<String, Object> stringObjectMap = convertToStr(table.innerList.get(i));
			Map<Header, Object> newMap = new LinkedHashMap<>();
			stringObjectMap.entrySet().forEach(entry -> newMap.put(headerByName(entry.getKey()), entry.getValue()));
			this.innerList.add(newMap);
		}
	}

	public Table(String[] headers, AbstractEvaluator evaluator)
	{
		this(evaluator);
		addColumns(headers);
	}

	public Table(String[][] lines, AbstractEvaluator evaluator)
	{
		this(evaluator);

		String[] firstLine = lines[0];
		addColumns(firstLine);
		for (int i = 1; i < lines.length; i++)
		{
			String[] line = lines[i];
			
			RowTable res = new RowTable();
			
			for (int j = 0; j < line.length; j++)
			{
				res.put(firstLine[j], line[j]);
			}
			this.add(res);
		}
	}

	public Table(ResultSet set, AbstractEvaluator evaluator) throws Exception
	{
		this(evaluator);

		try
		{
			ResultSetMetaData meta = set.getMetaData();
			this.headers = new Header[meta.getColumnCount()];

			for (int column = 0; column < meta.getColumnCount(); column++)
			{
				this.headers[column] = new Header(meta.getColumnName(column + 1), Header.HeaderType.forName(meta.getColumnClassName(column + 1)));
			}

			while (set.next())
			{
				Map<Header, Object> line = new LinkedHashMap<>();

				for (int i = 0; i < headers.length; i++)
				{
					line.put(headers[i], set.getObject(i + 1));
				}

				this.innerList.add(line);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	public Table(String fileName, char delimiter, AbstractEvaluator evaluator) throws Exception
	{
		this(evaluator);

		this.fileName = fileName;
		
		try (Reader reader = new BufferedReader(new FileReader(fileName)))
		{
			read(reader, delimiter);
		}
	}

	public Table(Reader reader, char delimiter, AbstractEvaluator evaluator) throws Exception
	{
		this(evaluator);
		read(reader, delimiter);
	}

	public Table(String dirName, AbstractEvaluator evaluator) throws Exception
	{
		this(evaluator);
		readFilesInfo(dirName);
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
	// Interface Cloneable
	//==============================================================================================
	@Override
	public Table clone() throws CloneNotSupportedException
	{
		Table clone = (Table)super.clone();
		
		clone.fileName = this.fileName;
		clone.headers = this.headers.clone();
		clone.innerList = new ArrayList<>();
		for (Map<Header, Object> item : this.innerList)
		{
			Map<Header, Object> copy = new LinkedHashMap<>();
			for (Entry<Header, Object> e : item.entrySet())
			{
				copy.put(e.getKey(), e.getValue());
			}
			clone.innerList.add(copy);
		}
		
		return clone;
	}

	//==============================================================================================
	
	public void setEvaluator(AbstractEvaluator evaluator)
	{
		this.evaluator = evaluator;
	}
	
	public boolean removeRow(int index)
	{
		this.innerList.remove(index);
		this.changed = true;
		return true;
	}

	public Table sort(String colName, boolean az) throws Exception
	{
		this.changed = true;
		for (int i = 0; i < this.headers.length; i++)
		{
			if (this.headers[i].name.equals(colName))
			{
				return this.sort(i, az);
			}
		}
		throw new Exception("Column this name \'" + colName + "\' not fount in the table");
	}

	public Table sort(int colNumber, boolean az)
	{
		this.changed = true;
		Header header = this.headers[colNumber];
		this.innerList.sort((o1, o2) -> 
		{
			Object obj1 = o1.get(header);
			Object obj2 = o2.get(header);
			int compare;
			if (obj1 instanceof Number && obj2 instanceof Number)
			{
				compare = Double.valueOf(((Number) obj1).doubleValue()).compareTo(((Number) obj2).doubleValue());
			}
			else
			{
				compare = String.valueOf(obj1).compareTo(String.valueOf(obj2));
			}
			return az ? compare : (-1) * compare;
		});
		return this;
	}

	public boolean save(String fileName, char delimiter, boolean saveValues, boolean withNmumbers)
	{
		CsvWriter writer = null;

		try (Writer bufferedWriter = new BufferedWriter(new FileWriter(fileName)))
		{
			writer = new CsvWriter(bufferedWriter, delimiter);
			return save(writer, "", saveValues, withNmumbers);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			return false;
		}
		finally
		{
			if (writer != null)
			{
				writer.close();
			}
		}
	}
	
	public boolean save(CsvWriter writer, String indent, boolean saveValues, boolean withNmumbers) throws IOException
	{
		int columns = this.headers.length + (withNmumbers ? 1 : 0);
		String[] record = new String[columns];
		int count = 0;
		if (withNmumbers)
		{
			record[count++] = indent + ROW_INDEX_SYMBOL;
		}
		for (int i = 0; i < this.headers.length; i++)
		{
			record[count++] = this.headers[i].name;
		}
		writer.writeRecord(record, true);


		List<Map<Header, Object>> innerList1 = this.innerList;
		for (int j = 0; j < innerList1.size(); j++)
		{
			count = 0;
			if (withNmumbers)
			{
				record[count++] = indent + String.valueOf(j);
			}
			Map<Header, Object> f = innerList1.get(j);
			for (int i = 0; i < this.headers.length; i++)
			{
				Object source = f.get(this.headers[i]);
				Object value = null;
				if (saveValues)
				{
					value = convertCell(headers[i], source);
				}
				else
				{
					value = source;
				}
				record[count++] = String.valueOf(value == null ? "" : value);
			}
			writer.writeRecord(record, true);
		}
		return true;
	}
	
	
	public void considerAsString(String... columns) throws Exception
	{
		considerAs(Header.HeaderType.STRING, columns);
	}

	public void considerAsBoolean(String... columns) throws Exception
	{
		considerAs(Header.HeaderType.BOOL, columns);
	}

	public void considerAsInt(String... columns) throws Exception
	{
		considerAs(Header.HeaderType.INT, columns);
	}

	public void considerAsDouble(String... columns) throws Exception
	{
		considerAs(Header.HeaderType.DOUBLE, columns);
	}

	public void considerAsDate(String... columns) throws Exception
	{
		considerAs(Header.HeaderType.DATE, columns);
	}

	public void considerAsBigDecimal(String... columns) throws Exception
	{
		considerAs(Header.HeaderType.BIG_DECIMAL, columns);
	}

	public void considerAsExpression(String... columns) throws Exception
	{
		considerAs(Header.HeaderType.EXPRESSION, columns);
	}

	public Table select(Condition[] conditions)
	{
		Table result = new Table(this.evaluator);
		result.headers = this.headers.clone();

		for (Map<Header, Object> row : this.innerList)
		{
			boolean matched = true;
			for (Condition condition : conditions)
			{
				String name = condition.getName();
				Object actualValue = row.get(headerByName(name));
				
				if (!condition.isMatched(name, actualValue))
				{
					matched = false;
					break;
				}
			}

			if (matched)
			{
				result.innerList.add(row);
			}
		}

		return result;
	}

	public void upload(SqlConnection connection, String table) throws SQLException
	{
		Statement statement = null;
		try
		{
			statement = connection.getConnection().createStatement();
			Header[] headers = this.headers;
			int i = 0;
			for (Map<Header, Object> map : this.innerList)
			{
				StringBuilder sql = new StringBuilder("INSERT INTO ");
				sql.append(table).append(" SET");

				for (Header header : headers)
				{
					sql.append(" ").append(header.name).append("=");
					sql.append("'").append(map.get(header)).append("',");
				}
				sql.deleteCharAt(sql.length() - 1);
				statement.addBatch(sql.toString());
				if (i == 10)
				{
					statement.executeBatch();
					i = 0;
				}
				else
				{
					i++;
				}
			}
		}
		finally
		{
			if (statement != null)
			{
				statement.executeBatch();
				statement.close();
			}
		}
	}

	public void addColumns(String... columns)
	{
		this.changed = true;
		List<Header> list = new ArrayList<Header>();
		if (this.headers != null)
		{
			list.addAll(Arrays.asList(this.headers));
		}
		
		for (String column : columns)
		{
			if (column.equals(ROW_INDEX_SYMBOL))
			{
				this.useColumnNumber = true;
				continue;
			}
			if (!columnIsPresent(column))
			{
				list.add(new Header(column, null));
			}
		}
		
		this.headers = list.toArray(new Header[0]);
	}

	private boolean columnIsPresent(String columnName)
	{
		if (this.headers == null || this.headers.length == 0)
		{
			return false;
		}
		for (Header header : this.headers)
		{
			if (header != null && Str.areEqual(columnName, header.name))
			{
				return true;
			}
		}
		return false;
	}

	public void addColumns(int index, String... columns)
	{
		for (String column : columns)
		{
			if (columnIsPresent(column))
			{
				throw new ColumnIsPresentException(String.format("Column with name %s already present", column));
			}
		}
		this.changed = true;
		if (this.headers == null)
		{
			this.headers = new Header[]{};
		}
		List<Header> newHeaders = new ArrayList<>(Arrays.asList(this.headers));
		newHeaders.addAll(index, Arrays.stream(columns).map(s -> new Header(s, null)).collect(Collectors.toList()));
		this.headers = newHeaders.toArray(new Header[newHeaders.size()]);
		//update all lines
		this.innerList.stream().forEach(e -> Arrays.stream(this.headers).filter(h -> !e.containsKey(h)).forEach(h -> e.put(h, "")));
	}

	public void removeColumns(String... columns)
	{
		this.changed = true;
		if (this.headers == null)
		{
			return;
		}
		List<String> strings = Arrays.asList(columns);
		List<Header> headers = new ArrayList<>(Arrays.asList(this.headers));
		Iterator<Header> iterator = headers.iterator();
		while (iterator.hasNext())
		{
			Header header = iterator.next();
			String next = header.name;
			if (strings.contains(next)) // TODO why isn't equals() ?
			{
				int index = headers.indexOf(header);
				if (index > 0 && index < this.innerList.size() - 1)
				{
					this.innerList.get(index).remove(header);
				}
				iterator.remove();
			}
		}
		this.headers = headers.toArray(new Header[headers.size()]);
	}

	public void setValue(int index, Map<String, Object> map)
	{
		this.changed = true;
		Map<Header, Object> line = this.innerList.get(index);
		for (Entry<String, Object> entry : map.entrySet())
		{
			String name = entry.getKey();
			Object value = entry.getValue();

			line.put(headerByName(name), value);
		}
	}

	public void renameColumn(String oldValue, String newValue) throws Exception
	{
		for (Header h : this.headers)
		{
			if (h.name.equals(oldValue))
			{
				h.name = newValue;
				return;
			}
		}
		throw new Exception(String.format("Column with name %s not presented into table", oldValue));
	}

	public void addValue(int index, Map<String, Object> map)
	{
		this.changed = true;
		if (this.headers != null)
		{
			Map<Header, Object> line = convert(map);
			if (index >= 0)
			{
				this.innerList.add(index, line);
			}
			else
			{
				this.innerList.add(line);
			}
		}
	}

	public void addValue(int index, Object[] arr)
	{
		this.changed = true;
		if (this.headers != null)
		{
			Map<Header, Object> line = convert(arr);
			this.innerList.add(index, line);
		}
	}

	public void addValue(Object[] arr)
	{
		this.changed = true;
		if (this.headers != null)
		{
			if (this.useColumnNumber)
			{
				arr = Arrays.copyOfRange(arr, 1, arr.length);
			}

			Map<Header, Object> line = convert(arr);
			this.innerList.add(line);
		}
	}

	public void changeValue(String headerName, int indexRow, Object newValue)
	{
		this.changed = true;
		if (this.headers != null)
		{
			Map<Header, Object> row = this.innerList.get(indexRow);
			if(row != null)
			{
				row.remove(headerByName(headerName));
				row.put(headerByName(headerName), newValue);
			}
		}
	}

	public void replace(Object source, Object dest, boolean matchCell, String ...columns)
	{
		if (columns == null || columns.length == 0 || areEqual(source, dest))
		{
			return;
		}

		List<Header> filtered = filter(columns);
		
		for (Map<Header, Object> row : this.innerList)
		{
			for (Header header : filtered)
			{
				Object value = row.get(header);
				if (!matchCell)
				{
					String sValue = String.valueOf(value);
					String sSource = String.valueOf(source);
					if (sValue.contains(sSource)) // TODO why contains ?!!
					{
						row.remove(header);
						row.put(header, sValue.replace(sSource, String.valueOf(dest)));
					}
				}
				else if (areEqual(value, source))
				{
					row.put(header, dest);
				}
			}
		}
	}
	public void replace(String regexp, Object dest, String ...columns)
	{
		if (columns == null || columns.length == 0)
		{
			return;
		}

		List<Header> filtered = filter(columns);
		
		for (Map<Header, Object> row : this.innerList)
		{
			for (Header header : filtered)
			{
				Object value = row.get(header);
				if (String.valueOf(value).matches(regexp))
				{
					row.put(header, dest);
				}
			}
		}
	}

	public void report(ReportBuilder report, String title, boolean withNumbers, boolean reportValues) throws Exception
	{
		String[] columns = Arrays.stream(this.headers).map(h -> h.name).toArray(num -> new String[num]);
		
		report(report, title, withNumbers, reportValues, columns);
	}

	public void report(ReportBuilder report, String title, boolean withNumbers, boolean reportValues, String... columns) throws Exception
	{
		int[] columnsIndexes = getIndexes(columns);

		if (columnsIndexes.length == 0)
		{
			columnsIndexes = new int[this.headers.length];
			for (int i = 0; i < this.headers.length; i++)
			{
				columnsIndexes[i] = i;
			}
		}

		int addition = withNumbers ? 1 : 0;
		String[] headers = new String[addition + columnsIndexes.length];
		if (withNumbers)
		{
			headers[0] = "#";
		}
		int col = 0;
		for (int index : columnsIndexes)
		{
			headers[col++ + addition] = this.headers[index].name;
		}

		ReportTable table = report.addTable(title, true, 0, new int[]{}, headers);

		int count = 0;
		for (Map<Header, Object> row : this.innerList)
		{
			Object[] value = new Object[headers.length];
			if (withNumbers)
			{
				value[0] = count;
			}

			for (int i = addition; i < headers.length; i++)
			{
				Header header = headerByName(headers[i]);
				if (reportValues)
				{
					value[i] = convertCell(header, row.get(header));
				}
				else
				{
					value[i] = row.get(header);
				}
			}
			table.addValues(value);
			count++;
		}
	}

	public boolean extendEquals(ReportBuilder report, Table expected, String[] exclude, boolean ignoreRowsOrder)
	{
		Set<String> expectedNames = names(expected, exclude);
		Set<String> actualNames = names(this, exclude);
		ReportTable table = null;

		if (!expectedNames.equals(actualNames))
		{
			table = createTable(table, report);
			table.addValues("", Arrays.toString(expectedNames.toArray()), Arrays.toString(actualNames.toArray()));
			return false;
		}

		boolean result = true;
		if (ignoreRowsOrder)
		{
			boolean[] actualMatched = new boolean[this.innerList.size()]; 
			boolean[] expectedMatched = new boolean[expected.innerList.size()];

			int actualCounter = 0;
			Iterator<Map<Header, Object>> actualIterator = this.innerList.iterator();
			while (actualIterator.hasNext())
			{
				Map<Header, Object> actualRow = actualIterator.next();

				int expectedCounter = 0;
				Iterator<Map<Header, Object>> expectedIterator = expected.innerList.iterator();
				while (expectedIterator.hasNext())
				{
					Map<Header, Object> expectedRow = expectedIterator.next();
					Map<String, ArrayList<Object>> stringMap = compareRows(convertToStr(actualRow), convertToStr(expectedRow), expectedNames);
					if (stringMap.isEmpty())
					{
						actualMatched[actualCounter] = true;
						expectedMatched[expectedCounter] = true;
					}

					expectedCounter++;
				}
				actualCounter++;
			}

			int count = 0;
			Iterator<Map<Header, Object>> expectedIterator = expected.innerList.iterator();
			while (expectedIterator.hasNext())
			{
				Map<Header, Object> expectedRow = expectedIterator.next();
				if (!expectedMatched[count])
				{
					table = createTable(table, report);
					table.addValues(count, ReportHelper.objToString(expectedRow, false), "");
					result = false;
				}
				count++;
			}

			count = 0;
			actualIterator = this.innerList.iterator();
			while (actualIterator.hasNext())
			{
				Map<Header, Object> actualRow = actualIterator.next();
				if (!actualMatched[count])
				{
					table = createTable(table, report);
					table.addValues(count, "", ReportHelper.objToString(actualRow, false));
					result = false;
				}
				count++;
			}
		}
		else
		{
			Iterator<Map<Header, Object>> actualIterator = this.innerList.iterator();
			Iterator<Map<Header, Object>> expectedIterator = expected.innerList.iterator();

			int rowCount = 0;
			while (actualIterator.hasNext() && expectedIterator.hasNext())
			{
				Map<Header, Object> actualRow = actualIterator.next();
				Map<Header, Object> expectedRow = expectedIterator.next();
				Map<String, ArrayList<Object>> stringMap = compareRows(convertToStr(actualRow), convertToStr(expectedRow), expectedNames);
				if (!stringMap.isEmpty())
				{
					table = createTable(table, report);
					table.addValues(rowCount, ReportHelper.objToString(expectedRow, false), ReportHelper.objToString(actualRow, false));
					for (Entry<String, ArrayList<Object>> entry : stringMap.entrySet())
					{
						table.addValues(entry.getKey(), entry.getValue().get(0), entry.getValue().get(1));
					}
					result = false;
				}

				rowCount++;
			}
			while (expectedIterator.hasNext())
			{
				Map<Header, Object> expectedRow = expectedIterator.next();
				table = createTable(table, report);
				table.addValues(rowCount, ReportHelper.objToString(expectedRow, false), "");
				rowCount++;
				result = false;
			}
			while (actualIterator.hasNext())
			{
				Map<Header, Object> actualRow = actualIterator.next();
				table = createTable(table, report);
				table.addValues(rowCount, "", ReportHelper.objToString(actualRow, false));
				rowCount++;
				result = false;
			}
		}

		return result;
	}

	@Override
	public String toString()
	{
		return Table.class.getSimpleName() + " [" + this.fileName + ":" + Arrays.toString(this.headers) + ":" + size() + "]";
	}

	//==============================================================================================
	// Interface List
	//==============================================================================================

	@Override
	public int size()
	{
		return this.innerList.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.innerList.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return this.innerList.contains(o);
	}

	@Override
	public Iterator<RowTable> iterator()
	{
		return new Iterator<RowTable>()
		{
			private Iterator<Map<Header, Object>> iterator = innerList.iterator();

			@Override
			public boolean hasNext()
			{
				return this.iterator.hasNext();
			}

			@Override
			public RowTable next()
			{
				return convertToStr(this.iterator.next());
			}

			@Override
			public void remove()
			{
				innerList.iterator().remove();
			}
		};
	}

	@Override
	public Object[] toArray()
	{
		return this.innerList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return this.innerList.toArray(a);
	}

	@Override
	public boolean add(RowTable e)
	{
		this.changed = true;
		return this.innerList.add(convert(e));
	}

	@Override
	public boolean remove(Object o)
	{
		this.changed = true;
		return this.innerList.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return this.innerList.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends RowTable> c)
	{
		this.changed = true;
		return this.innerList.addAll(convert(c));
	}

	@Override
	public boolean addAll(int index, Collection<? extends RowTable> c)
	{
		this.changed = true;
		return this.innerList.addAll(index, convert(c));
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		this.changed = true;
		return this.innerList.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		this.changed = true;
		return this.innerList.retainAll(c);
	}

	@Override
	public void clear()
	{
		this.changed = true;
		this.innerList.clear();
	}

	@Override
	public RowTable get(int index)
	{
		return convertToStr(this.innerList.get(index));
	}

	@Override
	public RowTable set(int index, RowTable element)
	{
		this.changed = true;
		Map<Header, Object> convert = convert(element);
		Map<Header, Object> set = this.innerList.set(index, convert);
		return convertToStr(set);
	}

	@Override
	public void add(int index, RowTable element)
	{
		this.changed = true;
		this.innerList.add(index, convert(element));
	}

	@Override
	public RowTable remove(int index)
	{
		this.changed = true;
		Map<Header, Object> remove = this.innerList.remove(index);
		return convertToStr(remove);
	}

	@Override
	public int indexOf(Object o)
	{
		return this.innerList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o)
	{
		return this.innerList.lastIndexOf(o);
	}

	@Override
	public ListIterator<RowTable> listIterator()
	{
		return new TableListIterator(innerList.listIterator());
	}

	@Override
	public ListIterator<RowTable> listIterator(int index)
	{
		return new TableListIterator(innerList.listIterator(index));
	}

	@Override
	public List<RowTable> subList(int fromIndex, int toIndex)
	{
		List<Map<Header, Object>> maps = this.innerList.subList(fromIndex, toIndex);
		List<RowTable> res = new ArrayList<>();
		for (Map<Header, Object> map : maps)
		{
			res.add(convertToStr(map));
		}
		return res;
	}

	//==============================================================================================
	
	public int getHeaderSize()
	{
		return this.headers.length;
	}

	public int getHeaderIndex(String name)
	{
		return IntStream.range(0, this.headers.length).filter(i -> name.equals(this.headers[i].name)).findFirst().orElse(-1);
	}

	public String getHeader(int index)
	{
		return this.headers[index].name;
	}

	public void setHeader(int index, String s)
	{
		if (this.headers[index] != null && Str.areEqual(this.headers[index].name, s))
		{
			return;
		}
		if (columnIsPresent(s))
		{
			throw new ColumnIsPresentException(s);
		}
		this.changed = true;
		this.headers[index].name = s.trim();
	}

	private ReportTable createTable(ReportTable table, ReportBuilder report)
	{
		if (table != null)
		{
			return table;
		}
		return report.addTable("Diffirents", true, 0, new int[]{10, 45, 45}, "#", "Expected", "Actual");
	}

	private Set<String> names(Table expected, String[] exclude)
	{
		Set<String> set = new LinkedHashSet<String>();
		set.addAll(Arrays.asList(exclude));
		Set<String> names = new LinkedHashSet<String>();
		for (Header name : expected.headers)
		{
			if (!set.contains(name.name))
			{
				names.add(name.name);
			}
		}

		return names;
	}

	private Map<String, ArrayList<Object>> compareRows(Map<String, Object> thisRow, Map<String, Object> otherRow, Set<String> names)
	{
		Map<String, ArrayList<Object>> map = new LinkedHashMap<>();
		for (String name : names)
		{
			if (!thisRow.containsKey(name) || !otherRow.containsKey(name))
			{
				map.put(name, new ArrayList<>());
			}

			Object thisValue = thisRow.get(name);
			Object otherValue = otherRow.get(name);

			if (thisValue == null)
			{
				if (otherValue == null)
				{
					continue;
				}
				else if (!otherValue.equals(thisValue))
				{
					ArrayList<Object> objects = new ArrayList<>();
					objects.add(otherValue);
					objects.add(thisValue);
					map.put(name, objects);
				}
			}
			else if (!thisValue.equals(otherValue))
			{
				ArrayList<Object> objects = new ArrayList<>();
				objects.add(otherValue);
				objects.add(thisValue);
				map.put(name, objects);
			}
		}

		return map;
	}

	private void considerAs(Header.HeaderType type, String... columns) throws Exception
	{
		this.changed = true;

		int[] columnsIndexes = getIndexes(columns);
		for (int index : columnsIndexes)
		{
			this.headers[index].type = type;
		}
	}

	private int[] getIndexes(String... columns) throws Exception
	{
		int[] columnsIndexes = new int[columns.length];

		int count = 0;
		for (String column : columns)
		{
			boolean found = false;
			for (int i = 0; i < headers.length; i++)
			{
				if (this.headers[i].name.equals(column))
				{
					columnsIndexes[count++] = i;
					found = true;
					break;
				}
			}
			if (!found)
			{
				throw new Exception("Column '" + column + "' is not found");
			}
		}
		return columnsIndexes;
	}

	private Header headerByName(String name)
	{
		for (Header h : this.headers)
		{
			if (h.name.equals(name))
			{
				return h;
			}
		}
		return null;
	}

	private Map<Header, Object> convert(Object[] e)
	{
		Map<Header, Object> map = new LinkedHashMap<>();
		for (int i = 0; i < Math.min(this.headers.length, e.length); i++)
		{
			map.put(this.headers[i], e[i]);
		}
		return map;
	}

	
	private Map<Header, Object> convert(Map<String, Object> e)
	{
		Map<Header, Object> map = new LinkedHashMap<>();
		Arrays.stream(this.headers).forEach(h -> map.put(h, null));
		
		for (Entry<String, Object> entry : e.entrySet())
		{
			Header key = headerByName(entry.getKey());
			if (key != null)
			{
				map.put(key, entry.getValue());
			}
		}
		return map;
	}

	private Collection<Map<Header, Object>> convert(Collection<? extends Map<String, Object>> e)
	{
		return e.stream().map(this::convert).collect(Collectors.toList());
	}

	private RowTable convertToStr(Map<Header, Object> map)
	{
		RowTable res = new RowTable(map);
		for (Entry<Header, Object> entry : map.entrySet())
		{
			Header header = entry.getKey();
			Object source = entry.getValue();
			res.put(header.name, convertCell(header, source));
		}
		return res;
	}
	
	private Object convertCell(Header header, Object source)
	{
		if (header.type == null)
		{
			return source;
		}
		Object value = null;
		try
		{
			if (header.type == Header.HeaderType.EXPRESSION && this.evaluator != null)
			{
				value = this.evaluator.evaluate("" + source);
			}
			else
			{
				value = Converter.convertToType(source, header.type.clazz);
			}
		}
		catch (Exception e)
		{
			value = e.getMessage();
		}
		
		return value;
	}
	
	private class TableListIterator implements ListIterator<RowTable>
	{
		private ListIterator<Map<Header, Object>> iterator;

		public TableListIterator(ListIterator<Map<Header, Object>> iterator)
		{
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext()
		{
			return this.iterator.hasNext();
		}

		@Override
		public RowTable next()
		{
			return convertToStr(this.iterator.next());
		}

		@Override
		public boolean hasPrevious()
		{
			return this.iterator.hasPrevious();
		}

		@Override
		public RowTable previous()
		{
			return convertToStr(this.iterator.previous());
		}

		@Override
		public int nextIndex()
		{
			return this.iterator.nextIndex();
		}

		@Override
		public int previousIndex()
		{
			return this.iterator.previousIndex();
		}

		@Override
		public void remove()
		{
			this.iterator.remove();
		}

		@Override
		public void set(RowTable stringObjectMap)
		{
			this.iterator.set(convert(stringObjectMap));
		}

		@Override
		public void add(RowTable stringObjectMap)
		{
			this.iterator.add(convert(stringObjectMap));
		}
	}

	private void read(Reader reader, char delimiter) throws Exception
	{
		CsvReader csvReader = null;

		try
		{
			csvReader = new CsvReader(reader);
			csvReader.setSkipEmptyRecords(true);
			csvReader.setDelimiter(delimiter);

			this.headers = null;
			String[] str;

			if (csvReader.readRecord())
			{
				addColumns(csvReader.getValues());

				while (csvReader.readRecord())
				{
					str = csvReader.getValues();

					Map<Header, Object> line = new LinkedHashMap<>();

					for (int i = 0; i < headers.length; i++)
					{
						if (str.length <= i)
						{
							break;
						}
						line.put(headers[i], str[i]);
					}
					this.innerList.add(line);
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
		finally
		{
			if (csvReader != null)
			{
				csvReader.close();
			}
		}
	}

	private void readFilesInfo(String dirName) throws Exception
	{
		try
		{
			this.headers=null;
			File directory = new File(dirName);
			if (directory.isDirectory())
			{
				File[] files = directory.listFiles();
				addColumns("Name", "Size", "Date", "Is directory", "Hidden");
				for (File file : files)
				{
					Map<Header, Object> line = new LinkedHashMap<>();
					line.put(headers[0], file.getName());
					line.put(headers[1], file.length());
					line.put(headers[2], new Date(file.lastModified()));
					line.put(headers[3], file.isDirectory());
					line.put(headers[4], file.isHidden());
					this.innerList.add(line);
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
	private List<Header> filter(String ... columns)
	{
		Set<String> set = new HashSet<String>(Arrays.asList(columns));
		
		return Arrays.stream(this.headers)
				.filter(h -> set.contains(h.name))
				.collect(Collectors.toList());
	}
	
    private static boolean areEqual(Object s1, Object s2)
    {
    	if (s1 == null)
    	{
    		return s1 == s2;
    	}
    	
    	return s1.equals(s2);
    }

	private boolean useColumnNumber = false;
	private static final String ROW_INDEX_SYMBOL = "@";
	private List<Map<Header, Object>> innerList = null;
	private Header[] headers;
	private AbstractEvaluator evaluator; 

	private String fileName;
	static int index = 0;
	private boolean changed;
	private static final Logger logger = Logger.getLogger(Table.class);
}
