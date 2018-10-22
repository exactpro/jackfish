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
package com.exactprosystems.jf.tool.custom.scroll;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.sun.javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;

public class CustomScrollPane extends ScrollPane
{
	private static final int HEIGHT_HORIZONTAL_SCROLL = 8;

	public CustomScrollPane()
	{
		this(60);
	}

	public CustomScrollPane(int prefHeight)
	{
		super();
		this.setVbarPolicy(ScrollBarPolicy.NEVER);
		this.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		this.getStyleClass().add(CssVariables.CUSTOM_SCROLL_PANE);
		this.setFitToHeight(true);
		this.setPrefHeight(prefHeight + HEIGHT_HORIZONTAL_SCROLL);
	}

	@Override
	protected Skin<?> createDefaultSkin()
	{
		CustomScrollPaneSkin skin = new CustomScrollPaneSkin(this);
		ScrollBar hsb = skin.getHsb();
		hsb.visibleProperty().addListener((observable, oldValue, newValue) -> Common.runLater(() ->
		{
			if (oldValue && !newValue)
			{
				this.setPrefHeight(this.getPrefHeight() - HEIGHT_HORIZONTAL_SCROLL);
			}

			if (!oldValue && newValue)
			{
				this.setPrefHeight(this.getPrefHeight() + HEIGHT_HORIZONTAL_SCROLL);
			}
		}));
		return skin;
	}

	private static class CustomScrollPaneSkin extends ScrollPaneSkin
	{
		public CustomScrollPaneSkin(ScrollPane scrollpane)
		{
			super(scrollpane);
		}

		public ScrollBar getHsb()
		{
			return super.hsb;
		}
	}
}
