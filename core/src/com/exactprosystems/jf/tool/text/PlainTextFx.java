////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.text;

import com.exactprosystems.jf.common.Settings;
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
	public PlainTextFx(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);
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

	public List<StyleWithRange> createStyles(String str, boolean isMatchCase, boolean isRegExp, AtomicInteger atomicInteger)
	{
		ArrayList<StyleWithRange> list = new ArrayList<>();
		try
		{
			Matcher matcher = createMatcher(str, isMatchCase, isRegExp);
			int last = 0;
			while (matcher.find())
			{
				list.add(new StyleWithRange(null, matcher.start() - last));
				list.add(new StyleWithRange("found", matcher.end() - matcher.start()));
				last = matcher.end();
				atomicInteger.addAndGet(1);
			}
			list.add(new StyleWithRange(null, super.property.get().length() - last));
		}
		catch (Exception e)
		{
			list.clear();
			list.add(new StyleWithRange(null, super.property.get().length()));
		}
		return list;
	}

	private Matcher createMatcher(String str, boolean isMatchCase, boolean isRegExp)
	{
		if (!isRegExp)
		{
			str = Pattern.quote(str);
		}
		Pattern pattern = Pattern.compile(str, isMatchCase ? 0 : Pattern.CASE_INSENSITIVE + Pattern.MULTILINE);
		return pattern.matcher(this.property.get());
	}

	public void replace(String str, String replaceTo, boolean isMatchCase, boolean isRegExp)
	{
		try
		{
			String newString = createMatcher(str, isMatchCase, isRegExp).replaceAll(replaceTo);
			this.property.setValue(newString);
			this.controller.displayText(super.property.get(), super.property::set);
		}
		catch (Exception e)
		{

		}
	}

	private void initController()
	{
		if (!this.isControllerInit)
		{
			this.controller = Common.loadController(PlainTextFxController.class.getResource("PlainTextFx.fxml"));
			this.controller.init(this, getFactory().getSettings());
			Optional.ofNullable(getFactory().getConfiguration()).ifPresent(c -> c.register(this));
			this.isControllerInit = true;
		}
	}

	private boolean isControllerInit = false;
	private PlainTextFxController controller;
}
