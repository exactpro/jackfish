/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.tool.matrix;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
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
import com.exactprosystems.jf.tool.custom.console.ConsoleArea;
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
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

import static com.exactprosystems.jf.tool.Common.getShortcutTooltip;
import static com.exactprosystems.jf.tool.Common.tryCatch;

@ControllerInfo(resourceName = "MatrixFx.fxml")
public class MatrixFxController extends AbstractDocumentController<MatrixFx> implements IMatrixListener
{
	@FXML
	private MatrixTreeView   tree;
	@FXML
	private Button           btnStartMatrix;
	@FXML
	private Button           btnStopMatrix;
	@FXML
	private Button           btnShowResult;
	@FXML
	private Button           btnPauseMatrix;
	@FXML
	private Button           btnStepMatrix;
	@FXML
	private ToggleButton     tbTracing;
	@FXML
	private Button           btnWatch;
	@FXML
	private ScrollPane       mainScrollPane;
	@FXML
	private ComboBox<String> cbDefaultApp;
	@FXML
	private ComboBox<String> cbDefaultClient;
	@FXML
	private ToggleButton     btnFind;
	@FXML
	private SplitPane        splitPane;
	@FXML
	private GridPane         gridPane;
	@FXML
	private HBox             hBox;
	@FXML
	private Label            lblTimer;
	@FXML
	private HBox             bottomBox;

	private ExpressionField efParameter;

	private WatcherFx watcher = null;
	private FindPanel<MatrixItem> findPanel;
	private boolean visible = false;

	private DisplayDriver driver;
	private Context       context;
	private boolean       ok;
	private String        exceptionMessage;

	private static final int MIN_TIME_FOR_SHOW_WAITS = 5000;
	private static final int MIN_TIME_FOR_HIDE_WAITS = 1000;
	private ConsoleArea<TreeItem<MatrixItem>> area;

	//region Initializable
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		super.initialize(url, resourceBundle);
		assert btnWatch != null : "fx:id=\"btnWatch\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		assert btnStopMatrix != null : "fx:id=\"btnStopMatrix\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		assert btnShowResult != null : "fx:id=\"btnShowResult\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		assert btnStartMatrix != null : "fx:id=\"btnStartMatrix\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		assert mainScrollPane != null : "fx:id=\"mainScrollPane\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		assert btnPauseMatrix != null : "fx:id=\"btnPauseMatrix\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		assert btnStepMatrix != null : "fx:id=\"btnStepMatrix\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		assert tbTracing != null : "fx:id=\"tbTracing\" was not injected: check your FXML file 'MatrixFx.fxml'.";

		Consumer<TreeItem<MatrixItem>> moveToMatrixItem = treeItem -> this.tree.setCurrent(treeItem, false);
		this.area = new ConsoleArea<>(moveToMatrixItem);
		this.area.setEditable(false);
		this.area.setMaxHeight(250);
		this.splitPane.getItems().add(new VirtualizedScrollPane<>(area));

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

		this.gridPane.add(this.findPanel, 0, 1, 2, 1);
		this.findPanel.setVisible(false);

		CustomDateTimePicker customDateTimePicker = new CustomDateTimePicker(date -> this.model.setStartTime(date));
		this.hBox.getChildren().add(0, customDateTimePicker);

		this.cbDefaultApp.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> super.model.setDefaultApp(newValue));
		this.cbDefaultClient.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> super.model.setDefaultClient(newValue));
	}

	//endregion

	//region MatrixListener
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
		String format = String.format(R.MATRIX_FX_CONTR_MATRIX_STARTED.get(), matrix.getNameProperty().get());
		Common.runLater(() -> {
			this.area.clear();
			this.area.appendDefaultTextOnNewLine(format);
		});
	}

	@Override
	public void matrixFinished(final Matrix matrix, final int passed, final int failed)
	{
		Common.runLater(() -> this.area.appendDefaultTextOnNewLine(String.format(R.MATRIX_FX_CONTR_MATRIX_FINISHED.get(), matrix.getNameProperty().get())));
		this.forceRefresh();
		this.disableButtons(false);
		this.model.getFactory().getConfiguration().refreshReport();

		String value = this.model.getFactory().getSettings().getValue(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_OPEN_REPORT_AFTER_FINISHED).getValue();
		if (Boolean.parseBoolean(value)) {
			DialogsHelper.displayReport(new File(matrix.getEngine().getReportName()), matrix.getNameProperty().get(), this.model.getFactory());
		}
	}

	@Override
	public void error(final Matrix matrix, int lineNumber, final MatrixItem item, final String message)
	{
		this.ok = false;
		this.exceptionMessage = String.format("error(%d, %s, %s)", lineNumber, item == null ? "<null>" : item.getPath(), message);
		Common.runLater(() -> {
			String format = item == null ? message : String.format("%s %s", item.getPath(), message);
			this.area.appendErrorTextOnNewLine(format);
			this.disableButtons(false);
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
		this.disableButtons(false);
		this.model.pausedMatrix(matrix);
		this.refreshTreeIfToogle();
		Optional.ofNullable(this.watcher).ifPresent(WatcherFx::update);
		TreeItem<MatrixItem> treeItem = this.tree.find(item);
		Common.runLater(() -> {
			this.area.appendDefaultTextOnNewLine(String.format(R.MATRIX_FX_CONTR_PAUSED_ON.get(), item.getNumber()));
			this.area.appendLink(String.format("%s", item.getItemName()), treeItem);
			this.tree.scrollTo(this.tree.getRow(treeItem));
		});
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

	//endregion

	//region AbstractDocumentController
	@Override
	protected void init(Document model, CustomTab customTab)
	{
		super.init(model, customTab);

		super.model.getRoot().setOnRemoveListener((integer, matrixItem) -> this.remove(matrixItem));
		//TODO think about second parameter of method display;
		super.model.getRoot().setOnAddListener((integer, matrixItem) -> this.display(matrixItem, false));
		super.model.timerProperty().setOnChangeListener((aLong, aLong2) -> this.displayTimer(aLong2, aLong2 > 0));
		super.model.getRoot().setOnBreakPoint((oldValue, newValue) -> this.tree.refresh());
		super.model.getRoot().setOnChangeParameter((integer, matrixItem) -> this.refreshParameters(matrixItem, integer));

		if (super.model.getEngine() == null)
		{
			this.context = super.model.getFactory().createContext();
		}
		else
		{
			this.context = super.model.getEngine().getContext();
		}
		Settings settings = super.model.getFactory().getSettings();
		MatrixParametersContextMenu parametersContextMenu = new MatrixParametersContextMenu(context, super.model, this.tree, settings);
		MatrixContextMenu rowContextMenu = new MatrixContextMenu(context, super.model, this.tree, settings);
		parametersContextMenu.initShortcuts(settings, this.tree, super.model, context);

		this.driver = new DisplayDriverFx(this.tree, this.context, rowContextMenu, parametersContextMenu);
		this.tree.init(super.model, settings, rowContextMenu);

		TabConsole console = new TabConsole(System.out);

		Optional.ofNullable(super.model.getEngine()).ifPresent(engine -> engine.getContext().setOut(console));
		console.setConsumer(s -> Common.runLater(() -> {
			if (s == null)
			{
				this.area.clear();
			}
			else
			{
				this.area.appendDefaultTextOnNewLine(s);
			}
		}));

		this.efParameter = new ExpressionField(context.getEvaluator());
		HBox.setHgrow(this.efParameter, Priority.ALWAYS);
		this.efParameter.setStretchable(false);
		this.efParameter.setPromptText(R.MATRIX_FX_CONTR_PARAMETER_FOR_START.get());
		this.efParameter.setMaxWidth(250.0);
		this.efParameter.setPrefWidth(250.0);
		this.efParameter.setMinWidth(250.0);
		this.efParameter.setHelperForExpressionField(R.MATRIX_FX_CONTR_PARAMETER_FOR_START.get(), super.model);
		this.bottomBox.getChildren().addAll(this.efParameter, Common.createSpacer(Common.SpacerEnum.HorizontalMin), new Separator(Orientation.VERTICAL));
		this.efParameter.textProperty().addListener((observable, oldValue, newValue) -> {
			Object value = null;
			try
			{
				value = this.getParameter();
			}
			catch (Exception ignored)
			{
			}
			this.model.setParameter(value);
		});

		initializeButtons(super.model.getFactory().getSettings());
		initShortcuts(super.model.getFactory().getSettings());

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
		Settings.SettingsValue foldSetting = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.MATRIX_NAME, Settings.MATRIX_FOLD_ITEMS);
		boolean fold = Boolean.parseBoolean(foldSetting.getValue());

		this.tree.setNeedExpand(fold);
	}

	@Override
	protected void close()
	{
		Optional.ofNullable(this.watcher).ifPresent(WatcherFx::close);
		super.close();
	}

	@Override
	protected void save()
	{
		this.customTab.getStyleClass().removeAll(CssVariables.MATRIX_FINISHED_OK, CssVariables.MATRIX_FINISHED_BAD);
		this.model.clearExecutingState();
		this.refresh();
	}

	//endregion

	//region event handlers
	@FXML
	private void showResult(ActionEvent event)
	{
		this.model.showResult();
	}

	@FXML
	private void stopMatrix(ActionEvent event)
	{
		this.model.stop();
		this.disableButtons(false);
	}

	@FXML
	private void startMatrix(ActionEvent event)
	{
		this.disableButtons(true);
		tryCatch(this.model::startMatrix, R.MATRIX_FX_CONTR_ERROR_ON_START.get());
	}

	@FXML
	private void pauseMatrix(ActionEvent event)
	{
		this.model.pauseMatrix();
		this.disableButtons(false);
	}

	@FXML
	private void stepMatrix(ActionEvent event)
	{
		this.model.stepMatrix();
	}

	@FXML
	private void toggleTracing(ActionEvent event)
	{
		boolean b = tbTracing.isSelected();
		tbTracing.getTooltip().setText(b ? R.MATRIX_FX_CONTR_COLOR_ON.get() : R.MATRIX_FX_CONTR_COLOR_OFF.get());
		this.context.setTracing(b);
		this.refresh();
		this.tree.setTracing(b);
	}

	@FXML
	private void showWatch(ActionEvent event)
	{
		this.showWatcher(super.model, this.context);
	}

	@FXML
	private void showFindPanel(ActionEvent actionEvent)
	{
		this.visible = !this.visible;
		this.findPanel.setVisible(this.visible);
		GridPane.setRowSpan(mainScrollPane, (this.visible ? 1 : 2));
		if (this.visible)
		{
			this.findPanel.requestFocus();
		}
		if (this.btnFind.isSelected())
		{
			this.btnFind.getStyleClass().remove(CssVariables.TRANSPARENT_BACKGROUND);
		}
		else
		{
			this.btnFind.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
		}
	}

	@FXML
	private void markAll(ActionEvent actionEvent)
	{
		mark(true);
	}

	@FXML
	private void unmarkAll(ActionEvent actionEvent)
	{
		mark(false);
	}

	//endregion

	public void setCurrent(int itemNumber)
	{
		TreeItem<MatrixItem> treeItem = this.tree.find(item -> item.getNumber() == itemNumber);
		this.tree.setCurrent(treeItem, true);
	}

	public void setCurrent(MatrixItem item, boolean needExpand)
	{
		TreeItem<MatrixItem> treeItem = this.tree.find(item);
		if (treeItem == null)
		{
			treeItem = this.tree.find(this.tree.getRoot(), matrixItem -> Str.areEqual(item.getId(), matrixItem.getId()));
		}
		this.tree.setCurrent(treeItem, needExpand);
	}

	//region private methods
	private void remove(MatrixItem item)
	{
		Common.runLater(() -> this.driver.deleteItem(item));
	}

	private void display(MatrixItem item, boolean needExpand)
	{
		Common.runLater(() -> {
			item.display(this.driver, this.context);
			this.driver.setCurrentItem(item, this.model, needExpand);
		});
	}

	private void mark(boolean flag)
	{
		tryCatch(() -> this.model.markFirstLevel(flag), R.MATRIX_FX_CONTR_ERROR_ON_MARKING.get());
		refresh();
	}

	private void displayTimer(long ms, boolean needShow)
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

	private String createText(long time)
	{
		time /= 1000;
		long hours = time / 3600;
		long minutes = (time % 3600) / 60;
		long seconds = time % 60;
		return String.format("Wait %s:%s:%s", hours < 10 ? "0" + hours : hours, minutes < 10 ? "0" + minutes : minutes, seconds < 10 ? "0" + seconds : seconds);
	}

	private void refreshTreeIfToogle()
	{
		if (this.tbTracing.isSelected())
		{
			this.tree.refresh();
		}
	}

	private void initShortcuts(final Settings settings)
	{
		this.parent.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> tryCatch(() -> {
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
					this.showWatcher(super.model, this.context);
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
		}, R.MATRIX_FX_CONTR_ERROR_SET_SHORTCUTS.get()));

	}

	private void initializeButtons(final Settings settings)
	{
		this.btnStartMatrix.setTooltip(new Tooltip(R.COMMON_START.get() + "\n" + getShortcutTooltip(settings, Settings.START_MATRIX)));
		this.btnStopMatrix.setTooltip(new Tooltip(R.COMMON_STOP.get() + "\n" + getShortcutTooltip(settings, Settings.STOP_MATRIX)));
		this.btnPauseMatrix.setTooltip(new Tooltip(R.COMMON_PAUSE.get() + "\n" + getShortcutTooltip(settings, Settings.PAUSE_MATRIX)));
		this.btnStepMatrix.setTooltip(new Tooltip(R.COMMON_STEP.get() + "\n" + getShortcutTooltip(settings, Settings.STEP_MATRIX)));
		this.btnFind.setTooltip(new Tooltip(R.COMMON_FIND.get() + "\n" + getShortcutTooltip(settings, Settings.FIND_ON_MATRIX)));

		this.tbTracing.setTooltip(new Tooltip(R.MATRIX_FX_CONTR_COLOR_OFF.get()));
		this.tbTracing.getStyleClass().add(CssVariables.TOGGLE_BUTTON_WITHOUT_BORDER);
	}

	private void disableButtons(boolean isOn)
	{
		this.btnStartMatrix.setDisable(isOn);
		this.btnStepMatrix.setDisable(isOn);
	}

	private void showWatcher(MatrixFx matrix, Context context)
	{
		tryCatch(() -> {
			if (this.watcher == null || !this.watcher.isShow())
			{
				this.watcher = new WatcherFx(btnWatch.getScene().getWindow(), matrix, context);
			}
			this.watcher.show();
		}, R.MATRIX_FX_CONTR_ERROR_SHOW_WATCHER.get());
	}

	private Object getParameter() throws Exception
	{
		return this.efParameter.getEvaluatedValue();
	}

	private void refresh()
	{
		this.tree.refresh();
	}

	private void forceRefresh()
	{
		this.tree.forceRefresh();
	}

	private void refreshParameters(MatrixItem item, int selectIndex)
	{
		this.tree.refreshParameters(item, selectIndex);
	}
	//endregion
}
