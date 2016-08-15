////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.combobox;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Skin;
import javafx.util.StringConverter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CheckedComboBox<T> extends ComboBox<T>
{
	private Map<T, Boolean> map = new LinkedHashMap<>();

	private StringConverter<T> stringConverter;
	private CustomListCell<T> buttonCell;

	public CheckedComboBox()
	{
		this(FXCollections.observableArrayList());
	}

	public CheckedComboBox(ObservableList<T> items)
	{
		super(items);

		this.buttonCell = new CustomListCell<>();
		this.setCellFactory(p -> new ListCell<T>()
		{
			private CheckBox checkBox;

			@Override
			protected void updateItem(T item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					checkBox = new CheckBox(convertToString(item));
					checkBox.setSelected(map.getOrDefault(item, false));
					checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
						map.put(item, newValue);
						buttonCell.updateItem(item, false);
					});
					setGraphic(checkBox);
				}
				else
				{
					setGraphic(null);
				}
			}
		});

		this.setButtonCell(this.buttonCell);

		this.getItems().addListener(new ListChangeListener<T>()
		{
			@Override
			public void onChanged(Change<? extends T> c)
			{
				map.clear();
				buttonCell.updateItem(null, true);
			}
		});
	}

	public void setStringConverter(StringConverter<T> stringConverter)
	{
		this.stringConverter = stringConverter;
	}

	public List<T> getChecked()
	{
		return this.map.entrySet()
				.stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	@Override
	protected Skin<?> createDefaultSkin()
	{
		return new ComboBoxListViewSkin<T>(this)
		{
			@Override
			protected boolean isHideOnClickEnabled()
			{
				return false;
			}
		};
	}

	private String buildString()
	{
		return this.map.entrySet()
				.stream()
				.filter(Map.Entry::getValue)
				.map(Map.Entry::getKey)
				.map(this::convertToString)
				.collect(Collectors.joining(", "));
	}

	private String convertToString(T value)
	{
		return this.stringConverter == null ? value.toString() : this.stringConverter.toString(value);
	}

	private class CustomListCell<S> extends ListCell<S>
	{
		@Override
		public void updateItem(S item, boolean empty)
		{
			if (item != null && !empty)
			{
				setText(buildString());
			}
			else
			{
				setText(null);
			}
		}
	}
}