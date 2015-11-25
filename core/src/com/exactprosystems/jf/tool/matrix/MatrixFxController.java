////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix;

import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.parser.DisplayDriver;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.Result;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.console.ConsoleText;
import com.exactprosystems.jf.tool.custom.console.CustomListView;
import com.exactprosystems.jf.tool.custom.date.CustomDateTimePicker;
import com.exactprosystems.jf.tool.custom.find.FindPanel;
import com.exactprosystems.jf.tool.custom.find.IFind;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.treetable.DisplayDriverFx;
import com.exactprosystems.jf.tool.custom.treetable.MatrixTreeView;
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
	public ToggleButton					toggleBtnColor;
	public CustomListView<MatrixItem>	listView;
	public Button						btnWatch;
	public ScrollPane					mainScrollPane;
	public ComboBox<String>				cbDefaultApp;
	public ComboBox<String>				cbDefaultClient;
	public Button						btnFind;
	public SplitPane					splitPane;
	public GridPane						gridPane;
	public HBox							hBox;

	private WatcherFx					watcher	= null;
	private FindPanel<MatrixItem>		findPanel;
	private boolean						visible	= false;

	private Parent						pane;
	private CustomTab					tab;
	private MatrixFx					model;
	private DisplayDriver				driver;
	private Context						context;
	private boolean						ok;
	private String 						exceptionMessage;


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
		assert toggleBtnColor != null : "fx:id=\"toggleBtnColor\" was not injected: check your FXML file 'MatrixFx.fxml'.";
		
		this.listView = new CustomListView<>(matrixItem -> tryCatch(() ->
		{
			TreeItem<MatrixItem> treeItem = this.tree.find(matrixItem);
			Optional.ofNullable(treeItem).ifPresent(item -> Platform.runLater(() -> this.tree.setCurrent(item)));
		}, "Error on move to item"), true);
		
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

	// ------------------------------------------------------------------------------------------------------------------------------
	// MatrixListener
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
			if (listView != null)
			{
				listView.getItems().add(ConsoleText.defaultText(format));
				Optional.ofNullable(tab1).ifPresent(t -> t.getStyleClass().add(CssVariables.EXECUTING_TAB));
			}
			else
			{
				DialogsHelper.showInfo(format);
			}
		});
	}

	@Override
	public void matrixFinished(final Matrix matrix, final int passed, final int failed)
	{
		Platform.runLater(() ->
		{
			String format = String.format("Matrix '%s' finished.	  PASSED: %d FAILED: %d", matrix.getName(), passed, failed);
			if (listView != null)
			{
				listView.getItems().add(ConsoleText.defaultText(format));
				Optional.ofNullable(tab).ifPresent(t -> {
					t.getStyleClass().remove(CssVariables.EXECUTING_TAB);
					Task<Void> task = new Task<Void>()
					{
						@Override
						protected Void call() throws Exception
						{
							if (failed == 0)
							{
								t.getStyleClass().add(CssVariables.MATRIX_FINISHED_OK);
							}
							else
							{
								t.getStyleClass().add(CssVariables.MATRIX_FINISHED_BAD);
							}
							Thread.sleep(3000);
							t.getStyleClass().removeAll(CssVariables.MATRIX_FINISHED_OK, CssVariables.MATRIX_FINISHED_BAD);
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
		if (this.tree != null && toggleBtnColor.isSelected())
		{
			TreeItem<MatrixItem> find = this.tree.find(treeItem -> treeItem.getNumber() == item.getNumber());
			Optional.ofNullable(find).ifPresent(f -> Platform.runLater(() -> {
				GridPane layout = (GridPane) f.getValue().getLayout();
				Optional.ofNullable(layout).ifPresent(l -> l.getStyleClass().add(CssVariables.EXECUTING_MATRIX_ITEM));
			}));
		}
	}

	@Override
	public void finished(Matrix matrix, MatrixItem item, Result result)
	{
		Optional.ofNullable(this.tree).ifPresent(treeView -> {
			TreeItem<MatrixItem> find = treeView.find(treeItem -> treeItem.getNumber() == item.getNumber());
			Optional.ofNullable(find).ifPresent(f -> Platform.runLater(() -> {
				GridPane layout = (GridPane) f.getValue().getLayout();
				Optional.ofNullable(layout).ifPresent(l -> {
					l.getStyleClass().remove(CssVariables.EXECUTING_MATRIX_ITEM);
					l.getStyleClass().remove(CssVariables.PAUSED_MATRIX_ITEM);
				});
			}));
		});
	}

	@Override
	public void paused(Matrix matrix, final MatrixItem item)
	{
		Optional.ofNullable(this.watcher).ifPresent(WatcherFx::update);
		Optional.ofNullable(this.tree).ifPresent(tree -> Platform.runLater(() -> {
			TreeItem<MatrixItem> treeItem = tree.find(item);
			((GridPane) treeItem.getValue().getLayout()).getStyleClass().add(CssVariables.PAUSED_MATRIX_ITEM);
			listView.getItems().add(ConsoleText.pausedItem(String.format("Matrix paused on \'%s\'", treeItem.getValue().getItemName()), item));
		}));
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
		Settings settings = context.getConfiguration().getSettings();
		this.model = model;
		this.context = context;
		this.driver = new DisplayDriverFx(this.tree, this.context);
		this.tree.init(model, settings);
		this.tab = createTab(model);
		this.tab.setContent(this.pane);
		console.setConsole(this.listView);
		getTabPane().getTabs().add(this.tab);
		if (isNeedSelectedTab())
		{
			getTabPane().getSelectionModel().select(this.tab);
		}
		initializeButtons(context.getConfiguration().getSettings());
		initShortcuts(context.getConfiguration().getSettings());
	}

	public void saved(String name)
	{
		this.tab.saved(name);
	}

	public void showResult(File file, String matrixName)
	{
		DialogsHelper.displayReport(file, matrixName, null, null);
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
		}, "Error on show watcher ");
	}

	public void close()
	{
		tryCatch(() ->
		{
			this.tab.close();
			getTabPane().getTabs().remove(this.tab);
			Optional.ofNullable(watcher).ifPresent(WatcherFx::close);
		}, "Error on close matrix");
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
		tryCatch(this.model::showResult, "Error on show result");
	}

	public void stopMatrix(ActionEvent event)
	{
		tryCatch(this.model::stopMatrix, "Error on stop matrix");
	}

	public void startMatrix(ActionEvent event)
	{
		listView.getItems().add(ConsoleText.defaultText("Prepare to start matrix..."));
		tryCatch(this.model::startMatrix, "Error on start matrix. See the matrix output for details.");
	}

	public void pauseMatrix(ActionEvent event)
	{
		tryCatch(this.model::pauseMatrix, "Error on pause matrix");
	}

	public void stepMatrix(ActionEvent event)
	{
		tryCatch(this.model::stepMatrix, "Error on pause matrix");
	}

	public void setColor(ActionEvent event)
	{
		tryCatch(() ->
		{
			boolean b = toggleBtnColor.isSelected();
			if (b)
			{
				((ImageView) toggleBtnColor.getGraphic()).setImage(new javafx.scene.image.Image(CssVariables.Icons.COLOR_ON_MATRIX_ICON));
			}
			else
			{
				((ImageView) toggleBtnColor.getGraphic()).setImage(new javafx.scene.image.Image(CssVariables.Icons.COLOR_OFF_MATRIX_ICON));
			}
			toggleBtnColor.getTooltip().setText("Color " + (!toggleBtnColor.isSelected() ? "off" : "on"));
		}, "Error on set color");
	}

	public void showWatch(ActionEvent event)
	{
		tryCatch(this.model::showWatch, "Error on show watcher");
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
		tryCatch(() -> this.model.changeDefaultApp(cbDefaultApp.getSelectionModel().getSelectedItem()), "Error on change app");
	}

	public void changeDefaultClient(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.changeDefaultClient(cbDefaultClient.getSelectionModel().getSelectedItem()), "Error on change client");
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
		tryCatch(() -> this.model.markFirstLevel(flag), "Error on mark all");
		this.tree.refresh();
	}

	// ------------------------------------------------------------------------------------------------------------------
	// display* methods
	// ------------------------------------------------------------------------------------------------------------------
	public void refresh()
	{
		Platform.runLater(() -> this.tree.refresh() );
	}

	public void refreshParameters(MatrixItem item)
	{
		Platform.runLater(() -> this.tree.refreshParameters(item) );
	}

	public void setCurrent(MatrixItem item)
	{
		Platform.runLater(() -> this.tree.setCurrent(this.tree.find(item)) );
	}

	public void remove(MatrixItem item)
	{
		Platform.runLater(() -> this.driver.deleteItem(item) );
	}

	public void display(MatrixItem item)
	{
		Platform.runLater(() -> item.display(this.driver, this.context) );
	}
	
	public void displayTitle(String title)
	{
		Platform.runLater(() -> this.tab.setTitle(title));
	}

	public void displayAppList(List<String> result)
	{
		Platform.runLater(() -> this.cbDefaultApp.setItems(FXCollections.observableList(result)));
	}

	public void displayClientList(List<String> result)
	{
		Platform.runLater(() -> this.cbDefaultClient.setItems(FXCollections.observableList(result)));
	}

	// ------------------------------------------------------------------------------------------------------------------
	// private methods
	// ------------------------------------------------------------------------------------------------------------------
	private void initShortcuts(final Settings settings)
	{
		getTabPane().getScene().addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> tryCatch(() ->
		{
			if (tab.isSelected())
			{
				if (SettingsPanel.match(settings, keyEvent, SettingsPanel.START_MATRIX))
				{
					model.startMatrix();
				}
				else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.STOP_MATRIX))
				{
					model.stopMatrix();
				}
				else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.PAUSE_MATRIX))
				{
					model.pauseMatrix();
				}
				else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.SHOW_RESULT))
				{
					model.showResult();
				}
				else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.SHOW_WATCH))
				{
					model.showWatch();
				}
				else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.COLORING))
				{
					model.setColor();
				}
				else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.FIND_ON_MATRIX))
				{
					showFindPanel(null);
				}
			}
		}, "Error on set shortcuts"));

	}

	private void initializeButtons(final Settings settings)
	{
		Platform.runLater(() -> tryCatch(() ->
		{
			btnStartMatrix.setTooltip(new Tooltip("Start\n" + getShortcutTooltip(settings, SettingsPanel.START_MATRIX)));
			btnStopMatrix.setTooltip(new Tooltip("Stop\n" + getShortcutTooltip(settings, SettingsPanel.STOP_MATRIX)));
			btnPauseMatrix.setTooltip(new Tooltip("Pause\n" + getShortcutTooltip(settings, SettingsPanel.PAUSE_MATRIX)));
			btnWatch.setTooltip(new Tooltip("Watch"));
			btnStepMatrix.setTooltip(new Tooltip("Step"));
			btnShowResult.setTooltip(new Tooltip("Show result"));
			toggleBtnColor.setTooltip(new Tooltip("Color off"));
			btnFind.setTooltip(new Tooltip("Find\n" + getShortcutTooltip(settings, SettingsPanel.FIND_ON_MATRIX)));

			customizeLabeled(btnStartMatrix, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.START_MATRIX_ICON);
			customizeLabeled(btnStopMatrix, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.STOP_MATRIX_ICON);
			customizeLabeled(btnPauseMatrix, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.PAUSE_MATRIX_ICON);
			customizeLabeled(btnWatch, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.WATCH_MATRIX_ICON);
			customizeLabeled(btnStepMatrix, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.STEP_MATRIX_ICON);
			customizeLabeled(btnShowResult, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.SHOW_RESULT_MATRIX_ICON);
			customizeLabeled(toggleBtnColor, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.COLOR_OFF_MATRIX_ICON);
			customizeLabeled(btnFind, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.FIND_ON_MATRIX);

			sizeButtons(BUTTON_SIZE_WITH_ICON, btnStartMatrix, btnStopMatrix, btnPauseMatrix, btnWatch, btnStepMatrix, btnShowResult);
		}, "Error on set tooltip or images"));
	}
}
