package com.exactprosystems.jf.tool.search;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.main.Main;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	private SearchController controller;
	private Main             model;
	private Configuration    configuration;
	private Settings         settings;

	static final String ALL_FILES = "*.*";

	private final ExecutorService executor = Executors.newFixedThreadPool(4);

	public Search(Main model, Configuration configuration, Settings settings)
	{
		this.model = model;
		this.configuration = configuration;
		this.settings = settings;

		this.controller = Common.loadController(this.getClass().getResource("Search.fxml"));
		this.controller.init(this);
		this.updateFromSettings();
	}

	public void show()
	{
		this.controller.show();
	}

	void find(String fileMask, String text, boolean caseSens, boolean wholeWord, boolean regexp, DocumentKind[] kinds)
	{
		this.controller.startFind();
		String res = checkParams(text, regexp, fileMask);
		if (res != null)
		{
			this.controller.displayResult(Collections.singletonList(new SearchResult.FailedSearchResult(res)));
			this.controller.finishFind();
			return;
		}
		if (kinds.length == 0)
		{
			this.controller.displayResult(Collections.singletonList(new SearchResult.FailedSearchResult("Select one or more scopes")));
			this.controller.finishFind();
			return;
		}
		saveMaskAndText(text, fileMask);

		this.configuration.forEachFile(file -> {
			SearchService service = new SearchService(file, caseSens, regexp, wholeWord, text, fileMask);
			service.setOnSucceeded(event -> this.controller.displayResult(((List<SearchResult>) event.getSource().getValue())));
			service.setExecutor(executor);
			service.start();
		}, kinds);

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

	private void updateFromSettings()
	{
		this.controller.updateFromSettings(
				this.settings.getValues(Settings.SETTINGS, Settings.SEARCH + Settings.MASK).stream().map(Settings.SettingsValue::getKey).collect(Collectors.toList())
				, this.settings.getValues(Settings.SETTINGS, Settings.SEARCH + Settings.TEXT).stream().map(Settings.SettingsValue::getKey).collect(Collectors.toList())
		);
	}

	private void saveMaskAndText(String containingText, String fileMask)
	{
		if (!Str.IsNullOrEmpty(containingText))
		{
			this.settings.setValue(Settings.SETTINGS, Settings.SEARCH + Settings.TEXT, containingText, 10, containingText);
		}
		if (!Str.IsNullOrEmpty(fileMask) && !fileMask.equals(ALL_FILES))
		{
			this.settings.setValue(Settings.SETTINGS, Settings.SEARCH + Settings.MASK, fileMask, 10, fileMask);
		}
		try
		{
			this.settings.saveIfNeeded();
		}
		catch (Exception e)
		{
			;
		}
		updateFromSettings();
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
		private final File    file;
		private final boolean isMatchCase;
		private final boolean isRegexp;
		private final boolean isWholeWord;
		private final String  what;
		private final String  fileMask;

		SearchService(File file, boolean isMatchCase, boolean isRegexp, boolean isWholeWord, String what, String fileMask)
		{
			this.file = file;
			this.isMatchCase = isMatchCase;
			this.isRegexp = isRegexp;
			this.isWholeWord = isWholeWord;
			this.what = what;
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
					AtomicInteger atomicInteger = new AtomicInteger(0);

					if (!file.getName().matches(filePattern(fileMask)))
					{
						return null;
					}

					if (Str.IsNullOrEmpty(what))
					{
						return Collections.singletonList(new SearchResult(file, 0));
					}

					try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath())))
					{
						return stream.peek(s -> atomicInteger.incrementAndGet()).filter(SearchService.this::processLine).map(s -> new SearchResult(file, atomicInteger.get())).collect(
								Collectors.toList());
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
				return SearchHelper.matches(line, this.what, this.isMatchCase, this.isWholeWord);
			}
		}
	}

	private class DummySearchService extends SearchService
	{
		DummySearchService()
		{
			super(null, false, false, false, null, null);
		}

		@Override
		protected Task<List<SearchResult>> createTask()
		{
			return new Task<List<SearchResult>>()
			{
				@Override
				protected List<SearchResult> call() throws Exception
				{
					Thread.sleep(500);
					return null;
				}
			};
		}
	}
	//endregion

}
