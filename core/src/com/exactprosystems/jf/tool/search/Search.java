package com.exactprosystems.jf.tool.search;

import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.main.Main;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Search
{
	enum Scope
	{
		Project,
		Matrices,
		Libraries,
		GuiDictionaries,
		MatricesAndLibraries,
		Directory
	}

	private SearchController controller;
	private Main model;
	private List<File> matrixDirs;
	private List<File> libsDirs;
	private List<File> guiDirs;

	private final ExecutorService executor = Executors.newFixedThreadPool(4);

	public Search(Main model, List<File> matrixes, List<File> libs, List<File> guiDic)
	{
		this.model = model;
		this.matrixDirs = matrixes;
		this.libsDirs = libs;
		this.guiDirs = guiDic;
		this.controller = Common.loadController(this.getClass().getResource("Search.fxml"));
		this.controller.init(this);

	}

	public void show()
	{
		this.controller.show();
	}

	void find(String what, boolean isFileName, boolean isMatchCase, boolean isRegexp, String fileMask, Scope scope, String dir)
	{
		this.controller.startFind();
		String res = checkParams(what, isRegexp, fileMask);
		if (res != null)
		{
			this.controller.displayResult(Collections.singletonList(new SearchResult.FailedSearchResult(res)));
			this.controller.finishFind();
			return;
		}
		switch (scope)
		{
			case Project:
				byPassDir(new File(new File("").getAbsolutePath()), what, isMatchCase, isRegexp, fileMask, isFileName);
				break;

			case Matrices:
				this.matrixDirs.forEach(root -> byPassDir(root, what, isMatchCase, isRegexp, fileMask, isFileName));
				break;

			case Libraries:
				this.libsDirs.forEach(root -> byPassDir(root, what, isMatchCase, isRegexp, fileMask, isFileName));
				break;

			case MatricesAndLibraries:
				List<File> matricesAndLibs = new ArrayList<>();
				matricesAndLibs.addAll(this.libsDirs);
				matricesAndLibs.addAll(this.matrixDirs);
				matricesAndLibs.forEach(root -> byPassDir(root, what, isMatchCase, isRegexp, fileMask, isFileName));
				break;

			case GuiDictionaries:
				this.guiDirs.forEach(root -> byPassDir(root, what, isMatchCase, isRegexp, fileMask, isFileName));
				break;

			case Directory:
				byPassDir(new File(dir), what, isMatchCase, isRegexp, fileMask, isFileName);
				break;
		}
		DummySearchService dummySearchService = new DummySearchService();
		dummySearchService.setExecutor(executor);
		dummySearchService.setOnSucceeded(event -> {
			this.controller.finishFind();
			this.controller.displayMatches();
		});
		dummySearchService.start();
	}

	void alertClose()
	{
		executor.shutdownNow();
	}

	void scrollFromConfig(File file)
	{
		this.model.showIntoConfiguration(file);
	}

	void openAsPlainText(File file)
	{
		Common.tryCatch(() -> this.model.loadPlainText(file.getAbsolutePath()), "Error on opem plain text");
	}

	void openAsMatrix(File file)
	{
		Common.tryCatch(() -> this.model.loadMatrix(file.getAbsolutePath()), "Error on opem plain text");
	}

	void openAsGuiDic(File file)
	{
		Common.tryCatch(() -> this.model.loadDictionary(file.getAbsolutePath(), null), "Error on opem plain text");
	}

	void openAsVars(File file)
	{
		Common.tryCatch(() -> this.model.loadSystemVars(file.getAbsolutePath()), "Error on opem plain text");
	}

	void openAsHtml(File file)
	{
		Common.tryCatch(() -> this.model.openReport(file), "Error on open file");
	}

	//region private methods
	private void byPassDir(File root, String what, boolean isMatchCase, boolean isRegexp, String fileMask, boolean isFileName)
	{
		if (root.isDirectory())
		{
			File[] list = root.listFiles();
			if (list != null)
			{
				Arrays.stream(list).forEach(file -> byPassDir(file, what, isMatchCase, isRegexp, fileMask, isFileName));
			}
		}
		else
		{
			SearchService service;
			if (isFileName)
			{
				service = new SearchOnlyFileService(root, isMatchCase, isRegexp, what, fileMask);
			}
			else
			{
				if (fileMask == null)
				{
					service = new SearchService(root, isMatchCase, isRegexp, what);
				}
				else
				{
					if (root.getName().matches(this.filePattern(fileMask)))
					{
						service = new SearchService(root, isMatchCase, isRegexp, what);
					}
					else
					{
						service = new DummySearchService();
					}
				}
			}
			service.setOnSucceeded(event -> this.controller.displayResult(((List<SearchResult>) event.getSource().getValue())));
			service.setExecutor(executor);
			service.start();
		}
	}

	private String checkParams(String what, boolean isRegexp, String fileMask)
	{
		if (isRegexp)
		{
			try
			{
				Pattern.compile(what);
			}
			catch (PatternSyntaxException e)
			{
				return "Invalid regexp pattern";
			}
		}
		if (fileMask != null)
		{
			try
			{
				Pattern.compile(filePattern(fileMask));
			}
			catch (PatternSyntaxException e)
			{
				return "Invalid file mask pattern";
			}
		}
		return null;
	}

	private String filePattern(String pattern)
	{
		return pattern.replace(".", "\\.").replace("?", ".?").replace("*", ".*");
	}

	private class SearchService extends Service<List<SearchResult>>
	{
		protected final File file;
		protected final boolean isMatchCase;
		protected final boolean isRegexp;
		protected final String what;

		SearchService(File file, boolean isMatchCase, boolean isRegexp, String what)
		{
			this.file = file;
			this.isMatchCase = isMatchCase;
			this.isRegexp = isRegexp;
			this.what = what;
		}

		@Override
		protected Task<List<SearchResult>> createTask()
		{
			return new Task<List<SearchResult>>()
			{
				@Override
				protected List<SearchResult> call() throws Exception
				{
					AtomicInteger atomicInteger = new AtomicInteger(0);

					try(Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath())))
					{
						return stream.peek(s -> atomicInteger.incrementAndGet())
								.filter(SearchService.this::processLine)
								.map(s -> new SearchResult(file, atomicInteger.get()))
								.collect(Collectors.toList());
					}
				}
			};
		}

		private boolean processLine(String line)
		{
			if (this.isRegexp)
			{
				return line.matches(this.what);
			}
			else
			{
				return SearchHelper.matches(line, this.what, this.isMatchCase, false);
			}
		}
	}

	private class SearchOnlyFileService extends SearchService
	{
		private String fileMask;

		SearchOnlyFileService(File file, boolean isMatchCase, boolean isRegexp, String what, String fileMask)
		{
			super(file, isMatchCase, isRegexp, what);
			this.fileMask = fileMask;
		}

		@Override
		protected Task<List<SearchResult>> createTask()
		{
			return new Task<List<SearchResult>>()
			{
				@Override
				protected List<SearchResult> call() throws Exception
				{
					if (isRegexp)
					{
						if (file.getName().matches(filePattern(what)))
						{
							return Collections.singletonList(new SearchResult(file, 0));
						}
					}
					else
					{
						if (file.getName().contains(what))
						{
							if (fileMask != null)
							{
								if (file.getName().matches(filePattern(fileMask)))
								{
									return Collections.singletonList(new SearchResult(file, 0));
								}
							}
							else
							{
								return Collections.singletonList(new SearchResult(file, 0));
							}
						}
					}
					return null;
				}
			};
		}
	}

	private class DummySearchService extends SearchService
	{
		DummySearchService()
		{
			super(null, false, false, null);
		}

		@Override
		protected Task<List<SearchResult>> createTask()
		{
			return new Task<List<SearchResult>>()
			{
				@Override
				protected List<SearchResult> call() throws Exception
				{
					return null;
				}
			};
		}
	}
	//endregion

}
