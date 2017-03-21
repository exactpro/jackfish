////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.main;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.Settings.SettingsValue;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.logs.LogsFx;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.tab.CustomTabPane;
import com.exactprosystems.jf.tool.custom.treetable.MatrixTreeView;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.schedule.RunnerScheduler;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainController implements Initializable, ContainingParent
{
	private final static int	PANE_WIDTH	= 800;
	private final static int	PANE_HEIGHT	= 600;

	private final static double	INIT_VALUE	= 0.15;
	private final static double	MIN_VALUE	= 0.05;

	private static final Logger	logger		= Logger.getLogger(MainController.class);

	private CustomTabPane		customTabPane;

	public BorderPane			projectPane;
	public ProgressBar			progressBar;
	public Label				progressLabel;
	public Label				projectLabel;
	public SplitPane			splitPane;
	public GridPane				projectGridPane;
	public ToolBar				mainToolbar;
	private LogsFx				log;

	public Menu					menuFile;

	public Menu					fileLoad;

	public Menu					fileNew;

	public MenuItem				fileSave;
	public MenuItem				fileSaveAs;
	public MenuItem				fileSaveAll;

	public Menu					fileLastOpenMatrix;
	public MenuItem				fileRunFromFile;
	public Menu					menuEdit;
	public MenuItem				editUndo;
	public MenuItem				editRedo;

	public MenuItem editSettings;
	public MenuItem				viewStore;
	public MenuItem viewShowTabs;

	public Menu					menuMatrix;
	public MenuItem				matrixSchedule;
	public MenuItem matrixStart;
	public MenuItem matrixStop;

	public MenuItem				gitCommit;
	public MenuItem				gitPush;
	public MenuItem				gitPull;
	public MenuItem				gitReset;
	public MenuItem				gitStatus;
	public MenuItem				gitMerge;
	public MenuItem				gitBranches;
	public MenuItem gitTags;
	public MenuItem				gitChangeCredential;

	public MenuItem				helpActionsHelp;

	public MenuItem				helpAboutProgram;

	public Button				btnOpenMatrix;
	public Button				btnNewMatrix;
	public Button				btnSaveDocument;
	public Button				btnSaveAsDocument;
	public Button				btnOpenMainLog;
	public Button				btnShowCalculator;
	public Button				btnUndo;
	public Button				btnRedo;

	public Label				lblMemory;

	private Stage				stage;
	private Parent				pane;

	private DocumentFactory 	factory;
	private Main				model;
	private Settings			settings;

	private volatile boolean	starting	= true;

	private RunnerScheduler		runnerScheduler;

	private double				position	= INIT_VALUE;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
		progressBar.setVisible(false);
		SplitPane.setResizableWithParent(this.projectGridPane, false);
		this.customTabPane = CustomTabPane.getInstance();
		this.customTabPane.setMinHeight(200.0);
		this.customTabPane.setPrefHeight(629.0);
		this.customTabPane.setPrefWidth(1280.0);
		this.splitPane.getItems().add(this.customTabPane);

		Common.setProgressBar(progressBar);

		listeners();
	}

	//region Action events
	public void reloadConfiguration(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.refreshConfig(), "Error on refresh configuration");
	}

	public void saveConfiguration(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.saveConfig(), "Error on save config");
	}
	//endregion

	public void close()
	{
		this.starting = false;
		this.stage.close();
	}

	private void initializeButtons(final Settings settings)
	{
		Platform.runLater(() -> Common.tryCatch(() ->
		{
			this.btnSaveAsDocument.setTooltip(new Tooltip("Save as\n"+shortcutsName(Settings.SAVE_DOCUMENT_AS)));
			this.btnSaveDocument.setTooltip(new Tooltip("Save\n"+shortcutsName(Settings.SAVE_DOCUMENT)));
			this.btnOpenMatrix.setTooltip(new Tooltip("Open matrix"));
			this.btnNewMatrix.setTooltip(new Tooltip("New matrix"));
			this.btnOpenMainLog.setTooltip(new Tooltip("Show log"));
			this.btnShowCalculator.setTooltip(new Tooltip("Show calculator\n"));
			this.btnUndo.setTooltip(new Tooltip("Undo\n" + shortcutsName(Settings.UNDO)));
			this.btnRedo.setTooltip(new Tooltip("Redo\n" + shortcutsName(Settings.REDO)));

			this.editUndo.setGraphic(new ImageView(new Image(CssVariables.Icons.UNDO_ICON_SMALL)));
			this.editUndo.setAccelerator(Common.getShortcut(settings, Settings.UNDO));

			this.editRedo.setGraphic(new ImageView(new Image(CssVariables.Icons.REDO_ICON_SMALL)));
			this.editRedo.setAccelerator(Common.getShortcut(settings, Settings.REDO));

			this.matrixSchedule.setGraphic(new ImageView(new Image(CssVariables.Icons.SCHEDULER_MATRIX_ICON)));

			this.editSettings.setGraphic(new ImageView(new Image(CssVariables.Icons.SHOW_SETTINGS_ICON)));
			this.helpActionsHelp.setGraphic(new ImageView(new Image(CssVariables.Icons.ACTIONS_HELP_ICON)));
			this.helpAboutProgram.setGraphic(new ImageView(new Image(CssVariables.Icons.ABOUT_PROGRAM_ICON)));

			this.fileSave.setAccelerator(Common.getShortcut(settings, Settings.SAVE_DOCUMENT));
			this.fileSaveAs.setAccelerator(Common.getShortcut(settings, Settings.SAVE_DOCUMENT_AS));

			this.viewShowTabs.setAccelerator(Common.getShortcut(settings, Settings.SHOW_ALL_TABS));

			this.matrixStart.setAccelerator(Common.getShortcut(settings, Settings.START_MATRIX));
			this.matrixStop.setAccelerator(Common.getShortcut(settings, Settings.STOP_MATRIX));
		}, "Error on set tooltips or images"));
	}

	private String shortcutsName(String shortName)
	{
		return Stream.of(Common.getShortcut(settings, shortName))
				.filter(Objects::nonNull)
				.map(KeyCombination::toString)
				.findFirst()
				.orElse("");
	}

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	public void display()
	{
		Scene scene = new Scene(this.pane, PANE_WIDTH, PANE_HEIGHT);
		scene.getAccelerators().addListener((MapChangeListener<KeyCombination, Runnable>) change ->
		{
			this.settings.getRemovedShortcuts()
					.stream()
					.filter(key -> change.getKey().equals(key))
					.findFirst()
					.ifPresent(scene.getAccelerators()::remove);
		});
		scene.getStylesheets().addAll(Common.currentThemesPaths());
		this.stage.setScene(scene);
		SettingsValue value = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.USE_FULL_SCREEN, "false");
		this.stage.setFullScreen(Boolean.parseBoolean(value.getValue()));
		this.model.changeDocument(null);
		this.stage.show();
	}

	public void init(DocumentFactory factory, Main model, Settings settings, Stage stage)
	{
		this.factory = factory;
		this.model = model;
		this.settings = settings;
		this.stage = stage;
		this.runnerScheduler = (RunnerScheduler) factory.getRunnerListener();
		CustomTabPane.getInstance().setSettings(this.settings);
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
		Common.addIcons(this.stage);
		initializeButtons(settings);
	}

	public void displayTitle(String title)
	{
		this.stage.setTitle(title);
	}

	public Document currentDocument()
	{
		Tab selectedTab = this.customTabPane.getSelectionModel().getSelectedItem();

		if (selectedTab instanceof CustomTab)
		{
			return ((CustomTab) selectedTab).getDocument();
		}
		return null;
	}

	//region Configuration
	public void openProject(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.openProject(null, this.projectPane), "Error on load configuration");
	}

	public void createProject(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.createNewProject(this.projectPane), "Error on create new configuration");
	}

	public void projectFromGit(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.projectFromGit(this.projectPane), "Error on clone project from git");
	}
	//endregion

	//region Dictionary
	public void loadDictionary(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadDictionary(null, null), "Error on load dictionary");
	}

	public void newDictionary(ActionEvent event)
	{
		Common.tryCatch(this.model::newDictionary, "Error on create new dictionary");
	}
	//endregion

	//region System vars
	public void loadSystemVars(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadSystemVars(null), "Error on load system vars");
	}

	public void newSystemVars(ActionEvent event)
	{
		Common.tryCatch(this.model::newSystemVars, "Error on create new system vars");
	}
	//endregion

	//region Matrix
	public void loadMatrix(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadMatrix(null), "Error on load matrix");
	}

	public void newMatrix(ActionEvent event)
	{
		Common.tryCatch(this.model::newMatrix, "Error on create new matrix");
	}

	public void newLibrary(ActionEvent event)
	{
		Common.tryCatch(this.model::newLibrary, "Error on create new matrix");
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
		Common.tryCatch(() -> this.runnerScheduler.show(this.customTabPane.getScene().getWindow()), "Error on schedule");
	}

	public void runFromFile(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::runMatrixFromFile, "Error on run matrix from file");
	}

	public void openReport(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::openReport, "Error on open report");
	}

	public void addToToolbar(String fullPath)
	{
		SplitMenuButton menuButton = new SplitMenuButton();
		menuButton.setTooltip(new Tooltip(fullPath));
		menuButton.setId("splitMenuButtonToolbar");
		menuButton.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);

		menuButton.setOnAction(e -> Common.tryCatch(() -> this.model.runMatrixFromFile(new File(fullPath)), "Error on start matrix"));

		MenuItem remove = new MenuItem("Remove");
		remove.setOnAction(e -> {
			Common.tryCatch(() -> this.model.removeFromToolbar(fullPath), "Error on remove from toolbar");
			this.mainToolbar.getItems().remove(menuButton);
		});

		MenuItem open = new MenuItem("Open");
		open.setOnAction(e -> Common.tryCatch(() -> this.model.loadMatrix(fullPath), "Error on open matrix"));

		menuButton.getItems().addAll(remove, open);
		this.mainToolbar.getItems().add(menuButton);
	}
	//endregion

	//region Plain text
	public void loadPlainText(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadPlainText(null), "Error on load plain text");
	}

	public void newPlainText(ActionEvent event)
	{
		Common.tryCatch(this.model::newPlainText, "Error on create new plain text");
	}
	//endregion

	//region Csv
	public void loadCsv(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadCsv(null), "Error on load csv file");
	}

	public void newCsv(ActionEvent event)
	{
		Common.tryCatch(this.model::newCsv, "Error on create new csv file");
	}
	//endregion

	//region Document
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
		Common.tryCatch(() -> this.model.undo(currentDocument()), "Error on undo document");
	}

	public void redo(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.redo(currentDocument()), "Error on redo document");
	}
	//endregion

	//region other events
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
		Common.tryCatch(() -> DialogsHelper.showActionsHelp(factory), "Error on show actions panel");
	}

	public void showCalculator(ActionEvent event)
	{
		Common.tryCatch(model::showCalculator, "Error on show calculator");
	}

	public void showCalculator(AbstractEvaluator evaluator)
	{
		DialogsHelper.showHelperDialog("<none>", evaluator, "'Helper'", null);
	}
	//endregion

	//region Git
	public void gitStatus(ActionEvent event)
	{
		Common.tryCatch(this.model::gitStatus, "Error on show status");
	}

	public void gitMerge(ActionEvent event)
	{
		Common.tryCatch(this.model::gitMerge, "Error on merge");
	}

	public void gitBranches(ActionEvent event)
	{
		Common.tryCatch(this.model::gitBranches, "Error on show branches");
	}

	public void gitTags(ActionEvent event)
	{
		Common.tryCatch(this.model::gitTags, "Error on show tags");
	}

	public void gitChangeCredential(ActionEvent event)
	{
		Common.tryCatch(this.model::changeCredential, "Error on show status");
	}

	public void gitClone(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.projectFromGit(this.projectPane), "Error on clone repository");
	}

	public void gitCommit(ActionEvent event)
	{
		Common.tryCatch(this.model::gitCommit, "Error on commit");
	}

	public void gitPush(ActionEvent event)
	{
		Common.tryCatch(this.model::gitPush, "Error on push");
	}

	public void gitPull(ActionEvent event)
	{
		Common.tryCatch(this.model::gitPull, "Error on pull");
	}

	public void gitReset(ActionEvent event)
	{
		Common.tryCatch(this.model::gitReset, "Error on reset");
	}
	//endregion

	//region progress tasks
	public void startTask(String title)
	{
		Platform.runLater(() -> {
			this.progressBar.setVisible(true);
			this.progressLabel.setText(title);
		});
	}

	public void updateTask(String title)
	{
		Platform.runLater(() -> {
			if (!this.progressLabel.getText().isEmpty())
			{
				this.progressLabel.setText(title);
			}
		});
	}

	public void endTask()
	{
		Platform.runLater(() -> {
			this.progressBar.setVisible(false);
			this.progressLabel.setText("");
		});
	}
	//endregion

	// TODO remake shortcuts over Menu.setAccelerator()
	public void initShortcuts()
	{
		setStatusBar();
		btnSaveAsDocument.getScene().addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> Common.tryCatch(() ->
		{
			if (keyEvent.getCode() == KeyCode.UNDEFINED)
			{
				return;
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.SHOW_ALL_TABS))
			{
				showAllTabs();
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.SAVE_DOCUMENT))
			{
				saveDocument(null);
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.SAVE_DOCUMENT_AS))
			{
				saveAsDocument(null);
			}
			else
			{
				if (SettingsPanel.match(settings, keyEvent, Settings.UNDO))
				{
					if (!(keyEvent.getTarget() instanceof TextInputControl))
					{
						undo(null);
					}
				}
				else if (SettingsPanel.match(settings, keyEvent, Settings.REDO))
				{
					if (!(keyEvent.getTarget() instanceof TextInputControl))
					{
						redo(null);
					}
				}
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
			menuItem.setOnAction(actionEvent -> Common.tryCatch(() ->
			{
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
		this.fileLoad.setDisable(flag);
		this.fileNew.setDisable(flag);
		this.fileSave.setDisable(flag);
		this.fileSaveAs.setDisable(flag);
		this.fileSaveAll.setDisable(flag);
		this.fileLastOpenMatrix.setDisable(flag);
		this.viewStore.setDisable(flag);
		this.menuEdit.setDisable(flag);
		this.menuMatrix.setDisable(flag);
		this.fileRunFromFile.setDisable(flag);
	}

	public void isGit(boolean flag)
	{
		gitCommit.setDisable(!flag);
		gitPush.setDisable(!flag);
		gitPull.setDisable(!flag);
		gitReset.setDisable(!flag);
		gitStatus.setDisable(!flag);
		gitMerge.setDisable(!flag);
		gitTags.setDisable(!flag);
		gitBranches.setDisable(!flag);
		gitChangeCredential.setDisable(!flag);
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
		Optional<Tab> first = this.customTabPane.getTabs().stream().filter(f ->
		{
			Document document = ((CustomTab) f).getDocument();
			return Str.areEqual(new File(document.getName()).getAbsolutePath(), file.getAbsolutePath());
		}).findFirst();
		first.ifPresent(this.customTabPane.getSelectionModel()::select);
		return first.isPresent();
	}

	Dimension getDimension()
	{
		return new Dimension((int) this.stage.getWidth(), (int) this.stage.getHeight());
	}

	Point getPosition()
	{
		return new Point((int) this.stage.getX(), (int) this.stage.getY());
	}

	//region Private methods
	private void listeners()
	{
		this.customTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue == null)
			{
				this.model.changeDocument(null);
			}
			else
			{
				if (newValue instanceof CustomTab)
				{
					CustomTab newCustomTab = (CustomTab) newValue;
					this.model.changeDocument(newCustomTab.getDocument());
					if (newCustomTab.getDocument() instanceof MatrixFx)
					{
						Node lookup = newCustomTab.getContent().lookup("."+CssVariables.CUSTOM_TREE_TABLE_VIEW);
						if (lookup instanceof MatrixTreeView)
						{
							Common.setFocused(lookup);
						}
					}
				}
			}
		});

		this.projectLabel.setOnMouseClicked(event ->
		{
			double currentPosition = this.splitPane.getDividerPositions()[0];
			if (currentPosition < MIN_VALUE)
			{
				this.splitPane.setDividerPositions(this.position);
			}
			else
			{
				this.position = currentPosition;
				this.splitPane.setDividerPositions(0.0);
			}
		});
	}

	private void openLogs()
	{
		Common.tryCatch(() ->
		{
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
		listView.getItems().addAll(this.customTabPane.getTabs().stream().map(tab -> ((CustomTab) tab).getTitle()).collect(Collectors.toList()));
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.getDialogPane().setContent(listView);
		dialog.getDialogPane().setPrefHeight(400);
		dialog.getDialogPane().setPrefWidth(432);
		dialog.getDialogPane().setPadding(new Insets(0,0,0,-11));
		dialog.setHeaderText("");
		dialog.setTitle("Choose tab");

		listView.setOnMouseClicked(mouseEvent ->
		{
			if (mouseEvent.getClickCount() == 2)
			{
				selectAndHide(listView, dialog);
			}
		});

		listView.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER)
			{
				selectAndHide(listView, dialog);
			}
		});
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> buttonType = dialog.showAndWait();
		buttonType.filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)).ifPresent(type -> selectAndHide(listView, dialog));
	}

	private void selectAndHide(ListView<String> listView, Dialog<?> dialog)
	{
		String selectedItem = listView.getSelectionModel().getSelectedItem();
		this.customTabPane.getTabs().stream().filter(tab -> ((CustomTab) tab).getTitle().equals(selectedItem)).findFirst()
				.ifPresent(this.customTabPane.getSelectionModel()::select);
		dialog.hide();
	}

	private long getMBytes(long l)
	{
		return l / 1024 / 1024;
	}

	private void setStatusBar()
	{
		Runnable runnable = () ->
		{
			while (this.starting)
			{
				try
				{
					Platform.runLater(() -> lblMemory.setText(getMBytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "MB of "
							+ getMBytes(Runtime.getRuntime().maxMemory()) + "MB"));

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
	//endregion
}
