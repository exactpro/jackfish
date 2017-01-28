////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix.schedule;

import com.exactprosystems.jf.api.common.IMatrixRunner;
import com.exactprosystems.jf.api.common.MatrixState;
import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.date.CustomDateTimePicker;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ScheduleController implements Initializable, ContainingParent
{
	public Button btnStart;
	public Button btnStop;
	public Button btnDestroy;
	public Button btnShowSelected;
	public Button btnLoadSeveral;
	private Parent parent;
	private RunnerScheduler model;
	private Dialog<?> dialog;
	public TableView<RunnerWithState> tableView;

	public TableColumn<RunnerWithState, String> columnMatrixName;
	public TableColumn<RunnerWithState, Date> columnStartDate;
	public TableColumn<RunnerWithState, MatrixState> columnState;
	public TableColumn<RunnerWithState, RunnerWithState> columnCheckBox;
	public TableColumn<RunnerWithState, String> columnDone;

	public static final SimpleDateFormat formatter = new SimpleDateFormat(Common.DATE_TIME_PATTERN);

	private static final int widthCheckBox	= 30;
	private static final int widthDate		= 160;
	private static final int widthState		= 70;
	private static final int widthDone		= 90;


	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		CheckBox box = new CheckBox();
		box.setOnAction(event -> {
			this.tableView.getItems().forEach(runner -> runner.setChecked(box.isSelected()));
			refresh();
		});
		columnCheckBox.setGraphic(box);
		columnCheckBox.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
		columnCheckBox.prefWidthProperty().bind(new SimpleObjectProperty<>(widthCheckBox));
		columnCheckBox.setCellFactory(p -> new TableCell<RunnerWithState, RunnerWithState>()
		{
			private CheckBox box = new CheckBox();

			@Override
			protected void updateItem(RunnerWithState item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null)
				{
					box.setSelected(item.isChecked());
					box.selectedProperty().addListener((observable, oldValue, newValue) -> item.setChecked(newValue));
					setGraphic(box);
				}
				else
				{
					setGraphic(null);
				}
			}
		});

		columnMatrixName.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getRunner().getMatrixName()));
		columnMatrixName.prefWidthProperty().bind(this.tableView.widthProperty().subtract(widthCheckBox + widthDate + widthState + widthDone + 2));

		columnStartDate.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getRunner().getStartTime()));
		columnStartDate.prefWidthProperty().bind(new SimpleObjectProperty<>(widthDate));
		columnStartDate.setEditable(true);
		columnStartDate.setCellFactory(param -> new TableCell<RunnerWithState, Date>()
		{
			@Override
			protected void updateItem(Date item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null)
				{
					setText(formatter.format(item));
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
				else
				{
					setGraphic(null);
					setText(null);
				}
			}

			@Override
			public void startEdit()
			{
				super.startEdit();
				TableColumn<RunnerWithState, Date> tableColumn = getTableColumn();
				if (tableColumn.isEditable())
				{
					CustomDateTimePicker picker = createPicker();
					setGraphic(picker);
					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					Platform.runLater(picker::requestFocus);
				}
			}

			@Override
			public void commitEdit(Date newValue)
			{
				super.commitEdit(newValue);
				RunnerWithState runner = (RunnerWithState) getTableRow().getItem();
				runner.getRunner().setStartTime(newValue);
			}

			@Override
			public void cancelEdit()
			{
				super.cancelEdit();
				setText(formatter.format(getItem()));
				setContentDisplay(ContentDisplay.TEXT_ONLY);
			}

			private CustomDateTimePicker createPicker()
			{
				return new CustomDateTimePicker(getItem(), this::commitEdit);
			}
		});

		columnState.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getState()));
		columnState.prefWidthProperty().bind(new SimpleObjectProperty<>(widthState));

		columnDone.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getExecuted()));
		columnDone.prefWidthProperty().bind(new SimpleObjectProperty<>(widthDone));

		initializeButtons();
	}

	private void initializeButtons()
	{
		Common.customizeLabeled(this.btnStart, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.START_MATRIX_ICON);
		Common.customizeLabeled(this.btnStop, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.STOP_MATRIX_ICON);
		Common.customizeLabeled(this.btnDestroy, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.DESTROY_MATRIX_ICON);
		Common.customizeLabeled(this.btnLoadSeveral, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.FOLDER_MATRIX_ICON);
		Common.customizeLabeled(this.btnShowSelected, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.OPEN_MATRIX_ICON);
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	public void init(RunnerScheduler runnerScheduler)
	{
		this.model = runnerScheduler;
	}

	public void show(Window window)
	{
		this.dialog = new Alert(Alert.AlertType.INFORMATION);
		this.dialog.setResizable(true);
		this.dialog.initModality(Modality.NONE);
		this.dialog.initOwner(window);
		this.dialog.setHeaderText("Monitoring matrices");
		this.dialog.getDialogPane().setContent(this.parent);
		this.dialog.getDialogPane().setPrefWidth(600);
		this.dialog.getDialogPane().setPrefHeight(600);
		this.dialog.setTitle("Monitoring");
		this.dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		this.dialog.showAndWait();
	}

	public void displayRunner(IMatrixRunner runner)
	{
        this.tableView.getItems().add(new RunnerWithState(runner));
	}

	public void removeRunner(IMatrixRunner runner)
	{
		this.tableView.getItems().removeIf(e -> e.runner == runner); 
	}

	public void displayState(IMatrixRunner runner, MatrixState state, int done, int total)
	{
		this.tableView.getItems().stream().filter(r -> r.getRunner().equals(runner)).findFirst().ifPresent(runnerWithState -> {
			runnerWithState.setState(state);
			runnerWithState.setChecked(runnerWithState.isChecked());
			runnerWithState.setExecuted(done + " / " + total);
		});
		refresh();
	}

	private void refresh()
	{
		Platform.runLater(() -> {
			this.tableView.getColumns().get(0).setVisible(false);
			this.tableView.getColumns().get(0).setVisible(true);
		});
	}

	public boolean isShowing()
	{
		return !(this.dialog == null || !this.dialog.isShowing());
	}

	public void startSelected(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.startSelected(getSelected()), "Error on start matrices");
	}

	public void stopSelected(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.stopSelected(getSelected()), "Error on stop matrices");
	}

	public void destroySelected(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.destroySelected(getSelected()), "Error on stop matrices");
	}

	public void showSelected(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.showSelected(getSelected()), "Error on show matrices");
	}

	public void loadSeveral(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::loadSeveral, "Error on load matrices");
	}

	private List<IMatrixRunner> getSelected()
	{
		return this.tableView.getItems()
				.stream()
				.filter(RunnerWithState::isChecked)
				.map(RunnerWithState::getRunner)
				.collect(Collectors.toList());
	}

	private class RunnerWithState {
		private String executed;
		private boolean checked;
		private IMatrixRunner runner;
		private MatrixState state;

		public RunnerWithState(IMatrixRunner runner)
		{
			this.runner = runner;
		}

		public void setState(MatrixState state)
		{
			this.state = state;
		}

		public IMatrixRunner getRunner()
		{
			return runner;
		}

		public MatrixState getState()
		{
			return state;
		}

		public boolean isChecked()
		{
			return checked;
		}

		public void setChecked(boolean checked)
		{
			this.checked = checked;
		}

		public String getExecuted()
		{
			return executed;
		}

		public void setExecuted(String executed)
		{
			this.executed = executed;
		}

		@Override
		public boolean equals(Object o)
		{
			return this == o || !(o == null || getClass() != o.getClass()) && runner.equals(((RunnerWithState) o).runner);
		}

		@Override
		public int hashCode()
		{
			return runner.hashCode();
		}
	}
}
