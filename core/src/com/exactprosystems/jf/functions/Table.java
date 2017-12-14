////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.functions;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.api.error.JFSQLException;
import com.exactprosystems.jf.api.error.common.WrongExpressionException;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportHelper;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.exceptions.ColumnIsPresentException;
import com.exactprosystems.jf.sql.SqlConnection;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Table implements List<RowTable>, Mutable
{
	//region fields
	private static final String EMPTY_HEADER       = "newH";
	private static final String EMPTY_ROW		   = "newR";
    
	protected static int index = 0;

	private boolean useColumnNumber = false;
    private static final String ROW_VAR_NAME       = "_";
	private static final String ROW_INDEX_SYMBOL   = "@";
	private List<Map<Header, Object>> innerList    = null;
	private Header[] headers;
	private AbstractEvaluator evaluator;

	private boolean changed;
	private static final Logger logger = Logger.getLogger(Table.class);

	private BiConsumer<Integer,Integer> onChangeConsumer;
	//endregion

	public static class TableCompareResult
	{
	    public TableCompareResult(boolean equal, int matched, int extraExpected, int extraActual)
        {
	        this.equal = equal;
	        this.matched = matched;
	        this.extraExpected = extraExpected;
	        this.extraActual = extraActual;
        }
	    public final boolean equal;
	    public final int matched;
        public final int extraExpected;
        public final int extraActual;
	}
	
	//region Constructors
	private Table(AbstractEvaluator evaluator)
	{
		this.evaluator = evaluator;
		this.innerList = new ArrayList<>();
	}

	/**
	 * copy constructor
	 */
	public Table(Table table)
	{
		this(table.evaluator);
		this.addColumns(Arrays.stream(table.headers).map(h -> h.name).toArray(String[]::new));
		this.addAll(table);
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
            
			RowTable res = addNew();
			for (int j = 0; j < line.length; j++)
			{
				res.put(firstLine[j], line[j]);
			}
		}
	}

    public Table(ResultSet set, AbstractEvaluator evaluator) throws JFSQLException
	{
		this(evaluator);

		try
		{
			ResultSetMetaData meta = set.getMetaData();
			this.headers = new Header[meta.getColumnCount()];

			for (int column = 0; column < meta.getColumnCount(); column++)
			{
				this.headers[column] = new Header(meta.getColumnLabel(column + 1), Header.HeaderType.forName(meta.getColumnClassName(column + 1)));
			}

			while (set.next())
			{
				Map<Header, Object> line = new LinkedHashMap<>();

				for (int i = 0; i < headers.length; i++)
				{
				    int type = set.getMetaData().getColumnType(i + 1);
				    
				    Object value = null;
					if (type == Types.LONGVARBINARY || type == Types.BLOB || type == Types.VARBINARY)
				    {
				        value = set.getBlob(i + 1);
				        if (value != null)
				        {
				            Object newValue = Converter.blobToStorable((Blob)value);
				            if (newValue == null)
				            {
				                newValue = Converter.blobToByteArray((Blob)value); 
				            }
				            value = newValue;
				        }
				    }
				    else
				    {
				        value = set.getObject(i + 1);
				    }
				    
					line.put(headers[i], value);
				}

				this.innerList.add(line);
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new JFSQLException(e.getMessage(), e);
		}
	}

	public Table(String fileName, char delimiter, AbstractEvaluator evaluator) throws Exception
	{
		this(evaluator);
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
	//endregion

	//region Static constructors
	public static Table emptyTable()
	{
		return new Table(new String[][]{new String[]{EMPTY_HEADER}, new String[]{EMPTY_ROW}}, null);
	}
	//endregion

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

	//endregion

	public void setOnChangeListener(BiConsumer<Integer, Integer> consumer)
	{
		this.onChangeConsumer = consumer;
	}

	public void fire()
	{
		Optional.ofNullable(this.onChangeConsumer).ifPresent(c -> c.accept(this.size(), this.size()));
	}

	public static TableCompareResult extendEquals(ReportBuilder report, Table differences, Table actual, Table expected, String[] exclude, 
	        boolean ignoreRowsOrder, boolean compareValues)
	{
		Set<String> expectedNames = expected.names(exclude);
		Set<String> actualNames   = actual.names(exclude);
		ReportTable table = null;

		if (!expectedNames.equals(actualNames))
		{
			table = addMismatchedRow(table, report, differences, "Column names", Arrays.toString(expectedNames.toArray()), Arrays.toString(actualNames.toArray()));
			return new TableCompareResult(false, 0, 0, 0);
		}

        int matched = 0;
        int extraExpected = 0;
        int extraActual = 0;
		
		boolean result = true;
		if (ignoreRowsOrder)
		{
			boolean[] actualMatched = new boolean[actual.size()]; 
			boolean[] expectedMatched = new boolean[expected.size()];

			int actualCounter = 0;
			Iterator<RowTable> actualIterator = actual.iterator();
			while (actualIterator.hasNext())
			{
                CopyRowTable actualRow = actualIterator.next().copy(expectedNames, compareValues);
				int expectedCounter = 0;
				Iterator<RowTable> expectedIterator = expected.iterator();
				while (expectedIterator.hasNext())
				{
				    if (expectedMatched[expectedCounter])
				    {
				        expectedIterator.next();
				        expectedCounter++;
				        continue;
				    }
				    CopyRowTable expectedRow = expectedIterator.next().copy(expectedNames, compareValues);
					if (Objects.equals(actualRow, expectedRow))
					{
					    matched++;
						actualMatched[actualCounter] = true;
						expectedMatched[expectedCounter] = true;
						break;
					}

					expectedCounter++;
				}
				actualCounter++;
			}

			int count = 0;
			Iterator<RowTable> expectedIterator = expected.iterator();
			while (expectedIterator.hasNext())
			{
			    CopyRowTable expectedRow = expectedIterator.next().copy(expectedNames, compareValues);
				if (!expectedMatched[count])
				{
				    extraExpected++;
					table = addMismatchedRow(table, report, differences, "Extra row[" + count + "]", ReportHelper.objToString(expectedRow, false), "");
					result = false;
				}
				count++;
			}

			count = 0;
			actualIterator = actual.iterator();
			while (actualIterator.hasNext())
			{
			    CopyRowTable actualRow = actualIterator.next().copy(expectedNames, compareValues); 
				if (!actualMatched[count])
				{
				    extraActual++;
					table = addMismatchedRow(table, report, differences, "Extra row[" + count + "]", "", ReportHelper.objToString(actualRow, false));
					result = false;
				}
				count++;
			}
		}
		else // ignoreRowsOrder
		{
			Iterator<RowTable> actualIterator   = actual.iterator();
			Iterator<RowTable> expectedIterator = expected.iterator();

			int rowCount = 0;
			while (actualIterator.hasNext() && expectedIterator.hasNext())
			{
				CopyRowTable actualRow = actualIterator.next().copy(expectedNames, compareValues);
				CopyRowTable expectedRow = expectedIterator.next().copy(expectedNames, compareValues);
				if (Objects.equals(actualRow, expectedRow))
                {
                    matched++;
                }
				else
				{
	                extraExpected++;
	                extraActual++;
					table = addMismatchedRow(table, report, differences, "Row[" + rowCount + "]", ReportHelper.objToString(expectedRow, false), ReportHelper.objToString(actualRow, false));
					for (String name : expectedNames)
					{
                        String expectedValue = Str.asString(expectedRow.get(name));
                        String actualValue   = Str.asString(actualRow.get(name));
	                    table = addMismatchedRow(table, report, differences, "Row[" + rowCount + "]." + name, expectedValue, actualValue);
					}
					result = false;
				}

				rowCount++;
			}
			while (expectedIterator.hasNext())
			{
			    extraExpected++;
                CopyRowTable expectedRow = expectedIterator.next().copy(expectedNames, compareValues);
				table = addMismatchedRow(table, report, differences, "Extra row[" + rowCount + "]", ReportHelper.objToString(expectedRow, false), "");
				rowCount++;
				result = false;
			}
			while (actualIterator.hasNext())
			{
			    extraActual++;
			    CopyRowTable actualRow = actualIterator.next().copy(expectedNames, compareValues);
				table = addMismatchedRow(table, report, differences, "Extra row[" + rowCount + "]", "", ReportHelper.objToString(actualRow, false));
				rowCount++;
				result = false;
			}
		}

		return new TableCompareResult(result, matched, extraExpected, extraActual);
	}

	public static String generateColumnName(Table table)
	{
		int currentIndexColumn = 0;
		String columnName = "NewColumn", temp = "NewColumn";
		while (table.columnIsPresent(columnName))
		{
			columnName = temp + currentIndexColumn++;
		}
		return columnName;
	}

	@Override
	public String toString()
	{
		return Table.class.getSimpleName() + " " + Arrays.toString(this.headers) + ":" + size();
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
		    private int index = 0;

			@Override
			public boolean hasNext()
			{
			    return this.index < Table.this.size();
			}

			@Override
			public RowTable next()
			{
			    return Table.this.get(this.index++);
			}

			@Override
			public void remove()
			{
			    Table.this.remove(this.index);
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
		changed(true);
		return this.innerList.add(convert(e));
	}

	@Override
	public boolean remove(Object o)
	{
		changed(true);
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
		changed(true);
		return this.innerList.addAll(convert(c));
	}

	@Override
	public boolean addAll(int index, Collection<? extends RowTable> c)
	{
		changed(true);
		return this.innerList.addAll(index, convert(c));
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		changed(true);
		return this.innerList.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		changed(true);
		return this.innerList.retainAll(c);
	}

	@Override
	public void clear()
	{
		changed(true);
		this.innerList.clear();
	}

	@Override
	public RowTable get(int index)
	{
		checkRange(index);
		return new RowTable(this, index);
	}

	@Override
	public RowTable set(int index, RowTable element)
	{
		changed(true);
		Map<Header, Object> convert = convert(element);
		this.innerList.set(index, convert);
		return get(index);
	}

	@Override
	public void add(int index, RowTable element)
	{
		changed(true);
		this.innerList.add(index, convert(element));
	}

	@Override
	public RowTable remove(int index)
	{
		changed(true);
		RowTable res = get(index).copy();
		this.innerList.remove(index);
		return res;
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
		return new TableListIterator(this, 0);
	}

	@Override
	public ListIterator<RowTable> listIterator(int index)
	{
		return new TableListIterator(this, index);
	}

	@Override
	public List<RowTable> subList(int fromIndex, int toIndex)
	{
		List<RowTable> res = new ArrayList<>();
		for (int index = fromIndex; index < toIndex; index++)
		{
		    res.add(get(index));
		}
		return res;
	}
	//endregion

    public RowTable addNew()
    {
        int index = this.innerList.size();
        addValue(index, Collections.emptyMap());
        return get(index);
    }
    
    public List<Object> getColumnAsList(String column, boolean getValues)
    {
        List<Object> res = new ArrayList<>(size());
        Header header = headerByName(column);
        
        for (Map<Header, Object> row : this.innerList)
        {
            Object value = row.get(header);
            if (getValues)
            {
                value = convertCell(row, header, value, null);
            }
            res.add(value);
        }
        return res;
    }
    
    public Set<String> names(String[] exclude)
    {
        Set<String> set = new LinkedHashSet<String>();
        set.addAll(Arrays.asList(exclude));
        Set<String> names = new LinkedHashSet<String>();
        for (Header name : this.headers)
        {
            if (!set.contains(name.name))
            {
                names.add(name.name);
            }
        }
        return names;
    }

	public Table sort(String colName, boolean az, boolean ignoreCase) throws Exception
	{
		changed(true);
		for (int i = 0; i < this.headers.length; i++)
		{
			if (this.headers[i].name.equals(colName))
			{
				return this.sort(i, az, ignoreCase);
			}
		}
		throw new Exception(String.format(R.TABLE_COLUMN_NOT_FOUND.get(), colName));
	}

	public Table sort(int colNumber, boolean az, boolean ignoreCase)
	{
		changed(true);
		Header header = this.headers[colNumber];
		this.innerList.sort((o1, o2) ->
		{
			Object obj1 = o1.get(header);
			Object obj2 = o2.get(header);

			obj1 = convertCell(o1, header, obj1, null);
			obj2 = convertCell(o2, header, obj2, null);

			Header.HeaderType type = header.type == null ? Header.HeaderType.STRING : header.type;
			int compare = type.compare(obj1, obj2, ignoreCase);
			return az ? compare : -compare;
		});
		return this;
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

	public void considerAsGroup(String... columns) throws Exception
	{
		considerAs(Header.HeaderType.GROUP, columns);
	}

    public void considerAsHyperlink(String... columns) throws Exception
    {
        considerAs(Header.HeaderType.HYPERLINK, columns);
    }

    public void considerAsColored(String... columns) throws Exception
    {
        considerAs(Header.HeaderType.COLORED, columns);
    }

	public Table select(Condition[] conditions)
	{
		Table result = new Table(this.evaluator);
		result.headers = new Header[this.headers.length];
		for (int i = 0; i < result.headers.length; i++)
		{
			result.headers[i] = new Header(this.headers[i]);
		}

		for (int index = 0; index < size(); index++)
		{
            RowTable rowTable =  get(index);
            boolean matched = true;
            for (Condition condition : conditions)
            {
                if (!condition.isMatched(rowTable))
                {
                    matched = false;
                    break;
                }
            }

            if (matched)
            {
                result.add(rowTable);
            }
		}
		return result;
	}

	public void upload(SqlConnection connection, String table) throws JFSQLException
	{
		try
		{
			Statement statement = null;
			try
			{
				//TODO add check, that map.get(key) may be Blob or something else
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
		catch (SQLException e)
		{
			throw new JFSQLException(e);
		}
	}

	public List<Integer> findAllIndexes(Condition[] conditions)
	{
		List<Integer> indexes = new ArrayList<>();
		for (int index = 0; index < size(); index++)
		{
            RowTable rowTable =  get(index);
            boolean matched = true;
            for (Condition condition : conditions)
            {
                if (!condition.isMatched(rowTable))
                {
                    matched = false;
                    break;
                }
            }

            if (matched)
            {
                indexes.add(index);
            }
		}
		return indexes;
	}

	public void replace(Object source, Object dest, boolean matchCell, String ...columns)
	{
		if (columns == null || columns.length == 0 || Objects.equals(source, dest))
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
				else if (Objects.equals(value, source))
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

	public void report(ReportBuilder report, String title, String beforeTestcase, boolean withNumbers, boolean reportValues) throws Exception
	{
		report(report, title, beforeTestcase, withNumbers, reportValues, true, Collections.emptyMap(), null);
	}

    public void report(ReportBuilder report, String title, String beforeTestcase, boolean withNumbers,
            boolean reportValues, boolean bordered, Map<String, String> columns, int[] widths) throws Exception
    {
        if (beforeTestcase != null || report.reportIsOn())
        {
            int[] columnsIndexes = columns.isEmpty() ? IntStream.range(0, this.headers.length).toArray() : getIndexes(columns.keySet().toArray(new String[] {}));

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
            
            widths = widths == null ? new int[] {} : widths;
            headers = convertHeaders(columns, headers, withNumbers);
            ReportTable table = report.addExplicitTable(title, beforeTestcase, true, bordered, widths, headers);

            Function<String, String> func = name -> columns.isEmpty() ? name
                    : columns.entrySet().stream().filter(e -> name.equals(String.valueOf(e.getValue()))).findFirst()
                            .map(Entry::getKey).orElse(name);

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
                    Header header = headerByName(func.apply(headers[i]));
                    if (reportValues)
                    {
                        value[i] = convertCell(row, header, row.get(header), report);
                    }
                    else
                    {
                        Object v = row.get(header);
                        if (v instanceof ImageWrapper)
                        {
                            ImageWrapper iw = (ImageWrapper) v;
                            String description = iw.getDescription() == null ? iw.toString() : iw.getDescription();
                            v = report.decorateLink(description,
                                    report.getImageDir() + File.separator + iw.getName(report.getReportDir()));
                        }
                        else if (v instanceof ReportBuilder)
                        {
                            ReportBuilder rb = (ReportBuilder) v;
                            String name = rb.getName();

                            v = report.decorateLink(name, name);
                        }
                        value[i] = v;
                    }
                }
                table.addValues(value);
                count++;
            }
        }
    }

	public boolean removeRow(int index)
	{
		this.innerList.remove(index);
		changed(true);
		return true;
	}

	public boolean isEmptyTable()
	{
		return this.headers.length == 1
				&& this.headers[0].name.equals(EMPTY_HEADER)
				&& this.innerList.size() == 1
				&& this.innerList.get(0).get(headerByName(EMPTY_HEADER)).equals(EMPTY_ROW);
	}

	public void fillFromTable(Table table)
	{
		this.headers = new ArrayList<Header>().toArray(new Header[0]);
		clear();
		addColumns(Arrays.stream(table.headers).map(h -> h.name).toArray(String[]::new));
		addAll(table);
	}
	
	public void setEvaluator(AbstractEvaluator evaluator)
	{
		this.evaluator = evaluator;
	}
	
	public boolean save(String fileName, char delimiter, boolean saveValues, boolean withNmumbers)
	{
		CsvWriter writer = null;

		try (Writer bufferedWriter = CommonHelper.writerToFileName(fileName))
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
					value = convertCell(f, headers[i], source, null);
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

	public void addColumns(String... columns)
	{
		changed(true);
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
		addEmptyStringToAllLinesInNewColumn();
	}

	public boolean columnIsPresent(String columnName)
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

	public void updateValue(int index, RowTable row)
	{
		changed(true);
		Map<Header, Object> newMap = new LinkedHashMap<>();
		Map<Header, Object> oldMap = this.innerList.get(index);
		Arrays.stream(this.headers).forEach(h ->
		{
			Object rowValue = row.get(h.name);
			if (rowValue == null)
			{
				rowValue = oldMap.get(h);
			}
			newMap.put(h, rowValue);
		});
		this.innerList.set(index, newMap);
	}

    public Object setValue(int index, String key, Object value) 
    {
        changed(true);
        Header header = headerByName(key);
        Map<Header, Object> row = this.innerList.get(index);
        return row.put(header, value);
    }

	public void setValue(int index, RowTable row)
	{
		changed(true);
		this.innerList.set(index, convert(row));
	}

	public void setValue(int index, Map<String, Object> map)
	{
		changed(true);
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
		throw new Exception(String.format(R.TABLE_COLUMN_NOT_FOUND.get(), oldValue));
	}

	public void addValue(int index, Map<String, Object> map)
	{
		changed(true);
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

	public void addValue(Object[] arr) 
	{
		changed(true);
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

	public int getHeaderSize()
	{
		return this.headers.length;
	}

	public String getHeader(int index)
	{
		return this.headers[index].name;
	}

    public void addColumns(int index, String... columns)
    {
        for (String column : columns)
        {
            if (columnIsPresent(column))
            {
                throw new ColumnIsPresentException(column);
            }
        }
        changed(true);
        if (this.headers == null)
        {
            this.headers = new Header[]{};
        }
        List<Header> newHeaders = new ArrayList<>(Arrays.asList(this.headers));
        newHeaders.addAll(index, Arrays.stream(columns).map(s -> new Header(s, null)).collect(Collectors.toList()));
        this.headers = newHeaders.toArray(new Header[newHeaders.size()]);
        addEmptyStringToAllLinesInNewColumn();
    }

    public void removeColumns(String... columns)
    {
        if (this.headers == null)
        {
            return;
        }
        changed(true);
        List<String> strings = Arrays.asList(columns);
        List<Header> headers = new ArrayList<>(Arrays.asList(this.headers));
        Iterator<Header> iterator = headers.iterator();
        while (iterator.hasNext())
        {
            Header header = iterator.next();
            String next = header.name;
            if (strings.contains(next))
            {
                this.innerList.forEach(row -> row.remove(header));
                iterator.remove();
            }

        }
        this.headers = headers.toArray(new Header[headers.size()]);
    }

    public void addValue(int index, Object[] arr)
    {
        changed(true);
        if (this.headers != null)
        {
            Map<Header, Object> line = convert(arr);
            this.innerList.add(index, line);
        }
    }

    public void changeValue(String headerName, int indexRow, Object newValue)
    {
        changed(true);
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
        changed(true);
        this.headers[index].name = s.trim();
    }

	public String[] getHeadersAsStringArray() {
		String[] strArr = new String[this.headers.length];
		for(int i = 0; i < this.headers.length; i++) {
			strArr[i] = String.valueOf(this.headers[i]);
		}
		return strArr;
	}

	//region private methods
	private void checkRange(int index)
	{
		if (index >= this.innerList.size())
		{
			throw new IndexOutOfBoundsException(String.format(R.TABLE_IOOB_EXCEPTION.get(), index, this.innerList.size()));
		}
	}

	private String[] convertHeaders(Map<String, String> parameters, String[] headers, boolean withNumbers)
	{
		if (parameters.isEmpty())
		{
			return headers;
		}
		List<String> list = new ArrayList<>();
		if (withNumbers)
		{
			list.add("#");
		}
		list.addAll(parameters.values()
				.stream()
				.map(String::valueOf)
				.collect(Collectors.toList())
		);

		return list.toArray(new String[list.size()]);
	}

	private void addEmptyStringToAllLinesInNewColumn()
	{
		final String EMPTY_STRING = "";
		this.innerList.forEach(row ->
				Arrays.stream(this.headers)
					.filter(header -> !row.containsKey(header))
					.forEach(header -> row.put(header, EMPTY_STRING))
		);
	}


	private static ReportTable addMismatchedRow(ReportTable table, ReportBuilder report, Table differences, String name, String expectedValue, String actualValue)
	{
	    ReportTable result = table;
		if (result == null)
		{
		    result = report.addTable(R.TABLE_DIFFERENCES.get(), null, true, true, new int[]{30, 35, 35}, R.TABLE_DESCRIPTION.get(), R	.TABLE_EXPECTED.get(), R.TABLE_ACTUAL.get());
		}
		result.addValues(name, expectedValue, actualValue);
        differences.addValue(new String[] { name, expectedValue, actualValue } );
		
        return result; 
	}

	private void considerAs(Header.HeaderType type, String... columns) throws Exception
	{
		changed(true);

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
				throw new Exception(String.format(R.TABLE_COLUMN_NOT_FOUND.get(), column));
			}
		}
		return columnsIndexes;
	}

	protected Header headerByName(String name)
	{
		return Arrays.stream(this.headers)
				.filter(h -> h.name.equals(name))
				.findFirst()
				.orElseThrow(() -> new WrongExpressionException(String.format(R.TABLE_COLUMN_NOT_FOUND.get(), name)));
	}

	private Map<Header, Object> convert(Object[] arr)
	{
        Map<Header, Object> res = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(this.headers.length, arr.length); i++)
        {
            res.put(this.headers[i], arr[i]);
        }
        Set<Header>  keys = res.keySet();
        Arrays.stream(this.headers).filter(h -> !keys.contains(h)).forEach(h -> res.put(h, null));
        return res;
	}

	private Map<Header, Object> convert(Map<String, Object> map)
	{
        Map<Header, Object> res = new LinkedHashMap<>();
        for (Entry<String, Object> e : map.entrySet())
        {
            res.put(headerByName(e.getKey()), e.getValue());
        }
		Set<Header>  keys = res.keySet();
		Arrays.stream(this.headers).filter(h -> !keys.contains(h)).forEach(h -> res.put(h, null));
		return res;
	}

	private Collection<Map<Header, Object>> convert(Collection<? extends Map<String, Object>> e)
	{
		return e.stream().map(this::convert).collect(Collectors.toList());
	}

    protected Object convertCell(Map<Header, Object> row,  Header header, Object source, ReportBuilder report)
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
                this.evaluator.getLocals().set(ROW_VAR_NAME, RowTable.asLinkedMap(row));
                value = this.evaluator.evaluate("" + source);
                this.evaluator.getLocals().delete(ROW_VAR_NAME);
            }
            else if (header.type == Header.HeaderType.GROUP)
            {
                if (report != null)
                {
                    AtomicInteger level = new AtomicInteger(0);
                    StringBuilder group = new StringBuilder();
                    boolean isNode = extract(Str.asString(source), group, level);
                    value = report.decorateGroupCell(group, level.get(), isNode);
                }
                else
                {
                    value = "" + source;
                }
            }
            else if (header.type == Header.HeaderType.HYPERLINK)
            {
                if (report != null)
                {
                    if (source instanceof ImageWrapper)
                    {
                        ImageWrapper iw = (ImageWrapper) source;
                        String description = iw.getDescription() == null ? iw.toString() : iw.getDescription();
                        value = report.decorateLink(description,
                                report.getImageDir() + File.separator + iw.getName(report.getReportDir()));
                    }
                    else if (source instanceof ReportBuilder)
                    {
                        ReportBuilder rb = (ReportBuilder) source;
                        String name = rb.getName();
                        value = report.decorateLink(name, name);
                    }
                    else
                    {
                        StringBuilder name = new StringBuilder();
                        StringBuilder link = new StringBuilder();
                        splitToTwo(Str.asString(source), name, link);
                        value = report.decorateLink(name.toString(), link.toString());
                    }
                }
                else
                {
                    value = "" + source;
                }
            }
            else if (header.type == Header.HeaderType.COLORED)
            {
                if (report != null)
                {
                    StringBuilder name = new StringBuilder();
                    StringBuilder color = new StringBuilder();
                    splitToTwo(Str.asString(source), name, color);
                    value = report.decorateStyle(name.toString(), color.toString());
                }
                else
                {
                    value = "" + source;
                }
            }
            else
            {
                value = Converter.convertToType(source, header.type.clazz);
            }
        }
        catch (Exception e)
        {
            value = e;
        }

        return value;
    }
	
	private void splitToTwo(String str, StringBuilder part1, StringBuilder part2)
    {
        String[] parts = str.split("\\|");
        if (parts.length > 1)
        {
            part1.append(parts[0]);
            part2.append(parts[1]);
        }
        else
        {
            part1.append(parts[0]);
            part2.append(parts[0]);
        }
    }

    private boolean extract(String path, StringBuilder group,  AtomicInteger outLevel)
	{
	    String[] parts = path.split("/");
	    int last = parts.length - 1;
	    if (parts[last].equals("*"))
	    {
	        group.append(parts[last - 1]);
	        outLevel.set(Math.max(0, parts.length - 2));
	        return true;
	    }
	    else
	    {
	        group.append(parts[last]);
            outLevel.set(Math.max(0, parts.length - 1));
	        return false;
	    }
	}
	
	private class TableListIterator implements ListIterator<RowTable>
	{
	    private Table table;
	    private int index;

		public TableListIterator(Table table, int index)
		{
		    this.table = table;
		    this.index = index;
		}

		@Override
		public boolean hasNext()
		{
		    return this.index < this.table.size();
		}

		@Override
		public RowTable next()
		{
		    return this.table.get(this.index++);
		}

		@Override
		public boolean hasPrevious()
		{
		    return this.index > 0;
		}

		@Override
		public RowTable previous()
		{
            return this.table.get(this.index--);
		}

		@Override
		public int nextIndex()
		{
		    return this.index + 1;
		}

		@Override
		public int previousIndex()
		{
		    return this.index - 1;
		}

		@Override
		public void remove()
		{
		    this.table.remove(this.index);
		}

		@Override
		public void set(RowTable stringObjectMap)
		{
		    this.table.setValue(this.index, stringObjectMap);
		}

		@Override
		public void add(RowTable stringObjectMap)
		{
		    this.table.add(this.index, stringObjectMap);
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
			File directory = new File(dirName);

				this.headers = null;
				addColumns("Name", "Size", "Date", "Is directory", "Hidden");
				this.considerAsString("Name");
				this.considerAsBoolean("Is directory", "Hidden");
				this.considerAsDouble("Size");
				this.considerAsDate("Date");
				File[] files = directory.listFiles();
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
	
    private void changed(boolean flag)
    {
        this.changed = flag;
    }
	//endregion

    protected Map<Header, Object> getInner(int index)
    {
        return this.innerList.get(index);
    }
}
