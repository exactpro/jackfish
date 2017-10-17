package com.exactprosystems.jf.tool.search;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.main.Main;
import com.exactprosystems.jf.tool.search.results.AggregateResult;
import com.exactprosystems.jf.tool.search.results.FailedResult;
import com.exactprosystems.jf.tool.search.results.SingleResult;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.util.Pair;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
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

	void find(String fileMask, String text, boolean caseSens, boolean wholeWord, boolean regexp, boolean multiLine, DocumentKind[] kinds)
	{
		this.controller.startFind();
		String res = checkParams(text, regexp, fileMask);
		if (res != null)
		{
			this.controller.displayFailedResult(new FailedResult(res));
			this.controller.finishFind();
			return;
		}
		if (kinds.length == 0)
		{
			this.controller.displayFailedResult(new FailedResult("Select one or more scopes"));
			this.controller.finishFind();
			return;
		}
		saveMaskAndText(text, fileMask);

		this.configuration.forEachFile((file,kind) -> {
			SearchService service = new SearchService(file, caseSens, regexp, wholeWord, multiLine, text, fileMask, kind);
			service.setOnSucceeded(event -> this.controller.displayResult(((AggregateResult) event.getSource().getValue())));
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

	public void scrollFromConfig(File file)
	{
		this.model.showIntoConfiguration(file);
	}

	public void openAsPlainText(File file)
	{
		Common.tryCatch(() -> this.model.loadPlainText(file.getAbsolutePath()), "Error on opem plain text");
	}

	public void openAsMatrix(File file)
	{
		Common.tryCatch(() -> this.model.loadMatrix(file.getAbsolutePath()), "Error on opem plain text");
	}

	public void openAsGuiDic(File file)
	{
		Common.tryCatch(() -> this.model.loadDictionary(file.getAbsolutePath(), null), "Error on opem plain text");
	}

	public void openAsVars(File file)
	{
		Common.tryCatch(() -> this.model.loadSystemVars(file.getAbsolutePath()), "Error on opem plain text");
	}

	public void openAsHtml(File file)
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

	private class SearchService extends Service<AggregateResult>
	{
		private final File    file;
		private final boolean isMatchCase;
		private final boolean isRegexp;
		private final boolean isWholeWord;
		private final boolean isMultiLine;
		private final String  what;
		private final String  fileMask;
		private final DocumentKind kind;

		SearchService(File file, boolean isMatchCase, boolean isRegexp, boolean isWholeWord, boolean multiLine, String what, String fileMask, DocumentKind kind)
		{
			this.file = file;
			this.isMatchCase = isMatchCase;
			this.isRegexp = isRegexp;
			this.isWholeWord = isWholeWord;
			this.isMultiLine = multiLine;
			this.what = what;
			this.fileMask = fileMask;
			this.kind = kind;
		}

		@Override
		protected Task<AggregateResult> createTask()
		{
			return new Task<AggregateResult>()
			{
				@Override
				protected AggregateResult call() throws Exception
				{
					AtomicInteger atomicInteger = new AtomicInteger(0);

					String fileName = file.getName();
					if (!fileName.matches(filePattern(fileMask)))
					{
						return null;
					}

					if (Str.IsNullOrEmpty(what))
					{
						return new AggregateResult(Search.this, Collections.singletonList(new SingleResult(fileName, null, 0, null)), file, kind);
					}

					List<SingleResult> collect;

					if (isMultiLine)
					{
						List<String> strings = Files.readAllLines(Paths.get(file.getAbsolutePath()));
						String str = strings.stream().collect(Collectors.joining(System.lineSeparator()));
						Pair<String, List<Pair<Integer, Integer>>> pair = processLine(str);
						if (pair == null)
						{
							return null;
						}
						String line = pair.getKey();
						collect = pair.getValue().stream()
								.map(Arrays::asList)
								.peek(p -> atomicInteger.set(indexOf(str, p.get(0).getKey())))
								.map(list -> new SingleResult(fileName, line, atomicInteger.get(), list))
								.collect(Collectors.toList());
					}
					else
					{
						try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath())))
						{
							collect = stream
									.peek(s -> atomicInteger.incrementAndGet())
									.map(String::trim)
									.map(SearchService.this::processLine)
									.filter(Objects::nonNull)
									.map(pair -> new SingleResult(fileName, pair.getKey(), atomicInteger.get(), pair.getValue()))
									.collect(Collectors.toList());
						}
					}

					return new AggregateResult(Search.this, collect, file, kind);
				}
			};
		}

		//string - is line, list - indexes of found substrings ( key - start, end - finish)
		private Pair<String, List<Pair<Integer, Integer>>> processLine(String line)
		{
			int caseInsensitive = 0;
			if (!this.isMatchCase)
			{
				caseInsensitive = Pattern.CASE_INSENSITIVE;
			}

			List<Pair<Integer, Integer>> list = new ArrayList<>();
			String patString = "(" + this.what + ")";
			if (!this.isRegexp)
			{
				patString = "(" + Matcher.quoteReplacement(this.what) + ")";
			}
			if (this.isWholeWord)
			{
				patString = "\\b(" + this.what + ")\\b";
			}
			Pattern compile = Pattern.compile(patString, caseInsensitive);
			Matcher matcher = compile.matcher(line);
			while (matcher.find())
			{
				list.add(new Pair<>(matcher.start(1), matcher.end(1)));
			}
			if (list.isEmpty())
			{
				return null;
			}
			return new Pair<>(line, list);
		}

		private int indexOf(String str, int index)
		{
			return (int) str.substring(0, index)
					.chars()
					.mapToObj(c -> Character.toString((char) c))
					.filter(System.lineSeparator()::equals)
					.count() + 1;
		}
	}

	private class DummySearchService extends SearchService
	{
		DummySearchService()
		{
			super(null, false, false, false, false, null, null, null);
		}

		@Override
		protected Task<AggregateResult> createTask()
		{
			return new Task<AggregateResult>()
			{
				@Override
				protected AggregateResult call() throws Exception
				{
					Thread.sleep(500);
					return null;
				}
			};
		}
	}
	//endregion

}
