////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.documents.guidic.actions;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.app.Resize;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.matrix.parser.listeners.ListProvider;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.BorderWrapper;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.custom.number.NumberTextField;
import com.exactprosystems.jf.tool.documents.guidic.ApplicationStatus;
import com.exactprosystems.jf.tool.documents.guidic.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.helpers.ExpressionFieldsPane;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.logger;
import static com.exactprosystems.jf.tool.Common.tryCatch;

public class ActionsController implements Initializable, ContainingParent
{
	public Label imageArea;

	public ComboBox<String> comboBoxApps;
	public ComboBox<String> comboBoxAppsStore;
	public Button           btnStartApplication;
	public Button           btnConnectApplication;
	public Button           btnStop;
	public TextField        tfSendKeys;
	public ComboBox<String> comboBoxTitles;

	public GridPane                       mainGrid;
	public GridPane                       doGridPane;
	public NumberTextField                ntfMoveToX;
	public NumberTextField                ntfMoveToY;
	public ToggleGroup                    groupSection;
	public RadioButton                    rbMin;
	public RadioButton                    rbMax;
	public RadioButton                    rbNormal;
	public RadioButton                    rbSize;
	public NumberTextField                ntfResizeH;
	public NumberTextField                ntfResizeW;
	public ExpressionField                efGetProperty;
	public ExpressionField                efSetProperty;
	public GridPane                       propGridPane;
	public ComboBox<String>               cbGetProperty;
	public ComboBox<String>               cbSetProperty;
	public ListView<ExpressionFieldsPane> lvNewInstance;
	public Button                         btnNewInstance;

	public ToggleGroup      dialogGroupSection;
	public NumberTextField  ntfDialogMoveToX;
	public NumberTextField  ntfDialogMoveToY;
	public RadioButton      rbDialogMin;
	public RadioButton      rbDialogMax;
	public RadioButton      rbDialogNormal;
	public RadioButton      rbDialogSize;
	public NumberTextField  ntfDialogResizeH;
	public NumberTextField  ntfDialogResizeW;
	public GridPane         propDialogGridPane;
	public ComboBox<String> cbGetDialogProperty;

	private ExpressionField doExpressionField;
	private Parent          pane;

	private DictionaryFx      model;
	private AbstractEvaluator evaluator;

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
		Common.runLater(() -> ((BorderPane) this.pane).setCenter(BorderWrapper.wrap(this.mainGrid).title(R.ACTIONS_CONTROLLER_TITLE.get()).color(Theme.currentTheme().getReverseColor()).build()));
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		this.imageArea.getStyleClass().add(CssVariables.IMAGE_AREA);
		setDisablePosAndSizeButtons(true);
		setDisableDialogsButtons(true);

		this.groupSection.selectedToggleProperty().addListener((observable, oldValue, newValue) -> setDisablePosAndSizeButtons(!(newValue != null && newValue == this.rbSize)));
		this.rbMin.setUserData(Resize.Minimize);
		this.rbMax.setUserData(Resize.Maximize);
		this.rbNormal.setUserData(Resize.Normal);
		this.rbSize.setUserData(null);

		this.dialogGroupSection.selectedToggleProperty().addListener((observable, oldValue, newValue) -> setDisableDialogsButtons(!(newValue != null && newValue == this.rbDialogSize)));
		this.rbDialogMin.setUserData(Resize.Minimize);
		this.rbDialogMax.setUserData(Resize.Maximize);
		this.rbDialogNormal.setUserData(Resize.Normal);
		this.rbDialogSize.setUserData(null);
	}

	public void init(DictionaryFx model, AbstractEvaluator evaluator, Consumer<Parent> consumer)
	{
		this.model = model;
		this.evaluator = evaluator;
		this.doExpressionField = new ExpressionField(evaluator);
		this.doGridPane.add(this.doExpressionField, 0, 1, 2, 1);
		HBox.setHgrow(this.doExpressionField, Priority.ALWAYS);
		this.doExpressionField.setHelperForExpressionField(null, null);

		consumer.accept(this.pane);
		GridPane.setColumnSpan(this.pane, 2);

		this.efGetProperty = new ExpressionField(evaluator);
		this.efGetProperty.setHelperForExpressionField(null, null);

		this.efSetProperty = new ExpressionField(evaluator);
		this.efSetProperty.setHelperForExpressionField(null, null);

		this.propGridPane.add(this.efGetProperty, 1, 0);
		this.propGridPane.add(this.efSetProperty, 1, 1);
		this.btnNewInstance.setDisable(true);

		this.comboBoxApps.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> this.model.setCurrentApp(newValue));

		this.comboBoxAppsStore.setOnShowing(event -> this.comboBoxAppsStore.getItems().setAll(this.storedConnections()));
		this.comboBoxAppsStore.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> this.model.setCurrentStoredApp(newValue));

		this.comboBoxTitles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> tryCatch(() -> this.model.switchTo(newValue), R.ACTIONS_CONTROLLER_ERROR_ON_SWITCH.get()));
	}

	//region application
	public void startApplication(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.startApplication(), R.ACTIONS_CONTROLLER_ERROR_ON_START.get());
	}

	public void connectApplication(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.connectToApplication(), R.ACTIONS_CONTROLLER_ERROR_ON_CONNECT.get());
	}

	public void stopConnection(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.stopApplication(), R.ACTIONS_CONTROLLER_ERROR_ON_STOP.get());
	}
	//endregion

	public void showEditVariables(ActionEvent event)
	{
		new EditVariableDialog(this.model.parametersProperty(), this.evaluator)
				.showAndWait()
				.ifPresent(this.model.parametersProperty()::from);
	}

	//region Do tab
	public void sendKeys(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.sendKeys(this.tfSendKeys.getText()), R.ACTIONS_CONTROLLER_ERROR_ON_SEND_KEYS.get());
	}

	public void doIt(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.doIt(this.doExpressionField.getEvaluatedValue()), R.ACTIONS_CONTROLLER_ERROR_ON_OPERATE.get());
	}

	public void click(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.click(), R.ACTIONS_CONTROLLER_ERROR_ON_CLICK.get());
	}

	public void find(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.find(), R.ACTIONS_CONTROLLER_ERROR_ON_FIND.get());
	}

	public void getValue(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.getValue(), R.ACTIONS_CONTROLLER_ERROR_ON_GET_VALUE.get());
	}

	//endregion

	//region Switch tab
	public void switchToParent(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.switchToParent(), R.ACTIONS_CONTROLLER_ERROR_ON_SWITCH_TO_CURRENT.get());
	}

	public void switchToCurrent(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.switchToCurrent(), R.ACTIONS_CONTROLLER_ERROR_ON_SWITCH_TO_CURRENT.get());
	}

	public void refreshTitles(ActionEvent event)
	{
		tryCatch(() -> this.model.refreshTitles(), R.ACTIONS_CONTROLLER_ERROR_ON_REFRESH_TITLES.get());
	}
	//endregion

	//region Navigate tab
	public void navigateBack(ActionEvent event)
	{
		tryCatch(() -> this.model.navigateBack(), R.ACTIONS_CONTROLLER_ERROR_ON_NAVIGATE_BACK.get());
	}

	public void navigateForward(ActionEvent event)
	{
		tryCatch(() -> this.model.navigateForward(), R.ACTIONS_CONTROLLER_ERROR_ON_NAVIGATE_FORWARD.get());
	}

	public void refresh(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.refresh(), R.ACTIONS_CONTROLLER_ERROR_ON_REFRESH.get());
	}

	public void closeWindow(ActionEvent event)
	{
		tryCatch(() -> this.model.closeWindow(), R.ACTIONS_CONTROLLER_ERROR_ON_CLOSE_WINDOW.get());
	}
	//endregion

	//region NewInstance tab
	public void newInstance(ActionEvent event)
	{
		tryCatch(() -> this.model.newInstance(
				this.lvNewInstance.getItems().stream().collect(Collectors.toMap(e -> e.getKey().getText(), e -> Str.IsNullOrEmpty(e.getValue().getText()) ? null : e.getValue().getText()))),
				R.ACTIONS_CONTROLLER_ERROR_ON_NEW_INSTANCE.get());

	}
	//endregion

	//region Pos&Size tab
	public void moveTo(ActionEvent e)
	{
		tryCatch(() -> this.model.moveTo(this.ntfMoveToX.getValue(), this.ntfMoveToY.getValue()), R.ACTIONS_CONTROLLER_ERROR_ON_MOVE_TO.get());
	}

	public void resize(ActionEvent e)
	{
		tryCatch(() ->
		{
			Toggle selectedToggle = this.groupSection.getSelectedToggle();

			if (selectedToggle == null)
			{
				throw new Exception(R.ACTIONS_CONTROLLER_ERROR_ON_FILL_RESIZE.get());
			}

			int h = selectedToggle == this.rbSize ? this.ntfResizeH.getValue() : 0;
			int w = selectedToggle == this.rbSize ? this.ntfResizeW.getValue() : 0;

			Resize resize = ((Resize) selectedToggle.getUserData());
			this.model.resize(resize, h, w);
		}, R.ACTIONS_CONTROLLER_ERROR_ON_RESIZE.get());
	}
	//endregion

	//region Property tab

	public void getProperty(ActionEvent event)
	{
		tryCatch(() -> this.model.getProperty(this.cbGetProperty.getSelectionModel().getSelectedItem(), this.efGetProperty.getEvaluatedValue()), R.ACTIONS_CONTROLLER_ERROR_ON_GET_PROP.get());
	}

	public void setProperty(ActionEvent event)
	{
		tryCatch(() -> this.model.setProperty(this.cbSetProperty.getSelectionModel().getSelectedItem(), this.efSetProperty.getEvaluatedValue()), R.ACTIONS_CONTROLLER_ERROR_ON_SET_PROP.get());
	}

	//endregion

	//region Dialog
	public void dialogMoveTo(ActionEvent event)
	{
		tryCatch(() -> this.model.dialogMoveTo(this.ntfDialogMoveToX.getValue(), this.ntfDialogMoveToY.getValue()), R.ACTIONS_CONTROLLER_ERROR_ON_MOVE_TO.get());
	}

	public void dialogResize(ActionEvent event)
	{
		tryCatch(() ->
		{
			Toggle selectedToggle = this.dialogGroupSection.getSelectedToggle();

			if (selectedToggle == null)
			{
				throw new Exception(R.ACTIONS_CONTROLLER_ERROR_ON_FILL_RESIZE.get());
			}

			int h = selectedToggle == this.rbDialogSize ? this.ntfDialogResizeH.getValue() : 0;
			int w = selectedToggle == this.rbDialogSize ? this.ntfDialogResizeW.getValue() : 0;

			Resize resize = ((Resize) selectedToggle.getUserData());
			this.model.dialogResize(resize, h, w);
		}, R.ACTIONS_CONTROLLER_ERROR_ON_RESIZE.get());
	}

	public void getDialogProperty(ActionEvent event)
	{
		tryCatch(() -> this.model.dialogGetProperty(this.cbGetDialogProperty.getValue()), R.ACTIONS_CONTROLLER_ERROR_ON_GET_PROP.get());
	}

	//endregion

	//region display* methods
	public void displayTitles(Collection<String> titles)
	{
		this.comboBoxTitles.getItems().setAll(titles);
	}

	public void displayApplications(Collection<String> applications)
	{
		this.comboBoxApps.getItems().addAll(applications);
	}

	public void displayCurrentApplication(String app)
	{
		this.comboBoxApps.getSelectionModel().select(app);
	}

	public void displayImage(ImageWrapper imageWrapper)
	{
		this.imageArea.setGraphic(Optional.ofNullable(imageWrapper).map(ImageWrapper::getImage).map(img -> SwingFXUtils.toFXImage(img, null)).map(ImageView::new).orElse(null));
	}

	public void displayProperties(List<String> getProperties, List<String> setProperties, List<String> getDialogProperties)
	{
		this.cbGetProperty.getItems().setAll(getProperties);
		this.cbGetProperty.getSelectionModel().selectFirst();

		this.cbSetProperty.getItems().setAll(setProperties);
		this.cbSetProperty.getSelectionModel().selectFirst();

		this.cbGetDialogProperty.getItems().setAll(getDialogProperties);
		this.cbGetDialogProperty.getSelectionModel().selectFirst();
	}

	public void displayParametersForNewInstance(List<String> names, Function<String, ListProvider> function)
	{
		if (names == null || names.isEmpty())
		{
			this.lvNewInstance.getItems().clear();
			this.btnNewInstance.setDisable(true);
		}
		else
		{
			this.lvNewInstance.getItems().setAll(names.stream().map(name -> new ExpressionFieldsPane(name, "", this.evaluator, function.apply(name))).collect(Collectors.toList()));
			this.btnNewInstance.setDisable(false);
		}
	}

	public void displayApplicationStatus(ApplicationStatus status, Throwable ifTrouble)
	{
		if (status != null)
		{
			switch (status)
			{
				case Disconnected:
					this.comboBoxApps.setDisable(false);
					this.comboBoxAppsStore.setDisable(false);
					this.btnStartApplication.setDisable(false);
					this.btnConnectApplication.setDisable(false);
					this.btnStop.setDisable(true);
					break;

				case Connecting:
					this.comboBoxApps.setDisable(true);
					this.comboBoxAppsStore.setDisable(true);
					this.btnStartApplication.setDisable(true);
					this.btnConnectApplication.setDisable(true);
					this.btnStop.setDisable(true);
					break;

				case Connected:
					this.comboBoxApps.setDisable(true);
					this.comboBoxAppsStore.setDisable(true);
					this.btnStartApplication.setDisable(true);
					this.btnConnectApplication.setDisable(true);
					this.btnStop.setDisable(false);
					break;
				case ConnectingFromStore:
					this.comboBoxApps.setDisable(true);
					this.btnStartApplication.setDisable(true);
					this.btnConnectApplication.setDisable(true);
					this.btnStop.setDisable(true);
					break;
			}
		}
		Optional.ofNullable(ifTrouble).ifPresent(twrbl -> {
			logger.error(twrbl.getMessage(), twrbl);
			DialogsHelper.showError(twrbl.getMessage());
		});
	}

	//endregion

	//region private methods
	private Collection<String> storedConnections()
	{
		List<String> storedConnection = this.model.getFactory().getConfiguration().getStoreMap().entrySet()
				.stream()
				.filter(entry -> entry.getValue() instanceof AppConnection)
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
		storedConnection.add(0, "");
		return storedConnection;
	}

	private void setDisablePosAndSizeButtons(boolean value)
	{
		this.ntfResizeH.setDisable(value);
		this.ntfResizeW.setDisable(value);
	}

	private void setDisableDialogsButtons(boolean value)
	{
		this.ntfDialogResizeH.setDisable(value);
		this.ntfDialogResizeW.setDisable(value);
	}
	//endregion
}
