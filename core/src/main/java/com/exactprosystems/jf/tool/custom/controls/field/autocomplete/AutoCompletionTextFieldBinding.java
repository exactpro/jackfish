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
package com.exactprosystems.jf.tool.custom.controls.field.autocomplete;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Collection;

/**
 * Represents a binding between a text field and a auto-completion popup
 */
public class AutoCompletionTextFieldBinding<T> extends AutoCompletionBinding<T>
{
	private final StringConverter<T> converter;

	private final ChangeListener<String> textChangeListener = (obs, oldText, newText) ->
	{
		if (this.getCompletionTarget().isFocused())
		{
			super.setUserInput(newText);
		}
	};

	private final ChangeListener<Boolean> focusChangedListener = (obs, oldFocused, newFocused) ->
	{
		if (!newFocused)
		{
			super.hidePopup();
		}
	};

	private static <T> StringConverter<T> defaultStringConverter()
	{
		return new StringConverter<T>()
		{
			@Override
			public String toString(T t)
			{
				return t == null ? null : t.toString();
			}

			@Override
			public T fromString(String string)
			{
				return (T) string;
			}
		};
	}

	public AutoCompletionTextFieldBinding(final TextField textField, Callback<String, Collection<T>> suggestionProvider)
	{
		this(textField, suggestionProvider, AutoCompletionTextFieldBinding.defaultStringConverter());
	}

	public AutoCompletionTextFieldBinding(final TextField textField, Callback<String, Collection<T>> suggestionProvider, final StringConverter<T> converter)
	{
		super(textField, suggestionProvider, converter);
		this.converter = converter;

		this.getCompletionTarget().textProperty().addListener(this.textChangeListener);
		this.getCompletionTarget().focusedProperty().addListener(this.focusChangedListener);
	}

	public void updateProvider(Callback<String, Collection<T>> suggestionProvider)
	{
		super.suggestionProvider = suggestionProvider;
	}

	@Override
	public TextField getCompletionTarget()
	{
		return (TextField) super.getCompletionTarget();
	}

	@Override
	public void dispose()
	{
		this.getCompletionTarget().textProperty().removeListener(this.textChangeListener);
		this.getCompletionTarget().focusedProperty().removeListener(this.focusChangedListener);
	}

	@Override
	protected void completeUserInput(T completion)
	{
		String newText = this.converter.toString(completion);
		this.getCompletionTarget().setText(newText);
		this.getCompletionTarget().positionCaret(newText.length());
	}
}
