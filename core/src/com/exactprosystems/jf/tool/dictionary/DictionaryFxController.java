////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary;

import com.exactprosystems.jf.actions.gui.DialogGetProperties;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.matrix.parser.listeners.ListProvider;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.function.Function;

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
		this.navigationController.init(super.model, this, this.mainGridPane);

		this.elementInfoController = Common.loadController(ElementInfoController.class.getResource("ElementInfo.fxml"));
		this.elementInfoController.init(super.model, super.model.getFactory().getConfiguration(), this.mainGridPane, this.navigationController);

		this.actionsController = Common.loadController(ActionsController.class.getResource("Actions.fxml"));
		this.actionsController.init(super.model, this.mainGridPane, super.model.getEvaluator(), this.navigationController, this.elementInfoController);

		//region actions
		super.model.getTitles().setOnAddAllListener((integer, collection) -> this.actionsController.displayTitles(DictionaryFx.convertToString(collection)));
		super.model.getAppsList().setOnAddAllListener((integer, collection) -> this.actionsController.displayApplications(DictionaryFx.convertToString(collection)));
		super.model.currentApp().setOnChangeListener((s, s2) -> this.actionsController.displayCurrentApplication(s2));
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
	}

	//endregion

	public void println(String str)
	{
		Common.runLater(() -> this.area.appendDefaultTextOnNewLine(str));
	}

	// ------------------------------------------------------------------------------------------------------------------
	// display* methods
	// ------------------------------------------------------------------------------------------------------------------
	public void displayTestingControl(IControl control, String text, Result result)
	{
		this.navigationController.displayTestingControl(control, text, result);
	}

	public void displayDialog(IWindow window, Collection<IWindow> windows)
	{
		this.navigationController.displayDialog(window, windows);
	}

	public void displaySection(SectionKind sectionKind)
	{
		//TODO
		this.navigationController.displaySection(sectionKind);
	}

	public void displayElement(Collection<IControl> controls, IControl control)
	{
		this.navigationController.displayElement(control, controls);
	}

	public void displayElementInfo(IWindow window, IControl control, Collection<IControl> owners, IControl owner, Collection<IControl> rows, IControl row, IControl header, IControl reference)
	{
		this.elementInfoController.displayInfo(window, control, owners, owner, rows, row, header, reference);
	}

	public void displayImage(ImageWrapper imageWrapper) throws Exception
	{
		this.actionsController.displayImage(imageWrapper);
	}

	public void displayApplicationStatus(ApplicationStatus status, Throwable throwable, AppConnection appConnection, Function<String, ListProvider> function)
	{
		this.navigationController.setAppConnection(appConnection);
		this.actionsController.displayApplicationStatus(status, throwable);
		if (appConnection != null)
		{
			IApplicationFactory factory = appConnection.getApplication().getFactory();
			String[] getProps = factory.wellKnownParameters(ParametersKind.GET_PROPERTY);
			String[] setProps = factory.wellKnownParameters(ParametersKind.SET_PROPERTY);
			String[] getDialogProps = {DialogGetProperties.sizeName, DialogGetProperties.positionName};
			this.actionsController.displayProperties(Arrays.asList(getProps), Arrays.asList(setProps), Arrays.asList(getDialogProps));

			String[] params = factory.wellKnownParameters(ParametersKind.NEW_INSTANCE);
			this.actionsController.displayParameters(Arrays.asList(params), function);
		}
		else
		{
			this.actionsController.displayProperties(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
			this.actionsController.displayParameters(null, function);
		}
	}

	public void displayActionControl(Collection<String> entries, String entry, String title)
	{
		this.actionsController.displayActionControl(entries, entry, title);
	}

}
