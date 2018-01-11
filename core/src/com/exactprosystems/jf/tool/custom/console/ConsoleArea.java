////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.console;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.fxmisc.richtext.MouseOverTextEvent;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.awt.MouseInfo;
import java.time.Duration;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ConsoleArea<T> extends StyleClassedTextArea
{
	private ArrayList<Link> list;
	private int             charIndex;

	public ConsoleArea()
	{
		this.createContextMenu();
	}

	public ConsoleArea(Consumer<T> consumer)
	{
		this.createContextMenu();
		this.list = new ArrayList<>();
		super.setMouseOverTextDelay(Duration.ofMillis(10));
		super.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> this.charIndex = e.getCharacterIndex());
		super.setOnMouseReleased(event -> this.list.stream()
				.filter(link -> this.charIndex > link.getStart() && this.charIndex < link.getEnd())
				.findFirst()
				.ifPresent(link -> consumer.accept(link.getItem())));
	}

	public void appendDefaultText(String text)
	{
		this.appendStyledText(text, false, null, CssVariables.CONSOLE_DEFAULT_TEXT);
	}

	public void appendDefaultTextOnNewLine(String text)
	{
		this.appendStyledText(text, this.getText().length() > 0, null, CssVariables.CONSOLE_DEFAULT_TEXT);
	}

	public void appendErrorText(String text)
	{
		this.appendStyledText(text, false, null, CssVariables.CONSOLE_ERROR_ITEM);
	}

	public void appendErrorTextOnNewLine(String text)
	{
		this.appendStyledText(text, this.getText().length() > 0, null, CssVariables.CONSOLE_ERROR_ITEM);
	}

	public void appendLink(String text, T item)
	{
		this.appendStyledText(text, false, item, CssVariables.CONSOLE_PAUSED_ITEM);
	}

	private void appendStyledText(String text, boolean newLine, T item, String style)
	{
		int start = super.getLength();
		super.appendText(newLine ? System.lineSeparator() + text : text);
		super.setStyleClass(start, super.getLength(), style);
		if (item != null)
		{
			this.list.add(new Link(start, super.getLength(), item));
		}
		if (super.totalHeightEstimateProperty().getValue() != null)
		{
			super.setEstimatedScrollY(super.getTotalHeightEstimate() - super.getHeight());
		}
	}

	@Override
	public void clear()
	{
		if (this.list != null)
		{
			this.list.clear();
		}
		super.clear();
	}

	private void createContextMenu()
	{
		ContextMenu contextMenu = new ContextMenu();
		contextMenu.setAutoFix(true);
		contextMenu.setAutoHide(true);

		MenuItem itemClear = new MenuItem(R.COMMON_CLEAR.get());
		itemClear.setOnAction(e -> this.clear());
		contextMenu.getItems().addAll(itemClear);

		super.setOnContextMenuRequested(e -> contextMenu.show(super.getScene().getWindow(), MouseInfo.getPointerInfo().getLocation().getX(), MouseInfo.getPointerInfo().getLocation().getY()));
	}

	private class Link
	{
		int start;
		int end;
		T   item;

		private Link(int start, int end, T item)
		{
			this.start = start;
			this.end = end;
			this.item = item;
		}

		private int getStart()
		{
			return start;
		}

		private int getEnd()
		{
			return end;
		}

		private T getItem()
		{
			return item;
		}
	}
}
