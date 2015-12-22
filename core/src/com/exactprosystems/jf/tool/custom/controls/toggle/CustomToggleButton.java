////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.controls.toggle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleButton;

public class CustomToggleButton extends ToggleButton
{
	private ObjectProperty<Node> right = new SimpleObjectProperty<>(this, "right");

	public CustomToggleButton()
	{
	}

	public CustomToggleButton(String text)
	{
		super(text);
	}

	public CustomToggleButton(String text, Node graphic)
	{
		super(text, graphic);
	}

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
		return new CustomToggleButtonSkin(this)
		{
			@Override
			public ObjectProperty<Node> rightProperty()
			{
				return right;
			}
		};
	}
}
