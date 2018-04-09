/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
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
