////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary.actions;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.app.Resize;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.matrix.parser.listeners.ListProvider;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.BorderWrapper;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.custom.number.NumberTextField;
import com.exactprosystems.jf.tool.dictionary.ApplicationStatus;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.dictionary.element.ElementInfoController;
import com.exactprosystems.jf.tool.dictionary.navigation.NavigationController;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.helpers.ExpressionFieldsPane;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.logger;
import static com.exactprosystems.jf.tool.Common.tryCatch;

public class ActionsController implements Initializable, ContainingParent
{
	public Label					imageArea;

	public ComboBox<String>			comboBoxApps;
	public ComboBox<String>         comboBoxAppsStore;
	public Button					btnStartApplication;
	public Button					btnConnectApplication;
	public Button					btnStop;
	public TextField				tfSendKeys;
	public ComboBox<String>			comboBoxWindows;

	public GridPane					mainGrid;
	public GridPane doGridPane;
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
	public ListView<ExpressionFieldsPane> listView;
	public Button btnNewInstance;


	private ExpressionField			expressionField;
	private Parent					pane;

	private DictionaryFx			model;
	private AbstractEvaluator		evaluator;
	private NavigationController 	navigation; 
	private ElementInfoController 	info;

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		imageArea.getStyleClass().add(CssVariables.IMAGE_AREA);
		comboBoxWindows.setOnShowing(event -> tryCatch(() -> this.model.displayTitles(), "Error on update titles"));
		comboBoxAppsStore.setOnShowing(event -> tryCatch(() -> this.model.displayStores(), "Error on update titles"));
		Platform.runLater(() -> ((BorderPane) this.pane).setCenter(BorderWrapper.wrap(this.mainGrid).title("Actions").color(Common.currentTheme().getReverseColor()).build()));
		setDisable(true);
		this.groupSection.selectedToggleProperty().addListener((observable, oldValue, newValue) -> setDisable(!(newValue != null && newValue == this.rbSize)));
		this.rbMin.setUserData(Resize.Minimize);
		this.rbMax.setUserData(Resize.Maximize);
		this.rbNormal.setUserData(Resize.Normal);
		this.rbSize.setUserData(null);

	}

	private void setDisable(boolean value)
	{
		this.ntfResizeH.setDisable(value);
		this.ntfResizeW.setDisable(value);
	}

	public void init(DictionaryFx model, GridPane gridPane, AbstractEvaluator evaluator, NavigationController navigation, ElementInfoController info)
	{
		this.model = model;
		this.evaluator = evaluator;
		this.navigation = navigation;
		this.info = info;
		this.expressionField = new ExpressionField(evaluator);
		this.doGridPane.add(this.expressionField, 0, 1, 2, 1);
		HBox.setHgrow(this.expressionField, Priority.ALWAYS);
		this.expressionField.setHelperForExpressionField(null, null);

		this.comboBoxApps.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> 
		{
			this.model.setCurrentAdapter(newValue);
			this.info.setAppName(newValue);
		});

		this.comboBoxAppsStore.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if(newValue != null)
			{
				this.model.setCurrentAdapterStore(newValue);
				this.connectApplicationFromStore();
			}
		});

		gridPane.add(this.pane, 0, 1);
		GridPane.setColumnSpan(this.pane, 2);

		this.efGetProperty = new ExpressionField(evaluator);
		this.efGetProperty.setHelperForExpressionField(null, null);

		this.efSetProperty = new ExpressionField(evaluator);
		this.efSetProperty.setHelperForExpressionField(null, null);

		this.propGridPane.add(this.efGetProperty, 1, 0);
		this.propGridPane.add(this.efSetProperty, 1, 1);
		this.btnNewInstance.setDisable(true);
	}

	//region Do tab
	public void sendKeysAction(ActionEvent actionEvent)
	{
		makeDo(() -> this.navigation.sendKeys(this.tfSendKeys.getText()), "Error on send keys");
	}

	public void doIt(ActionEvent actionEvent)
	{
		makeDo(() -> this.navigation.doIt(this.expressionField.getEvaluatedValue()), "Error on operate");
	}

	public void clickAction(ActionEvent actionEvent)
	{
		makeDo(() -> this.navigation.click(), "Error on click");
	}

	public void findAction(ActionEvent actionEvent)
	{
		makeDo(() -> this.navigation.find(), "Error on find");
	}

	public void getValue(ActionEvent actionEvent)
	{
		makeDo(() -> this.navigation.getValue(), "Error on get value");
	}

	//Using wrapping method because of IEDriver(IEDriver not support fails on multiple invokes)
	private void makeDo(Common.Function func, String message) {
		if (!this.model.isFinding()) {
			Common.tryCatch(func, message);
		}else{
			DialogsHelper.showInfo("Please wait until the end of the check");
		}
	}
	//endregion

	//region Switch tab
	public void changeWindow(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.switchTo(currentWindow()), "Error on switch window");
	}

	public void switchToParent(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.switchToParent(), "Error on switch to current");
	}

	public void switchToCurrent(ActionEvent actionEvent)
	{
		tryCatch(() -> this.navigation.switchToCurrent(), "Error on switch to current");
	}
	//endregion

	//region Navigate tab
	public void navigateBack(ActionEvent event)
	{
		tryCatch(() -> this.model.navigateBack(), "Error on navigate back");
	}

	public void navigateForward(ActionEvent event)
	{
		tryCatch(() -> this.model.navigateForward(), "Error on navigate forward");
	}

	public void refresh(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.refresh(), "Error on refresh");
	}

	public void closeWindow(ActionEvent event)
	{
		tryCatch(() -> this.model.closeWindow(), "Error on close window");
	}
	//endregion

	//region NewInstance tab
	public void newInstance(ActionEvent event)
	{
		tryCatch(() -> {
			Map<String, String> parameters = this.listView.getItems()
					.stream()
					.collect(Collectors.toMap(e -> e.getKey().getText(), e -> Str.IsNullOrEmpty(e.getValue().getText()) ? null : e.getValue().getText()));
			this.model.newInstance(parameters);
		}, "Error on new instance");

	}
	//endregion

	//region Change
	public void moveTo(ActionEvent e)
	{
		tryCatch(() -> this.model.moveTo(this.ntfMoveToX.getValue(), this.ntfMoveToY.getValue()), "Error on move to");
	}

	public void resize(ActionEvent e)
	{
		Toggle selectedToggle = this.groupSection.getSelectedToggle();

		int h = selectedToggle == this.rbSize ? this.ntfResizeH.getValue() : 0;
		int w = selectedToggle == this.rbSize ? this.ntfResizeW.getValue() : 0;

		Resize resize = ((Resize) selectedToggle.getUserData());
		tryCatch(() -> this.model.resize(resize , h ,w), "Error on resize");
	}
	//endregion

	//region Property tab

	public void getProperty(ActionEvent event)
	{
		tryCatch(() -> this.model.getProperty(this.cbGetProperty.getSelectionModel().getSelectedItem(), this.efGetProperty.getEvaluatedValue()), "Error on get property");
	}

	public void setProperty(ActionEvent event)
	{
		tryCatch(() -> this.model.setProperty(this.cbSetProperty.getSelectionModel().getSelectedItem(), this.efSetProperty.getEvaluatedValue()), "Error on set property");
	}

	//endregion

	public void startApplication(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.startApplication(currentApp()), "Error on start application");
	}

	public void connectApplication(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.connectToApplication(currentApp()), "Error on connect application");
	}

	public void connectApplicationFromStore()
	{
		tryCatch(() -> this.model.connectToApplicationFromStore(currentAppStore()), "Error on connect application");
	}

	public void stopConnection(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.stopApplication(), "Error on stop application");
	}

	// ------------------------------------------------------------------------------------------------------------------
	// display* methods
	// ------------------------------------------------------------------------------------------------------------------
	public void displayProperties(List<String> getProperties, List<String> setProperties)
	{
		this.cbGetProperty.getItems().setAll(getProperties);
		this.cbGetProperty.getSelectionModel().selectFirst();

		this.cbSetProperty.getItems().setAll(setProperties);
		this.cbSetProperty.getSelectionModel().selectFirst();
	}

	public void displayParameters(List<String> names, Function<String, ListProvider> function)
	{
		if (names == null)
		{
			this.listView.getItems().clear();
			this.btnNewInstance.setDisable(true);
		}
		else
		{
			this.listView.getItems().setAll(
					names.stream()
							.map(name -> new ExpressionFieldsPane(name, "", this.evaluator, function.apply(name)))
							.collect(Collectors.toList()
							)
			);
			this.btnNewInstance.setDisable(false);
		}
	}

	public void displayImage(ImageWrapper imageWrapper)
	{
		Platform.runLater(() ->
		{
			tryCatch(() ->
			{
				ImageView imageView = null;
				if (imageWrapper != null)
				{
					BufferedImage image = imageWrapper.getImage();
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ImageIO.write(image, "jpg", outputStream);
					Image imageFX = new Image(new ByteArrayInputStream(outputStream.toByteArray()));
					imageView = new ImageView();
					imageView.setImage(imageFX);
				}
				this.imageArea.setGraphic(imageView);
			}, "Error on display image");
		});
	}

	public void displayApplicationStatus(ApplicationStatus status, Throwable ifTrouble)
	{
		Platform.runLater(() ->
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
		});
		Optional.ofNullable(ifTrouble).ifPresent(twrbl -> {
			logger.error(twrbl.getMessage(), twrbl);
			DialogsHelper.showError(twrbl.getMessage());
		});
	}

	public void displayTitles(Collection<String> titles)
	{
		if (titles != null)
		{
			this.comboBoxWindows.getItems().setAll(titles);
		}
		else
		{
			this.comboBoxWindows.getItems().setAll(FXCollections.observableArrayList());
		}
	}

	public void displayStoreActionControl(Collection<String> stories, String lastSelectedStore)
	{
		Platform.runLater(() ->
		{
			if (stories!=null)
			{
				this.comboBoxAppsStore.getItems().setAll(stories);
			}
			this.comboBoxAppsStore.getSelectionModel().select(lastSelectedStore);
		});
	}

	public void displayActionControl(Collection<String> entries, String entry, String title)
	{
		Platform.runLater(() ->
		{
			if (entries != null)
			{
				this.comboBoxApps.getItems().setAll(entries);
			}
			this.comboBoxApps.getSelectionModel().select(entry);
			
			/*
				//TODO
				this is need, because if title is null, that listener try to change value to null.
				After that title will be go in remoteApplication and in method switchTo will be throw Exception, because title == null.
				And if the first element is null, i not think about it.
			 */
			if (title != null)
			{
				this.comboBoxWindows.getSelectionModel().select(title);
			}
//			else
//			{
//				this.comboBoxWindows.getSelectionModel().selectFirst();
//			}

		});
	}

	// ------------------------------------------------------------------------------------------------------------------
	private String currentApp()
	{
		return this.comboBoxApps.getSelectionModel().getSelectedItem();
	}

	private String currentAppStore()
	{
		return this.comboBoxAppsStore.getSelectionModel().getSelectedItem();
	}

	private String currentWindow()
	{
		return this.comboBoxWindows.getSelectionModel().getSelectedItem();
	}
}
