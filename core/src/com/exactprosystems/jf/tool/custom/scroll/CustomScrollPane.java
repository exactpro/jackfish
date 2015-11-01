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
	private final int prefHeight;

	public CustomScrollPane()
	{
		this(60);
	}

	public CustomScrollPane(int prefHeight)
	{
		super();
		this.setVbarPolicy(ScrollBarPolicy.NEVER);
		this.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		this.getStyleClass().add(CssVariables.PARAMETERS_PANE);
		this.setFitToHeight(true);
		this.prefHeight = prefHeight;
		this.setPrefHeight(this.prefHeight);
	}

	@Override
	protected Skin<?> createDefaultSkin()
	{
		CustomScrollPaneSkin skin = new CustomScrollPaneSkin(this);
		ScrollBar hsb = skin.getHsb();
		if (hsb.isVisible())
		{
			Platform.runLater(() -> {
				this.setPrefHeight(prefHeight + 15);
				this.setMinHeight(prefHeight + 16);
				this.setMaxHeight(prefHeight + 14);
			});
		}
		hsb.visibleProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue && !oldValue)
			{
				Platform.runLater(() -> {
					this.setPrefHeight(prefHeight + 15);
					this.setMinHeight(prefHeight + 16);
					this.setMaxHeight(prefHeight + 14);
				});
			}
			else if (!newValue && oldValue)
			{
				Platform.runLater(() -> {
					this.setPrefHeight(prefHeight);
					this.setMinHeight(prefHeight - 1);
					this.setMaxHeight(prefHeight + 1);
				});
			}
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
