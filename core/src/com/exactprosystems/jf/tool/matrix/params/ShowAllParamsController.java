////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix.params;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ShowAllParamsController implements Initializable, ContainingParent
{
	public TreeView<ReadableValue> treeView;
	public ListView<CellOnList>    listView;
	public TextField               tfFilter;

	private CheckBoxTreeItem<ReadableValue> notMandatory;
	private CheckBoxTreeItem<ReadableValue> extra;

	private ObservableList<TreeItem<ReadableValue>> notMandatoryChildren;
	private ObservableList<TreeItem<ReadableValue>> extraChildren;

	private Dialog<ButtonType> dialog;
	private Parent parent;
	private String title;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert tfFilter != null : "fx:id=\"tf\" was not injected: check your FXML file 'showAllParams.fxml'.";
		assert treeView != null : "fx:id=\"treeView\" was not injected: check your FXML file 'showAllParams.fxml'.";
		assert listView != null : "fx:id=\"listView\" was not injected: check your FXML file 'showAllParams.fxml'.";

		this.listView.setCellFactory(tempListListView -> new MyListCell());
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	public void setContent(Map<ReadableValue, TypeMandatory> map, Parameters parameters, String title)
	{
		this.title = title;
		this.treeView.setCellFactory(CheckBoxTreeCell.forTreeView());

		CheckBoxTreeItem<ReadableValue> rootItem = new CheckBoxTreeItem<>(new ReadableValue("All"));
		this.treeView.setRoot(rootItem);
		rootItem.setExpanded(true);
		rootItem.setIndependent(false);

		this.notMandatory = new CheckBoxTreeItem<>(new ReadableValue(TypeMandatory.NotMandatory.name()));
		this.notMandatory.setIndependent(false);

		this.extra = new CheckBoxTreeItem<>(new ReadableValue(TypeMandatory.Extra.name()));
		this.extra.setIndependent(false);

		rootItem.getChildren().addAll(this.notMandatory, this.extra);

		map.forEach((value, mandatory) -> {
			CheckBoxTreeItem<ReadableValue> checkBox = new CheckBoxTreeItem<>(value);
			boolean isPresented = parameters.entrySet().stream().anyMatch(entry -> entry.getKey().equals(value.getValue()));
			checkBox.selectedProperty().addListener((observableValue, prevValue, newValue) -> addRemoveItem(newValue, new CellOnList(value, mandatory)));
			switch (mandatory)
			{
				case NotMandatory:
					if (!isPresented)
					{
						this.notMandatory.getChildren().add(checkBox);
					}
					break;
				case Extra:
					this.extra.getChildren().add(checkBox);
					break;
			}
		});

		notMandatoryChildren = FXCollections.observableArrayList(notMandatory.getChildren());
		extraChildren = FXCollections.observableArrayList(extra.getChildren());
		listeners();
	}

	public ArrayList<Pair<ReadableValue, TypeMandatory>> show()
	{
		this.dialog = new Alert(Alert.AlertType.CONFIRMATION);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		this.dialog.setHeaderText(title);
		this.dialog.setResizable(true);
		this.dialog.getDialogPane().setContent(this.parent);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Common.runLater(() -> expandTree(this.treeView.getRoot()));
		this.dialog.setOnShown(e -> Common.setFocused(tfFilter));
		Optional<ButtonType> optional = this.dialog.showAndWait();
		ArrayList<Pair<ReadableValue, TypeMandatory>> res = new ArrayList<>();
		if (optional.get().getButtonData().equals(ButtonBar.ButtonData.OK_DONE))
		{
			res.addAll(this.listView.getItems().stream().map(cell -> 
				new Pair<>(cell.getReadableValue(), cell.getTypeMandatory())).collect(Collectors.toList()));
		}
		return res;
	}

	//============================================================
	// private methods
	//============================================================
	private void listeners()
	{
		this.tfFilter.textProperty().addListener(new ChangeListener<String>()
		{
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String s, String t1)
			{
				notMandatory.getChildren().setAll(notMandatoryChildren);
				extra.getChildren().setAll(extraChildren);
				if (!t1.isEmpty())
				{
					ObservableList<TreeItem<ReadableValue>> nC = FXCollections.observableArrayList();
					ObservableList<TreeItem<ReadableValue>> eC = FXCollections.observableArrayList();

					filtering(nC, notMandatory, t1);
					filtering(eC, extra, t1);
				}
			}

			private void filtering(ObservableList<TreeItem<ReadableValue>> list, CheckBoxTreeItem<ReadableValue> treeItem, String str)
			{
				treeItem.setIndependent(false);
				list.addAll(treeItem.getChildren().stream().filter(item -> item.getValue().getValue().toLowerCase().contains(str.toLowerCase())).collect(Collectors.toList()));
				treeItem.getChildren().clear();
				treeItem.getChildren().addAll(list);
				if (list.size() > 0)
				{
					treeItem.setExpanded(true);
				}
				treeItem.setIndependent(true);
			}
		});

		this.tfFilter.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.TAB)
			{
				Common.setFocused(this.treeView, 0);
				this.treeView.getSelectionModel().clearAndSelect(0);
			}
		});

		this.treeView.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.SPACE)
			{
				CheckBoxTreeItem selectedItem = (CheckBoxTreeItem) this.treeView.getSelectionModel().getSelectedItem();
				selectedItem.setSelected(!selectedItem.isSelected());
			}
			else if ((event.getCode() == KeyCode.UP && this.treeView.getSelectionModel().getSelectedItem() == this.treeView.getRoot()) || event.getCode() == KeyCode.TAB)
			{
				this.treeView.getSelectionModel().clearSelection();
				Common.setFocused(this.tfFilter, 0);
			}
			else if (event.getCode() == KeyCode.ENTER)
			{
				this.dialog.setResult(ButtonType.OK);
				this.dialog.hide();
			}
			else if (event.getCode() == KeyCode.ESCAPE)
			{
				this.dialog.setResult(ButtonType.CANCEL);
				this.dialog.hide();
			}
		});
	}

	private void expandTree(TreeItem<ReadableValue> root)
	{
		root.setExpanded(true);
		root.getChildren().forEach(this::expandTree);
	}

	private void addRemoveItem(Boolean add, CellOnList cell)
	{
		if (add)
		{
			this.listView.getItems().add(cell);
		}
		else
		{
			this.listView.getItems().remove(cell);
		}
	}

	private class CellOnList
	{
		private ReadableValue value;
		private TypeMandatory typeMandatory;

		public CellOnList(ReadableValue text, TypeMandatory typeMandatory)
		{
			this.value = text;
			this.typeMandatory = typeMandatory;
		}

		public ReadableValue getReadableValue()
		{
			return value;
		}

		public TypeMandatory getTypeMandatory()
		{
			return typeMandatory;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			CellOnList that = (CellOnList) o;

			return !(value != null ? !value.equals(that.value) : that.value != null);
		}

		@Override
		public int hashCode()
		{
			return value != null ? value.hashCode() : 0;
		}

		@Override
		public String toString()
		{
			return this.value.toString();
		}
	}

	private class MyListCell extends ListCell<CellOnList>
	{
		public MyListCell()
		{
		}

		@Override
		protected void updateItem(CellOnList cellOnList, boolean empty)
		{
			super.updateItem(cellOnList, empty);
			if (empty)
			{
				setText(null);
			}
			else
			{
				setText(cellOnList.toString());
			}
		}
	}
}
