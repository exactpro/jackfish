////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.controls.area;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;

import java.util.Optional;

public abstract class CustomArea extends TextArea
{
	public CustomArea(String text)
	{
		super(text);
		getStyleClass().add(CssVariables.CUSTOM_TEXT_AREA);
		this.editableProperty().addListener((observable, oldValue, newValue) -> {
			Optional.ofNullable(this.right.get()).ifPresent(node -> node.setOpacity(newValue ? 1.0 : 0.0));
		});
	}

	public CustomArea()
	{
		this("");
	}

	private ObjectProperty<Node> right = new SimpleObjectProperty<>(this, "right");

	public final ObjectProperty<Node> rightProperty()
	{
		return right;
	}

	public final Node getRight()
	{
		return right.get();
	}

	public final void setRight(Node value)
	{
		right.set(value);
	}

	@Override
	protected Skin<?> createDefaultSkin()
	{
		return new CustomAreaSkin(this)
		{
			@Override
			public ObjectProperty<Node> rightProperty()
			{
				return CustomArea.this.rightProperty();
			}
		};
	}
}