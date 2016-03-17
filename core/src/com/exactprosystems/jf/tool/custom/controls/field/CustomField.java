////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
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
	public CustomField(String text)
	{
		super(text);
		getStyleClass().add(CssVariables.CUSTOM_TEXT_FIELD);
	}

	public CustomField()
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
