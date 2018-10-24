/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.tool.documents.text;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.highlighter.Highlighter;
import com.exactprosystems.jf.common.highlighter.StyleWithRange;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
import com.exactprosystems.jf.documents.text.PlainText;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.scene.control.ButtonType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlainTextFx extends PlainText
{
	static final String FOUND_STYLE_CLASS = "found";

	private MutableValue<Highlighter> highlighter;
	private Matcher                   matcher;
	private int lastPos = 0;

	private MutableValue<Integer> findCountProperty;

	public PlainTextFx(String fileName, DocumentFactory factory, Highlighter highlighter)
	{
		super(fileName, factory);
		this.highlighter = new MutableValue<>(highlighter);
		resetMatcher("", false, false);
		this.findCountProperty = new MutableValue<>(0);
	}

	public MutableValue<Integer> getFindCountProperty() {
		return this.findCountProperty;
	}

	//region AbstractDocument
	@Override
	public void display() throws Exception
	{
		super.display();
		this.property.fire();
		this.highlighter.fire();
		//fixme workaround
		this.saved();
		getChangedProperty().accept(false);
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
	List<StyleWithRange> findAll()
	{
		this.findCountProperty.accept(0);

		if (Str.IsNullOrEmpty(this.matcher.pattern().pattern())) {
			return Collections.singletonList(new StyleWithRange(null, getLength()));
		}

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
				this.findCountProperty.accept(this.findCountProperty.get() + 1);
			}
			list.add(new StyleWithRange(null, getLength() - last));
		}
		catch (Exception e)
		{
			list.clear();
			list.add(new StyleWithRange(null, getLength()));
			this.findCountProperty.accept(0);
		}
		return list;
	}

	List<StyleWithRange> findNext()
	{
		ArrayList<StyleWithRange> list = new ArrayList<>();
		Matcher matcher = this.matcher.reset(this.property.get());
		if (this.lastPos > this.getProperty().get().length()) {
			this.lastPos = this.getProperty().get().length() - 1;
		}
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
		if (this.findCountProperty.get() <= 0) {
			return Collections.singletonList(new StyleWithRange(null, getLength()));
		}
		this.lastPos = matcher.start() + lastDifference;
		return findNext();
	}

	void replaceAll(String replaceTo)
	{
		String newString = this.matcher.reset(this.property.get()).replaceAll(replaceTo);
		this.property.accept(newString);
		this.findCountProperty.accept(-1);
	}

	void replaceCurrent(String replacement)
	{
		if (this.findCountProperty.get() <= 0) {
			return;
		}

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
			this.findCountProperty.accept(this.findCountProperty.get() - 1);
		}
	}

	void resetMatcher(String str, boolean isMatchCase, boolean isRegExp)
	{
		if (!isRegExp && !Str.IsNullOrEmpty(str))
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
}
