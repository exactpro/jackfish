package com.exactprosystems.jf.tool.settings.tabs;

import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

//TODO need review
public class ShortcutsTabController implements Initializable, ContainingParent, ITabHeight
{
	private enum ShortcutType
	{
		Document,
		Matrix,
		Other;
	}

	public GridPane gridPane;
	public Parent parent;

	public ComboBox<ShortcutType> cbShortcutsName;
	public GridPane documentGrid;
	public Button btnAccept;
	public Button btnDefault;
	public Button btnDelete;

	private TreeView<GridPane> treeView;

	private SettingsPanel model;
	private Map<String, String> documents = new LinkedHashMap<>();
	private Map<String, String> matrixNavigation = new LinkedHashMap<>();
	private Map<String, String> matrixActions = new LinkedHashMap<>();
	private Map<String, String> other = new LinkedHashMap<>();

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.treeView = new TreeView<>();
		this.treeView.setShowRoot(false);
		this.treeView.setRoot(new TreeItem<>());
		this.treeView.setCellFactory(e -> new CustomTreeCell());
		this.gridPane.add(this.treeView, 0, 1, 2, 1);

		this.cbShortcutsName.getItems().addAll(ShortcutType.values());
		this.cbShortcutsName.getSelectionModel().selectFirst();
		this.cbShortcutsName.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> displayShortcuts(newValue));
	}
	//endregion

	//region ContainingParent
	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}
	//endregion

	public void init(SettingsPanel model)
	{
		this.model = model;
	}

	public void displayInfo(Map<String, String> documents, Map<String, String> matrixNavigation, Map<String, String> matrixActions, Map<String, String> other)
	{
		this.documents = documents;
		this.matrixNavigation = matrixNavigation;
		this.matrixActions = matrixActions;
		this.other = other;

		displayShortcuts(ShortcutType.Document);
	}

	public void displayInto(Tab tab)
	{
		tab.setContent(this.parent);
		tab.setUserData(this);
	}

	@Override
	public double getHeight()
	{
		return -1;
	}

	public void save()
	{
		Consumer<Map.Entry<String, String>> consumer = e -> this.model.updateSettingsValue(e.getKey(), SettingsPanel.SHORTCUTS_NAME, e.getValue());

		this.documents.entrySet().forEach(consumer);
		this.matrixNavigation.entrySet().forEach(consumer);
		this.matrixActions.entrySet().forEach(consumer);
		this.other.entrySet().forEach(consumer);
	}

	//region actions methods
	public void acceptShortCuts(ActionEvent actionEvent)
	{
		//TODO implement
	}

	public void defaultShortCuts(ActionEvent actionEvent)
	{
		//TODO implement
	}

	public void deleteShortcuts(ActionEvent actionEvent)
	{
		//TODO implement
	}
	//endregion

	private void displayShortcuts(ShortcutType type)
	{
		switch (type)
		{
			case Document:	displayDocsShortcuts(); break;
			case Matrix:	displayMatrixShortcuts(); break;
			case Other:		displayOtherShortcuts(); break;
		}
	}

	//region private methods
	private void displayDocsShortcuts()
	{
		ObservableList<TreeItem<GridPane>> children = this.treeView.getRoot().getChildren();
		children.clear();
		this.documents.entrySet().forEach(e -> children.add(new TreeItem<>(createGridPane(e.getKey(), e.getValue()))));
	}

	private void displayMatrixShortcuts()
	{
		this.treeView.getRoot().getChildren().clear();

		TreeItem<GridPane> treeItemNavigation = new TreeItem<>(createGridPane("Navigation"));
		TreeItem<GridPane> treeItemActions = new TreeItem<>(createGridPane("Actions"));

		this.treeView.getRoot().getChildren().addAll(treeItemNavigation, treeItemActions);

		this.matrixNavigation.entrySet().forEach(e -> treeItemNavigation.getChildren().add(new TreeItem<>(createGridPane(e.getKey(), e.getValue()))));
		this.matrixActions.entrySet().forEach(e -> treeItemActions.getChildren().add(new TreeItem<>(createGridPane(e.getKey(), e.getValue()))));
	}

	private void displayOtherShortcuts()
	{
		ObservableList<TreeItem<GridPane>> children = this.treeView.getRoot().getChildren();
		children.clear();
		this.other.entrySet().forEach(e -> children.add(new TreeItem<>(createGridPane(e.getKey(), e.getValue()))));
	}

	private GridPane createGridPane(String first, String second)
	{
		GridPane gridPane = new GridPane();
		ColumnConstraints c0 = new ColumnConstraints();
		c0.setHalignment(HPos.RIGHT);
		c0.setPercentWidth(50);
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setHalignment(HPos.LEFT);
		c1.setPercentWidth(50);
		gridPane.getColumnConstraints().addAll(c0,c1);
		gridPane.setHgap(10);

		Label name = new Label(first);

		//TODO
		Label value = new Label(second);
		GridPane.setMargin(name, new Insets(0, 10, 0, 0));
		GridPane.setHalignment(name, HPos.RIGHT);
		GridPane.setMargin(value, new Insets(0, 0, 0, 10));
		GridPane.setHalignment(value, HPos.LEFT);

		gridPane.add(name, 0, 0);
		gridPane.add(value, 1, 0);

		return gridPane;
	}

	private GridPane createGridPane(String lbl)
	{
		GridPane pane = new GridPane();
		Label label = new Label(lbl);
		pane.add(label, 0, 0);
		return pane;
	}
	//endregion

	private class CustomTreeCell extends TreeCell<GridPane>
	{
		@Override
		protected void updateItem(GridPane item, boolean empty)
		{
			//TODO set styleclass instead of style
			this.setStyle("");
			super.updateItem(item, empty);
			if (item != null && !empty)
			{
				this.setStyle("-fx-border-color:black; -fx-border-width : 0 0 1 0");
				this.setGraphic(item);
			}
			else
			{
				this.setGraphic(null);
			}
		}
	}
}