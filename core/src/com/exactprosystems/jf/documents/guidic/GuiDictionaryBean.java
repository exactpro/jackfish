////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.guidic;

import com.exactprosystems.jf.api.app.Addition;
import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.documents.matrix.parser.items.MutableArrayList;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GuiDictionary", propOrder = { "windows" })
@XmlRootElement(name = "dictionary")
public class GuiDictionaryBean implements Mutable
{
	@XmlElement(name = "window")
	protected MutableArrayList<Window> windows;

	public static final Class<?>[] jaxbContextClasses =
		{
			GuiDictionaryBean.class,
			Window.class,
			Section.class,
			AbstractControl.class,
			ExtraInfo.class,
			Rect.class,
			Attr.class,
			Addition.class,
		};

	public GuiDictionaryBean()
	{
		this.windows = new MutableArrayList<>();
	}

	//region interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.windows.isChanged();
	}

	@Override
	public void saved()
	{
		this.windows.saved();
	}
	//endregion
}
