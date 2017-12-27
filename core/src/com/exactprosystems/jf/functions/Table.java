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
import com.exactprosystems.jf.documents.config.Configuration;
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
	private static final Logger logger = Logger.getLogger(Table.class);

	private static final String EMPTY_HEADER       = "newH";
	private static final String EMPTY_ROW		   = "newR";

	protected static int index = 0;

	private              boolean                   useColumnNumber  = false;
	private static final String                    ROW_VAR_NAME     = "_";
	private static final String                    ROW_INDEX_SYMBOL = "@";
	private              List<Map<Header, Object>> innerList        = null;

	private Header[]                     headers;
	private AbstractEvaluator            evaluator;
	private boolean                      changed;
	private BiConsumer<Integer, Integer> onChangeConsumer;
	//endregion

	public static class TableCompareResult
	{
		public final boolean equal;
		public final int     matched;
		public final int     extraExpected;
		public final int     extraActual;

		public TableCompareResult(boolean equal, int matched, int extraExpected, int extraActual)
		{
			this.equal = equal;
			this.matched = matched;
			this.extraExpected = extraExpected;
			this.extraActual = extraActual;
		}
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

	/**
	 * Create the table with passed headers
	 */
	public Table(String[] headers, AbstractEvaluator evaluator)
	{
		this(evaluator);
		this.addColumns(headers);
	}

	/**
	 * Create the table from two-dimensional array
	 */
	public Table(String[][] lines, AbstractEvaluator evaluator)
	{
		this(evaluator);

		String[] headerLine = lines[0];
		this.addColumns(headerLine);
		Arrays.stream(lines, 1, lines.length)
				.forEach(line ->
				{
					RowTable res = this.addNew();
					IntStream.range(0, line.length)
							.forEach(j -> res.put(headerLine[j], line[j]));
				});
	}

	/**
	 * Create the table from passed ResultSet
	 */
	public Table(ResultSet resultSet, AbstractEvaluator evaluator) throws JFSQLException
	{
		this(evaluator);

		try
		{
			ResultSetMetaData meta = resultSet.getMetaData();
			this.headers = new Header[meta.getColumnCount()];

			for (int column = 0; column < meta.getColumnCount(); column++)
			{
				this.headers[column] = new Header(meta.getColumnLabel(column + 1), Header.HeaderType.forName(meta.getColumnClassName(column + 1)));
			}

			while (resultSet.next())
			{
				Map<Header, Object> line = new LinkedHashMap<>();

				for (int i = 0; i < this.headers.length; i++)
				{
					int type = resultSet.getMetaData().getColumnType(i + 1);

					Object value;
					if (type == Types.LONGVARBINARY || type == Types.BLOB || type == Types.VARBINARY)
					{
						value = resultSet.getBlob(i + 1);
						if (value != null)
						{
							Object newValue = Converter.blobToStorable((Blob) value);
							if (newValue == null)
							{
								newValue = Converter.blobToByteArray((Blob) value);
							}
							value = newValue;
						}
					}
					else
					{
						value = resultSet.getObject(i + 1);
					}

					line.put(this.headers[i], value);
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

	/**
	 * Read a table from the passed file name with separate - passed delimiter
	 */
	public Table(String fileName, char delimiter, AbstractEvaluator evaluator) throws Exception
	{
		this(evaluator);
		try (Reader reader = new BufferedReader(new FileReader(fileName)))
		{
			this.read(reader, delimiter);
		}
	}

	/**
	 * Read a table from the passed reader with separate the passed delimiter
	 */
	public Table(Reader reader, char delimiter, AbstractEvaluator evaluator) throws Exception
	{
		this(evaluator);
		this.read(reader, delimiter);
	}

	/**
	 * Read the passed directory and create a table, which has all files from the directory
	 */
	public Table(String dirName, AbstractEvaluator evaluator) throws Exception
	{
		this(evaluator);
		this.readFromDirectory(dirName);
	}
	//endregion

	//region Static constructors

	/**
	 * Create a empty table, which has only one column {@link Table#EMPTY_HEADER} and one row with value {@link Table#EMPTY_ROW}
	 *
	 * @return the created empty table
	 */
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

	public void setOnChangeListener(BiConsumer<Integer, Integer> consumer)
	{
		this.onChangeConsumer = consumer;
	}

	/**
	 * Force call the change listener
	 */
	public void fire()
	{
		Optional.ofNullable(this.onChangeConsumer).ifPresent(c -> c.accept(this.size(), this.size()));
	}

	/**
	 * Return difference between 2 tables
	 * @param report the report
	 * @param differences the table, which will has difference between passed tables
	 * @param actual the first table
	 * @param expected the second table
	 * @param exclude the array of columns, which will exclude from find difference
	 * @param ignoreRowsOrder the attribute, which indicate, how this method should find difference - ignore rows or not
	 * @param compareValues the attribute, which indicate, should check values or just expressions
	 * @return a TableCompareResult with difference between tables
	 */
	public static TableCompareResult extendEquals(ReportBuilder report, Table differences, Table actual, Table expected, String[] exclude, boolean ignoreRowsOrder, boolean compareValues)
	{
		Set<String> expectedNames = expected.names(exclude);
		Set<String> actualNames   = actual.names(exclude);
		ReportTable table = null;

		if (!expectedNames.equals(actualNames))
		{
			addMismatchedRow(null, report, differences, "Column names", Arrays.toString(expectedNames.toArray()), Arrays.toString(actualNames.toArray()));
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
					table = addMismatchedRow(table, report, differences, String.format("Extra row[%d]", count), ReportHelper.objToString(expectedRow, false), "");
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
					table = addMismatchedRow(table, report, differences, String.format("Extra row[%d]", count), "", ReportHelper.objToString(actualRow, false));
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
						String actualValue = Str.asString(actualRow.get(name));
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
				table = addMismatchedRow(table, report, differences, String.format("Extra row[%d]", rowCount), ReportHelper.objToString(expectedRow, false), "");
				rowCount++;
				result = false;
			}
			while (actualIterator.hasNext())
			{
				extraActual++;
				CopyRowTable actualRow = actualIterator.next().copy(expectedNames, compareValues);
				table = addMismatchedRow(table, report, differences, String.format("Extra row[%d]", rowCount), "", ReportHelper.objToString(actualRow, false));
				rowCount++;
				result = false;
			}
		}

		return new TableCompareResult(result, matched, extraExpected, extraActual);
	}

	/**
	 * Generate a new unique column name from the passed table.
	 */
	public static String generateColumnName(Table table)
	{
		int currentIndexColumn = 0;
		String columnName = "NewColumn";
		String temp = "NewColumn";
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

	//region Interface List
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
		this.changed(true);
		return this.innerList.add(this.convert(e));
	}

	@Override
	public boolean remove(Object o)
	{
		this.changed(true);
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
		this.changed(true);
		return this.innerList.addAll(this.convert(c));
	}

	@Override
	public boolean addAll(int index, Collection<? extends RowTable> c)
	{
		this.changed(true);
		return this.innerList.addAll(index, this.convert(c));
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		this.changed(true);
		return this.innerList.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		this.changed(true);
		return this.innerList.retainAll(c);
	}

	@Override
	public void clear()
	{
		this.changed(true);
		this.innerList.clear();
	}

	@Override
	public RowTable get(int index)
	{
		this.checkRange(index);
		return new RowTable(this, index);
	}

	@Override
	public RowTable set(int index, RowTable element)
	{
		this.changed(true);
		Map<Header, Object> convert = this.convert(element);
		this.innerList.set(index, convert);
		return this.get(index);
	}

	@Override
	public void add(int index, RowTable element)
	{
		this.changed(true);
		this.innerList.add(index, this.convert(element));
	}

	@Override
	public RowTable remove(int index)
	{
		this.changed(true);
		RowTable res = this.get(index).copy();
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
		return IntStream.range(fromIndex, toIndex)
				.mapToObj(this::get)
				.collect(Collectors.toList());
	}
	//endregion

	//region public API

	/**
	 * Add new empty row to the table
	 * @return the last row on the table
	 */
	public RowTable addNew()
	{
		int size = this.innerList.size();
		this.addValue(size, Collections.emptyMap());
		return this.get(size);
	}

	/**
	 * Collect the expressions ( or if the parameters getValues is true - evaluated values) for the passed column from the table
	 *
	 * @param column name of column, for which need return a list.
	 * @param getValues if true, the list will contains evaluated values. Otherwise the list will contains only expression ( String objects)
	 *
	 * @return the list of cells for the column
	 */
	public List<Object> getColumnAsList(String column, boolean getValues)
	{
		List<Object> res = new ArrayList<>(this.size());
		Header header = this.headerByName(column);

		for (Map<Header, Object> row : this.innerList)
		{
			Object value = row.get(header);
			if (getValues)
			{
				value = this.convertCell(row, header, value, null);
			}
			res.add(value);
		}
		return res;
	}

	/**
	 * Collect and return a set of columns for the table, exclude the passed columns name
	 *
	 * @param exclude the array of columns, which should be exclude from resulting set
	 *
	 * @return the set of columns name
	 */
	public Set<String> names(String[] exclude)
	{
		Set<String> set = new LinkedHashSet<>(Arrays.asList(exclude));
		return Arrays.stream(this.headers)
				.filter(name -> !set.contains(name.name))
				.map(name -> name.name)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	/**
	 * Sort the table by passed column name and passed parameters
	 *
	 * @param colName the column name, on which sorting will be made
	 * @param az if true, natural order will used.
	 * @param ignoreCase if true,
	 *
	 * @return this table, which sorted via passed parameters
	 *
	 * @throws Exception if a column by passed name not found in the table
	 */
	public Table sort(String colName, boolean az, boolean ignoreCase) throws Exception
	{
		this.changed(true);
		for (int i = 0; i < this.headers.length; i++)
		{
			if (this.headers[i].name.equals(colName))
			{
				return this.sort(i, az, ignoreCase);
			}
		}
		throw new Exception(String.format(R.TABLE_COLUMN_NOT_FOUND.get(), colName));
	}

	/**
	 * Sort the table by passed column index and passed parameters
	 *
	 * @param colNumber the column index, on which sorting will be made
	 * @param az if true, natural order will used.
	 * @param ignoreCase if true,
	 *
	 * @return new Table instance, which sorted via passed parameters
	 */
	public Table sort(int colNumber, boolean az, boolean ignoreCase)
	{
		this.changed(true);
		Header header = this.headers[colNumber];
		this.innerList.sort((o1, o2) ->
		{
			Object obj1 = o1.get(header);
			Object obj2 = o2.get(header);

			obj1 = this.convertCell(o1, header, obj1, null);
			obj2 = this.convertCell(o2, header, obj2, null);

			Header.HeaderType type = header.type == null ? Header.HeaderType.STRING : header.type;
			int compare = type.compare(obj1, obj2, ignoreCase);
			return az ? compare : -compare;
		});
		return this;
	}

	/**
	 * Set the {@link com.exactprosystems.jf.functions.Header.HeaderType#STRING} type for passed columns
	 */
	public void considerAsString(String... columns) throws Exception
	{
		this.considerAs(Header.HeaderType.STRING, columns);
	}

	/**
	 * Set the {@link com.exactprosystems.jf.functions.Header.HeaderType#BOOL} type for passed columns
	 */
	public void considerAsBoolean(String... columns) throws Exception
	{
		this.considerAs(Header.HeaderType.BOOL, columns);
	}

	/**
	 * Set the {@link com.exactprosystems.jf.functions.Header.HeaderType#INT} type for passed columns
	 */
	public void considerAsInt(String... columns) throws Exception
	{
		this.considerAs(Header.HeaderType.INT, columns);
	}

	/**
	 * Set the {@link com.exactprosystems.jf.functions.Header.HeaderType#DOUBLE} type for passed columns
	 */
	public void considerAsDouble(String... columns) throws Exception
	{
		this.considerAs(Header.HeaderType.DOUBLE, columns);
	}

	/**
	 * Set the {@link com.exactprosystems.jf.functions.Header.HeaderType#DATE} type for passed columns
	 */
	public void considerAsDate(String... columns) throws Exception
	{
		this.considerAs(Header.HeaderType.DATE, columns);
	}

	/**
	 * Set the {@link com.exactprosystems.jf.functions.Header.HeaderType#BIG_DECIMAL} type for passed columns
	 */
	public void considerAsBigDecimal(String... columns) throws Exception
	{
		this.considerAs(Header.HeaderType.BIG_DECIMAL, columns);
	}

	/**
	 * Set the {@link com.exactprosystems.jf.functions.Header.HeaderType#EXPRESSION} type for passed columns
	 */
	public void considerAsExpression(String... columns) throws Exception
	{
		this.considerAs(Header.HeaderType.EXPRESSION, columns);
	}

	/**
	 * Set the {@link com.exactprosystems.jf.functions.Header.HeaderType#GROUP} type for passed columns
	 */
	public void considerAsGroup(String... columns) throws Exception
	{
		this.considerAs(Header.HeaderType.GROUP, columns);
	}

	/**
	 * Set the {@link com.exactprosystems.jf.functions.Header.HeaderType#HYPERLINK} type for passed columns
	 */
	public void considerAsHyperlink(String... columns) throws Exception
	{
		this.considerAs(Header.HeaderType.HYPERLINK, columns);
	}

	/**
	 * Set the {@link com.exactprosystems.jf.functions.Header.HeaderType#COLORED} type for passed columns
	 */
	public void considerAsColored(String... columns) throws Exception
	{
		this.considerAs(Header.HeaderType.COLORED, columns);
	}

	/**
	 * Select the rows from the table by passed conditions and create a new table from these rows
	 *
	 * @param conditions the array on conditions, which will used for selecting rows
	 *
	 * @return a new table from selected rows
	 *
	 * @see Condition
	 */
	public Table select(Condition[] conditions)
	{
		Table result = new Table(this.evaluator);
		result.headers = new Header[this.headers.length];
		Arrays.setAll(result.headers, i -> new Header(this.headers[i]));

		this.stream()
				.filter(rowTable -> Arrays.stream(conditions).allMatch(condition -> condition.isMatched(rowTable)))
				.forEach(result::add);

		return result;
	}

	/**
	 * Upload the table to a database to a passed tabe name via the passed connection
	 *
	 * @param connection the connection for uploading the table
	 * @param table the database table name for uploading
	 *
	 * @throws JFSQLException if something went wrong
	 */
	public void upload(SqlConnection connection, String table) throws JFSQLException
	{
		try
		{
			Statement statement = null;
			try
			{
				statement = connection.getConnection().createStatement();
				int i = 0;
				for (Map<Header, Object> map : this.innerList)
				{
					StringBuilder sql = new StringBuilder("INSERT INTO ")
							.append(table)
							.append(" SET");

					for (Header header : this.headers)
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

	/**
	 * Select the rows from the table by passed conditions and return a list of numbers these rows
	 *
	 * @param conditions the array on conditions, which will used for selecting rows
	 *
	 * @return a list of numbers found rows
	 */
	public List<Integer> findAllIndexes(Condition[] conditions)
	{
		return IntStream.range(0, this.size())
				.filter(lineNumber -> Arrays.stream(conditions).allMatch(condition -> condition.isMatched(this.get(lineNumber))))
				.boxed()
				.collect(Collectors.toList());
	}

	/**
	 * Replace cells on the table. If columns for the table is not presented or empty OR source value equals dest value, nothing will happens
	 *
	 * @param source the object, which will replaced
	 * @param dest the object, which will inserting instead of source object
	 * @param matchCell if true, the search will check whole cell. Otherwise will check part of the cell ( e.g. substring)
	 * @param columns the array of columns, where need search and replace
	 */
	public void replace(Object source, Object dest, boolean matchCell, String ...columns)
	{
		if (columns == null || columns.length == 0 || Objects.equals(source, dest))
		{
			return;
		}

		List<Header> filtered = this.filter(columns);

		for (Map<Header, Object> row : this.innerList)
		{
			for (Header header : filtered)
			{
				Object value = row.get(header);
				if (!matchCell)
				{
					String sValue = String.valueOf(value);
					String sSource = String.valueOf(source);
					if (sValue.contains(sSource))
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

	/**
	 * Replace cells on the table. If columns for the table is not presented or empty , nothing will happens
	 *
	 * @param regexp the Regular Expression, which will used for replacing cells
	 * @param dest the object, which will inserting instead of source object
	 * @param columns the array of columns, where need search and replace
	 */
	public void replace(String regexp, Object dest, String ...columns)
	{
		if (columns == null || columns.length == 0)
		{
			return;
		}

		List<Header> filtered = filter(columns);

		this.innerList.forEach(row ->
				filtered.stream()
						.filter(header -> String.valueOf(row.get(header)).matches(regexp))
						.forEach(header -> row.put(header, dest))
		);
	}

	/**
	 * Report the table.
	 *
	 * @see com.exactprosystems.jf.actions.tables.TableReport
	 * @see Table#report(ReportBuilder, String, String, boolean, boolean, boolean, Map, int[])
	 */
	public void report(ReportBuilder report, String title, String beforeTestcase, boolean withNumbers, boolean reportValues) throws Exception
	{
		this.report(report, title, beforeTestcase, withNumbers, reportValues, true, Collections.emptyMap(), null);
	}

	/**
	 * Report the table.
	 * @param report the report instance for reporting
	 * @param title the title of table
	 * @param beforeTestcase
	 * @param withNumbers if true, before each line will added number of a line
	 * @param reportValues if true, a reported table will has values instead of expressions
	 * @param bordered if true, a reported table will with border
	 * @param columns the columns, which will in a reported table
	 * @param widths the array of cell widths
	 */
	public void report(ReportBuilder report, String title, String beforeTestcase, boolean withNumbers, boolean reportValues, boolean bordered, Map<String, String> columns, int[] widths) throws Exception
	{
		if (beforeTestcase != null || report.reportIsOn())
		{
			int[] columnsIndexes = columns.isEmpty() ? IntStream.range(0, this.headers.length).toArray() : this.getIndexes(columns.keySet().toArray(new String[]{}));

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
			for (int columnIndex : columnsIndexes)
			{
				headers[col++ + addition] = this.headers[columnIndex].name;
			}

			widths = widths == null ? new int[]{} : widths;
			headers = this.convertHeaders(columns, headers, withNumbers);
			ReportTable reportTable = report.addExplicitTable(title, beforeTestcase, true, bordered, widths, headers);

			Function<String, String> func = name -> columns.isEmpty() ? name : columns.entrySet().stream()
					.filter(e -> name.equals(String.valueOf(e.getValue())))
					.findFirst()
					.map(Entry::getKey)
					.orElse(name);

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
					Header header = this.headerByName(func.apply(headers[i]));
					if (reportValues)
					{
						value[i] = this.convertCell(row, header, row.get(header), report);
					}
					else
					{
						Object v = row.get(header);
						if (v instanceof ImageWrapper)
						{
							ImageWrapper iw = (ImageWrapper) v;
							String description = iw.getDescription() == null ? iw.toString() : iw.getDescription();
							v = report.decorateLink(description, report.getImageDir() + File.separator + iw.getName(report.getReportDir()));
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
				reportTable.addValues(value);
				count++;
			}
		}
	}

	/**
	 * Remove a row by passed index.
	 * @param index the index of row, which should be removed
	 * @return true, if removing was successful
	 */
	public boolean removeRow(int index)
	{
		this.innerList.remove(index);
		this.changed(true);
		return true;
	}

	public boolean isEmptyTable()
	{
		return this.headers.length == 1
				&& this.headers[0].name.equals(EMPTY_HEADER)
				&& this.innerList.size() == 1
				&& this.innerList.get(0).get(headerByName(EMPTY_HEADER)).equals(EMPTY_ROW);
	}

	/**
	 * Fill the table from the passed table instance
	 */
	public void fillFromTable(Table table)
	{
		this.headers = new Header[0];
		this.clear();
		this.addColumns(Arrays.stream(table.headers).map(h -> h.name).toArray(String[]::new));
		this.addAll(table);
	}

	public void setEvaluator(AbstractEvaluator evaluator)
	{
		this.evaluator = evaluator;
	}

	/**
	 * Save the table to a file with passed fileName
	 *
	 * @param fileName the name of file, which used for stored the table
	 * @param delimiter the delimiter char for separate cells
	 * @param saveValues if this parameter is true, values will stored ( instead of expressions)
	 * @param withNumbers if this parameter is true, numbers of lines will stored
	 *
	 * @return true, if saving was successful
	 *
	 * @see Table#save(CsvWriter, String, boolean, boolean)
	 */
	public boolean save(String fileName, char delimiter, boolean saveValues, boolean withNumbers)
	{
		CsvWriter writer = null;

		try (Writer bufferedWriter = CommonHelper.writerToFileName(fileName))
		{
			writer = new CsvWriter(bufferedWriter, delimiter);
			return this.save(writer, "", saveValues, withNumbers);
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

	/**
	 * Save the table via the passed writer
	 *
	 * @param writer the instance of CsvWriter for stored the table
	 * @param indent the indent for each line.
	 * @param saveValues if this parameter is true, values will stored ( instead of expressions)
	 * @param withNumbers if this parameter is true, numbers of lines will stored
	 *
	 * @return true, if saving was successful
	 *
	 * @throws IOException if file not found
	 */
	public boolean save(CsvWriter writer, String indent, boolean saveValues, boolean withNumbers) throws IOException
	{
		int columns = this.headers.length + (withNumbers ? 1 : 0);
		final String[] record = new String[columns];
		int count = 0;
		if (withNumbers)
		{
			record[count++] = indent + ROW_INDEX_SYMBOL;
		}
		for (Header header : this.headers)
		{
			record[count++] = header.name;
		}
		writer.writeRecord(record, true);

		for (int j = 0; j < this.size(); j++)
		{
			count = 0;
			if (withNumbers)
			{
				record[count++] = indent + String.valueOf(j);
			}
			Map<Header, Object> map = this.innerList.get(j);
			for (Header header : this.headers)
			{
				Object source = map.get(header);
				Object value = null;
				if (saveValues)
				{
					value = this.convertCell(map, header, source, null);
				}
				else
				{
					value = source;
				}
				record[count++] = Str.asString(value).replaceAll(String.valueOf(Configuration.matrixDelimiter), Configuration.unicodeDelimiter);
			}
			writer.writeRecord(record, true);
		}
		return true;
	}

	/**
	 * Add the passed columns in the table ( if a column is not present in the table)
	 */
	public void addColumns(String... columns)
	{
		this.changed(true);
		List<Header> list = new ArrayList<>();
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
			if (!this.columnIsPresent(column))
			{
				list.add(new Header(column, null));
			}
		}
		
		this.headers = list.toArray(new Header[0]);
		this.addEmptyStringToAllLinesInNewColumn();
	}

	/**
	 * @return true, if a column with the passed name is present in the table.
	 */
	public boolean columnIsPresent(String columnName)
	{
		return this.headers != null
				&& this.headers.length != 0
				&& Arrays.stream(this.headers).anyMatch(header -> header != null && Str.areEqual(columnName, header.name));
	}

	/**
	 * Update values of the line with passed index from the passed row
	 */
	public void updateValue(int index, RowTable row)
	{
		this.changed(true);
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

	/**
	 * Set the passed value to the row with the passed index and the passed column
	 */
	public Object setValue(int index, String key, Object value)
	{
		this.changed(true);
		Header header = this.headerByName(key);
		Map<Header, Object> row = this.innerList.get(index);
		return row.put(header, value);
	}

	/**
	 * Set the passed row into the table by passed index
	 */
	public void setValue(int index, RowTable row)
	{
		this.changed(true);
		this.innerList.set(index, this.convert(row));
	}

	/**
	 * Set the passed map into the table by passed index
	 */
	public void setValue(int index, Map<String, Object> map)
	{
		this.changed(true);
		Map<Header, Object> line = this.innerList.get(index);
		map.forEach((key, value) -> line.put(this.headerByName(key), value));
	}

	/**
	 * Add the passed map into the table by passed index. If the passed index less than zero, the map will added to the end of the table
	 */
	public void addValue(int index, Map<String, Object> map)
	{
		this.changed(true);
		if (this.headers != null)
		{
			Map<Header, Object> line = this.convert(map);
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
		this.changed(true);
		if (this.headers != null)
		{
			if (this.useColumnNumber)
			{
				arr = Arrays.copyOfRange(arr, 1, arr.length);
			}

			Map<Header, Object> line = this.convert(arr);
			this.innerList.add(line);
		}
	}

	public void addValue(int index, Object[] arr)
	{
		this.changed(true);
		if (this.headers != null)
		{
			Map<Header, Object> line = this.convert(arr);
			this.innerList.add(index, line);
		}
	}

	/**
	 * Rename the old column ( which has name oldValue) to the new passed value
	 *
	 * @param oldValue old column name
	 * @param newValue new column name
	 *
	 * @throws Exception if a column with oldName not found
	 */
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

	public int getHeaderSize()
	{
		return this.headers.length;
	}

	/**
	 * @return header name by the passed index
	 */
	public String getHeader(int index)
	{
		return this.headers[index].name;
	}

	/**
	 * Add the passed columns for the passed index.
	 * If one of columns is present on the table, the {@link ColumnIsPresentException} exception will thrown
	 */
	public void addColumns(int index, String... columns)
	{
		for (String column : columns)
		{
			if (this.columnIsPresent(column))
			{
				throw new ColumnIsPresentException(column);
			}
		}
		this.changed(true);
		if (this.headers == null)
		{
			this.headers = new Header[0];
		}
		List<Header> newHeaders = new ArrayList<>(Arrays.asList(this.headers));
		newHeaders.addAll(index, Arrays.stream(columns)
				.map(s -> new Header(s, null))
				.collect(Collectors.toList())
		);
		this.headers = newHeaders.toArray(new Header[newHeaders.size()]);
		this.addEmptyStringToAllLinesInNewColumn();
	}

	/**
	 * Remove the passed columns from the table
	 */
	public void removeColumns(String... columns)
	{
		if (this.headers == null)
		{
			return;
		}
		this.changed(true);
		List<String> removedColumnList = Arrays.asList(columns);
		List<Header> headerList = new ArrayList<>(Arrays.asList(this.headers));
		Iterator<Header> iterator = headerList.iterator();
		while (iterator.hasNext())
		{
			Header header = iterator.next();
			String next = header.name;
			if (removedColumnList.contains(next))
			{
				this.innerList.forEach(row -> row.remove(header));
				iterator.remove();
			}
		}
		this.headers = headerList.toArray(new Header[headerList.size()]);
	}

	public void changeValue(String headerName, int indexRow, Object newValue)
	{
		this.changed(true);
		if (this.headers != null)
		{
			Map<Header, Object> row = this.innerList.get(indexRow);
			if (row != null)
			{
				row.put(this.headerByName(headerName), newValue);
			}
		}
	}

	public void setHeader(int index, String s)
	{
		if (this.headers[index] != null && Str.areEqual(this.headers[index].name, s))
		{
			return;
		}
		if (this.columnIsPresent(s))
		{
			throw new ColumnIsPresentException(s);
		}
		this.changed(true);
		this.headers[index].name = s.trim();
	}

	public String[] getHeadersAsStringArray()
	{
		return Arrays.stream(this.headers)
				.map(String::valueOf)
				.toArray(String[]::new);
	}
	//endregion

	//region private methods

	/**
	 * If the passed index is more than this.size(), the {@link IndexOutOfBoundsException} will thrown
	 */
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
			result = report.addTable(R.TABLE_DIFFERENCES.get(), null, true, true, new int[]{30, 35, 35}, R.TABLE_DESCRIPTION.get(), R.TABLE_EXPECTED.get(), R.TABLE_ACTUAL.get());
		}
		result.addValues(name, expectedValue, actualValue);
		differences.addValue(new String[]{name, expectedValue, actualValue});

		return result;
	}

	private void considerAs(Header.HeaderType type, String... columns) throws Exception
	{
		this.changed(true);
		Arrays.stream(this.getIndexes(columns))
				.forEach(columnIndex -> this.headers[columnIndex].type = type);
	}

	private int[] getIndexes(String... columns) throws Exception
	{
		int[] columnsIndexes = new int[columns.length];

		int count = 0;
		for (String column : columns)
		{
			boolean found = false;
			for (int i = 0; i < this.headers.length; i++)
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
		Set<Header> keys = res.keySet();
		Arrays.stream(this.headers)
				.filter(h -> !keys.contains(h))
				.forEach(h -> res.put(h, null));
		return res;
	}

	private Map<Header, Object> convert(Map<String, Object> map)
	{
		Map<Header, Object> result = new LinkedHashMap<>();
		map.forEach((key, value) -> result.put(this.headerByName(key), value));
		Set<Header> keys = result.keySet();
		Arrays.stream(this.headers)
				.filter(h -> !keys.contains(h))
				.forEach(h -> result.put(h, null));
		return result;
	}

	private Collection<Map<Header, Object>> convert(Collection<? extends Map<String, Object>> e)
	{
		return e.stream()
				.map(this::convert)
				.collect(Collectors.toList());
	}

	protected Object convertCell(Map<Header, Object> row, Header header, Object source, ReportBuilder report)
	{
		if (header.type == null)
		{
			return source;
		}
		Object value;
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
					boolean isNode = this.extract(Str.asString(source), group, level);
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
						value = report.decorateLink(description, report.getImageDir() + File.separator + iw.getName(report.getReportDir()));
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
						this.splitToTwo(Str.asString(source), name, link);
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
					this.splitToTwo(Str.asString(source), name, color);
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

	private boolean extract(String path, StringBuilder group, AtomicInteger outLevel)
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
		private final Table table;
		private       int   index;

		TableListIterator(Table table, int index)
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
				this.addColumns(csvReader.getValues());

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
						line.put(this.headers[i], str[i]);
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

	private void readFromDirectory(String dirName) throws Exception
	{
		try
		{
			File directory = new File(dirName);

			this.headers = null;
			this.addColumns("Name", "Size", "Date", "Is directory", "Hidden");
			this.considerAsString("Name");
			this.considerAsBoolean("Is directory", "Hidden");
			this.considerAsDouble("Size");
			this.considerAsDate("Date");
			File[] files = directory.listFiles();
			if (files != null)
			{
				Arrays.stream(files).forEach(file -> {
					Map<Header, Object> line = new LinkedHashMap<>();
					line.put(this.headers[0], file.getName());
					line.put(this.headers[1], file.length());
					line.put(this.headers[2], new Date(file.lastModified()));
					line.put(this.headers[3], file.isDirectory());
					line.put(this.headers[4], file.isHidden());
					this.innerList.add(line);
				});
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
		Set<String> set = new HashSet<>(Arrays.asList(columns));
		
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
