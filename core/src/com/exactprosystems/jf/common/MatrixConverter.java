package com.exactprosystems.jf.common;

import com.csvreader.CsvWriter;
import com.exactprosystems.jf.actions.system.Vars;
import com.exactprosystems.jf.api.error.common.MatrixException;
import com.exactprosystems.jf.documents.ConsoleDocumentFactory;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.CommentString;
import com.exactprosystems.jf.documents.matrix.parser.items.Let;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MatrixConverter
{

	private Map<String, Predicate<MatrixItem>> predicates = new HashMap<>();
	private List<String> matricesFolders;
	private List<String> libsFolders;
	private String       toFolder;


	{
		predicates.put("Vars", item -> item.getClass() == ActionItem.class && ((ActionItem) item).getActionClass() == Vars.class);
	}

	public MatrixConverter(List<String> matr, List<String> libs, String toFolder)
	{
		this.matricesFolders = matr;
		this.libsFolders = libs;
		this.toFolder = toFolder.endsWith(File.separator) ? toFolder : (toFolder + File.separator);
	}

	public boolean start()
	{

		List<String> commonFolder = new ArrayList<>(matricesFolders);
		commonFolder.addAll(libsFolders);
		AtomicBoolean atomicBoolean = new AtomicBoolean(true);
		commonFolder.forEach(folder -> convert(new File(folder), atomicBoolean));
		return atomicBoolean.get();
	}

	private void convert(File path, AtomicBoolean atomicBoolean)
	{
		createFolders(toFolder, path);

		if (path != null && path.exists())
		{
			if (path.isDirectory() && path.listFiles() != null)
			{
				for (File f : path.listFiles())
				{
					convert(f, atomicBoolean);
				}
			}
			else
			{
				if (path.getName().endsWith(".jf"))
				{
					CsvWriter writer = null;
					try
					{
						Matrix matrix = new Matrix(path.getPath(), new ConsoleDocumentFactory(VerboseLevel.None));
						matrix.load(new FileReader(path));
						writer = prepareCsvWriter(toFolder + path.getParent() + File.separator + path.getName());
						List<MatrixItem> items = findItems(matrix, predicates.get("Vars"));
						String s = varsToLet(items, matrix);
						if (!s.isEmpty())
						{
							atomicBoolean.set(false);
							System.err.println(s);
							System.err.println("Matrix " + path + " not saved. Fix the problems and restart converting");
							return;
						}
						matrix.getRoot().write(-1, writer);

					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						if (writer != null)
						{
							writer.flush();
							writer.close();
						}
					}
				}
			}
		}
	}

	private CsvWriter prepareCsvWriter(String toFolder) throws IOException
	{
		CsvWriter writer = new CsvWriter(new FileWriter(toFolder), Configuration.matrixDelimiter);
		writer.setDelimiter(Configuration.matrixDelimiter);
		writer.setForceQualifier(false);
		writer.setUseTextQualifier(false);
		writer.setTextQualifier(Parser.prefferedQuotes);
		return writer;
	}

	private List<MatrixItem> findItems(Matrix matrix, Predicate<MatrixItem> predicate)
	{

		List<MatrixItem> result = new ArrayList<>();
		matrix.getRoot().bypass(item ->
		{
			if (predicate.test(item))
			{
				result.add(item);
			}
		});

		return result;
	}

	private String varsToLet(List<MatrixItem> items, Matrix matrix)
	{
		return items.stream().map(item ->
		{
			ActionItem actionItem = (ActionItem) item;
			if (actionItem.assertIsPresented())
			{
				return String.format("Assert on vars not expected. Please, remove Assert  ( or move this assert to item Assert) on vars in the matrix '%s' on line '%s' (path : %s)\n"
						, item.getMatrix().getName(), item.getNumber(), item.getPath());
			}
			for (Parameter parameter : item.getParameters())
			{
				Let let = new Let();
				Map<Tokens, String> map = new HashMap<>();
				List<String> collect = item.getComments().stream().map(CommentString::toString).collect(Collectors.toList());
				map.put(Tokens.Let, parameter.getExpression());
				map.put(Tokens.Id, parameter.getName());
				map.put(Tokens.Off, item.isOff() ? "1" : "0");
				map.put(Tokens.RepOff, item.isRepOff() ? "1" : "0");
				map.put(Tokens.Global, item.isGlobal() ? "1" : "0");
				try
				{
					let.init(matrix, collect, map, null);
				}
				catch (MatrixException e)
				{
					e.printStackTrace();
				}
				int varsIndex = item.getParent().index(item);
				item.getParent().insert(varsIndex, let);
			}

			matrix.remove(item);
			return "";
		}).collect(Collectors.joining(""));
	}

	private void createFolders(String toFolder, File path)
	{

		if (path.getParent() == null)
		{
			return;
		}

		Predicate<File> isFolderExist = f -> f.exists() && f.isDirectory();
		File folder = new File(toFolder + path.getParent());

		if (!isFolderExist.test(folder))
		{
			folder.mkdirs();
		}
	}
}


