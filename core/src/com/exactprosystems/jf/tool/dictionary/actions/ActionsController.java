////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary.actions;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.css.images.Images;
import com.exactprosystems.jf.tool.custom.BorderWrapper;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.dictionary.ApplicationStatus;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.dictionary.element.ElementInfoController;
import com.exactprosystems.jf.tool.dictionary.navigation.NavigationController;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
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
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.exactprosystems.jf.tool.Common.logger;
import static com.exactprosystems.jf.tool.Common.tryCatch;

public class ActionsController implements Initializable, ContainingParent
{
	public Label					imageArea;
	public GridPane					elementActionsGrid;
	
	public ComboBox<String>			comboBoxApps;
	public ComboBox<String>         comboBoxAppsStore;
	public Button					btnStartApplication;
	public Button					btnConnectApplication;
	public Button					btnStop;
	public TextField				tfSendKeys;
	public ComboBox<String>			comboBoxWindows;

	public GridPane					mainGrid;
	public HBox						hBoxDoIt;
	private ExpressionField			expressionField;
	private Parent					pane;

	private DictionaryFx			model;
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
		assert btnStop != null : "fx:id=\"btnStopConnection\" was not injected: check your FXML file 'Actions.fxml'.";
		assert mainGrid != null : "fx:id=\"mainGrid\" was not injected: check your FXML file 'Actions.fxml'.";
		assert comboBoxWindows != null : "fx:id=\"comboBoxDriverWindows\" was not injected: check your FXML file 'Actions.fxml'.";
		assert tfSendKeys != null : "fx:id=\"tfSendKeys\" was not injected: check your FXML file 'Actions.fxml'.";
		assert elementActionsGrid != null : "fx:id=\"elementActionsGrid\" was not injected: check your FXML file 'Actions.fxml'.";
		assert comboBoxApps != null : "fx:id=\"comboBoxApps\" was not injected: check your FXML file 'Actions.fxml'.";
		assert comboBoxAppsStore != null : "fx:id=\"comboBoxAppsEntries\" was not injected: check your FXML file 'Actions.fxml'.";
		assert btnStartApplication != null : "fx:id=\"btnStartApplication\" was not injected: check your FXML file 'Actions.fxml'.";
		assert btnConnectApplication != null : "fx:id=\"btnConnectApplication\" was not injected: check your FXML file 'Actions.fxml'.";
		assert imageArea != null : "fx:id=\"labelArea\" was not injected: check your FXML file 'Actions.fxml'.";
		String imageText = Images.class.getResource("texture.png").toExternalForm();
		imageArea.setStyle("-fx-background-image:url('" + imageText + "');\n" + "    -fx-background-repeat: repeat;");
		comboBoxWindows.setOnShowing(event -> tryCatch(() -> this.model.displayTitles(), "Error on update titles"));
		comboBoxAppsStore.setOnShowing(event -> tryCatch(() -> this.model.displayStores(), "Error on update titles"));
		Platform.runLater(() -> {
			btnStartApplication.setTooltip(new Tooltip("Start application"));
			btnStop.setTooltip(new Tooltip("Stop application"));
			btnConnectApplication.setTooltip(new Tooltip("Connect application"));

			Common.customizeLabeled(btnStartApplication, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.START_APPLICATION);
			Common.customizeLabeled(btnConnectApplication, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.CONNECT_APPLICATION);
			Common.customizeLabeled(btnStop, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.STOP_APPLICATION);

			((BorderPane) this.pane).setCenter(BorderWrapper.wrap(this.mainGrid).title("Actions").color(Common.currentTheme().getReverseColor()).build());

		});
	}

	public void init(DictionaryFx model, GridPane gridPane, AbstractEvaluator evaluator, NavigationController navigation, 
			ElementInfoController info)
	{
		this.model = model;
		this.navigation = navigation;
		this.info = info;

		this.expressionField = new ExpressionField(evaluator);
//		this.elementActionsGrid.add(this.expressionField, 0, 3, 2, 1);
		this.hBoxDoIt.getChildren().add(0, this.expressionField);
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
	}

	// ------------------------------------------------------------------------------------------------------------------
	// Event handlers
	// ------------------------------------------------------------------------------------------------------------------
	public void sendKeysAction(ActionEvent actionEvent)
	{
		tryCatch(() -> this.navigation.sendKeys(this.tfSendKeys.getText()), "Error on send keys");
	}

	public void clickAction(ActionEvent actionEvent)
	{
		tryCatch(() -> this.navigation.click(), "Error on click");
	}

	public void findAction(ActionEvent actionEvent)
	{
		tryCatch(() -> this.navigation.find(), "Error on find");
	}

	public void doIt(ActionEvent actionEvent)
	{
		tryCatch(() -> this.navigation.doIt(this.expressionField.getEvaluatedValue()), "Error on operate");
	}
	
	
	public void changeWindow(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.switchTo(currentWindow()), "Error on switch window");
	}

	public void refresh(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.refresh(), "Error on refresh");
	}

	public void switchToCurrent(ActionEvent actionEvent)
	{
		tryCatch(() -> this.navigation.switchToCurrent(), "Error on switch to current");
	}

	public void switchToParent(ActionEvent actionEvent)
	{
		tryCatch(() -> this.model.switchToParent(), "Error on switch to current");
	}

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
