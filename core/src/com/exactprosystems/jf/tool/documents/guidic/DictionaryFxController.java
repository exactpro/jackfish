////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.documents.guidic;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.actions.gui.DialogGetProperties;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.console.ConsoleArea;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.documents.AbstractDocumentController;
import com.exactprosystems.jf.tool.documents.ControllerInfo;
import com.exactprosystems.jf.tool.documents.guidic.actions.ActionsController;
import com.exactprosystems.jf.tool.documents.guidic.element.ElementInfoController;
import com.exactprosystems.jf.tool.documents.guidic.navigation.NavigationController;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@ControllerInfo(resourceName = "DictionaryTab.fxml")
public class DictionaryFxController extends AbstractDocumentController<DictionaryFx>
{
	public enum Result
	{
		PASSED(Color.GREEN), FAILED(Color.RED), NOT_ALLOWED(Color.DARKGRAY);

		Result(Color color)
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

	private NavigationController navigationController;
	private ElementInfoController elementInfoController;

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
		this.navigationController.init(super.model, this, pane -> this.mainGridPane.add(pane, 0, 0));

		this.elementInfoController = Common.loadController(ElementInfoController.class.getResource("ElementInfo.fxml"));
		this.elementInfoController.init(super.model, pane -> this.mainGridPane.add(pane, 1, 0));

		ActionsController actionsController = Common.loadController(ActionsController.class.getResource("Actions.fxml"));
		actionsController.init(super.model, super.model.getEvaluator(), pane -> this.mainGridPane.add(pane, 0,1));

		//region actions
		super.model.getTitles().setOnAddAllListener((integer, collection) -> actionsController.displayTitles(DictionaryFx.convertToString(collection)));
		super.model.getAppsList().setOnAddAllListener((integer, collection) -> actionsController.displayApplications(DictionaryFx.convertToString(collection)));
		super.model.currentApp().setOnChangeListener((s, s2) -> {
			actionsController.displayCurrentApplication(s2);

			Collection<ControlKind> kinds = Arrays.asList(ControlKind.values());
			try
			{
				kinds = super.model.getFactory().getConfiguration().getApplicationPool().loadApplicationFactory(s2).getInfo().supportedControlKinds();
			}
			catch (Exception ignored){}

			this.elementInfoController.displaySupportedControls(kinds);
		});
		super.model.imageProperty().setOnChangeListener((imageWrapper, imageWrapper2) -> actionsController.displayImage(imageWrapper2));
		//endregion

		//region navigation
		super.model.setOnChangeListener((integer, integer2) -> this.navigationController.displayDialogs(this.model.getWindows()));
		super.model.setOnAddListener(this.navigationController::addDialog);
		super.model.setOnRemoveListener((integer, window) -> this.navigationController.removeDialog(window));
		super.model.currentWindow().setOnChangeListener((window, newValue) -> this.navigationController.displayDialog(newValue));
		super.model.setChangeWindowName((newName, newName2) -> this.navigationController.dialogChangeName(newName2));
		super.model.currentSection().setOnChangeListener((sectionKind, newValue) -> this.navigationController.displaySection(newValue));

		super.model.currentElement().setOnChangeListener((iControl, newValue) ->
		{
			this.navigationController.displayElement(newValue);
			this.elementInfoController.displayElement(newValue);
		});
		super.model.onRemoveElement(this.navigationController::removeElement);
		super.model.onAddElement(this.navigationController::addElement);
		super.model.currentElements().setOnAddAllListener((integer, collection) -> this.navigationController.displayElements(collection));
		super.model.currentElements().setOnChangeListener((integer, collection) -> this.navigationController.clearElements());
		super.model.testingControls().setOnAddListener((integer, controlWithState) -> this.navigationController.displayTestingControls(controlWithState));
		super.model.testingControls().setOnSetListener((integer, controlWithState) -> this.navigationController.displayTestingControls(controlWithState));

		//endregion

		super.model.outProperty().setOnChangeListener((s, s2) -> Common.runLater(() -> this.area.appendDefaultTextOnNewLine(s2)));

		super.model.appStatusProperty().setOnChangeListener((applicationStatusBean, applicationStatusBean2) -> {
			actionsController.displayApplicationStatus(applicationStatusBean2.getStatus(), applicationStatusBean2.getThrowable());
			AppConnection appCon = applicationStatusBean2.getAppConnection();
			if (appCon != null)
			{
				IApplicationFactory factory = appCon.getApplication().getFactory();
				String[] getProps = factory.wellKnownParameters(ParametersKind.GET_PROPERTY);
				String[] setProps = factory.wellKnownParameters(ParametersKind.SET_PROPERTY);
				String[] getDialogProps = {DialogGetProperties.sizeName, DialogGetProperties.positionName};
				actionsController.displayProperties(Arrays.asList(getProps), Arrays.asList(setProps), Arrays.asList(getDialogProps));

				String[] params = factory.wellKnownParameters(ParametersKind.NEW_INSTANCE);
				actionsController.displayParametersForNewInstance(Arrays.asList(params), key -> {
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
				actionsController.displayProperties(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
				actionsController.displayParametersForNewInstance(null, null);
			}

			//display supported control from app
			Collection<ControlKind> kindList = Arrays.asList(ControlKind.values());
			if (appCon != null)
			{
				kindList = appCon.getApplication().getFactory().supportedControlKinds();
			}
			elementInfoController.displaySupportedControls(kindList);
		});
	}

	@Override
	protected void restoreSettings(Settings settings)
	{
		String absolutePath = new File(super.model.getNameProperty().get()).getAbsolutePath();
		Settings.SettingsValue value = settings.getValue(Settings.MAIN_NS, DictionaryFx.DIALOG_DICTIONARY_SETTINGS, absolutePath);
		Optional.ofNullable(value).ifPresent(s -> super.model.setCurrentApp(s.getValue()));

		//key='value' , key2='value , 2'
		Settings.SettingsValue variables = settings.getValue(Settings.MAIN_NS, DictionaryFx.DIALOG_STORED_VARIABLES, absolutePath);
		Optional.ofNullable(variables)
				.map(Settings.SettingsValue::getValue)
				.map(DictionaryFx.CONVERTER::fromString)
				.ifPresent(this.model.parametersProperty()::from);
	}

	//endregion

	public void elementChanged(IControl element)
	{
		this.elementInfoController.lostFocus();
		this.model.setCurrentElement(element);
	}
}
