////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.matrix.parser.listeners.ListProvider;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.console.ConsoleText;
import com.exactprosystems.jf.tool.custom.console.CustomListView;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.tab.CustomTabPane;
import com.exactprosystems.jf.tool.dictionary.actions.ActionsController;
import com.exactprosystems.jf.tool.dictionary.element.ElementInfoController;
import com.exactprosystems.jf.tool.dictionary.navigation.NavigationController;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.function.Function;

public class DictionaryFxController implements Initializable, ContainingParent
{
	public enum Result
	{ 
		PASSED (Color.GREEN), FAILED (Color.RED), NOT_ALLOWED (Color.DARKGRAY);
		
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

	public CustomListView<String>	listView;
	public SplitPane				splitPane;
	public GridPane					mainGridPane;
	private Parent					pane;
	private CustomTab				tab;
	
	private ActionsController		actionsController;
	private ElementInfoController	elementInfoController;
	private NavigationController	navigationController;
	private Settings settings;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		this.listView = new CustomListView<>(true);
		this.listView.setMaxHeight(250.0);
		this.splitPane.getItems().add(listView);
	}

	public void saved(String name)
	{
		this.tab.saved(name);
	}

	public void close() throws Exception
	{
		this.tab.close();
		this.navigationController.close();
		CustomTabPane.getInstance().removeTab(this.tab);
	}

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	public void init(final DictionaryFx model, Settings settings, Configuration configuration, AbstractEvaluator evaluator) throws Exception
	{
		this.settings = settings;
		this.tab = CustomTabPane.getInstance().createTab(model);
		this.tab.setContent(pane);

		this.navigationController = Common.loadController(NavigationController.class.getResource("Navigation.fxml"));
		this.navigationController.init(model, this.mainGridPane, this.settings, this.tab);

		Settings.SettingsValue themePath = this.settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.SETTINGS, Settings.THEME, Theme.WHITE.name());
		this.elementInfoController = Common.loadController(ElementInfoController.class.getResource("ElementInfo.fxml"));
		this.elementInfoController.init(model, configuration, this.mainGridPane, this.navigationController, Theme.valueOf(themePath.getValue()).getPath());

		this.actionsController = Common.loadController(ActionsController.class.getResource("Actions.fxml"));
		this.actionsController.init(model, this.mainGridPane, evaluator, this.navigationController, this.elementInfoController);

		CustomTabPane.getInstance().addTab(this.tab);
		CustomTabPane.getInstance().selectTab(this.tab);
	}

	public void println(String str)
	{
		Platform.runLater(() ->this.listView.getItems().add(ConsoleText.defaultText(str)));
	}


	// ------------------------------------------------------------------------------------------------------------------
	// display* methods
	// ------------------------------------------------------------------------------------------------------------------
	public void displayTestingControl(IControl control, String text, Result result)
	{
		this.navigationController.displayTestingControl(control, text, result);
	}

	public void displayTitle(String title)
	{
		Platform.runLater(() -> this.tab.setTitle(title));
	}

	public void displayDialog(IWindow window, Collection<IWindow> windows)
	{
		this.navigationController.displayDialog(window, windows);
	}

	public void displaySection(SectionKind sectionKind)
	{
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
			this.actionsController.displayProperties(Arrays.asList(getProps), Arrays.asList(setProps));

			String[] params = factory.wellKnownParameters(ParametersKind.NEW_INSTANCE);
			this.actionsController.displayParameters(Arrays.asList(params), function);
		}
		else
		{
			this.actionsController.displayProperties(new ArrayList<>(), new ArrayList<>());
			this.actionsController.displayParameters(null, function);
		}
	}

	public void displayTitles(Collection<String> titles)
	{
		this.actionsController.displayTitles(titles);
	}

	public void displayActionControl(Collection<String> entries, String entry, String title)
	{
		this.actionsController.displayActionControl(entries, entry, title);
	}

	public void displayStoreActionControl(Collection<String> stories, String lastSelectedStore)
	{
		this.actionsController.displayStoreActionControl(stories, lastSelectedStore);
	}

	public void showInfo(String info)
	{
		DialogsHelper.showInfo(info);
	}
}
