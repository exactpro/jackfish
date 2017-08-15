////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix.schedule;

import com.exactprosystems.jf.api.common.MatrixState;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class ScheduleController implements Initializable, ContainingParent
{
	public Button                     btnStart;
	public Button                     btnStop;
	public Button                     btnDestroy;
	public Button                     btnShowSelected;
	public Button                     btnLoadSeveral;
	private Parent                    parent;
	private RunnerScheduler           model;
	private Dialog<?>                 dialog;
	public TableView<MatrixWithState> tableView;

	public TableColumn<MatrixWithState, String>          columnMatrixName;
	public TableColumn<MatrixWithState, Date>            columnStartDate;
	public TableColumn<MatrixWithState, MatrixState>     columnState;
	public TableColumn<MatrixWithState, MatrixWithState> columnCheckBox;
	public TableColumn<MatrixWithState, String>          columnDone;

	private static final SimpleDateFormat formatter = new SimpleDateFormat(Common.DATE_TIME_PATTERN);

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
		columnCheckBox.setCellFactory(p -> new TableCell<MatrixWithState, MatrixWithState>()
		{
			private CheckBox box = new CheckBox();

			@Override
			protected void updateItem(MatrixWithState item, boolean empty)
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

		columnMatrixName.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getMatrix().getNameProperty().get()));
		columnMatrixName.prefWidthProperty().bind(this.tableView.widthProperty().subtract(widthCheckBox + widthDate + widthState + widthDone + 2));

		columnStartDate.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().date));
		columnStartDate.prefWidthProperty().bind(new SimpleObjectProperty<>(widthDate));
		columnStartDate.setEditable(true);
		columnStartDate.setCellFactory(param -> new TableCell<MatrixWithState, Date>()
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
		});

		columnState.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getState()));
		columnState.prefWidthProperty().bind(new SimpleObjectProperty<>(widthState));

		columnDone.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getExecuted()));
		columnDone.prefWidthProperty().bind(new SimpleObjectProperty<>(widthDone));
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
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		this.dialog.setResizable(true);
		this.dialog.initModality(Modality.NONE);
		this.dialog.initOwner(window);
		this.dialog.setHeaderText("");
		this.dialog.getDialogPane().setHeader(new Label());
		this.dialog.setTitle("Monitoring matrices");
		this.dialog.getDialogPane().setContent(this.parent);
		this.dialog.getDialogPane().setPrefWidth(600);
		this.dialog.getDialogPane().setPrefHeight(600);
		this.dialog.getDialogPane().setPadding(new Insets(-15,0,0,0));
		this.dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		this.dialog.showAndWait();
	}

	void displayState(Matrix matrix, MatrixState state, int done, int total)
	{
		switch (state)
		{
			case Created:
				this.addMatrix(matrix);
				break;
			case Running:
				changeDate(matrix);
				changeState(matrix, state, done, total);
				break;
			case Waiting:
				clearDate(matrix);
				changeState(matrix, state, done, total);
				break;
			case Pausing:
			case Stopped:
			case Finished:
				changeState(matrix, state, done, total);
				break;
			case Destroyed:
				this.removeMatrix(matrix);
				break;
		}
		refresh();
	}

	boolean isShowing()
	{
		return !(this.dialog == null || !this.dialog.isShowing());
	}

	//region Action methods
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
	//endregion

	//region private methods

	private Optional<MatrixWithState> getFirst(Matrix matrix)
	{
		return this.tableView.getItems().stream().filter(m -> m.matrix.equals(matrix)).findFirst();
	}

	private void refresh()
	{
		Platform.runLater(() -> {
			this.tableView.getColumns().get(0).setVisible(false);
			this.tableView.getColumns().get(0).setVisible(true);
		});
	}

	private List<Matrix> getSelected()
	{
		return this.tableView.getItems()
				.stream()
				.filter(MatrixWithState::isChecked)
				.map(MatrixWithState::getMatrix)
				.collect(Collectors.toList());
	}

	private void addMatrix(Matrix matrix)
	{
		MatrixWithState matrixWithState = new MatrixWithState(matrix);
		matrixWithState.state = MatrixState.Created;
		if (!this.getFirst(matrix).isPresent())
		{
			this.tableView.getItems().add(matrixWithState);
		}
	}

	private void removeMatrix(Matrix matrix)
	{
		this.tableView.getItems().removeIf(e -> e.matrix == matrix);
	}

	private void changeDate(Matrix matrix)
	{
		final Date date = new Date();
		this.tableView.getItems().stream()
				.filter(m -> m.matrix == matrix && m.date == null)
				.findFirst()
				.ifPresent(m -> m.date = date);
	}

	private void changeState(Matrix matrix, MatrixState state, int done, int total)
	{
		getFirst(matrix).ifPresent(matrixWithState -> {
			matrixWithState.state = state;
			matrixWithState.checked = matrixWithState.isChecked();
			matrixWithState.executed = done + " / " + total;
		});
	}

	private void clearDate(Matrix matrix)
	{
		getFirst(matrix).ifPresent(m -> m.date = null);
	}

	//endregion

	private class MatrixWithState
	{
		private String      executed;
		private boolean     checked;
		private Matrix      matrix;
		private MatrixState state;
		private Date		date;

		public MatrixWithState(Matrix matrix)
		{
			this.matrix = matrix;
		}

		public void setState(MatrixState state)
		{
			this.state = state;
		}

		public Matrix getMatrix()
		{
			return matrix;
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

		public Date getDate()
		{
			return date;
		}

		public void setDate(Date date)
		{
			this.date = date;
		}

		@Override
		public boolean equals(Object o)
		{
			return this == o || !(o == null || getClass() != o.getClass()) && matrix.equals(((MatrixWithState) o).matrix);
		}

		@Override
		public int hashCode()
		{
			return matrix.hashCode();
		}
	}
}
