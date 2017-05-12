////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.text;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.highlighter.Highlighter;
import com.exactprosystems.jf.common.highlighter.StyleWithRange;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.text.PlainText;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlainTextFx extends PlainText
{
	static final String FOUND_STYLE_CLASS = "found";

	public PlainTextFx(String fileName, DocumentFactory factory)
	{
		this(fileName, factory, Highlighter.None);
	}

	public PlainTextFx(String fileName, DocumentFactory factory, Highlighter highlighter)
	{
		super(fileName, factory);
		this.initHighlighter = highlighter;
	}

	//region AbstractDocument
	@Override
	public void display() throws Exception
	{
		super.display();

		initController();

		this.controller.displayTitle(Common.getSimpleTitle(getName()));
		this.controller.displayText(super.property.get(), super.property::set);
	}

	@Override
	public boolean canClose() throws Exception
	{
		if (!super.canClose())
		{
			return false;
		}

		if (isChanged())
		{
			ButtonType desision = DialogsHelper.showSaveFileDialog(this.getName());
			if (desision == ButtonType.YES)
			{
				save(getName());
			}
			if (desision == ButtonType.CANCEL)
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void save(String fileName) throws Exception
	{
		super.save(fileName);
		this.controller.saved(getName());
	}

	@Override
	public void close(Settings settings) throws Exception
	{
		super.close(settings);
		this.controller.close();
	}
	//endregion

	List<StyleWithRange> findAll(String str, boolean isMatchCase, boolean isRegExp, AtomicInteger atomicInteger)
	{
		resetMatcher(str, isMatchCase, isRegExp);
		ArrayList<StyleWithRange> list = new ArrayList<>();
		try
		{
			Matcher matcher = this.matcher.reset(this.property.get());
			int last = 0;
			while (matcher.find())
			{
				list.add(new StyleWithRange(null, matcher.start() - last));
				list.add(new StyleWithRange(FOUND_STYLE_CLASS, matcher.end() - matcher.start()));
				last = matcher.end();
				atomicInteger.incrementAndGet();
			}
			list.add(new StyleWithRange(null, getLength() - last));
		}
		catch (Exception e)
		{
			list.clear();
			list.add(new StyleWithRange(null, getLength()));
		}
		return list;
	}

	void replaceAll(String str, String replaceTo, boolean isMatchCase, boolean isRegExp)
	{
		resetMatcher(str, isMatchCase, isRegExp);
		try
		{
			String newString = this.matcher.reset(this.property.get()).replaceAll(replaceTo);
			this.property.setValue(newString);
			this.controller.displayText(super.property.get(), super.property::set);
		}
		catch (Exception e)
		{

		}
	}

	List<StyleWithRange> findNext(String text, boolean isMatchCase, boolean isRegExp)
	{
		ArrayList<StyleWithRange> list = new ArrayList<>();
		if (this.matcher == null)
		{
			resetMatcher(text, isMatchCase, isRegExp);
		}
		Matcher matcher = this.matcher.reset(this.property.get());
		boolean lastFind = matcher.find(this.lastPos);
		if (!lastFind)
		{
			this.lastPos = 0;
			boolean findFromZero = matcher.find(this.lastPos);
			if (!findFromZero)
			{
				list.add(new StyleWithRange(null, getLength()));
			}
			else
			{
				return findNext(text, isMatchCase, isRegExp);
			}
		}
		else
		{
			list.add(new StyleWithRange(null, matcher.start()));
			list.add(new StyleWithRange(FOUND_STYLE_CLASS, matcher.end() - matcher.start()));
			list.add(new StyleWithRange(null, getLength() - matcher.end()));
			this.lastPos = matcher.end();
		}
		return list;
	}

	void replaceCurrent(String replacement)
	{
		try
		{
			int start = this.matcher.start();
			Matcher matcher = this.matcher.reset(this.property.get());
			boolean find = matcher.find(start);
			if (find)
			{
				StringBuffer sb = new StringBuffer();
				matcher.appendReplacement(sb, replacement);
				matcher.appendTail(sb);
				String newString = sb.toString();
				this.property.setValue(newString);
				this.controller.displayText(super.property.get(), super.property::set);
			}
		}
		catch (Exception e)
		{
			;
		}
	}

	void resetMatcher(String str, boolean isMatchCase, boolean isRegExp)
	{
		if (!isRegExp)
		{
			str = Pattern.quote(str);
		}
		Pattern pattern = Pattern.compile(str, isMatchCase ? 0 : Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
		this.matcher = pattern.matcher(this.property.get());
		this.lastPos = 0;
	}

	//region private methods
	private int getLength()
	{
		return super.property.get().length();
	}

	private void initController()
	{
		if (!this.isControllerInit)
		{
			this.controller = Common.loadController(PlainTextFxController.class.getResource("PlainTextFx.fxml"));
			this.controller.init(this, getFactory().getSettings(), this.initHighlighter);
			Optional.ofNullable(getFactory().getConfiguration()).ifPresent(c -> c.register(this));
			this.isControllerInit = true;
		}
	}
	//endregion

	private Highlighter initHighlighter;
	private Matcher matcher;
	private int lastPos = 0;
	private boolean isControllerInit = false;

	private PlainTextFxController controller;
}
