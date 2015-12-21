////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.settings;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.number.NumberTextField;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class SettingsPanelController implements Initializable, ContainingParent
{
	private static final Logger			logger						= Logger.getLogger(SettingsPanelController.class);
	public GridPane						mainGrid;

	private Pane						pane;

	public NumberTextField				ntfMaxLastMatrixCount;
	public CheckBox						useFullScreen;
	public ComboBox<Theme>				comboBoxTheme;
	public ComboBox<String>				cbFontFamily;
	public ComboBox<Double>				cbFontSize;
	public CheckBox						useSmallWindow;
	public CheckBox						useFullScreenXpath;
	public NumberTextField				ntfTimeNotification;
	public TextArea						taCopyright;

	// Logs colors
	public ColorPicker					cpAll;
	public ColorPicker					cpDebug;
	public ColorPicker					cpError;
	public ColorPicker					cpFatal;
	public ColorPicker					cpInfo;
	public ColorPicker					cpTrace;
	public ColorPicker					cpWarn;
	private Map<String, ColorPicker>	colorMap					= new HashMap<>();

	// SHORTCUTS DOCUMENT
	public GridPane						documentGrid;
	private Map<String, ShortcutRow>	documentShortcuts			= new HashMap<>();

	// SHORTCUTS MATRIX NAVIGATION
	public GridPane						matrixNavigationGrid;
	private Map<String, ShortcutRow>	matrixNavigationShortcuts	= new HashMap<>();

	// SHORTCUTS MATRIX ACTION
	public GridPane						matrixActionGrid;
	private Map<String, ShortcutRow>	matrixActionShortcuts		= new HashMap<>();

	// SHORTCUTS OTHER
	public GridPane						otherGrid;
	private Map<String, ShortcutRow>	otherShortcuts				= new HashMap<>();

	private ShortcutRow.EditShortcut	edit;
	private SettingsPanel				model;
	private Dialog<ButtonType>			dialog;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		Common.tryCatch(() ->
		{
			assert useFullScreen != null : "fx:id=\"useFullScreen\" was not injected: check your FXML file 'Settings.fxml'.";
			assert comboBoxTheme != null : "fx:id=\"comboBoxTheme\" was not injected: check your FXML file 'Settings.fxml'.";
			this.ntfMaxLastMatrixCount = new NumberTextField(0);
			this.ntfMaxLastMatrixCount.setId(Main.MAX_FILES_COUNT);
			this.ntfTimeNotification = new NumberTextField(0);
			this.ntfTimeNotification.setId(Main.TIME_NOTIFICATION);
			this.mainGrid.add(this.ntfMaxLastMatrixCount, 1, 0);
			this.mainGrid.add(this.ntfTimeNotification, 1, 2);

			this.comboBoxTheme.setId(Main.THEME);
			this.useFullScreen.setId(Main.USE_FULL_SCREEN);
			this.useSmallWindow.setId(Main.USE_SMALL_WINDOW);

			colorMap.put(cpFatal.getId(), cpFatal);
			colorMap.put(cpError.getId(), cpError);
			colorMap.put(cpWarn.getId(), cpWarn);
			colorMap.put(cpInfo.getId(), cpInfo);
			colorMap.put(cpDebug.getId(), cpDebug);
			colorMap.put(cpTrace.getId(), cpTrace);
			colorMap.put(cpAll.getId(), cpAll);
			comboBoxTheme.setItems(FXCollections.observableArrayList(Arrays.stream(Theme.values()).filter(Theme::isVisible).collect(Collectors.toList())));
			initializeFont();
			listeners();
		}, "Error on initialize setting panel configuration");
	}

	private void initializeFont()
	{
		cbFontFamily.getStyleClass().add("font-menu-button");
		cbFontFamily.setMinWidth(150);
		cbFontFamily.setPrefWidth(150);
		cbFontFamily.setMaxWidth(150);
		cbFontFamily.setFocusTraversable(false);
		// cbFontFamily.getProperties().put("comboBoxRowsToMeasureWidth", 0);
		cbFontFamily.setCellFactory(param ->
		{
			final ListCell<String> cell = new ListCell<String>()
			{
				@Override
				public void updateItem(String item, boolean empty)
				{
					super.updateItem(item, empty);
					if (empty)
					{
						setText(null);
						setGraphic(null);
					}
					else
					{
						setText(item);
						setFont(new Font(item, 12));
					}
				}
			};
			cell.setMinWidth(100);
			cell.setPrefWidth(100);
			cell.setMaxWidth(100);
			return cell;
		});
		Platform.runLater(() -> cbFontFamily.setItems(FXCollections.observableArrayList(Font.getFamilies())));
		cbFontSize.getStyleClass().add("font-menu-button");
		cbFontSize.setFocusTraversable(false);
		cbFontSize.getItems().add((double) 8);
		cbFontSize.getItems().add((double) 10);
		cbFontSize.getItems().add((double) 12);
		cbFontSize.getItems().add((double) 13);
		cbFontSize.getItems().add((double) 14);
		cbFontSize.getItems().add((double) 18);
		cbFontSize.getItems().add((double) 24);
		cbFontSize.getItems().add((double) 36);
		cbFontSize.setCellFactory(param -> new ListCell<Double>()
		{
			@Override
			public void updateItem(Double item, boolean empty)
			{
				super.updateItem(item, empty);
				if (empty)
				{
					setText(null);
					setGraphic(null);
				}
				else
				{
					setText(String.valueOf(item));
					setFont(new Font(cbFontFamily.getValue(), item));
				}
			}
		});
		Arrays.asList(cbFontFamily, cbFontSize).stream().forEach(cb -> cb.setOnAction(event ->
		{
			Font font = Font.font(cbFontFamily.getValue(), cbFontSize.getValue());
			model.updateSettingsValue(SettingsPanel.FONT, SettingsPanel.SETTINGS, Common.stringFromFont(font));
		}));
	}

	@Override
	public void setParent(Parent parent)
	{
		this.pane = (Pane) parent;
	}

	public void create(final SettingsPanel model)
	{
		this.model = model;
		this.edit = new ShortcutRow.EditShortcut()
		{
			@Override
			public void edit(String key, String newShortcut)
			{
				SettingsPanelController.this.model.updateSettingsValue(key, SettingsPanel.SHORTCUTS_NAME, newShortcut);
			}

			@Override
			public String nameOtherShortcut(String value, String currentKey)
			{
				return SettingsPanelController.this.model.nameOtherShortcut(value, currentKey);
			}
		};
		createDocumentShortcuts();
		createMatrixNavigationShortcuts();
		createMatrixActionShortcuts();

		createOtherShortcuts();
	}

	public void displayMain(Map<String, String> res)
	{
		Font font = Common.fontFromString(res.get(SettingsPanel.FONT));

		this.cbFontFamily.getSelectionModel().select(font.getFamily());
		this.cbFontSize.getSelectionModel().select(font.getSize());
		this.comboBoxTheme.getSelectionModel().select(
				Theme.valueOf(res.get(comboBoxTheme.getId()) == null ? Theme.WHITE.name() : res.get(comboBoxTheme.getId())));

		this.ntfMaxLastMatrixCount.setText(res.get(ntfMaxLastMatrixCount.getId()) == null ? Main.DEFAULT_MAX_FILES_COUNT : res.get(ntfMaxLastMatrixCount.getId()));
		this.ntfTimeNotification.setText(res.get(ntfTimeNotification.getId()) == null ? "5" : res.get(ntfTimeNotification.getId()));
		this.useFullScreen.setSelected(Boolean.valueOf(res.get(useFullScreen.getId()) == null ? "false" : res.get(useFullScreen.getId())));
		this.useSmallWindow.setSelected(Boolean.valueOf(res.get(useSmallWindow.getId()) == null ? "false" : res.get(useSmallWindow.getId())));
		this.useFullScreenXpath.setSelected(Boolean.valueOf(res.get(useFullScreenXpath.getId()) == null ? "false" : res.get(useFullScreenXpath.getId())));
		this.taCopyright.setText(res.get(taCopyright.getId()) == null ? "" : res.get(taCopyright.getId()).replaceAll("\\\\n", "\n"));
	}

	public void displayLogs(Map<String, String> res)
	{
		res.entrySet().forEach(entry -> colorMap.get(entry.getKey()).setValue(Color.valueOf(entry.getValue())));
	}

	public void displayShortcuts(Map<String, String> res)
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

	public void display(String themePath, String title)
	{
		// this.dialog = new Alert(Alert.AlertType.CONFIRMATION, "", new
		// ButtonType("Save", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
		this.dialog = new Dialog<>();
		this.dialog.getDialogPane().getButtonTypes().addAll(new ButtonType("Save", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
		this.dialog.setResizable(true);
		DialogPane dialogPane = this.dialog.getDialogPane();
		dialogPane.setContent(this.pane);
		this.dialog.setHeaderText(title);
		this.dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
		Optional<ButtonType> optional = this.dialog.showAndWait();
		optional.filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)).ifPresent(bt ->
		{
			if (save())
			{
				DialogsHelper.showInfo(String.format("Settings saved to file [%s] %n Restart application for all changes apply", Common.settingsPath));
			}
			else
			{
				DialogsHelper.showError("Error to save.\nnSee log for details");
			}
		});
	}

	// ============================================================
	// private methods
	// ============================================================
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

	private void createMatrixNavigationShortcuts()
	{
		ShortcutRow rowAddChild = new ShortcutRow(SettingsPanel.ADD_CHILD, edit);
		ShortcutRow rowAddBefore = new ShortcutRow(SettingsPanel.ADD_BEFORE, edit);
		ShortcutRow rowAddAfter = new ShortcutRow(SettingsPanel.ADD_AFTER, edit);
		ShortcutRow rowBreakPoint = new ShortcutRow(SettingsPanel.BREAK_POINT, edit);
		ShortcutRow rowHelp = new ShortcutRow(SettingsPanel.HELP, edit);
		ShortcutRow rowGoToLine = new ShortcutRow(SettingsPanel.GO_TO_LINE, edit);
		ShortcutRow rowShowAll = new ShortcutRow(SettingsPanel.SHOW_ALL, edit);
		ShortcutRow rowPasteItems = new ShortcutRow(SettingsPanel.PASTE_ITEMS, edit);
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
		createOneShortcut(rowHelp, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowGoToLine, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowShowAll, matrixNavigationShortcuts, count++, matrixNavigationGrid);
		createOneShortcut(rowPasteItems, matrixNavigationShortcuts, count++, matrixNavigationGrid);
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

	private void createOneShortcut(ShortcutRow row, Map<String, ShortcutRow> map, int index, GridPane grid)
	{
		map.put(row.getId(), row);
		grid.add(row, 0, index);
		GridPane.setHalignment(row, HPos.CENTER);
	}

	private void listeners()
	{
		this.taCopyright.focusedProperty().addListener((observable1, oldValue, newValue) ->
		{
			if (!newValue && oldValue)
			{
				model.updateSettingsValue(this.taCopyright.getId(), SettingsPanel.SETTINGS, this.taCopyright.getText().replaceAll("\n", "\\\\n"));
			}
		});
		this.ntfMaxLastMatrixCount.focusedProperty().addListener(
				(observable, oldValue, newValue) -> model.updateSettingsValue(ntfMaxLastMatrixCount.getId(), SettingsPanel.SETTINGS,
						String.valueOf(ntfMaxLastMatrixCount.getValue())));
		this.ntfTimeNotification.focusedProperty().addListener(
				(observable, oldValue, newValue) -> model.updateSettingsValue(ntfTimeNotification.getId(), SettingsPanel.SETTINGS,
						String.valueOf(ntfTimeNotification.getValue())));

		comboBoxTheme.getSelectionModel().selectedItemProperty().addListener((observableValue, theme, theme2) ->
		{
			Platform.runLater(() -> useFullScreen.getScene().getStylesheets().setAll(theme2.getPath()));
			model.updateSettingsValue(comboBoxTheme.getId(), SettingsPanel.SETTINGS, comboBoxTheme.getSelectionModel().getSelectedItem().toString());
		});

		useFullScreen.setOnAction(actionEvent -> model.updateSettingsValue(useFullScreen.getId(), SettingsPanel.SETTINGS,
				String.valueOf(useFullScreen.isSelected())));
		useSmallWindow.setOnAction(actionEvent -> model.updateSettingsValue(useSmallWindow.getId(), SettingsPanel.SETTINGS,
				String.valueOf(useSmallWindow.isSelected())));
		useFullScreenXpath.setOnAction(actionEvent -> model.updateSettingsValue(useFullScreenXpath.getId(), SettingsPanel.SETTINGS,
				String.valueOf(useFullScreenXpath.isSelected())));

		this.colorMap.entrySet().forEach(entry -> 
		{
			entry.getValue().setOnAction(e -> 
				this.model.updateSettingsValue(entry.getKey(), SettingsPanel.LOGS_NAME, entry.getValue().getValue().toString()) );
		}); 
	}

	private boolean save()
	{
		try
		{
			this.model.save();
			return true;
		}
		catch (Exception e)
		{
			logger.error("error on save");
			logger.error(e.getMessage(), e);
		}
		finally
		{
			this.dialog.hide();
		}
		return false;
	}
}
