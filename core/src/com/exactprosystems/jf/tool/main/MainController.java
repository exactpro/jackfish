////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.main;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Document;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.Settings.SettingsValue;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.logs.LogsFx;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.treetable.MatrixTreeView;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.schedule.RunnerScheduler;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable, ContainingParent
{
	private final static int PANE_WIDTH = 800;
	private final static int PANE_HEIGHT = 600;

	private final static double INIT_VALUE = 0.15;
	private final static double MIN_VALUE = 0.05;

	private static final Logger logger = Logger.getLogger(MainController.class);

	public TabPane tabPane;
	public ProgressBar progressBar;
	public ToolBar tbMain;
	public BorderPane mainPanel;
	private LogsFx log;

	public Menu menuFile;
	public MenuItem fileLoadConfiguration;
	public MenuItem fileNewConfiguration;
	public MenuItem fileLoadConfiguration2;
	public MenuItem fileNewConfiguration2;

	public Menu fileLoad;
	public MenuItem fileLoadDictionary;
	public MenuItem fileLoadSystemVars;
	public MenuItem fileLoadMatrix;
	public MenuItem fileLoadPlainText;
	public MenuItem fileLoadCsv;

	public Menu fileNew;
	public MenuItem fileNewDictionary;
	public MenuItem fileNewSystemVars;
	public MenuItem fileNewMatrix;
	public MenuItem fileNewPlainText;
	public MenuItem fileNewCsv;

	public MenuItem fileSave;
	public MenuItem fileSaveAs;
	public MenuItem fileSaveAll;

	public Menu fileLastOpenMatrix;
	public MenuItem fileRunFromFile;
	public MenuItem fileOpenReport;

	public MenuItem fileClose;

	public Menu menuEdit;
	public MenuItem editUndo;
	public MenuItem editRedo;

	public Menu menuView;
	public MenuItem viewLogs;
	public MenuItem viewSettings;
	public MenuItem viewStore;
	public MenuItem viewAllTabs;

	public Menu menuMatrix;
	public MenuItem matrixStart;
	public MenuItem matrixStop;
	public MenuItem matrixSchedule;

	public Menu menuHelp;
	public MenuItem helpActionsHelp;

	public MenuItem helpAboutProgram;

	public Button btnOpenMatrix;
	public Button btnNewMatrix;
	public Button btnSaveDocument;
	public Button btnSaveAsDocument;
	public Button btnOpenMainLog;
	public Button btnShowCalculator;
	public Button btnUndo;
	public Button btnRedo;

	public Label lblMemory;
	public Label lblStartedMatrixCount;

	private Stage stage;
	private Parent pane;

	private Main model;
	private Settings settings;

	private volatile boolean starting = true;

	private RunnerScheduler runnerScheduler;

	private BorderPane configurationPanel;
	private double position = INIT_VALUE;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert matrixStop != null : "fx:id=\"matrixStop\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileLoadSystemVars != null : "fx:id=\"fileLoadSystemVars\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileLoadPlainText != null : "fx:id=\"fileLoadPlainText\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileLoadCsv != null : "fx:id=\"fileLoadCsv\" was not injected: check your FXML file 'tool.fxml'.";
		assert menuFile != null : "fx:id=\"menuFile\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileNewConfiguration != null : "fx:id=\"fileNewConfiguration\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileNewMatrix != null : "fx:id=\"fileNewMatrix\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileLoadConfiguration != null : "fx:id=\"fileLoadConfiguration\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileLoadDictionary != null : "fx:id=\"fileLoadDictionary\" was not injected: check your FXML file 'tool.fxml'.";
		assert matrixStart != null : "fx:id=\"matrixStart\" was not injected: check your FXML file 'tool.fxml'.";
		assert helpAboutProgram != null : "fx:id=\"helpAboutProgram\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileLoadMatrix != null : "fx:id=\"fileLoadMatrix\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileNewSystemVars != null : "fx:id=\"fileNewSystemVars\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileNewPlainText != null : "fx:id=\"fileNewPlainText\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileNewCsv != null : "fx:id=\"fileNewCsv\" was not injected: check your FXML file 'tool.fxml'.";
		assert progressBar != null : "fx:id=\"progressBar\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileLastOpenMatrix != null : "fx:id=\"fileLastOpenMatrix\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileClose != null : "fx:id=\"fileClose\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileNewDictionary != null : "fx:id=\"fileNewDictionary\" was not injected: check your FXML file 'tool.fxml'.";
		assert btnSaveDocument != null : "fx:id=\"btnSaveDocument\" was not injected: check your FXML file 'tool.fxml'.";
		assert btnSaveAsDocument != null : "fx:id=\"btnSaveAsDocument\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileSave != null : "fx:id=\"fileSave\" was not injected: check your FXML file 'tool.fxml'.";
		assert fileSaveAs != null : "fx:id=\"fileSaveAs\" was not injected: check your FXML file 'tool.fxml'.";
		assert menuEdit != null : "fx:id=\"menuEdit\" was not injected: check your FXML file 'tool.fxml'.";
		assert menuHelp != null : "fx:id=\"menuHelp\" was not injected: check your FXML file 'tool.fxml'.";
		assert tabPane != null : "fx:id=\"tabPane\" was not injected: check your FXML file 'tool.fxml'.";

		progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		progressBar.setVisible(false);

		// TODO crutch
		if (VersionInfo.getVersion().contains("local"))
		{
			//initialize new panel
			SplitPane splitPane = new SplitPane();
			splitPane.setOrientation(Orientation.HORIZONTAL);
			splitPane.setDividerPositions(0.0);

			GridPane gridPane = new GridPane();
			gridPane.setMinWidth(20.0);
			ColumnConstraints c0 = new ColumnConstraints();
			c0.setHgrow(Priority.SOMETIMES);
			c0.setMaxWidth(30.0);
			c0.setMinWidth(30.0);
			c0.setPrefWidth(30.0);
			ColumnConstraints c1 = new ColumnConstraints();
			c1.setMinWidth(10.0);
			c1.setPrefWidth(100.0);
			c1.setHgrow(Priority.SOMETIMES);
			RowConstraints r0 = new RowConstraints();
			r0.setMinHeight(10.0);
			r0.setPrefHeight(30);
			r0.setVgrow(Priority.SOMETIMES);
			gridPane.getRowConstraints().add(r0);
			gridPane.getColumnConstraints().addAll(c0, c1);

			Label project = new Label("Project");
			project.setOnMouseClicked(event -> 
			{
				double currentPosition = splitPane.getDividerPositions()[0];
				if (currentPosition < MIN_VALUE)
				{
					splitPane.setDividerPositions(this.position);
				}
				else
				{
					this.position = currentPosition;
					splitPane.setDividerPositions(0.0);
				}
			});
			gridPane.add(project, 0, 0);

			project.setRotate(-90.0);
			GridPane.setValignment(project, VPos.TOP);
			GridPane.setMargin(project, new Insets(20, 0, 0, -15.0));

			Node node = this.mainPanel.getCenter();

			splitPane.getItems().addAll(gridPane, node);

			this.configurationPanel = new BorderPane();
			this.configurationPanel.setMinWidth(0.0);
			gridPane.add(this.configurationPanel, 1, 0);

			this.mainPanel.setCenter(splitPane);
		}
		
		listeners();

		Common.setTabPane(tabPane);
		Common.setProgressBar(progressBar);
	}

	public void reloadTab(ActionEvent actionEvent)
	{
		Common.tryCatch(((CustomTab) this.tabPane.getSelectionModel().getSelectedItem())::reload, "Error on reload current tab");
	}

	public void close()
	{
		this.starting = false;
		this.stage.close();
	}

	private void initializeButtons(final Settings settings)
	{
		Platform.runLater(() -> Common.tryCatch(() -> {
			btnSaveAsDocument.setTooltip(new Tooltip("Save as"));
			btnSaveDocument.setTooltip(new Tooltip("Save"));
			btnOpenMatrix.setTooltip(new Tooltip("Open matrix"));
			btnNewMatrix.setTooltip(new Tooltip("New matrix"));
			btnOpenMainLog.setTooltip(new Tooltip("Show log"));
			btnShowCalculator.setTooltip(new Tooltip("Show calculator\n"));
			btnUndo.setTooltip(new Tooltip("Undo\n" + Common.getShortcutTooltip(settings, SettingsPanel.UNDO)));
			btnRedo.setTooltip(new Tooltip("Redo\n" + Common.getShortcutTooltip(settings, SettingsPanel.REDO)));

			Common.customizeLabeled(btnSaveAsDocument, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.DOCUMENT_SAVE_AS_ICON);
			Common.customizeLabeled(btnSaveDocument, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.DOCUMENT_SAVE_ICON);
			Common.customizeLabeled(btnOpenMatrix, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.LOAD_MATRIX_ICON);
			Common.customizeLabeled(btnNewMatrix, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.NEW_MATRIX_ICON);
			Common.customizeLabeled(btnOpenMainLog, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.LOG);
			Common.customizeLabeled(btnShowCalculator, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.CALCULATOR_ICON);
			Common.customizeLabeled(btnUndo, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.UNDO_ICON);
			Common.customizeLabeled(btnRedo, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.REDO_ICON);

			editUndo.setGraphic(new ImageView(new Image(CssVariables.Icons.UNDO_ICON_SMALL)));
			editRedo.setGraphic(new ImageView(new Image(CssVariables.Icons.REDO_ICON_SMALL)));
			matrixSchedule.setGraphic(new ImageView(new Image(CssVariables.Icons.SCHEDULER_MATRIX_ICON)));

			viewSettings.setGraphic(new ImageView(new Image(CssVariables.Icons.SHOW_SETTINGS_ICON)));
			helpActionsHelp.setGraphic(new ImageView(new Image(CssVariables.Icons.ACTIONS_HELP_ICON)));
			helpAboutProgram.setGraphic(new ImageView(new Image(CssVariables.Icons.ABOUT_PROGRAM_ICON)));
		}, "Error on set tooltips or images"));
	}

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	public void selectConfig()
	{
		if (this.tabPane != null && !this.tabPane.getTabs().isEmpty())
		{
			this.tabPane.getSelectionModel().clearSelection();
			this.tabPane.getSelectionModel().select(0);
		}
	}

	public void display()
	{
		Scene scene = new Scene(this.pane, PANE_WIDTH, PANE_HEIGHT);

		scene.getStylesheets().addAll(Common.currentTheme().getPath());
		this.stage.setScene(scene);
		SettingsValue value = settings.getValueOrDefault(Settings.GLOBAL_NS, SettingsPanel.SETTINGS, Main.USE_FULL_SCREEN, "false");
		this.stage.setFullScreen(Boolean.parseBoolean(value.getValue()));
		this.stage.show();
	}

	public void init(Main model, Settings settings, Stage stage, RunnerScheduler runnerListener)
	{
		this.model = model;
		this.settings = settings;
		this.stage = stage;
		this.runnerScheduler = runnerListener;
		this.stage.setOnCloseRequest(windowEvent -> 
		{
			if (!this.model.closeApplication())
			{
				windowEvent.consume();
			}
		});
		this.stage.setTitle(Configuration.projectName);
		this.stage.setMinHeight(600);
		this.stage.setMinWidth(600);
		this.stage.getIcons().add(new Image(CssVariables.Icons.MAIN_ICON));
		initializeButtons(settings);
	}

	public void displayTitle(String title)
	{
		this.stage.setTitle(title);
	}

	public Document currentDocument()
	{
		Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();
		
		if (selectedTab instanceof CustomTab)
		{
			return ((CustomTab)selectedTab).getDocument();
		}
		return null;
	}

	// ====================================================
	// Configuration
	// ====================================================
	public void loadConfiguration(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.loadConfiguration(null), "Error on load configuration");
	}

	public void newConfiguration(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::newConfiguration, "Error on create new configuration");
	}

	public void loadConfiguration2(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.loadConfiguration2(null), "Error on load configuration");
	}

	public void newConfiguration2(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::newConfiguration2, "Error on create new configuration");
	}

	// ====================================================
	// Dictionary
	// ====================================================
	public void loadDictionary(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadDictionary(null, null), "Error on load dictionary");
	}

	public void newDictionary(ActionEvent event)
	{
		Common.tryCatch(this.model::newDictionary, "Error on create new dictionary");
	}

	// ====================================================
	// SystemVars
	// ====================================================
	public void loadSystemVars(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadSystemVars(null), "Error on load system vars");
	}

	public void newSystemVars(ActionEvent event)
	{
		Common.tryCatch(this.model::newSystemVars, "Error on create new system vars");
	}

	// ====================================================
	// Matrix
	// ====================================================
	public void loadMatrix(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadMatrix(null), "Error on load matrix");
	}

	public void newMatrix(ActionEvent event)
	{
		Common.tryCatch(this.model::newMatrix, "Error on create new matrix");
	}

	public void startMatrix(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.startMatrix(currentDocument()), "Error on start matrix");
	}

	public void stopMatrix(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.stopMatrix(currentDocument()), "Error on stop matrix");
	}

	public void matrixSchedule(ActionEvent event)
	{
		Common.tryCatch(() -> this.runnerScheduler.show(this.tabPane.getScene().getWindow()), "Error on schedule");
	}

	public void runFromFile(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::runMatrixFromFile, "Error on run matrix from file");
	}

	public void openReport(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::openReport, "Error on open report");
	}

	// ====================================================
	// PlainText
	// ====================================================
	public void loadPlainText(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadPlainText(null), "Error on load plain text");
	}

	public void newPlainText(ActionEvent event)
	{
		Common.tryCatch(this.model::newPlainText, "Error on create new plain text");
	}


	// ====================================================
	// Csv
	// ====================================================
	public void loadCsv(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadCsv(null), "Error on load csv file");
	}

	public void newCsv(ActionEvent event)
	{
		Common.tryCatch(this.model::newCsv, "Error on create new csv file");
	}


	// ====================================================
	// Document
	// ====================================================
	public void saveAsDocument(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.documentSaveAs(currentDocument()), "Error on save document as");
	}

	public void saveDocument(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.documentSave(currentDocument()), "Error on save document");
	}

	public void saveAll(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::documentsSaveAll, "Error on save all");
	}

	public void undo(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.undo(currentDocument()), "Error on save document");
	}

	public void redo(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.redo(currentDocument()), "Error on save document");
	}

	// ====================================================
	// other events
	// ====================================================
	public void showStore(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::store, "Error on show store");
	}

	public void showAllTabs(ActionEvent actionEvent)
	{
		Common.tryCatch(this::showAllTabs, "Error on show all tabs");
	}

	public void closeApplication(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::closeApplication, "Error on close application");
	}

	public void showSettingsPanel(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> new SettingsPanel(this.settings).show(), "Error on show settings panel");
	}

	public void showAboutProgram(ActionEvent actionEvent)
	{
		Common.tryCatch(DialogsHelper::showAboutProgram, "Error on show about program");
	}

	public void showActionsHelp(ActionEvent event)
	{
		Common.tryCatch(DialogsHelper::showActionsHelp, "Error on show actions panel");
	}

	public void showCalculator(ActionEvent event)
	{
		Common.tryCatch(model::showCalculator, "Error on show calculator");
	}

	public void showCalculator(AbstractEvaluator evaluator)
	{
		DialogsHelper.showHelperDialog("<none>", evaluator, "'Helper'", null);
	}

	public void updateStatusBar(final int i)
	{
		// TODO not used
		Platform.runLater(() -> {
			if (i == 0)
			{
				if (!lblStartedMatrixCount.getText().equals(""))
				{
					lblStartedMatrixCount.setText("");
				}
			}
			else
			{
				lblStartedMatrixCount.setText("Running matrix count : " + i);
			}
		});
	}

	// TODO remake shortcuts over Menu.setAccelerator()
	public void initShortcuts()
	{
		setStatusBar();
		btnSaveAsDocument.getScene().addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> Common.tryCatch(() -> {
			if (keyEvent.getCode() == KeyCode.UNDEFINED)
			{
				return;
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.SHOW_ALL_TABS))
			{
				showAllTabs();
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.SAVE_DOCUMENT))
			{
				saveDocument(null);
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.UNDO))
			{
				undo(null);
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.REDO))
			{
				redo(null);
			}
		}, "Error on set Shortcuts"));
	}

	public void openMainLog(ActionEvent actionEvent)
	{
		openLogs();
	}

	public void updateFileLastMatrix(Collection<SettingsValue> list)
	{
		fileLastOpenMatrix.getItems().clear();
		list.forEach(lastMatrix -> 
		{
			MenuItem menuItem = new MenuItem(lastMatrix.getKey());
			menuItem.setMnemonicParsing(false);
			final File file = new File(lastMatrix.getValue());
			menuItem.setOnAction(actionEvent -> Common.tryCatch(() -> {
				try
				{
					model.loadMatrix(file.getAbsolutePath());
				}
				catch (FileNotFoundException e)
				{
					this.model.removeMatrixFromSettings(lastMatrix.getKey());
				}

			}, "Error on load matrix"));
			fileLastOpenMatrix.getItems().add(menuItem);
		});
		fileLastOpenMatrix.getItems().add(new SeparatorMenuItem());
		MenuItem clearList = new MenuItem("Clear list");
		clearList.setOnAction(actionEvent -> Common.tryCatch(model::clearFileLastOpenMatrix, "Error on clear list"));
		fileLastOpenMatrix.getItems().add(clearList);
	}

	public void disableMenu(boolean flag)
	{
		fileLoad.setDisable(flag);
		fileNew.setDisable(flag);
		fileSave.setDisable(flag);
		fileSaveAs.setDisable(flag);
		fileSaveAll.setDisable(flag);
		fileLastOpenMatrix.setDisable(flag);
		viewStore.setDisable(flag);
		menuEdit.setDisable(flag);
		menuMatrix.setDisable(flag);
		fileRunFromFile.setDisable(flag);
	}

	public void clearLastMatrixMenu()
	{
		fileLastOpenMatrix.getItems().clear();
	}

	public boolean checkFile(File file)
	{
		if (file == null)
		{
			return false;
		}
		Optional<Tab> first = this.tabPane.getTabs().stream().filter(f -> {
			Document document = ((CustomTab) f).getDocument();
			return Str.areEqual(new File(document.getName()).getAbsolutePath(), file.getAbsolutePath());
		}).findFirst();
		first.ifPresent(this.tabPane.getSelectionModel()::select);
		return first.isPresent();
	}
	// ============================================================
	// private methods
	// ============================================================
	private boolean checkTab()
	{
		return tabPane.getSelectionModel().getSelectedItem() != null;
	}

	private void listeners()
	{
		lblStartedMatrixCount.setOnMouseClicked(mouseEvent -> matrixSchedule(null));
		this.tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null)
			{
				this.model.changeDocument(null);
			}
			else
			{
				this.model.changeDocument(((CustomTab) newValue).getDocument());
			}
			if (oldValue != null)
			{
				CustomTab tab = (CustomTab) oldValue;
				if (tab.getDocument() instanceof MatrixFx)
				{
					Node lookup = tab.getContent().lookup(".customTreeTableView");
					if (lookup instanceof MatrixTreeView)
					{
						((MatrixTreeView) lookup).getSelectionModel().clearSelection();
						((MatrixTreeView) lookup).getFocusModel().focus(-1);
					}
				}
			}
		});
	}

	private void openLogs()
	{
		Common.tryCatch(() -> {
			if (this.log == null)
			{
				this.log = new LogsFx(this.settings);
			}
			log.show();
		}, "Error on open log");
	}

	private void showAllTabs()
	{
		ListView<String> listView = new ListView<>();
		listView.getItems().addAll(tabPane.getTabs().stream().map(tab -> ((CustomTab) tab).getTitle()).collect(Collectors.toList()));
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		dialog.getDialogPane().setContent(listView);
		dialog.getDialogPane().setPrefHeight(500);
		dialog.getDialogPane().setPrefWidth(400);
		dialog.setHeaderText("Choose tab");

		listView.setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getClickCount() == 2)
			{
				selectAndHide(listView, dialog);
			}
		});

		listView.setOnKeyPressed(keyEvent -> {
			if (keyEvent.getCode() == KeyCode.ENTER)
			{
				selectAndHide(listView, dialog);
			}
		});
		dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		buttonType.filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)).ifPresent(type -> selectAndHide(listView, dialog));
	}

	private void selectAndHide(ListView<String> listView, Dialog<?> dialog)
	{
		String selectedItem = listView.getSelectionModel().getSelectedItem();
		tabPane.getTabs().stream().filter(tab -> ((CustomTab) tab).getTitle().equals(selectedItem)).findFirst().ifPresent(tabPane.getSelectionModel()::select);
		dialog.hide();
	}

	private long getMBytes(long l)
	{
		return l / 1024 / 1024;
	}

	private void setStatusBar()
	{
		Runnable runnable = () -> {
			while (this.starting)
			{
				try
				{
					Platform.runLater(() -> lblMemory.setText(getMBytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "MB of " + getMBytes(Runtime.getRuntime().maxMemory()) + "MB"));

					Thread.sleep(1000);
				}
				catch (InterruptedException ie)
				{
					break;
				}
				catch (Exception e)
				{
					logger.error("Error on update RAM");
					logger.error(e.getMessage(), e);
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.setName("Status bar thread");
		thread.setDaemon(true);
		thread.start();
	}
}
