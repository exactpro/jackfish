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
import com.exactprosystems.jf.common.parser.SearchHelper;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.console.ConsoleText;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.scene.paint.Color;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

public class LogsFx implements AutoCloseable
{
	public final static String Dialog	= "Logs";

	private LogsFxController				controller;
	private ArrayList<ConsoleText<String>>	lines;
	private Settings settings;

	public LogsFx(Settings settings) throws Exception
	{
		this.settings = settings;
		this.controller = Common.loadController(LogsFx.class.getResource("LogsFx.fxml"));
		this.controller.init(this);
		readFile();
		this.controller.setTextToList(lines);
	}

	@Override
	public void close() throws Exception
	{
		if (this.controller != null)
		{
			this.controller.close();
			this.controller = null;
		}
	}

	public void show() throws Exception
	{
		refresh();
		this.controller.show();
	}

	public void refresh() throws Exception
	{
		this.controller.clearListView();
		readFile();
		this.controller.setTextToList(this.lines);
	}

	public List<ConsoleText<String>> findItem(String what, boolean matchCase, boolean wholeWord)
	{
		ArrayList<ConsoleText<String>> results = new ArrayList<>();
		for (ConsoleText<String> consoleText : lines)
		{
			String text = consoleText.getText().substring(consoleText.getText().indexOf("\t") + 1);
			for (String s : text.split(" "))
			{
				if (SearchHelper.matches(s, what, matchCase, wholeWord))
				{
					results.add(consoleText);
					break;
				}
			}
		}
		return results;
	}

	public void find(ConsoleText<String> row)
	{
		for (int i = 0; i < lines.size(); i++)
		{
			ConsoleText<String> text = lines.get(i);
			if (text.equals(row))
			{
				controller.clearAndSelect(i);
				break;
			}
		}
	}

	// TODO this is should be in model
	private void readFile() throws Exception
	{
		int i = 1;
		lines = new ArrayList<>();
		String mainLogFileName = mainLogFileName();
		if (mainLogFileName == null)
		{
			DialogsHelper.showError("Main.log not found");
			return;
		}
		try (BufferedReader br = new BufferedReader(new FileReader(new File(mainLogFileName))))
		{
			String str;
			while ((str = br.readLine()) != null)
			{
				ConsoleText<String> consoleText = ConsoleText.defaultText(i++ + "\t" + str);
				setColor(consoleText, str, Level.DEBUG);
				setColor(consoleText, str, Level.ERROR);
				setColor(consoleText, str, Level.FATAL);
				setColor(consoleText, str, Level.INFO);
				setColor(consoleText, str, Level.TRACE);
				setColor(consoleText, str, Level.WARN);
				lines.add(consoleText);
			}
		}
	}

	//============================================================
	// private methods
	//============================================================
	private void setColor(ConsoleText<String> consoleText, String str, Level level)
	{
		if (str.contains(level.toString()) )
		{
			SettingsValue res = this.settings.getValue(Settings.GLOBAL_NS, Dialog, level.toString());
			Optional.ofNullable(res).ifPresent(result -> {
				consoleText.getStyleClass().removeAll(CssVariables.CONSOLE_DEFAULT_TEXT);
				consoleText.setFill(Color.valueOf(result.getValue()));
			});
		}
	}

	// TODO we don't have a root logger now
	private static String mainLogFileName()
	{
		Enumeration<Appender> e = Logger.getRootLogger().getAllAppenders();
		while (e.hasMoreElements())
		{
			Appender app = e.nextElement();
			if (app instanceof FileAppender)
			{
				return ((FileAppender) app).getFile();
			}
		}
		return null;
	}

}
