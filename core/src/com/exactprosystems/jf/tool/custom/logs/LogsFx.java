////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.logs;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.Settings.SettingsValue;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.tool.Common;
import javafx.scene.paint.Color;
import org.apache.log4j.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LogsFx
{
	private LogsFxController    controller;
	private List<LineWithStyle> lines;
	private Settings            settings;

	public LogsFx(Settings settings) throws Exception
	{
		this.settings = settings;
		this.controller = Common.loadController(LogsFx.class.getResource("LogsFx.fxml"));
		this.controller.init(this);
	}

	public void show() throws Exception
	{
		refresh();
		this.controller.show();
	}

	public void refresh() throws Exception
	{
		this.controller.clearListView();
		this.controller.clearFiles();
		this.displayAllFiles();
	}

	public List<LineWithStyle> findItem(String what, boolean matchCase, boolean wholeWord)
	{
		return this.lines.stream()
				.filter(line -> {
					String text = line.getLine();
					for (String s : text.split(" "))
					{
						if (SearchHelper.matches(s, what, matchCase, wholeWord))
						{
							return true;
						}
					}
					return false;
				}).collect(Collectors.toList());
	}

	public void find(LineWithStyle row)
	{
		IntStream.range(0, lines.size())
				.filter(i -> lines.get(i).equals(row))
				.findFirst()
				.ifPresent(controller::clearAndSelect);
	}

	void displayLines(File file)
	{
		this.controller.clearListView();
		try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath())))
		{
			this.lines = stream.map(s -> new LineWithStyle(s, getStyle(s))).collect(Collectors.toList());
			this.controller.displayLines(this.lines);
		}
		catch (IOException ignore)
		{}
	}

	//region private methods
	private void displayAllFiles()
	{
		this.controller.displayFiles(allLogFiles());
	}

	private Color getStyle(String line)
	{
		return Stream.of(Level.DEBUG, Level.ERROR, Level.FATAL, Level.INFO, Level.TRACE, Level.WARN)
				.map(Priority::toString)
				.filter(line::startsWith)
				.map(l -> this.settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.LOGS_NAME, l))
				.map(SettingsValue::getValue)
				.map(Color::valueOf)
				.findFirst()
				.orElse(Color.BLACK);
	}

	private static List<File> allLogFiles()
	{
		Set<String> files = new HashSet<>();

		@SuppressWarnings("unchecked")
		Enumeration<Logger> logs = LogManager.getCurrentLoggers();
		forEach(logs, log ->
		{
			@SuppressWarnings("unchecked")
			Enumeration<Appender> apps = log.getAllAppenders();
			forEach(apps, app -> files.add(((FileAppender) app).getFile()));
		});
		return files.stream().map(File::new).filter(File::exists).collect(Collectors.toList());
	}

	private static <T> void forEach(Enumeration<T> e, Consumer<T> consumer)
	{
		while (e.hasMoreElements())
		{
			consumer.accept(e.nextElement());
		}
	}
	//endregion

	static class LineWithStyle
	{
		private String line;
		private Color style;

		public LineWithStyle(String line, Color style)
		{
			this.line = line;
			this.style = style;
		}

		public String getLine()
		{
			return line;
		}

		public void setLine(String line)
		{
			this.line = line;
		}

		public Color getStyle()
		{
			return style;
		}

		public void setStyle(Color style)
		{
			this.style = style;
		}
	}

}
