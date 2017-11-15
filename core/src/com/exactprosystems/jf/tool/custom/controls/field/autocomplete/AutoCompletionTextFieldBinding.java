////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.controls.field.autocomplete;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Collection;

/**
 * Represents a binding between a text field and a auto-completion popup
 *
 * @param <T>
 */
public class AutoCompletionTextFieldBinding<T> extends AutoCompletionBinding<T>
{
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

	private StringConverter<T> converter;

	public AutoCompletionTextFieldBinding(final TextField textField, Callback<String, Collection<T>> suggestionProvider)
	{
		this(textField, suggestionProvider, AutoCompletionTextFieldBinding.defaultStringConverter());
	}

	public AutoCompletionTextFieldBinding(final TextField textField, Callback<String, Collection<T>> suggestionProvider, final StringConverter<T> converter)
	{
		super(textField, suggestionProvider, converter);
		this.converter = converter;

		getCompletionTarget().textProperty().addListener(textChangeListener);
		getCompletionTarget().focusedProperty().addListener(focusChangedListener);
	}

	@Override
	public TextField getCompletionTarget()
	{
		return (TextField) super.getCompletionTarget();
	}

	@Override
	public void dispose()
	{
		getCompletionTarget().textProperty().removeListener(textChangeListener);
		getCompletionTarget().focusedProperty().removeListener(focusChangedListener);
	}

	@Override
	protected void completeUserInput(T completion)
	{
		String newText = converter.toString(completion);
		getCompletionTarget().setText(newText);
		getCompletionTarget().positionCaret(newText.length());
	}

	private final ChangeListener<String> textChangeListener = (obs, oldText, newText) -> {
		if (getCompletionTarget().isFocused())
		{
			setUserInput(newText);
		}
	};

	private final ChangeListener<Boolean> focusChangedListener = (obs, oldFocused, newFocused) -> {
		if (!newFocused)
			hidePopup();
	};
}
