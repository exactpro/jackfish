////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.main;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.Settings.SettingsValue;
import com.exactprosystems.jf.common.documentation.DocumentationBuilder;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ContextHelpFactory;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.logs.LogsFx;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.tab.CustomTabPane;
import com.exactprosystems.jf.tool.custom.treetable.MatrixTreeView;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainController implements Initializable, ContainingParent
{
	private final static int	PANE_WIDTH	= 800;
	private final static int	PANE_HEIGHT	= 600;

	private final static double	INIT_VALUE	= 0.20;
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

	public Menu menuView;
	public MenuItem viewSearch;
	public MenuItem viewStore;
	public MenuItem viewLogs;
	public MenuItem viewShowTabs;

	public Menu					menuMatrix;
	public MenuItem				matrixSchedule;
	public MenuItem matrixStart;
	public MenuItem matrixStop;
	public CheckMenuItem matrixShowWait;

	public Menu menuGit;
	public MenuItem				gitClone;
	public Menu					gitPublishing;
	public MenuItem				gitCommit;
	public MenuItem				gitPush;
	public MenuItem				gitPull;
	public MenuItem				gitReset;
	public MenuItem				gitStatus;
	public MenuItem				gitMerge;
	public MenuItem				gitBranches;
	public MenuItem gitTags;
	public MenuItem				gitChangeCredential;

	public Menu					menuHelp;
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
		Common.tryCatch(() -> this.model.refreshConfig(), R.MAIN_CONTROLLER_ERROR_ON_REFRESH_CONF.get());
	}

	public void saveConfiguration(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.saveConfig(), R.MAIN_CONTROLLER_ERROR_ON_SAVE_CONF.get());
	}
	//endregion

	public void close()
	{
		this.starting = false;
		this.stage.close();
	}

	private void initializeButtons(final Settings settings)
	{
		Common.runLater(() -> Common.tryCatch(() ->
		{
			this.btnSaveAsDocument.setTooltip(new Tooltip(MessageFormat.format(R.TOOL_BUTTON_SAVE_AS_DOCUMENT.get(), shortcutsName(Settings.SAVE_DOCUMENT_AS))));
			this.btnSaveDocument.setTooltip(new Tooltip(MessageFormat.format(R.TOOL_BUTTON_SAVE_DOCUMENT.get(), shortcutsName(Settings.SAVE_DOCUMENT))));
			this.btnOpenMatrix.setTooltip(new Tooltip(R.TOOL_BUTTON_OPEN_MATRIX.get()));
			this.btnNewMatrix.setTooltip(new Tooltip(R.TOOL_BUTTON_NEW_MATRIX.get()));
			this.btnOpenMainLog.setTooltip(new Tooltip(R.TOOL_BUTTON_OPEN_MAIN_LOG.get()));
			this.btnShowCalculator.setTooltip(new Tooltip(R.TOOL_BUTTON_SHOW_CALCULATOR.get()));
			this.btnUndo.setTooltip(new Tooltip(MessageFormat.format(R.TOOL_BUTTON_UNDO.get(), shortcutsName(Settings.UNDO))));
			this.btnRedo.setTooltip(new Tooltip(MessageFormat.format(R.TOOL_BUTTON_REDO.get(), shortcutsName(Settings.REDO))));

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
			this.viewSearch.setAccelerator(Common.getShortcut(settings, Settings.SEARCH));

			this.matrixStart.setAccelerator(Common.getShortcut(settings, Settings.START_MATRIX));
			this.matrixStop.setAccelerator(Common.getShortcut(settings, Settings.STOP_MATRIX));
		}, R.MAIN_CONTROLLER_ERROR_ON_SET_TOOLTIP.get()));
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
		scene.getStylesheets().addAll(Theme.currentThemesPaths());
		this.stage.setScene(scene);
		SettingsValue value = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.USE_FULL_SCREEN);
		this.stage.setFullScreen(Boolean.parseBoolean(value.getValue()));
		this.model.changeDocument(null);
		this.stage.show();
	}

	public void init(DocumentFactory factory, Main model, WizardManager wizardManager, Settings settings, Stage stage)
	{
		this.factory = factory;
		this.model = model;
		this.settings = settings;
		this.stage = stage;
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

		Menu wizard = new Menu(R.COMMON_WIZARD.get());
		wizard.setId("wizard");
		this.menuHelp.getItems().add(0, wizard);

		Map<WizardCategory, Menu> map = Arrays.stream(WizardCategory.values())
				.collect(Collectors.toMap(Function.identity(), v -> Stream.of(v)
						.map(WizardCategory::toString)
						.map(Menu::new)
						.peek(menu -> menu.setId(menu.getText()))
						.peek(wizard.getItems()::add)
						.findFirst()
						.orElse(null))
				);

		wizardManager.allWizards()
				.stream()
				.filter(wizardClass -> {
					WizardAttribute annotation = wizardClass.getAnnotation(WizardAttribute.class);
					return VersionInfo.isDevVersion() || annotation != null && !annotation.experimental();
				})
				.forEach(wizardClass -> {
					WizardCategory wizardCategory = wizardManager.categoryOf(wizardClass);
					String wizardName = wizardManager.nameOf(wizardClass);
					MenuItem menuItem = new MenuItem(wizardName);

					menuItem.setOnAction(e -> Common.tryCatch(() -> {
						Context context = factory.createContext();
						ReportBuilder report = new ContextHelpFactory().createReportBuilder(null, null, new Date());
						MatrixItem help = DocumentationBuilder.createHelpForWizard(report, context, wizardClass);
						DialogsHelper.showHelpDialog(context, wizardName, report, help);
					},""));

					map.get(wizardCategory).getItems().add(menuItem);
				});

		Menu menuAll = new Menu(R.COMMON_ALL.get());
		menuAll.getItems().addAll(map.values().stream()
				.map(Menu::getItems)
				.flatMap(Collection::stream)
				.map(menuItem -> {
					MenuItem newMenu = new MenuItem(menuItem.getText());
					newMenu.setOnAction(e -> menuItem.getOnAction().handle(e));
					return newMenu;
				})
				.sorted(Comparator.comparing(k -> k.getText().toLowerCase()))
				.collect(Collectors.toList())
		);
		wizard.getItems().add(menuAll);
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
		Common.tryCatch(() -> this.model.openProject(null, this.projectPane), R.MAIN_CONTROLLER_ERROR_ON_LOAD_CONF.get());
	}

	public void createProject(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.createNewProject(this.projectPane), R.MAIN_CONTROLLER_ERROR_ON_CREATE_NEW_CONF.get());
	}

	public void projectFromGit(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.projectFromGit(this.projectPane), R.MAIN_CONTROLLER_ERROR_ON_CLONE_PROJECT.get());
	}
	//endregion

	//region Dictionary
	public void loadDictionary(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadDictionary(null, null), R.MAIN_CONTROLLER_ERROR_ON_LOAD_DIC.get());
	}

	public void newDictionary(ActionEvent event)
	{
		Common.tryCatch(this.model::newDictionary, R.MAIN_CONTROLLER_ERROR_ON_CREATE_DIC.get());
	}
	//endregion

	//region System vars
	public void loadSystemVars(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadSystemVars(null), R.MAIN_CONTROLLER_ERROR_ON_LOAD_VARS.get());
	}

	public void newSystemVars(ActionEvent event)
	{
		Common.tryCatch(this.model::newSystemVars, R.MAIN_CONTROLLER_ERROR_ON_CREATE_VARS.get());
	}
	//endregion

	//region Matrix
	public void loadMatrix(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadMatrix(null), R.MAIN_CONTROLLER_ERROR_ON_LOAD_MATRIX.get());
	}

	public void newMatrix(ActionEvent event)
	{
		Common.tryCatch(this.model::newMatrix, R.MAIN_CONTROLLER_ERROR_ON_CREATE_MATRIX.get());
	}

	public void newLibrary(ActionEvent event)
	{
		Common.tryCatch(this.model::newLibrary, R.MAIN_CONTROLLER_ERROR_ON_CREATE_MATRIX.get());
	}

	public void startMatrix(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.startMatrix(currentDocument()), R.MAIN_CONTROLLER_ERROR_ON_START_MATRIX.get());
	}

	public void stopMatrix(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.stopMatrix(currentDocument()), R.MAIN_CONTROLLER_ERROR_ON_STOP_MATRIX.get());
	}

	public void matrixSchedule(ActionEvent event)
	{
		this.factory.showMatrixScheduler();
	}

	public void runFromFile(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::runMatrixFromFile, R.MAIN_CONTROLLER_ERROR_ON_RUN_MATRIX.get());
	}

	public void openReport(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::openReport, R.MAIN_CONTROLLER_ERROR_ON_OPEN_REPORT.get());
	}

	public void addToToolbar(String fullPath, String visibleName)
	{
		SplitMenuButton menuButton = new SplitMenuButton();
		menuButton.setText(visibleName);
		menuButton.setTooltip(new Tooltip(fullPath));
		menuButton.setId("splitMenuButtonToolbar");
		menuButton.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);

		menuButton.setOnAction(e -> Common.tryCatch(() -> this.model.runMatrixFromFile(new File(fullPath)), R.MAIN_CONTROLLER_ERROR_ON_START_MATRIX.get()));

		MenuItem remove = new MenuItem(R.COMMON_REMOVE.get());
		remove.setOnAction(e -> {
			Common.tryCatch(() -> this.model.removeFromToolbar(fullPath), R.MAIN_CONTROLLER_ERROR_ON_TOOLBAR.get());
			this.mainToolbar.getItems().remove(menuButton);
		});

		MenuItem open = new MenuItem(R.COMMON_OPEN.get());
		open.setOnAction(e -> Common.tryCatch(() -> this.model.loadMatrix(fullPath), R.MAIN_CONTROLLER_ERROR_ON_OPEN_MATRIX.get()));

		MenuItem rename = new MenuItem(R.COMMON_RENAME.get());
		rename.setOnAction(e -> Common.tryCatch(() ->
		{
			Optional<String> newName = DialogsHelper.showInputDialog(R.MAIN_CONTROLLER_ENTER_NAME.get(), visibleName);
			if (newName.isPresent())
			{
				this.model.renameFromToolbar(fullPath, newName.get());
				menuButton.setText(newName.get());
			}
		}, R.MAIN_CONTROLLER_ERROR_ON_RENAME.get()));

		menuButton.getItems().addAll(remove, open, rename);
		this.mainToolbar.getItems().add(menuButton);
	}
	//endregion

	//region Plain text
	public void loadPlainText(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadPlainText(null), R.MAIN_CONTROLLER_ERROR_ON_LOAD_PLAIN.get());
	}

	public void newPlainText(ActionEvent event)
	{
		Common.tryCatch(this.model::newPlainText, R.MAIN_CONTROLLER_ERROR_ON_CREATE_PLAIN.get());
	}
	//endregion

	//region Csv
	public void loadCsv(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.loadCsv(null), R.MAIN_CONTROLLER_ERROR_ON_LOAD_CSV.get());
	}

	public void newCsv(ActionEvent event)
	{
		Common.tryCatch(this.model::newCsv, R.MAIN_CONTROLLER_ERROR_ON_CREATE_CSV.get());
	}
	//endregion

	//region Document
	public void saveAsDocument(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.documentSaveAs(currentDocument()), R.MAIN_CONTROLLER_ERROR_ON_SAVE_DOC_AS.get());
	}

	public void saveDocument(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.documentSave(currentDocument()), R.MAIN_CONTROLLER_ERROR_ON_SAVE_DOC.get());
	}

	public void saveAll(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::documentsSaveAll, R.MAIN_CONTROLLER_ERROR_ON_SAVE_ALL.get());
	}

	public void undo(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.undo(currentDocument()), R.MAIN_CONTROLLER_ERROR_ON_UNDO.get());
	}

	public void redo(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.redo(currentDocument()), R.MAIN_CONTROLLER_ERROR_ON_REDO.get());
	}
	//endregion

	//region other events
	public void search(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::search, R.MAIN_CONTROLLER_ERROR_ON_SHOW_SEARCH.get());
	}

	public void showStore(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::store, R.MAIN_CONTROLLER_ERROR_ON_SHOW_STORE.get());
	}

	public void showAllTabs(ActionEvent actionEvent)
	{
		Common.tryCatch(this::showAllTabs, R.MAIN_CONTROLLER_ERROR_ALL_TABS.get());
	}

	public void closeApplication(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::closeApplication, R.MAIN_CONTROLLER_ERROR_ON_CLOSE_APP.get());
	}

	public void showSettingsPanel(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> new SettingsPanel(this.settings).show(), R.MAIN_CONTROLLER_ERROR_ON_SHOW_PANEL.get());
	}

	public void showAboutProgram(ActionEvent actionEvent)
	{
		Common.tryCatch(DialogsHelper::showAboutProgram, R.MAIN_CONTROLLER_ERROR_ON_SHOW_ABOUT.get());
	}

    public void showActionsHelp(ActionEvent event)
    {
        Common.tryCatch(() -> DialogsHelper.showActionsHelp(factory), R.MAIN_CONTROLLER_ERROR_ACTIONS_PANEL.get());
    }

	public void showCalculator(ActionEvent event)
	{
		Common.tryCatch(model::showCalculator, R.MAIN_CONTROLLER_ERROR_ON_SHOW_CALC.get());
	}

	public void showCalculator(AbstractEvaluator evaluator)
	{
		DialogsHelper.showHelperDialog("<none>", evaluator, R.MAIN_CONTROLLER_SHOW_CALC_VALUE.get(), null);
	}
	//endregion

	//region Git
	public void gitStatus(ActionEvent event)
	{
		Common.tryCatch(this.model::gitStatus, R.MAIN_CONTROLLER_ERROR_ON_SHOW_STATUS.get());
	}

	public void gitMerge(ActionEvent event)
	{
		Common.tryCatch(this.model::gitMerge, R.MAIN_CONTROLLER_ERROR_ON_MERGE.get());
	}

	public void gitBranches(ActionEvent event)
	{
		Common.tryCatch(this.model::gitBranches, R.MAIN_CONTROLLER_ERROR_ON_BRANCHES.get());
	}

	public void gitTags(ActionEvent event)
	{
		Common.tryCatch(this.model::gitTags, R.MAIN_CONTROLLER_ERROR_ON_SHOW_TAGS.get());
	}

	public void gitChangeCredential(ActionEvent event)
	{
		Common.tryCatch(this.model::changeCredential, R.MAIN_CONTROLLER_ERROR_ON_STATUS.get());
	}

	public void gitClone(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.projectFromGit(this.projectPane), R.MAIN_CONTROLLER_ERROR_ON_REPOSITORY.get());
	}

	public void gitCommit(ActionEvent event)
	{
		Common.tryCatch(this.model::gitCommit, R.MAIN_CONTROLLER_ERROR_ON_COMMIT.get());
	}

	public void gitPush(ActionEvent event)
	{
		Common.tryCatch(this.model::gitPush, R.MAIN_CONTROLLER_ERROR_ON_PUSH.get());
	}

	public void gitPull(ActionEvent event)
	{
		Common.tryCatch(this.model::gitPull, R.MAIN_CONTROLLER_ERROR_ON_PULL.get());
	}

	public void gitReset(ActionEvent event)
	{
		Common.tryCatch(this.model::gitReset, R.MAIN_CONTROLLER_ERROR_ON_RESET.get());
	}
	//endregion

	//region progress tasks
	public void startTask(String title)
	{
		Common.runLater(() -> {
			this.progressBar.setVisible(true);
			this.progressLabel.setText(title);
		});
	}

	public void updateTask(String title)
	{
		Common.runLater(() -> {
			if (!this.progressLabel.getText().isEmpty())
			{
				this.progressLabel.setText(title);
			}
		});
	}

	public void endTask()
	{
		Common.runLater(() -> {
			this.progressBar.setVisible(false);
			this.progressLabel.setText("");
		});
	}
	//endregion

	public CustomTab createTab(Document document)
	{
		return this.customTabPane.createTab(document);
	}

	public void selectTab(CustomTab tab)
	{
		this.customTabPane.addTab(tab);
		this.customTabPane.selectTab(tab);
	}

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
			else if (keyEvent.getCode() == KeyCode.F1 && !keyEvent.isControlDown() && !keyEvent.isShiftDown() && !keyEvent.isAltDown())
			{
				showAboutProgram(null);
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.SEARCH))
			{
				search(null);
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
		}, R.MAIN_CONTROLLER_ERROR_ON_SET_SHORTCUTS.get()));
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

			}, R.MAIN_CONTROLLER_ERROR_ON_LOAD_MATRIX.get()));
			fileLastOpenMatrix.getItems().add(menuItem);
		});
		fileLastOpenMatrix.getItems().add(new SeparatorMenuItem());
		MenuItem clearList = new MenuItem(R.MAIN_CONTROLLER_CLEAR_LIST.get());
		clearList.setOnAction(actionEvent -> Common.tryCatch(model::clearFileLastOpenMatrix, R.MAIN_CONTROLLER_ERROR_ON_CLEAR_LIST.get()));
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
			return Str.areEqual(new File(document.getNameProperty().get()).getAbsolutePath(), file.getAbsolutePath());
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

	void showInTree(File file)
	{

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
							Common.setFocusedFast(lookup);
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

		this.matrixShowWait.selectedProperty().addListener((observable, oldValue, newValue) -> this.model.showWaits(newValue && !oldValue));
	}

	private void openLogs()
	{
		new LogsFx(this.settings).show();
	}

	private void showAllTabs()
	{
		ListView<String> listView = new ListView<>();
		listView.getItems().addAll(this.customTabPane.getTabs().stream().map(tab -> ((CustomTab) tab).getTitle()).collect(Collectors.toList()));
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.getDialogPane().setContent(listView);
		dialog.getDialogPane().setPrefHeight(400);
		dialog.getDialogPane().setPrefWidth(432);
		dialog.getDialogPane().setPadding(new Insets(0,0,0,-11));
		dialog.setHeaderText("");
		dialog.setTitle(R.MAIN_CONTROLLER_CHOOSE_TAB.get());

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
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
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
					Common.runLater(() -> lblMemory.setText(getMBytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "MB of "
							+ getMBytes(Runtime.getRuntime().maxMemory()) + "MB"));

					Thread.sleep(1000);
				}
				catch (InterruptedException ie)
				{
					break;
				}
				catch (Exception e)
				{
					logger.error(R.MAIN_CONTROLLER_ERROR_ON_RAM);
					logger.error(e.getMessage(), e);
					return;
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
