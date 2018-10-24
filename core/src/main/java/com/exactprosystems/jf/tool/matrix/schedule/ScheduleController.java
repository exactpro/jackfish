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

package com.exactprosystems.jf.tool.matrix.schedule;

import com.exactprosystems.jf.api.common.MatrixState;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
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

import static com.exactprosystems.jf.tool.settings.Theme.currentThemesPaths;

@SuppressWarnings("deprecation")
public class ScheduleController implements Initializable, ContainingParent
{
	public  Button                     btnStart;
	public  Button                     btnStop;
	public  Button                     btnDestroy;
	public  Button                     btnShowSelected;
	public  Button                     btnLoadSeveral;
	private Parent                     parent;
	private MatrixScheduler            model;
	private Dialog<?>                  dialog;
	public  TableView<MatrixWithState> tableView;

	public TableColumn<MatrixWithState, String>          columnMatrixName;
	public TableColumn<MatrixWithState, Date>            columnStartDate;
	public TableColumn<MatrixWithState, MatrixState>     columnState;
	public TableColumn<MatrixWithState, MatrixWithState> columnCheckBox;
	public TableColumn<MatrixWithState, String>          columnDone;
	public TableColumn<MatrixWithState, MatrixWithState> columnReport;

	private static final SimpleDateFormat formatter = new SimpleDateFormat(Common.DATE_TIME_PATTERN);

	private static final int widthCheckBox	= 30;
	private static final int widthDate		= 160;
	private static final int widthState		= 70;
	private static final int widthDone		= 90;
	private static final int widthReport	= 90;

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
			@Override
			protected void updateItem(MatrixWithState item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null)
				{
					CheckBox box = new CheckBox();
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
		columnMatrixName.prefWidthProperty().bind(this.tableView.widthProperty().subtract(widthCheckBox + widthDate + widthReport + widthState + widthDone + 2));

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

		columnReport.prefWidthProperty().bind(new SimpleObjectProperty<>(widthReport));
		columnReport.setStyle("-fx-alignment: CENTER");
		columnReport.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
		columnReport.setCellFactory(p -> new TableCell<MatrixWithState, MatrixWithState>()
		{
			@Override
			protected void updateItem(MatrixWithState item, boolean empty)
			{
				super.updateItem(item, empty);
				if (!empty && item.getState().equals(MatrixState.Finished))
				{
					Button btn = new Button();
					btn.getStyleClass().add("transparentBackground");
					btn.setId("scheduleShowResultBtn");
					btn.setOnAction(event -> showReport(item.getMatrix()));
					setGraphic(btn);

				}
				else
				{
					setGraphic(null);
				}
			}
		});
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	public void init(MatrixScheduler runnerScheduler)
	{
		this.model = runnerScheduler;
	}

	public void show(Window window)
	{
		this.dialog = new Alert(Alert.AlertType.INFORMATION);
		DialogsHelper.centreDialog(this.dialog);
		Common.addIcons(((Stage) this.dialog.getDialogPane().getScene().getWindow()));
		this.dialog.setResizable(true);
		this.dialog.initModality(Modality.NONE);
		this.dialog.initOwner(window);
		this.dialog.setHeaderText("");
		this.dialog.getDialogPane().setHeader(new Label());
		this.dialog.setTitle(R.SCHEDULE_TITLE.get());
		this.dialog.getDialogPane().setContent(this.parent);
		this.dialog.getDialogPane().setPrefWidth(600);
		this.dialog.getDialogPane().setPrefHeight(600);
		this.dialog.getDialogPane().setPadding(new Insets(-15,0,0,0));
		this.dialog.getDialogPane().getStylesheets().addAll(currentThemesPaths());
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
				this.changeDate(matrix);
				this.changeState(matrix, state, done, total);
				break;
			case Waiting:
				this.addMatrix(matrix);
				this.clearDate(matrix);
				this.changeState(matrix, state, done, total);
				break;
			case Pausing:
			case Stopped:
			case Finished:
				this.changeState(matrix, state, done, total);
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
		Common.tryCatch(() -> this.model.startSelected(getSelected()), R.SCHEDULE_CONTR_ERROR_START.get());
	}

	public void stopSelected(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.stopSelected(getSelected()), R.SCHEDULE_CONTR_ERROR_STOP.get());
	}

	public void destroySelected(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.destroySelected(getSelected()), R.SCHEDULE_CONTR_ERROR_STOP.get());
	}

	public void showSelected(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> this.model.showSelected(getSelected()), R.SCHEDULE_CONTR_ERROR_SHOW.get());
	}

	public void loadSeveral(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::loadSeveral, R.SCHEDULE_CONTR_ERROR_LOAD.get());
	}

	public void showReport(Matrix matrix)
	{
		this.model.showReport(matrix);
	}
	//endregion

	//region private methods

	private Optional<MatrixWithState> getFirst(Matrix matrix)
	{
		return this.tableView.getItems().stream().filter(m -> m.matrix == matrix).findFirst();
	}

	private void refresh()
	{
		Common.runLater(() -> {
			this.tableView.getColumns().get(0).setVisible(false);
			this.tableView.getColumns().get(0).setVisible(true);
		});
	}

	private List<Matrix> getSelected()
	{
		List<Matrix> collect = this.tableView.getItems().stream().filter(MatrixWithState::isChecked).map(MatrixWithState::getMatrix).collect(Collectors.toList());
		return collect;
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
