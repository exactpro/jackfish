////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.console.ConsoleText;
import com.exactprosystems.jf.tool.custom.console.CustomListView;
import com.exactprosystems.jf.tool.custom.date.CustomDateTimePicker;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.custom.find.FindPanel;
import com.exactprosystems.jf.tool.custom.find.IFind;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.treetable.DisplayDriverFx;
import com.exactprosystems.jf.tool.custom.treetable.MatrixContextMenu;
import com.exactprosystems.jf.tool.custom.treetable.MatrixParametersContextMenu;
import com.exactprosystems.jf.tool.custom.treetable.MatrixTreeView;
import com.exactprosystems.jf.tool.documents.AbstractDocumentController;
import com.exactprosystems.jf.tool.documents.ControllerInfo;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.watch.WatcherFx;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.io.File;
import java.net.URL;
import java.util.*;

import static com.exactprosystems.jf.tool.Common.*;

@ControllerInfo(resourceName = "MatrixFx.fxml")
public class MatrixFxController extends AbstractDocumentController<MatrixFx> implements IMatrixListener
{
	public MatrixTreeView				tree;
	public Button						btnStartMatrix;
	public Button						btnStopMatrix;
	public Button						btnShowResult;
	public Button						btnPauseMatrix;
	public Button						btnStepMatrix;
	public ToggleButton					toggleTracing;
	public CustomListView<MatrixItem>	listView;
	public Button						btnWatch;
	public ScrollPane					mainScrollPane;
	public ComboBox<String>				cbDefaultApp;
	public ComboBox<String>				cbDefaultClient;
	public Button						btnFind;
	public SplitPane					splitPane;
	public GridPane						gridPane;
	public HBox							hBox;
	public Label						lblTimer;
	public HBox bottomBox;
	private ExpressionField efParameter;

	private WatcherFx					watcher	= null;
	private FindPanel<MatrixItem>		findPanel;
	private boolean						visible	= false;

	private DisplayDriver				driver;
	private Context						context;
	private boolean						ok;
	private String						exceptionMessage;

	private static final int MIN_TIME_FOR_SHOW_WAITS = 5000;
	private static final int MIN_TIME_FOR_HIDE_WAITS = 1000;

	// ==============================================================================================================================
	// interface Initializable
	// ==============================================================================================================================
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert btnWatch != null : "fx:id=\"btnWatch\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		assert btnStopMatrix != null : "fx:id=\"btnStopMatrix\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		assert btnShowResult != null : "fx:id=\"btnShowResult\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		assert btnStartMatrix != null : "fx:id=\"btnStartMatrix\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		assert mainScrollPane != null : "fx:id=\"mainScrollPane\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		assert btnPauseMatrix != null : "fx:id=\"btnPauseMatrix\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		assert btnStepMatrix != null : "fx:id=\"btnStepMatrix\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		assert toggleTracing != null : "fx:id=\"toggleTracing\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		
		this.listView = new CustomListView<>(matrixItem -> tryCatch(() ->
		{
			TreeItem<MatrixItem> treeItem = this.tree.find(matrixItem);
			Optional.ofNullable(treeItem).ifPresent(item -> Common.runLater(() -> this.tree.setCurrent(item, false)));
		}, "Error on moving to item"), true);
		this.listView.autoScroll(true);
		this.listView.setMinHeight(100.0);
		this.listView.setMaxHeight(250.0);
		this.splitPane.getItems().add(this.listView);
		this.tree = new MatrixTreeView();
		this.mainScrollPane.setContent(this.tree);
		this.findPanel = new FindPanel<>(new IFind<MatrixItem>()
		{
			@Override
			public void find(MatrixItem matrixItem)
			{
				tree.getSelectionModel().clearSelection();
				TreeItem<MatrixItem> root = tree.getRoot();
				TreeItem<MatrixItem> treeItem = tree.find(root, matrixItem);
				tree.setCurrent(treeItem, false);
			}

			@Override
			public List<MatrixItem> findItem(String what, boolean matchCase, boolean wholeWord)
			{
				return model.find(what, matchCase, wholeWord);
			}
		});

		gridPane.add(this.findPanel, 0, 1, 2, 1);
		this.findPanel.setVisible(false);
		CustomDateTimePicker customDateTimePicker = new CustomDateTimePicker(date -> this.model.setStartTime(date));
		hBox.getChildren().add(0, customDateTimePicker);

		this.cbDefaultApp.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> super.model.setDefaultApp(newValue));
		this.cbDefaultClient.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> super.model.setDefaultClient(newValue));
	}

	// ==============================================================================================================================
	// MatrixListener
	// ==============================================================================================================================
	@Override
	public void reset(final Matrix matrix)
	{
		this.ok = true;
	}
	
	@Override
	public void matrixStarted(final Matrix matrix)
	{
		this.model.clearExecutingState();
		this.refresh();
		CustomTab tab1 = checkDocument(matrix);
		String format = String.format("Matrix '%s' started...", matrix.getNameProperty().get());
		if (this.listView != null)
		{
		this.listView.getItems().clear();
		this.listView.getItems().add(ConsoleText.defaultText(format));
		Optional.ofNullable(tab1).ifPresent(t -> {
		t.getStyleClass().removeAll(CssVariables.MATRIX_FINISHED_OK, CssVariables.MATRIX_FINISHED_BAD);
		t.getStyleClass().add(CssVariables.EXECUTING_TAB);
		});
		}
		else
		{
		DialogsHelper.showInfo(format);
		}
	}

	@Override
	public IMatrixListener clone() throws CloneNotSupportedException {
		try
		{
			return this;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new InternalError();
		}
	}

	@Override
	public void matrixFinished(final Matrix matrix, final int passed, final int failed)
	{
		String format = String.format("Matrix '%s' finished.", matrix.getNameProperty().get());
		if (this.listView != null)
		{
			this.listView.getItems().add(ConsoleText.defaultText(format));
			Optional.ofNullable(this.customTab).ifPresent(t -> {
				t.getStyleClass().remove(CssVariables.EXECUTING_TAB);
				t.getStyleClass().add(failed == 0 ? CssVariables.MATRIX_FINISHED_OK : CssVariables.MATRIX_FINISHED_BAD);
			});
		}
		else
		{
			DialogsHelper.showInfo(format);
		}
		this.refresh();
		this.disableButtons(false);
	}

	@Override
	public void error(final Matrix matrix, int lineNumber, final MatrixItem item, final String message)
	{
		this.ok = false;
		this.exceptionMessage = String.format("error(%d, %s, %s)", lineNumber, item == null ? "<null>" : item.getPath(), message);
		Common.runLater(() ->
		{
			String format = item == null ? message : String.format("%s %s", item.getPath(), message);
			if (listView != null)
			{
				listView.getItems().add(ConsoleText.errorItem(format, item));
			}
			else
			{
				DialogsHelper.showError(format);
			}
		});
	}

	@Override
	public void started(Matrix matrix, MatrixItem item)
	{
		this.refreshTreeIfToogle();
	}

	@Override
	public void finished(Matrix matrix, MatrixItem item, Result result)
	{
		refresh();
	}

	@Override
	public void paused(Matrix matrix, final MatrixItem item)
	{
		try
		{
			this.disableButtons(false);
			this.model.pausedMatrix(matrix);
			this.refreshTreeIfToogle();
			Optional.ofNullable(this.watcher).ifPresent(WatcherFx::update);
			TreeItem<MatrixItem> treeItem = this.tree.find(item);
			if (treeItem == null)
			{
				DialogsHelper.showInfo(String.format("Matrix paused on \'%s\' in file \'%s\'", item, matrix.getNameProperty().get()));
				Optional.ofNullable(this.listView).ifPresent(lv -> lv.getItems().add(ConsoleText.pausedItem(String.format("Paused on %s", item), null)));
			}
			else
			{
				DialogsHelper.showInfo(String.format("Matrix paused on \'%s\'", treeItem.getValue().getItemName()));
				Optional.ofNullable(this.listView).ifPresent(lv -> lv.getItems().add(ConsoleText.pausedItem(String.format("Paused on %s", item), item)));
				this.tree.scrollTo(this.tree.getRow(treeItem));
			}
		}
		catch (Exception e)
		{
			logger.error("Error on matrix paused.\n" + e.getMessage(), e);
		}
	}

	@Override
	public String getExceptionMessage()
	{
		return this.exceptionMessage;
	}

	@Override
	public boolean isOk()
	{
		return this.ok;
	}

	@Override
	protected void init(Document model, CustomTab customTab)
	{
		super.init(model, customTab);

		this.model.getRoot().setOnRemoveListener((integer, matrixItem) -> this.remove(matrixItem));
		//TODO think about second parameter of method display;
		this.model.getRoot().setOnAddListener((integer, matrixItem) -> this.display(matrixItem, false));
		this.model.currentItemProperty().setOnChangeListener(((oldValue, newValue) -> this.setCurrent(newValue, false)));
		this.model.timerProperty().setOnChangeListener((aLong, aLong2) -> this.displayTimer(aLong2, aLong2 > 0));
		this.model.refreshProperty().setOnChangeListener((aBoolean, aBoolean2) -> this.tree.refresh());

		this.context = this.model.getEngine().getContext();
		Settings settings = this.model.getFactory().getSettings();
		MatrixParametersContextMenu parametersContextMenu 	= new MatrixParametersContextMenu(context, this.model, this.tree, settings);
		MatrixContextMenu 			rowContextMenu 			= new MatrixContextMenu(context, this.model, this.tree, settings);
		parametersContextMenu.initShortcuts(settings, this.tree, this.model, context);

		this.driver = new DisplayDriverFx(this.tree, this.context, rowContextMenu, parametersContextMenu);
		this.tree.init(this.model, settings, rowContextMenu);

		TabConsole console = new TabConsole(System.out);
		super.model.getEngine().getContext().setOut(console);
		console.setConsumer(s -> Common.runLater(() -> {
			if (s == null)
			{
				this.listView.getItems().clear();
			}
			else
			{
				this.listView.getItems().add(ConsoleText.defaultText(s));
			}
		}));

		this.efParameter = new ExpressionField(context.getEvaluator());
		HBox.setHgrow(this.efParameter, Priority.ALWAYS);
		this.efParameter.setStretchable(false);
		this.efParameter.setPromptText("Parameter for start");
		this.efParameter.setMaxWidth(250.0);
		this.efParameter.setPrefWidth(250.0);
		this.efParameter.setMinWidth(250.0);
		this.efParameter.setHelperForExpressionField("Parameter for start", this.model);
		this.bottomBox.getChildren().addAll(this.efParameter, Common.createSpacer(SpacerEnum.HorizontalMin), new Separator(Orientation.VERTICAL));
		initializeButtons(context.getFactory().getSettings());
		initShortcuts(context.getFactory().getSettings());
		this.efParameter.textProperty().addListener((observable, oldValue, newValue) -> {
			Object value = null;
			try
			{
				value = this.getParameter();
			}
			catch (Exception ignored)
			{}
			this.model.parameterProperty().set(value);
		});

		displayGuiDictionaries();
		displayClientDictionaries();
		this.model.setListener(this);
	}

	@Override
	protected void restoreSettings(Settings settings)
	{
		Settings.SettingsValue defaults = settings.getValue(Settings.MAIN_NS, MatrixFx.DIALOG_DEFAULTS, new File(super.model.getNameProperty().get()).getAbsolutePath());
		if (Objects.isNull(defaults))
		{
			this.cbDefaultApp.getSelectionModel().select(MatrixFx.EMPTY_STRING);
			this.cbDefaultClient.getSelectionModel().select(MatrixFx.EMPTY_STRING);
		}
		else
		{
			String[] split = defaults.getValue().split(MatrixFx.DELIMITER);
			if (split.length == 2)
			{
				this.cbDefaultApp.getSelectionModel().select(split[0]);
				this.cbDefaultClient.getSelectionModel().select(split[1]);
			}
		}
		Settings.SettingsValue foldSetting = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_FOLD_ITEMS, "false");
		boolean fold = Boolean.parseBoolean(foldSetting.getValue());

		this.tree.setNeedExpand(fold);
	}

	public void init(MatrixFx model, Context context, TabConsole console)
	{
		Settings settings = context.getFactory().getSettings();
		Settings.SettingsValue foldSetting = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_FOLD_ITEMS, "false");
		boolean fold = Boolean.parseBoolean(foldSetting.getValue());

		this.tree.setNeedExpand(fold);
		MatrixParametersContextMenu parametersContextMenu 	= new MatrixParametersContextMenu(context, model, this.tree, settings);
		MatrixContextMenu 			rowContextMenu 			= new MatrixContextMenu(context, model, this.tree, settings);
		parametersContextMenu.initShortcuts(settings, this.tree, model, context);
		
		this.model = model;
		this.context = context;
		this.driver = new DisplayDriverFx(this.tree, this.context, rowContextMenu, parametersContextMenu);
		this.tree.init(model, settings, rowContextMenu);
//		this.tab.setContent(this.pane);
		console.setConsumer(s -> Common.runLater(() -> this.listView.getItems().add(ConsoleText.defaultText(s))));

		this.efParameter = new ExpressionField(context.getEvaluator());
		HBox.setHgrow(this.efParameter, Priority.ALWAYS);
		this.efParameter.setStretchable(false);
		this.efParameter.setPromptText("Parameter for start");
		this.efParameter.setMaxWidth(250.0);
		this.efParameter.setPrefWidth(250.0);
		this.efParameter.setMinWidth(250.0);
		this.efParameter.setHelperForExpressionField("Parameter for start", this.model);
		this.bottomBox.getChildren().addAll(this.efParameter, Common.createSpacer(SpacerEnum.HorizontalMin), new Separator(Orientation.VERTICAL));
		initializeButtons(context.getFactory().getSettings());
		initShortcuts(context.getFactory().getSettings());
		this.efParameter.textProperty().addListener((observable, oldValue, newValue) -> {
			Object value = null;
			try
			{
				value = this.getParameter();
			}
			catch (Exception ignored)
			{}
			this.model.parameterProperty().set(value);
		});
	}

	public void save(String name)
	{
		this.customTab.getStyleClass().removeAll(CssVariables.MATRIX_FINISHED_OK, CssVariables.MATRIX_FINISHED_BAD);
		this.customTab.saved(name);
		this.model.clearExecutingState();
		this.refresh();
	}

	public void showWatcher(MatrixFx matrix, Context context)
	{
		tryCatch(() ->
		{
			if (this.watcher == null || !this.watcher.isShow())
			{
				this.watcher = new WatcherFx(btnWatch.getScene().getWindow(), matrix, context);
			}
			this.watcher.show();
		}, "Error on showing watcher ");
	}

	@Override
	protected void close()
	{
		Optional.ofNullable(this.watcher).ifPresent(WatcherFx::close);
		super.close();
	}

	public Object getParameter() throws Exception
	{
		return this.efParameter.getEvaluatedValue();
	}

	// ------------------------------------------------------------------------------------------------------------------
	// event handlers
	// ------------------------------------------------------------------------------------------------------------------
	public void showResult(ActionEvent event)
	{
		tryCatch(this.model::showResult, "Error on showing result");
	}

	public void stopMatrix(ActionEvent event)
	{
		tryCatch(this.model::stop, "Error on stopping matrix");
		this.disableButtons(false);
	}

	public void startMatrix(ActionEvent event)
	{
		tryCatch(this.model::startMatrix, "Error on starting matrix. See the matrix output for details.");
		this.disableButtons(true);
	}

	public void pauseMatrix(ActionEvent event)
	{
		tryCatch(this.model::pauseMatrix, "Error on pausing matrix");
		this.disableButtons(false);
	}

	public void stepMatrix(ActionEvent event)
	{
		tryCatch(this.model::stepMatrix, "Error on stepping matrix");
	}

	public void toggleTracing(ActionEvent event)
	{
		tryCatch(() ->
		{
			boolean b = toggleTracing.isSelected();
			toggleTracing.getTooltip().setText("Color " + (b ? "on" : "off"));
			this.context.setTracing(b);
			this.refresh();
			this.tree.setTracing(b);
			
		}, "Error on setting color");
	}

	public void showWatch(ActionEvent event)
	{
		tryCatch(this.model::showWatch, "Error on showing watcher");
	}

	public void showFindPanel(ActionEvent actionEvent)
	{
		tryCatch(() ->
		{
			this.visible = !this.visible;
			this.findPanel.setVisible(this.visible);
			GridPane.setRowSpan(mainScrollPane, (this.visible ? 1 : 2));
			if (this.visible)
			{
				this.findPanel.requestFocus();
			}
		}, "Error on showing the find panel");
	}

	public void markAll(ActionEvent actionEvent)
	{
		mark(true);
	}

	public void unmarkAll(ActionEvent actionEvent)
	{
		mark(false);
	}

	private void mark(boolean flag)
	{
		tryCatch(() -> this.model.markFirstLevel(flag), "Error on marking all items");
		refresh();
	}

	// ------------------------------------------------------------------------------------------------------------------
	// display* methods
	// ------------------------------------------------------------------------------------------------------------------
	void displayTimer(long ms, boolean needShow)
	{
		Common.runLater(() -> {
			if (ms < MIN_TIME_FOR_SHOW_WAITS && !lblTimer.isVisible())
			{
				return;
			}
			if (!needShow || ms < MIN_TIME_FOR_HIDE_WAITS)
			{
				this.lblTimer.setVisible(false);
				return;
			}
			this.lblTimer.setVisible(true);
			this.lblTimer.setText(createText(ms));
		});
	}

	private String createText(long time)
	{
		time = time / 1000;
		long hours = time / 3600;
		long minutes = (time % 3600) / 60;
		long seconds = time % 60;
		return String.format("Wait %s:%s:%s", hours < 10 ? "0" + hours : hours
				, minutes < 10 ? "0" + minutes : minutes
				, seconds < 10 ? "0" + seconds : seconds
		);
	}

	public void refresh()
	{
		this.tree.refresh();
	}

	public void refreshParameters(MatrixItem item, int selectIndex)
	{
		this.tree.refreshParameters(item, selectIndex);
	}

	public void setCurrent(MatrixItem item, boolean needExpand)
	{
		TreeItem<MatrixItem> treeItem = this.tree.find(item);
		if (treeItem == null)
		{
			treeItem = this.tree.find(this.tree.getRoot(), matrixItem -> item.getId().equals(matrixItem.getId()));
		}
		this.tree.setCurrent(treeItem, needExpand);
	}

	public void remove(MatrixItem item)
	{
		Common.runLater(() -> this.driver.deleteItem(item) );
	}

	public void display(MatrixItem item, boolean needExpand)
	{
		Common.runLater(() -> {
			item.display(this.driver, this.context);
			this.driver.setCurrentItem(item, this.model, needExpand);
		});
	}

	public void setDefaultApp(String id)
	{
		Common.runLater(() -> this.cbDefaultApp.getSelectionModel().select(id));
	}

	public void setDefaultClient(String id)
	{
		Common.runLater(() -> this.cbDefaultClient.getSelectionModel().select(id));
	}
	// ------------------------------------------------------------------------------------------------------------------
	// private methods
	// ------------------------------------------------------------------------------------------------------------------

	private void displayGuiDictionaries()
	{
		ArrayList<String> result = new ArrayList<>();
		result.add(Matrix.EMPTY_STRING);
		result.addAll(new ArrayList<>(super.model.getFactory().getConfiguration().getApplicationPool().appNames()));
		Common.runLater(() -> this.cbDefaultApp.setItems(FXCollections.observableList(result)));
	}

	private void displayClientDictionaries()
	{
		ArrayList<String> result = new ArrayList<>();
		result.add(Matrix.EMPTY_STRING);
		result.addAll(new ArrayList<>(super.model.getFactory().getConfiguration().getClientPool().clientNames()));
		Common.runLater(() -> this.cbDefaultClient.setItems(FXCollections.observableList(result)));
	}


	private void refreshTreeIfToogle()
	{
		if (this.toggleTracing.isSelected())
		{
			this.tree.refresh();
		}
	}

	private void initShortcuts(final Settings settings)
	{
		this.parent.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> tryCatch(() ->
		{
			if (customTab.isSelected())
			{
				if (SettingsPanel.match(settings, keyEvent, Settings.START_MATRIX))
				{
					model.startMatrix();
				}
				else if (SettingsPanel.match(settings, keyEvent, Settings.STOP_MATRIX))
				{
					model.stop();
				}
				else if (SettingsPanel.match(settings, keyEvent, Settings.PAUSE_MATRIX))
				{
					model.pauseMatrix();
				}
				else if (SettingsPanel.match(settings, keyEvent, Settings.STEP_MATRIX))
				{
					model.stepMatrix();
				}
				else if (SettingsPanel.match(settings, keyEvent, Settings.SHOW_RESULT))
				{
					model.showResult();
				}
				else if (SettingsPanel.match(settings, keyEvent, Settings.SHOW_WATCH))
				{
					model.showWatch();
				}
				else if (SettingsPanel.match(settings, keyEvent, Settings.TRACING))
				{
					this.context.setTracing(true);
				}
				else if (SettingsPanel.match(settings, keyEvent, Settings.FIND_ON_MATRIX))
				{
					showFindPanel(null);
				}
			}
		}, "Error on setting shortcuts"));

	}

	private void initializeButtons(final Settings settings)
	{
		Common.runLater(() -> tryCatch(() ->
		{
			this.btnStartMatrix.setTooltip(new Tooltip("Start\n" + getShortcutTooltip(settings, Settings.START_MATRIX)));
			this.btnStopMatrix.setTooltip(new Tooltip("Stop\n" + getShortcutTooltip(settings, Settings.STOP_MATRIX)));
			this.btnPauseMatrix.setTooltip(new Tooltip("Pause\n" + getShortcutTooltip(settings, Settings.PAUSE_MATRIX)));
			this.btnPauseMatrix.setTooltip(new Tooltip("Step\n" + getShortcutTooltip(settings, Settings.STEP_MATRIX)));
			this.btnFind.setTooltip(new Tooltip("Find\n" + getShortcutTooltip(settings, Settings.FIND_ON_MATRIX)));

			this.toggleTracing.setTooltip(new Tooltip("Color off"));
			this.toggleTracing.getStyleClass().add(CssVariables.TOGGLE_BUTTON_WITHOUT_BORDER);
		}, "Error on setting tooltip or images"));
	}

	void disableButtons(boolean isOn) {
		this.btnStartMatrix.setDisable(isOn);
		this.btnStepMatrix.setDisable(isOn);
	}
}
