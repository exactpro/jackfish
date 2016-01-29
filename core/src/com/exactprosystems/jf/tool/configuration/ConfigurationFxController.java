////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.configuration;

import com.exactprosystems.jf.api.client.Possibility;
import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Configuration.Entry;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.SupportedEntry;
import com.exactprosystems.jf.tool.configuration.appentry.AppEntryFxController;
import com.exactprosystems.jf.tool.configuration.cliententry.ClientEntryFxController;
import com.exactprosystems.jf.tool.configuration.evaluator.EvaluatorController;
import com.exactprosystems.jf.tool.configuration.formats.FormatsController;
import com.exactprosystems.jf.tool.configuration.libentry.LibEntryFxController;
import com.exactprosystems.jf.tool.configuration.paths.PathsController;
import com.exactprosystems.jf.tool.configuration.serviceentry.ServiceEntryFxController;
import com.exactprosystems.jf.tool.configuration.sqlentry.SqlEntryFxController;
import com.exactprosystems.jf.tool.configuration.sqlentry.testing.TestingConnectionFxController;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.*;

public class ConfigurationFxController implements Initializable, ContainingParent
{
	public Accordion								accordionSqlEntries;
	public TextField								tfSqlEntryName;

	public TextField								tfClientEntryName;
	public Accordion								accordionClientEntries;

	public TextField								tfAppEntryName;
	public Accordion								accordionAppEntries;

	public TextField								tfServiceEntryName;
	public Accordion								accordionServiceEntries;

	public TextField								tfLibEntryName;
	public ListView<GridPane>						listViewLibs;

	public TitledPane								titledPanePaths;
	public TitledPane								titledPaneFormats;
	public TitledPane								titledPaneEvaluator;

	private CustomTab								tab;
	private Parent									pane;
	private ConfigurationFx							model;
	private PathsController							pathsController;
	private FormatsController						formatsController;
	private EvaluatorController						evaluatorController;
	private Map<Entry, AppEntryFxController>		appMap		= new LinkedHashMap<>();
	private Map<Entry, LibEntryFxController>		libMap		= new LinkedHashMap<>();
	private Map<Entry, ClientEntryFxController>		clientMap	= new LinkedHashMap<>();
	private Map<Entry, ServiceEntryFxController>	serviceMap	= new LinkedHashMap<>();
	private Map<Entry, SqlEntryFxController>		sqlMap		= new LinkedHashMap<>();

	private TestingConnectionFxController			testSqlController;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert tfClientEntryName != null : "fx:id=\"tfClientEntryName\" was not injected: check your FXML file 'ConfigurationTab.fxml'.";
		assert tfAppEntryName != null : "fx:id=\"tfAppEntryName\" was not injected: check your FXML file 'ConfigurationTab.fxml'.";
		assert tfSqlEntryName != null : "fx:id=\"tfSqlEntryName\" was not injected: check your FXML file 'ConfigurationTab.fxml'.";
		assert accordionSqlEntries != null : "fx:id=\"accordionSqlEntries\" was not injected: check your FXML file 'ConfigurationTab.fxml'.";
		assert accordionAppEntries != null : "fx:id=\"accordionAppEntries\" was not injected: check your FXML file 'ConfigurationTab.fxml'.";
		assert accordionClientEntries != null : "fx:id=\"accordionClientEntries\" was not injected: check your FXML file 'ConfigurationTab.fxml'.";
		accordionSqlEntries.setPrefWidth(Control.USE_COMPUTED_SIZE);
	}

	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	public void saved()
	{
		this.tab.saved(this.model.getName());
	}

	public void addNewLibEntry(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> 
		{
			this.model.addNewLibEntry(tfLibEntryName.getText());
			tfLibEntryName.clear();
		}, "Error on add new lib entry");
	}

	public void addNewSqlEntry(ActionEvent event)
	{
		Common.tryCatch(() -> 
		{
			this.model.addNewSql(tfSqlEntryName.getText());
			tfSqlEntryName.clear();
		}, "Error on add new sql entry");
	}

	public void addNewClientEntry(ActionEvent event)
	{
		Common.tryCatch(() -> 
		{
			this.model.addNewClientEntry(tfClientEntryName.getText());
			tfClientEntryName.clear();
		}, "Error on add new client entry");
	}

	public void addNewAppEntry(ActionEvent event)
	{
		Common.tryCatch(() -> 
		{
			this.model.addNewAppEntry(tfAppEntryName.getText());
			tfAppEntryName.clear();
		}, "Error on add new app entry");
	}

	public void addNewServiceEntry(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> 
		{
			this.model.addNewService(tfServiceEntryName.getText());
			tfServiceEntryName.clear();
		}, "Error on add new service entry");
	}

	public void testAppEntries(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::testVersionApp, "Error on test app entries");
	}

	public void testClientEntries(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::testVersionClient, "Error on test client entry");
	}

	public void testServiceEntries(ActionEvent actionEvent)
	{
		Common.tryCatch(this.model::testVersionService, "Error on test service entries");
	}

	public void displaySubControllers()
	{
//		this.appMap.values().stream().forEach(entry -> entry.display(accordionAppEntries));
//		this.libMap.values().stream().forEach(entry -> entry.display(listViewLibs));
//		this.clientMap.values().stream().forEach(entry -> entry.display(accordionClientEntries));
//		this.serviceMap.values().stream().forEach(entry -> entry.display(accordionServiceEntries));
//		this.sqlMap.values().stream().forEach(entry -> entry.display(accordionSqlEntries));
	}

	public void init(final ConfigurationFx model)
	{
		this.model = model;
		this.tab = Common.createTab(model, this.model.getSettings());
		this.tab.setContent(this.pane);
		Common.getTabPane().getTabs().add(this.tab);
		Common.getTabPane().getSelectionModel().select(this.tab);
	}

	public void setTitle(String title)
	{
		this.tab.setTitle(title);
	}

	public void close()
	{
		Common.tryCatch(() -> 
		{
			this.tab.close();
			// TODO this should do the custom tab itself
			Common.getTabPane().getTabs().removeAll(tab);
		}, "Error on close tab");
	}

	public void displayPaths(String output, String sysVars, String userSysVars)
	{
		this.pathsController.display(output, sysVars, userSysVars);
	}

	public void displayFormats(String dateTime, String date, String time, String additionalFormat)
	{
		this.formatsController.display(dateTime, date, time, additionalFormat);
	}

	public void displayEvaluator(String evaluatorImport)
	{
		this.evaluatorController.display(evaluatorImport);
	}

	// app entry
	public void displayAppEntries()
	{
		clear(this.appMap, accordionAppEntries);
		model.getAppEntries().forEach(appEntry -> {
			AppEntryFxController appController = Common.loadController(AppEntryFxController.class.getResource("AppEntryFx.fxml"));
			appController.init(model, appEntry, accordionAppEntries);
			this.appMap.put(appEntry, appController);
		});
		this.appMap.values().forEach(AppEntryFxController::display);
	}

	public void updateAppVersion(HashMap<Configuration.AppEntry, SupportedEntry> map)
	{
		map.entrySet().forEach(app -> appMap.get(app.getKey()).displaySupported(app.getValue()));
	}

	public void showAppHelp(String help)
	{
		DialogsHelper.showAppHelp(help);
	}

	// lib entry
	public void displayLibEntries()
	{
		this.libMap.clear();
		listViewLibs.getItems().clear();
		model.getLibEntries().forEach(libEntry -> 
		{
			LibEntryFxController libController = Common.loadController(LibEntryFxController.class.getResource("LibEntryFx.fxml"));
			libController.init(model, libEntry, listViewLibs);
			this.libMap.put(libEntry, libController);
		});
		this.libMap.values().forEach(LibEntryFxController::display);
	}

	// client entry
	public void displayClientEntries()
	{
		clear(this.clientMap, accordionClientEntries);
		model.getClientEntries().forEach(clientEntry -> {
			ClientEntryFxController sqlController = Common.loadController(ClientEntryFxController.class.getResource("ClientEntryFx.fxml"));
			sqlController.init(model, clientEntry, accordionClientEntries);
			this.clientMap.put(clientEntry, sqlController);
		});
		this.clientMap.values().forEach(ClientEntryFxController::display);
	}

	public void showPossibilities(Set<Possibility> possibilities, String entryName)
	{
		ListView<String> listView = new ListView<>();
		possibilities.stream().forEach((possibility) -> listView.getItems().add(possibility.getDescription()));
		Dialog<ButtonType> dialog = new Alert(Alert.AlertType.INFORMATION);
		dialog.setHeaderText("Possibilities for " + entryName);
		dialog.setTitle("Possibilities");
		dialog.getDialogPane().setContent(listView);
		dialog.getDialogPane().setPrefWidth(500);
		dialog.getDialogPane().setPrefHeight(300);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
		dialog.show();
	}

	public void updateClientVersion(HashMap<Configuration.ClientEntry, SupportedEntry> map)
	{
		map.entrySet().forEach(client -> clientMap.get(client.getKey()).displaySupported(client.getValue()));
	}

	// service entry
	public void displayServiceEntries()
	{
		clear(this.serviceMap, accordionServiceEntries);
		model.getServiceEntries().forEach(serviceEntry -> {
			ServiceEntryFxController serviceController = Common.loadController(ServiceEntryFxController.class.getResource("ServiceEntryFx.fxml"));
			serviceController.init(model, serviceEntry, accordionServiceEntries);
			this.serviceMap.put(serviceEntry, serviceController);
		});
		this.serviceMap.values().forEach(ServiceEntryFxController::display);
	}

	public void displayBeforeStartService()
	{
		Common.progressBarVisible(true);
	}

	public void displayAfterStartService(Configuration.ServiceEntry entry, boolean isGood, String error)
	{
		Common.progressBarVisible(false);
		if (isGood)
		{
			DialogsHelper.showInfo("Service " + entry + " has been started");
		}
		else
		{
			DialogsHelper.showError("Service " + entry + " error: " + error);
		}
		this.serviceMap.get(entry).displayAfterStartService(isGood);
	}

	public void updateServiceVersion(HashMap<Configuration.ServiceEntry, SupportedEntry> map)
	{
		map.entrySet().forEach((service) -> serviceMap.get(service.getKey()).displaySupported(service.getValue()));
	}

	// sql entry
	public void displaySqlEntries()
	{
		clear(this.sqlMap, accordionSqlEntries);
		this.model.getSqlEntries().forEach(sqlEntry -> 
		{
			SqlEntryFxController sqlController = Common.loadController(SqlEntryFxController.class.getResource("SqlEntryFx.fxml"));
			sqlController.init(this.model, sqlEntry, accordionSqlEntries);
			this.sqlMap.put(sqlEntry, sqlController);
		});
		this.sqlMap.values().forEach(SqlEntryFxController::display);
	}

	public void showTestSqlPanel(Configuration.SqlEntry entry, List<Settings.SettingsValue> values)
	{
		Common.tryCatch(() -> {
			testSqlController = Common.loadController(TestingConnectionFxController.class.getResource("TestingConnectionFx.fxml"));
			testSqlController.init(model, entry.toString(), values);
			testSqlController.display();
		}, "Error on show test sql panel");
	}

	public void updateEntry(Entry entry)
	{
		if (entry instanceof Configuration.AppEntry)
		{
			appMap.get(entry).display();
		}
		else if (entry instanceof Configuration.LibEntry)
		{
			libMap.get(entry).display();
		}
		else if (entry instanceof Configuration.ClientEntry)
		{
			clientMap.get(entry).display();
		}
		else if (entry instanceof Configuration.ServiceEntry)
		{
			serviceMap.get(entry).display();
		}
		else if (entry instanceof Configuration.SqlEntry)
		{
			sqlMap.get(entry).display();
		}
	}

	public void displaySqlConnectionGood()
	{
		testSqlController.displayConnectionGood();
	}

	public void displaySqlConnectionBad(String message)
	{
		testSqlController.displayConnectionBad(message);
	}

	public static void displaySupported(TitledPane pane, SupportedEntry value, String entryName)
	{
		//TODO may be remake this code
		if (value.isSupported())
		{
			pane.setText(entryName + " confirmed");
			if (pane.getTextFill().equals(Color.RED))
			{
				pane.setTextFill(Color.BLACK);
			}
		}
		else
		{
			if (value.getRequaredMajorVersion() == -1 || value.getRequaredMinorVersion() == -1)
			{
				pane.setText(entryName + " load error");
			}
			else
			{
				pane.setText(entryName + ", required " + value.getRequaredMajorVersion() + "." + value.getRequaredMinorVersion());
			}
			pane.setTextFill(Color.RED);
		}
	}

	public void initSubControllers(ConfigurationFx model)
	{
		this.pathsController = Common.loadController(PathsController.class.getResource("Paths.fxml"));
		this.pathsController.init(model, titledPanePaths);

		this.formatsController = Common.loadController(FormatsController.class.getResource("Formats.fxml"));
		this.formatsController.init(model, titledPaneFormats);

		this.evaluatorController = Common.loadController(EvaluatorController.class.getResource("Evaluator.fxml"));
		this.evaluatorController.init(model, titledPaneEvaluator);
	}

	///============================================================
	// private methods
	//============================================================

	private void clear(Map<Entry, ? extends ContainingParent> map, Accordion accordion)
	{
		map.clear();
		accordion.getPanes().clear();
	}
}
