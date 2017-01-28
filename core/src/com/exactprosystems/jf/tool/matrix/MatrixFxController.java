////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.DisplayDriver;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.console.ConsoleText;
import com.exactprosystems.jf.tool.custom.console.CustomListView;
import com.exactprosystems.jf.tool.custom.date.CustomDateTimePicker;
import com.exactprosystems.jf.tool.custom.find.FindPanel;
import com.exactprosystems.jf.tool.custom.find.IFind;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.tab.CustomTabPane;
import com.exactprosystems.jf.tool.custom.treetable.DisplayDriverFx;
import com.exactprosystems.jf.tool.custom.treetable.MatrixContextMenu;
import com.exactprosystems.jf.tool.custom.treetable.MatrixParametersContextMenu;
import com.exactprosystems.jf.tool.custom.treetable.MatrixTreeView;
import com.exactprosystems.jf.tool.dictionary.ApplicationStatus;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.watch.WatcherFx;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.exactprosystems.jf.tool.Common.*;

public class MatrixFxController implements Initializable, ContainingParent, IMatrixListener
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
	public Button						btnStartDefaultApplication;
	public Button						btnConnectDefaultApplication;
	public Button						btnStopDefaultApplication;
	public Button						btnStartDefaultClient;
	public Button						btnStopDefaultClient;

	private WatcherFx					watcher	= null;
	private FindPanel<MatrixItem>		findPanel;
	private boolean						visible	= false;

	private Parent						pane;
	private CustomTab					tab;
	private MatrixFx					model;
	private DisplayDriver				driver;
	private Context						context;
	private boolean						ok;
	private String						exceptionMessage;

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
			Optional.ofNullable(treeItem).ifPresent(item -> Platform.runLater(() -> this.tree.setCurrent(item)));
		}, "Error on moving to item"), true);
		this.listView.autoScroll(true);
		this.listView.setMinHeight(100.0);
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
				tree.setCurrent(treeItem);
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
	}

	// ==============================================================================================================================
	// interface ContainingParent
	// ==============================================================================================================================
	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
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
		Platform.runLater(() ->
		{
			CustomTab tab1 = checkDocument(matrix);
			String format = String.format("Matrix '%s' started...", matrix.getName());
			if (this.listView != null)
			{
				this.listView.getItems().clear();
				this.listView.getItems().add(ConsoleText.defaultText(format));
				Optional.ofNullable(tab1).ifPresent(t -> t.getStyleClass().add(CssVariables.EXECUTING_TAB));
			}
			else
			{
				DialogsHelper.showInfo(format);
			}
		});
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
		Platform.runLater(() ->
		{
			String format = String.format("Matrix '%s' finished.", matrix.getName());
			if (listView != null)
			{
				listView.getItems().add(ConsoleText.defaultText(format));
				Optional.ofNullable(tab).ifPresent(t -> {
					tab.getStyleClass().remove(CssVariables.EXECUTING_TAB);
					Task<Void> task = new Task<Void>()
					{
						@Override
						protected Void call() throws Exception
						{
							if (failed == 0)
							{
								tab.getStyleClass().add(CssVariables.MATRIX_FINISHED_OK);
							}
							else
							{
								tab.getStyleClass().add(CssVariables.MATRIX_FINISHED_BAD);
							}
							Thread.sleep(3000);
							tab.getStyleClass().removeAll(CssVariables.MATRIX_FINISHED_OK, CssVariables.MATRIX_FINISHED_BAD);
							return null;
						}
					};
					new Thread(task).start();
				});
			}
			else
			{
				DialogsHelper.showInfo(format);
			}
		});
	}

	@Override
	public void error(final Matrix matrix, int lineNumber, final MatrixItem item, final String message)
	{
		this.ok = false;
		this.exceptionMessage = String.format("error(%d, %s, %s)", lineNumber, item == null ? "<null>" : item.getPath(), message);
		Platform.runLater(() ->
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
		if (toggleTracing.isSelected())
		{
			Platform.runLater(this.tree::refresh);
		}
	}

	@Override
	public void finished(Matrix matrix, MatrixItem item, Result result)
	{
		if (toggleTracing.isSelected())
		{
			Platform.runLater(this.tree::refresh);
		}
	}

	@Override
	public void paused(Matrix matrix, final MatrixItem item)
	{
		try
		{
			Platform.runLater(this.tree::refresh);
			Optional.ofNullable(this.watcher).ifPresent(WatcherFx::update);
			TreeItem<MatrixItem> treeItem = this.tree.find(item);
			if (treeItem == null)
			{
				DialogsHelper.showInfo(String.format("Matrix paused on \'%s\' in file \'%s\'", item, matrix.getName()));
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

	public void init(MatrixFx model, Context context, TabConsole console)
	{
		Settings settings = context.getFactory().getSettings();
		
		MatrixParametersContextMenu parametersContextMenu 	= new MatrixParametersContextMenu(context, model, this.tree, settings);
		MatrixContextMenu 			rowContextMenu 			= new MatrixContextMenu(context, model, this.tree, settings);
		rowContextMenu.initShortcuts(settings, this.tree, model, context);
		
		this.model = model;
		this.context = context;
		this.driver = new DisplayDriverFx(this.tree, this.context, rowContextMenu, parametersContextMenu);
		this.tree.init(model, settings, rowContextMenu);
		this.tab = CustomTabPane.getInstance().createTab(model);
		this.tab.setContent(this.pane);
		console.setConsole(this.listView);
		CustomTabPane.getInstance().addTab(this.tab);
		CustomTabPane.getInstance().selectTab(this.tab);
		initializeButtons(context.getFactory().getSettings());
		initShortcuts(context.getFactory().getSettings());
	}

	public void saved(String name)
	{
		this.tab.saved(name);
	}

	public void showResult(File file, String matrixName)
	{
		DialogsHelper.displayReport(file, matrixName, this.model.getFactory());
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

	public void close()
	{
		tryCatch(() ->
		{
			this.tab.close();
			CustomTabPane.getInstance().removeTab(this.tab);
			Optional.ofNullable(watcher).ifPresent(WatcherFx::close);
		}, "Error on closing matrix");
	}

	public void displayTab(MatrixItem matrixItem)
	{
		matrixItem.display(this.driver, this.context);
	}

	public void coloring()
	{
		// this.driver.coloring();
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
		tryCatch(this.model::stopMatrix, "Error on stopping matrix");
	}

	public void startMatrix(ActionEvent event)
	{
		tryCatch(this.model::startMatrix, "Error on starting matrix. See the matrix output for details.");
	}

	public void pauseMatrix(ActionEvent event)
	{
		tryCatch(this.model::pauseMatrix, "Error on pausing matrix");
	}

	public void stepMatrix(ActionEvent event)
	{
		tryCatch(this.model::stepMatrix, "Error on stepping matrix");
	}

	public void toogleTracing(ActionEvent event)
	{
		tryCatch(() ->
		{
			boolean b = toggleTracing.isSelected();
			if (b)
			{
				((ImageView) toggleTracing.getGraphic()).setImage(new javafx.scene.image.Image(CssVariables.Icons.COLOR_ON_MATRIX_ICON));
			}
			else
			{
				((ImageView) toggleTracing.getGraphic()).setImage(new javafx.scene.image.Image(CssVariables.Icons.COLOR_OFF_MATRIX_ICON));
			}
			toggleTracing.getTooltip().setText("Color " + (!toggleTracing.isSelected() ? "off" : "on"));
			
			this.context.setTracing(b);
			
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

	public void changeDefaultApp(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.setDefaultApp(cbDefaultApp.getSelectionModel().getSelectedItem()), "Error on changing app");
	}

	public void changeDefaultClient(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.setDefaultClient(cbDefaultClient.getSelectionModel().getSelectedItem()), "Error on changing client");
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
		this.tree.refresh();
	}

	public void startDefaultApplication(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.startDefaultApplication(cbDefaultApp.getSelectionModel().getSelectedItem()), "Error on start default application");
	}

	public void connectDefaultApplication(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.connectDefaultApplication(cbDefaultApp.getSelectionModel().getSelectedItem()), "Error on start default application");
	}

	public void stopDefaultApplication(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.stopDefaultApplication(), "Error on start default application");
	}

	public void startDefaultClient(ActionEvent actionEvent)
	{

	}

	public void stopDefaultClient(ActionEvent actionEvent)
	{

	}
	// ------------------------------------------------------------------------------------------------------------------
	// display* methods
	// ------------------------------------------------------------------------------------------------------------------
	public void displayBeforeStart(String msg)
	{
		Platform.runLater(() ->
		{
			this.listView.getItems().clear();
			this.listView.getItems().add(ConsoleText.defaultText(msg));
		});
	}

	public void refresh()
	{
		Platform.runLater(() -> this.tree.refresh() );
	}

	public void refreshParameters(MatrixItem item, int selectIndex)
	{
		Platform.runLater(() -> this.tree.refreshParameters(item, selectIndex));
	}

	public void setCurrent(MatrixItem item)
	{
		Platform.runLater(() -> {
			TreeItem<MatrixItem> treeItem = this.tree.find(item);
			if (treeItem == null)
			{
				treeItem = this.tree.find(this.tree.getRoot(), matrixItem -> item.getId().equals(matrixItem.getId()));
			}
			this.tree.setCurrent(treeItem);
		} );
	}

	public void remove(MatrixItem item)
	{
		Platform.runLater(() -> this.driver.deleteItem(item) );
	}

	public void display(MatrixItem item)
	{
		Platform.runLater(() -> {
			item.display(this.driver, this.context);
			this.driver.setCurrentItem(item);
		});
	}
	
	public void displayTitle(String title)
	{
		Platform.runLater(() -> this.tab.setTitle(title));
	}

	public void displayAppList(List<String> result)
	{
		Platform.runLater(() -> this.cbDefaultApp.setItems(FXCollections.observableList(result)));
	}

	public void setDefaultApp(String id)
	{
		Platform.runLater(() -> this.cbDefaultApp.getSelectionModel().select(id));
	}

	public void displayClientList(List<String> result)
	{
		Platform.runLater(() -> this.cbDefaultClient.setItems(FXCollections.observableList(result)));
	}

	public void setDefaultClient(String id)
	{
		Platform.runLater(() -> this.cbDefaultClient.getSelectionModel().select(id));
	}

	public void displayApplicationStatus(ApplicationStatus status, AppConnection connection, Throwable throwable)
	{
		Platform.runLater(() -> {
			if (status != null)
			{
				switch (status)
				{
					case Connecting:
					case Connected:
						this.cbDefaultApp.setDisable(true);
						this.btnStartDefaultApplication.setDisable(true);
						this.btnConnectDefaultApplication.setDisable(true);
						break;

					case Disconnected:
						this.cbDefaultApp.setDisable(false);
						this.btnStartDefaultApplication.setDisable(false);
						this.btnConnectDefaultApplication.setDisable(false);
						break;

					default:
                        break;
				}
			}
		});
		Optional.ofNullable(throwable).ifPresent(twrbl -> {
			logger.error(twrbl.getMessage(), twrbl);
			DialogsHelper.showError(twrbl.getMessage());
		});
	}

	// ------------------------------------------------------------------------------------------------------------------
	// private methods
	// ------------------------------------------------------------------------------------------------------------------
	private void initShortcuts(final Settings settings)
	{
		this.pane.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> tryCatch(() ->
		{
			if (tab.isSelected())
			{
				if (SettingsPanel.match(settings, keyEvent, Settings.START_MATRIX))
				{
					model.startMatrix();
				}
				else if (SettingsPanel.match(settings, keyEvent, Settings.STOP_MATRIX))
				{
					model.stopMatrix();
				}
				else if (SettingsPanel.match(settings, keyEvent, Settings.PAUSE_MATRIX))
				{
					model.pauseMatrix();
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
		Platform.runLater(() -> tryCatch(() ->
		{
			btnStartMatrix.setTooltip(new Tooltip("Start\n" + getShortcutTooltip(settings, Settings.START_MATRIX)));
			btnStopMatrix.setTooltip(new Tooltip("Stop\n" + getShortcutTooltip(settings, Settings.STOP_MATRIX)));
			btnPauseMatrix.setTooltip(new Tooltip("Pause\n" + getShortcutTooltip(settings, Settings.PAUSE_MATRIX)));
			btnWatch.setTooltip(new Tooltip("Watch"));
			btnStepMatrix.setTooltip(new Tooltip("Step"));
			btnShowResult.setTooltip(new Tooltip("Show result"));
			toggleTracing.setTooltip(new Tooltip("Color off"));
			toggleTracing.getStyleClass().add(CssVariables.TOGGLE_BUTTON_WITHOUT_BORDER);
			btnFind.setTooltip(new Tooltip("Find\n" + getShortcutTooltip(settings, Settings.FIND_ON_MATRIX)));
			btnStartDefaultApplication.setTooltip(new Tooltip("Start default application"));
			btnConnectDefaultApplication.setTooltip(new Tooltip("Connect default application"));
			btnStopDefaultApplication.setTooltip(new Tooltip("Stop default application"));
			btnStartDefaultClient.setTooltip(new Tooltip("Start default client"));
			btnStopDefaultClient.setTooltip(new Tooltip("Stop default client"));

			customizeLabeled(btnStartDefaultApplication, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.START_APPLICATION);
			customizeLabeled(btnConnectDefaultApplication, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.CONNECT_APPLICATION);
			customizeLabeled(btnStopDefaultApplication, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.STOP_APPLICATION);
			customizeLabeled(btnStopDefaultClient, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.STOP_APPLICATION);
			customizeLabeled(btnStartDefaultClient, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.START_APPLICATION);
			customizeLabeled(btnStartMatrix, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.START_MATRIX_ICON);
			customizeLabeled(btnStopMatrix, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.STOP_MATRIX_ICON);
			customizeLabeled(btnPauseMatrix, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.PAUSE_MATRIX_ICON);
			customizeLabeled(btnWatch, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.WATCH_MATRIX_ICON);
			customizeLabeled(btnStepMatrix, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.STEP_MATRIX_ICON);
			customizeLabeled(btnShowResult, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.SHOW_RESULT_MATRIX_ICON);
			customizeLabeled(toggleTracing, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.COLOR_OFF_MATRIX_ICON);
			customizeLabeled(btnFind, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.FIND_ON_MATRIX);

			sizeButtons(BUTTON_SIZE_WITH_ICON, btnStartMatrix, btnStopMatrix, btnPauseMatrix, btnWatch, btnStepMatrix, btnShowResult);
		}, "Error on setting tooltip or images"));
	}
}
