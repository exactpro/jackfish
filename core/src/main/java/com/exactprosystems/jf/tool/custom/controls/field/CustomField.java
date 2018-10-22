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
