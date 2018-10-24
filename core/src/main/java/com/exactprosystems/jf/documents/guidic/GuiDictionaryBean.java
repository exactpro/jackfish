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
