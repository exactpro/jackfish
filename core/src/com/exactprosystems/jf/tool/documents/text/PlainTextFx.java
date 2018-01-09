////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.documents.text;

import com.exactprosystems.jf.common.highlighter.Highlighter;
import com.exactprosystems.jf.common.highlighter.StyleWithRange;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
import com.exactprosystems.jf.documents.text.PlainText;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlainTextFx extends PlainText
{
	static final String FOUND_STYLE_CLASS = "found";

	public PlainTextFx(String fileName, DocumentFactory factory, Highlighter highlighter)
	{
		super(fileName, factory);
		this.highlighter = new MutableValue<>(highlighter);
		resetMatcher("", false, false);
	}

	//region AbstractDocument
	@Override
	public void display() throws Exception
	{
		super.display();
		this.property.fire();
		this.highlighter.fire();
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
			ButtonType desision = DialogsHelper.showSaveFileDialog(getNameProperty().get());
			if (desision == ButtonType.YES)
			{
				save(getNameProperty().get());
			}
			if (desision == ButtonType.CANCEL)
			{
				return false;
			}
		}

		return true;
	}

	//endregion

	public MutableValue<Highlighter> getHighlighter()
	{
		return highlighter;
	}

	//region Works with highlight
	List<StyleWithRange> findAll(AtomicInteger atomicInteger)
	{
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

	List<StyleWithRange> findNext()
	{
		ArrayList<StyleWithRange> list = new ArrayList<>();
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
				return findNext();
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

	List<StyleWithRange> getCurrentStyles(int lastDifference)
	{
		this.lastPos = matcher.start() + lastDifference;
		return findNext();
	}

	void replaceAll(String replaceTo)
	{
		String newString = this.matcher.reset(this.property.get()).replaceAll(replaceTo);
		this.property.accept(newString);
	}

	void replaceCurrent(String replacement)
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
			this.property.accept(newString);
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
	//endregion

	//region private methods
	private int getLength()
	{
		return super.property.get().length();
	}
	//endregion

	private MutableValue<Highlighter> highlighter;
	private Matcher                   matcher;
	private int lastPos = 0;
}
