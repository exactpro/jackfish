package com.exactprosystems.jf.tool.settings.tabs;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.exactprosystems.jf.common.Settings.GLOBAL_NS;
import static com.exactprosystems.jf.common.Settings.SHORTCUTS_NAME;

public class ShortcutsTabController implements Initializable, ContainingParent, ITabHeight, ITabRestored
{
	private enum ShortcutType
	{
		Document,
		Matrix,
		Other
	}

	public GridPane gridPane;
	public Parent parent;

	public ComboBox<ShortcutType> cbShortcutsName;
	public Button btnAccept;
	public Button btnDefault;
	public Button btnDelete;

	private TreeView<GridPane> treeView;

	private SettingsPanel model;
	private Map<String, String> documents = new LinkedHashMap<>();
	private Map<String, String> matrixNavigation = new LinkedHashMap<>();
	private Map<String, String> matrixActions = new LinkedHashMap<>();
	private Map<String, String> other = new LinkedHashMap<>();

	private EditableCell currentEditableCell = null;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.treeView = new TreeView<>();
		this.treeView.setShowRoot(false);
		this.treeView.setRoot(new TreeItem<>());
		this.treeView.setCellFactory(e -> new CustomTreeCell());
		this.gridPane.add(this.treeView, 0, 1, 2, 2);

		this.treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if (this.currentEditableCell != null)
			{
				this.currentEditableCell.hideTextField();
				this.currentEditableCell = null;
			}
			this.btnDefault.setDisable(newValue != null && !newValue.getChildren().isEmpty());
			this.btnDelete.setDisable(newValue != null && !newValue.getChildren().isEmpty());
		});

		this.cbShortcutsName.getItems().addAll(ShortcutType.values());
		this.cbShortcutsName.getSelectionModel().selectFirst();
		this.cbShortcutsName.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> displayShortcuts(newValue));
		restoreToDefault();
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

		Settings settings = Settings.defaultSettings();

		replaceNoneValuesToDefault(this.documents, settings);
		replaceNoneValuesToDefault(this.matrixNavigation, settings);
		replaceNoneValuesToDefault(this.matrixActions, settings);
		replaceNoneValuesToDefault(this.other, settings);

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
		Consumer<Map.Entry<String, String>> consumer = e -> this.model.updateSettingsValue(e.getKey(), Settings.SHORTCUTS_NAME, e.getValue());

		this.documents.entrySet().forEach(consumer);
		this.matrixNavigation.entrySet().forEach(consumer);
		this.matrixActions.entrySet().forEach(consumer);
		this.other.entrySet().forEach(consumer);
	}

	@Override
	public void restoreToDefault()
	{
		Settings settings = Settings.defaultSettings();

		BiFunction<String, String, String> biFunction = (key, value) -> settings.getValue(GLOBAL_NS, SHORTCUTS_NAME, key).getValue();

		this.documents.replaceAll(biFunction);
		this.matrixNavigation.replaceAll(biFunction);
		this.matrixActions.replaceAll(biFunction);
		this.other.replaceAll(biFunction);
	}

	public void restoreDefaults(ActionEvent actionEvent)
	{
		restoreToDefault();
	}

	//region actions methods
	public void acceptShortCuts(ActionEvent actionEvent)
	{
		if (this.currentEditableCell != null)
		{
			this.currentEditableCell.acceptValue();
			updateShortcut(this.currentEditableCell.lblValue.getText(), this.currentEditableCell);
			this.currentEditableCell = null;
		}
	}

	public void defaultShortCuts(ActionEvent actionEvent)
	{
		TreeItem<GridPane> selectedItem = this.treeView.getSelectionModel().getSelectedItem();
		if (selectedItem != null)
		{
			EditableCell editableCell = (EditableCell) selectedItem.getValue();
			String key = editableCell.lblName.getText();
			Settings.SettingsValue valueOrDefault = Settings.defaultSettings().getValue(GLOBAL_NS, Settings.SHORTCUTS_NAME, key);
			updateShortcut(valueOrDefault.getValue(), editableCell);
		}
	}

	public void deleteShortcuts(ActionEvent actionEvent)
	{
		TreeItem<GridPane> selectedItem = this.treeView.getSelectionModel().getSelectedItem();
		EditableCell cell = (EditableCell) selectedItem.getValue();
		updateShortcut(Common.EMPTY, cell);
	}

	private void updateShortcut(String newValue, EditableCell editableCell)
	{
		ShortcutType currentType = this.cbShortcutsName.getSelectionModel().getSelectedItem();
		switch (currentType)
		{
			case Document:
				this.documents.replace(editableCell.lblName.getText(), newValue);
				break;
			case Matrix:
				this.matrixActions.replace(editableCell.lblName.getText(), newValue);
				this.matrixNavigation.replace(editableCell.lblName.getText(), newValue);
				break;
			case Other:
				this.other.replace(editableCell.lblName.getText(), newValue);
				break;
		}
		this.displayShortcuts(currentType);
		this.treeView.getSelectionModel().select(find(this.treeView.getRoot(), editableCell));
	}

	private TreeItem<GridPane> find(TreeItem<GridPane> root, GridPane item)
	{
		if (root.getValue() != null && root.getValue().equals(item))
		{
			return root;
		}
		for (TreeItem<GridPane> grid : root.getChildren())
		{
			TreeItem<GridPane> gridItem = find(grid, item);
			if (gridItem != null)
			{
				return gridItem;
			}
		}
		return null;
	}
	//endregion

	//region private methods
	private void replaceNoneValuesToDefault(Map<String, String> map, Settings settings)
	{
		map.replaceAll((key,value) -> {
			if (Objects.equals(value, Common.EMPTY))
			{
				return settings.getValue(Settings.GLOBAL_NS, Settings.SHORTCUTS_NAME, key).getValue();
			}
			return value;
		});
	}

	private void displayShortcuts(ShortcutType type)
	{
		this.btnDefault.setDisable(true);
		this.btnDelete.setDisable(true);
		switch (type)
		{
			case Document:
				displayDocsShortcuts();
				break;
			case Matrix:
				displayMatrixShortcuts();
				break;
			case Other:
				displayOtherShortcuts();
				break;
		}
		this.treeView.getSelectionModel().selectFirst();
	}

	private void displayDocsShortcuts()
	{
		ObservableList<TreeItem<GridPane>> children = this.treeView.getRoot().getChildren();
		children.clear();
		this.documents.forEach((key, value) -> children.add(new TreeItem<>(new EditableCell(key, value))));
	}

	private void displayMatrixShortcuts()
	{
		this.treeView.getRoot().getChildren().clear();

		TreeItem<GridPane> treeItemNavigation = new TreeItem<>(createGridPane("Navigation"));
		TreeItem<GridPane> treeItemActions = new TreeItem<>(createGridPane("Actions"));

		this.treeView.getRoot().getChildren().addAll(treeItemNavigation, treeItemActions);

		this.matrixNavigation.forEach((key, value) -> treeItemNavigation.getChildren().add(new TreeItem<>(new EditableCell(key, value))));
		this.matrixActions.forEach((key, value) -> treeItemActions.getChildren().add(new TreeItem<>(new EditableCell(key, value))));
	}

	private void displayOtherShortcuts()
	{
		ObservableList<TreeItem<GridPane>> children = this.treeView.getRoot().getChildren();
		children.clear();
		this.other.forEach((key, value) -> children.add(new TreeItem<>(new EditableCell(key, value))));
	}

	private GridPane createGridPane(String lbl)
	{
		GridPane pane = new GridPane();
		Label label = new Label(lbl);
		pane.add(label, 0, 0);
		return pane;
	}
	//endregion

	//region private classes
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

	private class EditableCell extends GridPane
	{
		private TextField textField;

		private Label lblName;
		private Label lblValue;

		public EditableCell(String first, String second)
		{
			ColumnConstraints c0 = new ColumnConstraints();
			c0.setHalignment(HPos.RIGHT);
			c0.setPercentWidth(50);
			ColumnConstraints c1 = new ColumnConstraints();
			c1.setFillWidth(true);
			c1.setHalignment(HPos.LEFT);
			c1.setPercentWidth(50);
			c1.setMaxWidth(150);
			c1.setMinWidth(150);
			c1.setPrefWidth(150);
			this.getColumnConstraints().addAll(c0, c1);

			this.setHgap(10);

			this.lblName = new Label(first);
			this.lblValue = new Label(second);
			this.lblValue.setMaxWidth(Double.MAX_VALUE);
			GridPane.setMargin(this.lblName, new Insets(0, 10, 0, 0));
			GridPane.setHalignment(this.lblName, HPos.RIGHT);
			GridPane.setMargin(this.lblValue, new Insets(0, 0, 0, 10));
			GridPane.setHalignment(this.lblValue, HPos.LEFT);

			this.lblValue.setOnMouseClicked(e ->
			{
				if (e.getClickCount() == 2)
				{
					if (currentEditableCell != null)
					{
						currentEditableCell.hideTextField();
						currentEditableCell = null;
					}
					currentEditableCell = this;
					btnAccept.setDisable(false);
					btnDefault.setDisable(true);
					btnDelete.setDisable(true);
					displayTextField();
				}
			});

			this.add(this.lblName, 0, 0);
			this.add(this.lblValue, 1, 0);
		}

		private String previousValue;

		private void displayTextField()
		{
			this.textField = new TextField(Common.EMPTY.equals(this.lblValue.getText()) ? "" : this.lblValue.getText());
			Common.setFocused(this.textField);

			this.textField.setOnKeyPressed(keyEvent ->
			{
				if (keyEvent.getCode() == KeyCode.ESCAPE)
				{
					hideTextField();
				}
				else if (validHotKey(keyEvent.getCode()))
				{
					KeyCombination keyCombination = createShortCut(keyEvent);
					this.previousValue = keyCombination.getName();
				}
			});

			this.textField.setOnKeyReleased(keyEvent -> {
				String value = this.previousValue;
				if (!Common.EMPTY.equals(value))
				{
					this.textField.setText(value);
					String otherShortcut = model.nameOtherShortcut(value, this.lblName.getText());
					if (otherShortcut != null)
					{
						btnAccept.setDisable(true);
						if (!this.textField.getStyleClass().contains(CssVariables.INCORRECT_FIELD))
						{
							this.textField.getStyleClass().add(CssVariables.INCORRECT_FIELD);
						}
						this.textField.setTooltip(new Tooltip("This shortcut used " + otherShortcut));
					}
					else
					{
						this.textField.getStyleClass().remove(CssVariables.INCORRECT_FIELD);
						this.textField.setTooltip(null);
					}
				}
			});
			GridPane.setMargin(this.textField, new Insets(0, 0, 0, 10));
			this.add(this.textField, 1, 0);
		}

		private void hideTextField()
		{
			this.getChildren().remove(this.textField);
			btnAccept.setDisable(true);
			btnDefault.setDisable(false);
			btnDelete.setDisable(false);
		}

		private void acceptValue()
		{
			if (this.textField != null)
			{
				String text = this.textField.getText();
				this.lblValue.setText(Str.IsNullOrEmpty(text) ? Common.EMPTY : text);
				hideTextField();
			}
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			EditableCell that = (EditableCell) o;

			return this.lblName.getText() != null ? this.lblName.getText().equals(that.lblName.getText()) : that.lblName.getText() == null;

		}

		@Override
		public int hashCode()
		{
			return this.lblName.getText() != null ? this.lblName.getText().hashCode() : 0;
		}

		boolean validHotKey(KeyCode code)
		{
			switch (code)
			{
				case Q: case W: case E: case R: case T: case Y: case U: case I: case O: case P:
				case A: case S: case D: case F: case G: case H: case J: case K: case L:
				case Z: case X: case C: case V: case B: case N: case M:

				case DIGIT1: case DIGIT2: case DIGIT3:
				case DIGIT4: case DIGIT5: case DIGIT6:
				case DIGIT7: case DIGIT8: case DIGIT9:
				case DIGIT0:

				case INSERT: case ENTER: case ESCAPE: case SPACE:

				case UP: case DOWN: case LEFT: case RIGHT:

				case F1 : case F2:  case F3:
				case F4 : case F5:  case F6:
				case F7	: case F8:  case F9:
				case F10: case F11: case F12:

				case MINUS: case PLUS: case EQUALS: case DELETE:
				return true;

				default:
					return false;
			}
		}

		KeyCombination createShortCut(KeyEvent keyEvent)
		{
			String combination = "";
			if (keyEvent.isShiftDown())
			{
				combination+= KeyCodeCombination.SHIFT_DOWN + "+";
			}
			if (keyEvent.isAltDown())
			{
				combination+=KeyCodeCombination.ALT_DOWN + "+";
			}
			if (keyEvent.isControlDown())
			{
				combination+=KeyCodeCombination.CONTROL_DOWN + "+";
			}
			combination+=keyEvent.getCode();
			return KeyCodeCombination.valueOf(combination);
		}
	}
	//endregion
}