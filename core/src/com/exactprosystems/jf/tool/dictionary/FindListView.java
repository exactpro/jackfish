////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.dictionary;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.controls.field.CustomFieldWithButton;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class FindListView<T> extends VBox
{
	private ListView<T> listView;
	private List<T> data;
	private CustomFieldWithButton cfbFind;
	private BiPredicate<T, String> filter;

	public FindListView(BiPredicate<T, String> filter, boolean isEditable)
	{
		this.filter = filter;
		this.listView = new ListView<>();
		this.listView.setTooltip(new Tooltip(R.DRAG_N_DROP_LIST_TOOLTIP.get()));
		this.cfbFind = new CustomFieldWithButton();
		this.cfbFind.setPromptText("find");
		this.getChildren().add(this.cfbFind);
		this.getChildren().add(Common.createSpacer(Common.SpacerEnum.VerticalMin));
		this.getChildren().add(this.listView);
		this.listView.setEditable(isEditable);
		VBox.setVgrow(this.listView, Priority.ALWAYS);
		listeners();
	}

	public void setData(List<T> data, boolean withClear)
	{
		this.data = data;
		if (withClear)
		{
			this.listView.getItems().clear();
		}
		this.listView.getItems().addAll(new ArrayList<>(this.data));
		if (!Str.IsNullOrEmpty(this.cfbFind.getText()))
		{
			this.filter(this.cfbFind.getText());
		}
	}

	public void addItem(int index, T item)
	{
		this.data.add(index, item);
	}

	public void setCellFactory(Callback<ListView<T>, ListCell<T>> value)
	{
		this.listView.setCellFactory(value);
	}

	public void select(int index)
	{
		this.listView.getSelectionModel().select(index);
	}

	public void select(T item)
	{
		this.listView.getSelectionModel().select(item);
	}

	public void setFilter(BiPredicate<T, String> filter)
	{
		this.filter = filter;
	}

	public void addChangeListener(ChangeListener<T> changeListener)
	{
		this.listView.getSelectionModel().selectedItemProperty().addListener(changeListener);
	}

	public void removeChangeListener(ChangeListener<T> changeListener)
	{
		this.listView.getSelectionModel().selectedItemProperty().removeListener(changeListener);
	}

	public T getSelectedItem()
	{
		return this.listView.getSelectionModel().getSelectedItem();
	}

	public void selectItem(T item)
	{
		this.listView.getSelectionModel().select(item);
	}

	public List<T> getItems()
	{
		return this.listView.getItems();
	}

	public ListView<T> getListView() {
		return listView;
	}

	private void listeners()
	{
		this.cfbFind.textProperty().addListener((observable, oldValue, newValue) -> {
			filter(newValue);
		});

		this.cfbFind.setOnKeyReleased(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER && this.listView.getItems().size() == 1)
			{
				this.listView.getSelectionModel().select(0);
			}
			if ((keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.TAB) && !this.listView.getItems().isEmpty())
			{
				this.listView.getSelectionModel().selectFirst();
				this.listView.requestFocus();
			}
		});
	}

	private void filter(String newValue)
	{
		this.listView.getItems().clear();
		if (newValue == null || newValue.isEmpty())
		{
			this.listView.getItems().addAll(this.data);
		}
		else
		{
			this.data.stream().filter(t -> this.filter.test(t, newValue)).forEach(this.listView.getItems()::add);
		}
	}
}
