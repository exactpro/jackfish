////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.console;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class ConsoleText<T> extends Text
{
	private T item;

	private ConsoleText(String text)
	{
		super(text);
	}

	public T getItem()
	{
		return item;
	}

	public static ConsoleText text(String text, Color color)
	{
		ConsoleText consoleText = new ConsoleText(text);
		consoleText.setFill(color);
		return consoleText;
	}

	public static <T> ConsoleText<T> defaultText(String text)
	{
		return consoleText(text, null, CssVariables.CONSOLE_DEFAULT_TEXT);
	}

	public static <T> ConsoleText<T> pausedItem(String text, T item)
	{
		return consoleText(text, item, CssVariables.CONSOLE_PAUSED_ITEM);
	}

	public static <T> ConsoleText<T> errorItem(String text, T item)
	{
		return consoleText(text, item, CssVariables.CONSOLE_ERROR_ITEM);
	}

	private static <T> ConsoleText<T> consoleText(String text, T item, String styleClass)
	{
		ConsoleText<T> consoleText = new ConsoleText<>(text);
		consoleText.getStyleClass().add(styleClass);
		consoleText.item = item;
		return consoleText;
	}

	@Override
	public String toString()
	{
		return this.getText();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ConsoleText<?> that = (ConsoleText<?>) o;
		return that.getText().equals(this.getText());

	}

	@Override
	public int hashCode()
	{
		return item != null ? item.hashCode() : 0;
	}
}
