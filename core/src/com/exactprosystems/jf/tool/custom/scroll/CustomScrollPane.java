////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
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
