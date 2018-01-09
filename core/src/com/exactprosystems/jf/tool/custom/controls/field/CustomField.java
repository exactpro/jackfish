////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.controls.field;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

public abstract class CustomField extends TextField
{
	private ObjectProperty<Node> right = new SimpleObjectProperty<>(this, "right");

	public CustomField(String text)
	{
		super(text);
		super.getStyleClass().add(CssVariables.CUSTOM_TEXT_FIELD);
	}

	public CustomField()
	{
		this("");
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
		return new CustomFieldSkin(this)
		{
			@Override
			public ObjectProperty<Node> rightProperty()
			{
				return CustomField.this.rightProperty();
			}
		};
	}
}
