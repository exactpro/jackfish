package com.exactprosystems.jf.tool.settings.tabs;

import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import com.exactprosystems.jf.tool.settings.ShortcutRow;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

//TODO need review
public class ShortcutsTabController implements Initializable, ContainingParent, ITabHeight
{
	public Parent parent;
	private SettingsPanel model;

	public ComboBox<String> cbShortcutsName;
	public GridPane documentGrid;
	public Button btnAccept;
	public Button btnDefault;
	public Button btnDelete;

	private Map<String, ShortcutRow> documentShortcuts = new HashMap<>();

	public GridPane matrixNavigationGrid;
	private Map<String, ShortcutRow> matrixNavigationShortcuts = new HashMap<>();

	public GridPane matrixActionGrid;
	private Map<String, ShortcutRow> matrixActionShortcuts = new HashMap<>();

	public GridPane otherGrid;
	private Map<String, ShortcutRow> otherShortcuts = new HashMap<>();

	private ShortcutRow.EditShortcut edit;

	//region Initializable
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		this.edit = new ShortcutRow.EditShortcut()
		{
			@Override
			public void edit(String key, String newShortcut)
			{
				ShortcutsTabController.this.model.updateSettingsValue(key, SettingsPanel.SHORTCUTS_NAME, newShortcut);
			}

			@Override
			public String nameOtherShortcut(String value, String currentKey)
			{
				return ShortcutsTabController.this.model.nameOtherShortcut(value, currentKey);
			}
		};
//		this.cbShortcutsName.getItems().addAll("Document", "Matrix", "Other");
//		this.cbShortcutsName.getSelectionModel().selectFirst();

		createMatrixActionShortcuts();
		createDocumentShortcuts();
		createMatrixNavigationShortcuts();

		createOtherShortcuts();

//		this.cbShortcutsName.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//			TODO implement
//		});
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

	public void displayInfo(Map<String, String> res)
	{
		for (Map.Entry<String, String> entry : res.entrySet())
		{
			ShortcutRow documentRow = documentShortcuts.get(entry.getKey());
			if (documentRow != null)
			{
				documentRow.setShortcut(entry.getValue());
			}
			else
			{
				ShortcutRow shortcutRow = otherShortcuts.get(entry.getKey());
				if (shortcutRow != null)
				{
					shortcutRow.setShortcut(entry.getValue());
				}
				else
				{
					ShortcutRow matrixNavigationRow = matrixNavigationShortcuts.get(entry.getKey());
					if (matrixNavigationRow != null)
					{
						matrixNavigationRow.setShortcut(entry.getValue());
					}
					else
					{
						ShortcutRow matrixActionRow = matrixActionShortcuts.get(entry.getKey());
						if (matrixActionRow != null)
						{
							matrixActionRow.setShortcut(entry.getValue());
						}
					}
				}
			}
		}
	}

	public void displayInto(Tab tab)
	{
		tab.setContent(this.parent);
		tab.setUserData(this);
	}

	@Override
	public double getHeight()
	{
		//TODO implement
		return -1;
	}

	public void save()
	{

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

	//region private methods
	private void createMatrixActionShortcuts()
	{
		ShortcutRow rowStartMatrix = new ShortcutRow(SettingsPanel.START_MATRIX, edit);
		ShortcutRow rowStopMatrix = new ShortcutRow(SettingsPanel.STOP_MATRIX, edit);
		ShortcutRow rowPauseMatrix = new ShortcutRow(SettingsPanel.PAUSE_MATRIX, edit);
		ShortcutRow rowShowResult = new ShortcutRow(SettingsPanel.SHOW_RESULT, edit);
		ShortcutRow rowShowWatch = new ShortcutRow(SettingsPanel.SHOW_WATCH, edit);
		ShortcutRow rowTracing = new ShortcutRow(SettingsPanel.TRACING, edit);
		ShortcutRow rowFindOnMatrix = new ShortcutRow(SettingsPanel.FIND_ON_MATRIX, edit);

		createOneShortcut(rowStartMatrix, matrixActionShortcuts, 0, matrixActionGrid);
		createOneShortcut(rowStopMatrix, matrixActionShortcuts, 1, matrixActionGrid);
		createOneShortcut(rowPauseMatrix, matrixActionShortcuts, 2, matrixActionGrid);
		createOneShortcut(rowShowResult, matrixActionShortcuts, 3, matrixActionGrid);
		createOneShortcut(rowShowWatch, matrixActionShortcuts, 4, matrixActionGrid);
		createOneShortcut(rowTracing, matrixActionShortcuts, 5, matrixActionGrid);
		createOneShortcut(rowFindOnMatrix, matrixActionShortcuts, 6, matrixActionGrid);

	}

	private void createOneShortcut(ShortcutRow row, Map<String, ShortcutRow> map, int index, GridPane grid)
	{
		map.put(row.getId(), row);
		grid.add(row, 0, index);
		GridPane.setHalignment(row, HPos.CENTER);
	}

	private void createMatrixNavigationShortcuts()
	{
		ShortcutRow rowAddChild = new ShortcutRow(SettingsPanel.ADD_CHILD, edit);
		ShortcutRow rowAddBefore = new ShortcutRow(SettingsPanel.ADD_BEFORE, edit);
		ShortcutRow rowAddAfter = new ShortcutRow(SettingsPanel.ADD_AFTER, edit);
		ShortcutRow rowBreakPoint = new ShortcutRow(SettingsPanel.BREAK_POINT, edit);
		ShortcutRow rowAddParameter = new ShortcutRow(SettingsPanel.ADD_PARAMETER, edit);
		ShortcutRow rowHelp = new ShortcutRow(SettingsPanel.HELP, edit);
		ShortcutRow rowGoToLine = new ShortcutRow(SettingsPanel.GO_TO_LINE, edit);
		ShortcutRow rowShowAll = new ShortcutRow(SettingsPanel.SHOW_ALL, edit);
		ShortcutRow rowPasteItemsAfter = new ShortcutRow(SettingsPanel.PASTE_ITEMS_AFTER, edit);
		ShortcutRow rowPasteItemsChild = new ShortcutRow(SettingsPanel.PASTE_ITEMS_CHILD, edit);
		ShortcutRow rowPasteItemsBefore = new ShortcutRow(SettingsPanel.PASTE_ITEMS_BEFORE, edit);
		ShortcutRow rowCopyItems = new ShortcutRow(SettingsPanel.COPY_ITEMS, edit);
		ShortcutRow rowDeleteItem = new ShortcutRow(SettingsPanel.DELETE_ITEM, edit);
		ShortcutRow rowCollapseAll = new ShortcutRow(SettingsPanel.COLLAPSE_ALL, edit);
		ShortcutRow rowExpandOne = new ShortcutRow(SettingsPanel.EXPAND_ONE, edit);
		ShortcutRow rowCollapseOne = new ShortcutRow(SettingsPanel.COLLAPSE_ONE, edit);
		ShortcutRow rowExpandAll = new ShortcutRow(SettingsPanel.EXPAND_ALL, edit);

		int count = 0;
		createOneShortcut(rowAddChild, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowAddBefore, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowAddAfter, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowBreakPoint, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowAddParameter, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowHelp, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowGoToLine, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowShowAll, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowPasteItemsAfter, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowPasteItemsChild, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowPasteItemsBefore, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowCopyItems, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowDeleteItem, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowCollapseAll, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowExpandOne, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowCollapseOne, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowExpandAll, matrixNavigationShortcuts, count++, matrixNavigationGrid);
	}

	private void createDocumentShortcuts()
	{
		ShortcutRow rowUndo = new ShortcutRow(SettingsPanel.UNDO, edit);
		ShortcutRow rowRedo = new ShortcutRow(SettingsPanel.REDO, edit);
		ShortcutRow rowSaveDocument = new ShortcutRow(SettingsPanel.SAVE_DOCUMENT, edit);

		createOneShortcut(rowSaveDocument, documentShortcuts, 1, documentGrid);
		createOneShortcut(rowUndo, documentShortcuts, 2, documentGrid);
		createOneShortcut(rowRedo, documentShortcuts, 3, documentGrid);
	}

	private void createOtherShortcuts()
	{
		ShortcutRow rowShowAllTabs = new ShortcutRow(SettingsPanel.SHOW_ALL_TABS, edit);

		createOneShortcut(rowShowAllTabs, otherShortcuts, 1, otherGrid);
	}
	//endregion
}