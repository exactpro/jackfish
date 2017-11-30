////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.actions.gui.DialogGetProperties;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.console.ConsoleArea;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.dictionary.actions.ActionsController;
import com.exactprosystems.jf.tool.dictionary.element.ElementInfoController;
import com.exactprosystems.jf.tool.dictionary.navigation.NavigationController;
import com.exactprosystems.jf.tool.documents.AbstractDocumentController;
import com.exactprosystems.jf.tool.documents.ControllerInfo;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@ControllerInfo(resourceName = "DictionaryTab.fxml")
public class DictionaryFxController extends AbstractDocumentController<DictionaryFx>
{
	public enum Result
	{
		PASSED(Color.GREEN), FAILED(Color.RED), NOT_ALLOWED(Color.DARKGRAY);

		private Result(Color color)
		{
			this.color = color;
		}

		public Color getColor()
		{
			return this.color;
		}

		private Color color;
	}

	@FXML
	private SplitPane   splitPane;
	@FXML
	private GridPane    mainGridPane;
	private ConsoleArea area;

	private ActionsController     actionsController;
	private ElementInfoController elementInfoController;
	private NavigationController  navigationController;

	//region Initializable
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		this.area = new ConsoleArea();
		this.area.setEditable(false);
		this.area.setMaxHeight(250);
		this.splitPane.getItems().add(new VirtualizedScrollPane<>(area));
	}
	//endregion

	//region AbstractDocumentController
	@Override
	protected void init(Document model, CustomTab customTab)
	{
		super.init(model, customTab);

		this.navigationController = Common.loadController(NavigationController.class.getResource("Navigation.fxml"));
		this.navigationController.init(super.model, this.mainGridPane);

		this.elementInfoController = Common.loadController(ElementInfoController.class.getResource("ElementInfo.fxml"));
		this.elementInfoController.init(super.model, super.model.getFactory().getConfiguration(), this.mainGridPane, this.navigationController);

		this.actionsController = Common.loadController(ActionsController.class.getResource("Actions.fxml"));
		this.actionsController.init(super.model, this.mainGridPane, super.model.getEvaluator());

		//region actions
		super.model.getTitles().setOnAddAllListener((integer, collection) -> this.actionsController.displayTitles(DictionaryFx.convertToString(collection)));
		super.model.getAppsList().setOnAddAllListener((integer, collection) -> this.actionsController.displayApplications(DictionaryFx.convertToString(collection)));
		super.model.currentApp().setOnChangeListener((s, s2) -> this.actionsController.displayCurrentApplication(s2));
		super.model.imageProperty().setOnChangeListener((imageWrapper, imageWrapper2) -> this.actionsController.displayImage(imageWrapper2));
		//TODO add listeners for events
		//endregion

		//region navigation
		this.model.setOnChangeListener((integer, integer2) -> this.navigationController.displayDialogs(this.model.getWindows()));
		this.model.setOnAddListener((integer, window) -> this.navigationController.addDialog(integer, window));
		this.model.setOnRemoveListener((integer, window) -> this.navigationController.removeDialog(window));
		this.model.currentWindow().setOnChangeListener((window, newValue) -> this.navigationController.displayDialog(newValue));
		this.model.setChangeWindowName((newName, newName2) -> this.navigationController.dialogChangeName(newName2));
		this.model.currentSection().setOnChangeListener((sectionKind, newValue) -> this.navigationController.displaySection(newValue));

		this.model.currentElement().setOnChangeListener((iControl, newValue) -> {
			this.navigationController.displayElement(newValue);
			this.elementInfoController.displayElement(newValue);
		});
		this.model.currentElements().setOnAddAllListener((integer, collection) -> this.navigationController.displayElements(collection));
		this.model.currentElements().setOnChangeListener((integer, collection) -> this.navigationController.clearElements());
		//endregion

		super.model.outProperty().setOnChangeListener((s, s2) -> Common.runLater(() -> this.area.appendDefaultTextOnNewLine(s2)));

		super.model.appStatusProperty().setOnChangeListener((applicationStatusBean, applicationStatusBean2) -> this.displayApplicationStatus(applicationStatusBean2));
	}

	@Override
	protected void restoreSettings(Settings settings)
	{
		String absolutePath = new File(super.model.getNameProperty().get()).getAbsolutePath();
		Settings.SettingsValue value = settings.getValue(Settings.MAIN_NS, DictionaryFx.DIALOG_DICTIONARY_SETTINGS, absolutePath);
		Optional.ofNullable(value).ifPresent(s -> super.model.setCurrentApp(s.getValue()));
	}

	//endregion

	private void displayApplicationStatus(ApplicationStatusBean appStatusBean)
	{
		this.actionsController.displayApplicationStatus(appStatusBean.getStatus(), appStatusBean.getThrowable());
		AppConnection appCon = appStatusBean.getAppConnection();
		if (appCon != null)
		{
			IApplicationFactory factory = appCon.getApplication().getFactory();
			String[] getProps = factory.wellKnownParameters(ParametersKind.GET_PROPERTY);
			String[] setProps = factory.wellKnownParameters(ParametersKind.SET_PROPERTY);
			String[] getDialogProps = {DialogGetProperties.sizeName, DialogGetProperties.positionName};
			this.actionsController.displayProperties(Arrays.asList(getProps), Arrays.asList(setProps), Arrays.asList(getDialogProps));

			String[] params = factory.wellKnownParameters(ParametersKind.NEW_INSTANCE);
			this.actionsController.displayParametersForNewInstance(Arrays.asList(params), key -> {
				if (factory.canFillParameter(key))
				{
					return () -> Arrays.stream(factory.listForParameter(key))
							.map(this.model.getEvaluator()::createString)
							.map(ReadableValue::new)
							.collect(Collectors.toList());
				}
				return null;
			});
		}
		else
		{
			this.actionsController.displayProperties(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
			this.actionsController.displayParametersForNewInstance(null, null);
		}
	}
}
