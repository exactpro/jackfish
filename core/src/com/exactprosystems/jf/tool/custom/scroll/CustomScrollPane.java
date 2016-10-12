////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.scroll;

import com.exactprosystems.jf.tool.CssVariables;
import com.sun.javafx.scene.control.skin.ScrollPaneSkin;
import javafx.application.Platform;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;

public class CustomScrollPane extends ScrollPane
{
    private static final int heightHorizontalScroll = 14;

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
		this.setPrefHeight(prefHeight + heightHorizontalScroll);
	}

	@Override
	protected Skin<?> createDefaultSkin()
	{
		CustomScrollPaneSkin skin = new CustomScrollPaneSkin(this);
		ScrollBar hsb = skin.getHsb();
		hsb.visibleProperty().addListener((observable, oldValue, newValue) -> {
				Platform.runLater(() -> {

                    if(oldValue && !newValue)
                    {
                        this.setPrefHeight(this.getPrefHeight() - heightHorizontalScroll);
                    }

                    if(!oldValue && newValue)
                    {
                        this.setPrefHeight(this.getPrefHeight() + heightHorizontalScroll);
                    }
				});
		});
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
