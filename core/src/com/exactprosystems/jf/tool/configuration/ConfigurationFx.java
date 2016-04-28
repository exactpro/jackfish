////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.configuration;

import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.client.IClientFactory;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.app.ApplicationPool;
import com.exactprosystems.jf.client.ClientsPool;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.listeners.RunnerListener;
import com.exactprosystems.jf.common.parser.listeners.SilenceMatrixListener;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.documents.config.AppEntry;
import com.exactprosystems.jf.documents.config.ClientEntry;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.config.Entry;
import com.exactprosystems.jf.documents.config.LibEntry;
import com.exactprosystems.jf.documents.config.Parameter;
import com.exactprosystems.jf.documents.config.ServiceEntry;
import com.exactprosystems.jf.documents.config.SqlEntry;
import com.exactprosystems.jf.service.ServicePool;
import com.exactprosystems.jf.sql.SqlConnection;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.SupportedEntry;
import com.exactprosystems.jf.tool.configuration.sqlentry.testing.TestingConnectionFxController;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;

import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;

import org.apache.log4j.Logger;

import javax.xml.bind.annotation.XmlRootElement;

import java.io.Reader;
import java.util.*;

@XmlRootElement(name = "configuration")
public class ConfigurationFx extends Configuration
{
	private static final Logger						logger				= Logger.getLogger(ConfigurationFx.class);
	public static final String						startParameters		= "StartParameters";
	public static final String						connectParameters	= "ConnectParameters";

	private boolean									isControllerInit = false;
	private Main									mainModel;

	private ConfigurationFxController				controller;
	private Context									context;
	private Map<ServiceEntry, ServiceConnection>	serviceMap			= new HashMap<>();
	private Task<Void>								startTask;

	public ConfigurationFx() throws Exception
	{
		this(null, null, null, null);
	}

	public ConfigurationFx(String fileName, RunnerListener runnerListener, Settings settings, Main mainModel) throws Exception
	{
		super(fileName,  settings);
		
		super.listener = DialogsHelper::showError;
		super.runnerListener = runnerListener;
		
		this.mainModel = mainModel;
		this.context = createContext(new SilenceMatrixListener(), System.out);
	}

	//==============================================================================================================================
	// AbstractDocument
	//==============================================================================================================================
	@Override
	public void display() throws Exception
	{
		super.display();

		displayPaths();
		displayFormats();
		displayEvaluator();

		displayLibEntries();
		displayAppEntries();
		displayClientEntries();
		displayServiceEntries();
		displaySqlEntries();

		this.controller.displaySubControllers();
	}

	@Override
	public void create() throws Exception
	{
		super.create();
		initController();
	}
	
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
		initController();
	}

	@Override
	public void save(String fileName) throws Exception
	{
		super.save(fileName);
		this.controller.saved();
		this.controller.setTitle(Common.getSimpleTitle(fileName));
	}
	
	@Override
	public boolean canClose() throws Exception
	{
		if (!super.canClose())
		{
			return false;
		}
		if (isChanged())
		{
			ButtonType desision = DialogsHelper.showSaveFileDialog(this.getName());
			if (desision == ButtonType.YES)
			{
				save(getName());
			}
			if (desision == ButtonType.CANCEL)
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public void close(Settings settings) throws Exception
	{
		super.close(settings);

		if (this.mainModel != null)
		{
			this.mainModel.setConfiguration(null);
		}

		if (this.context != null)
		{
			this.context.close();
		}

		this.controller.close();
	}

	@Override
	protected void afterRedoUndo() 
	{
		super.afterRedoUndo();
	}

	// =======================================================================
	// paths
	// =======================================================================
	public void displayPaths()
	{
		Common.tryCatch(() -> this.controller.displayPaths(get(outputPath), get(variables), get(userVariables)), "Error on display paths");
	}

	public void changePaths(String output, String variables, String userVariables) throws Exception
	{
		String lastOutput			= get(Configuration.outputPath); 
		String lastVariables		= get(Configuration.variables);
		String lastUserVariables	= get(Configuration.userVariables);
		if (Str.areEqual(lastOutput, output) && Str.areEqual(lastVariables, variables) && Str.areEqual(lastUserVariables, userVariables))
		{
			return;
		}
		Command undo = () -> 
		{
			changePath(Configuration.outputPath, lastOutput);
			changePath(Configuration.variables, lastVariables);
			changePath(Configuration.userVariables, lastUserVariables);
			displayPaths();
		};
		Command redo = () -> 
		{
			changePath(Configuration.outputPath, output);
			changePath(Configuration.variables, variables);
			changePath(Configuration.userVariables, userVariables);
			displayPaths();
		};
		addCommand(undo, redo);
		super.changed(true);
	}

	public void openVars(final String path) throws Exception
	{
		this.mainModel.loadSystemVars(path);
	}

	// =======================================================================
	// formats
	// =======================================================================
	public void displayFormats()
	{
		Common.tryCatch(() -> this.controller.displayFormats(get(dateTimeFormat), get(dateFormat), get(timeFormat), get(additionFormats)), "Error on display formats");
	}

	public void changeFormats(String dateTime, String date, String time, String additionFormat) throws Exception
	{
		String lastDateTime			= get(Configuration.dateTimeFormat);
		String lastDate				= get(Configuration.dateFormat); 
		String lastTime				= get(Configuration.timeFormat);
		String lastAdditionFormat	= get(Configuration.additionFormats);
		if (Str.areEqual(lastDateTime, dateTime) && Str.areEqual(lastDate, date) && Str.areEqual(lastTime, time) && Str.areEqual(lastAdditionFormat, additionFormat))
		{
			return;
		}
		Command undo = () -> 
		{
			change(Configuration.dateTimeFormat, lastDateTime);
			change(Configuration.dateFormat, lastDate);
			change(Configuration.timeFormat, lastTime);
			change(Configuration.additionFormats, lastAdditionFormat);
			displayFormats();
		};
		Command redo = () -> 
		{
			change(Configuration.dateTimeFormat, dateTime);
			change(Configuration.dateFormat, date);
			change(Configuration.timeFormat, time);
			change(Configuration.additionFormats, additionFormat);
			displayFormats();
		};
		addCommand(undo, redo);
		super.changed(true);
	}

	// =======================================================================
	// evaluator
	// =======================================================================
	public void displayEvaluator()
	{
		Common.tryCatch(() -> this.controller.displayEvaluator(get(evaluatorImports)), "Error on init evaluator");
	}

	public void changeEvaluator(String evaluatorImports) throws Exception
	{
		String lastEvaluatorImports = get(Configuration.evaluatorImports);
		if (Str.areEqual(lastEvaluatorImports, evaluatorImports))
		{
			return;
		}
		Command undo = () -> 
		{
			change(Configuration.evaluatorImports, lastEvaluatorImports);
			displayEvaluator();
		};
		Command redo = () -> 
		{
			change(Configuration.evaluatorImports, evaluatorImports);
			displayEvaluator();
		};
		addCommand(undo, redo);
		super.changed(true);
}

	private void change(String value, String newValue)
	{
		Common.tryCatch(() -> {
			set(value, newValue);
		}, "Error on change " + value + " to set " + newValue);
	}

	private void changePath(String value, String newValue)
	{
		Common.tryCatch(() -> {
			if (newValue == null || newValue.equals("null"))
			{
				return;
			}
			String relativePath = Common.getRelativePath(newValue);
			set(value, relativePath);
		}, "Error on set " + value + " to " + newValue);
	}

	// =======================================================================
	// apps entry
	// =======================================================================
	private void displayAppEntries() throws Exception
	{
		this.controller.displayAppEntries();
	}

	public void testSqlConnection(String sql, String server, String base, String user, String password) throws Exception
	{
		Common.tryCatch(() ->
		{
			settings.removeAll(Settings.GLOBAL_NS, Settings.SQL + sql);
			settings.setValue(Settings.GLOBAL_NS, Settings.SQL + sql, TestingConnectionFxController.SERVER_NAME, server);
			settings.setValue(Settings.GLOBAL_NS, Settings.SQL + sql, TestingConnectionFxController.USER, user);
			settings.setValue(Settings.GLOBAL_NS, Settings.SQL + sql, TestingConnectionFxController.DATABASE_NAME, base);
			settings.saveIfNeeded();
			SqlConnection connect = getDataBasesPool().connect(sql, server, base, user, password);
			if (connect != null && !connect.isClosed() && connect.getConnection().isValid(1))
			{
				this.controller.displaySqlConnectionGood();
			}
			else
			{
				this.controller.displaySqlConnectionBad(null);
			}
		}, "Error on test sql connection");
	}
	

	public void addAllKnowAppParameters(Entry entry, List<Parameter> parameters) throws Exception
	{
		addAllKnowParameters(entry, parameters, new ApplicationPool(this).wellKnownParameters(entry.toString()));
	}

	public void loadDictionary(String pathToDictionary, String entryName) throws Exception
	{
		this.mainModel.loadDictionary(pathToDictionary, entryName);
	}

	public void removeAppEntry(AppEntry entry) throws Exception
	{
		removeEntry(AppEntry.class, getAppEntries(), "" + entry, this.controller::displayAppEntries);
	}

	public void addNewAppEntry(String entryName) throws Exception
	{
		addNewEntry(AppEntry.class, getAppEntries(), entryName, this.controller::displayAppEntries);
	}

	public void testVersionApp() throws Exception
	{
		Common.tryCatchThrow(() -> {
			HashMap<AppEntry, SupportedEntry> map = new HashMap<>();
			ApplicationPool appPool = new ApplicationPool(this);
			for (AppEntry entry : getAppEntries())
			{
				String id = entry.toString();
				map.put(entry, new SupportedEntry(appPool.isSupported(id), appPool.requiredMajorVersion(id), appPool.requiredMinorVersion(id)));
			}
			this.controller.updateAppVersion(map);
		}, "Error on test version");
	}

	public void showHelp(AppEntry entry) throws Exception
	{
		IApplicationFactory iApplicationFactory = this.getApplicationPool().loadApplicationFactory(entry.get(entryName));
		String help = iApplicationFactory.getHelp();
		this.controller.showAppHelp(help);
	}

	// =======================================================================
	// lib entry
	// =======================================================================
	private void displayLibEntries() throws Exception
	{
		this.controller.displayLibEntries();
	}

	public void openLib(String pathToFile) throws Exception
	{
		this.mainModel.loadMatrix(pathToFile);
	}

	public void removeLibEntry(LibEntry entry) throws Exception
	{
		removeEntry(LibEntry.class, getLibEntries(), "" + entry, this.controller::displayLibEntries);
	}

	public void addNewLibEntry(String entryName) throws Exception
	{
		addNewEntry(LibEntry.class, getLibEntries(), entryName, this.controller::displayLibEntries);
	}

	// =======================================================================
	// client entry
	// =======================================================================
	private void displayClientEntries() throws Exception
	{
		this.controller.displayClientEntries();
	}

	public void addAllKnowClientParameters(Entry entry, ArrayList<Parameter> parameters) throws Exception
	{
		addAllKnowParameters(entry, parameters, new ClientsPool(this).wellKnownParameters("" + entry));
	}

	public void removeClientEntry(ClientEntry entry) throws Exception
	{
		removeEntry(ClientEntry.class, getClientEntries(), "" + entry, this.controller::displayClientEntries);
	}

	public void addNewClientEntry(String entryName) throws Exception
	{
		addNewEntry(ClientEntry.class, getClientEntries(), entryName, this.controller::displayClientEntries);
	}

	public void showPossibilities(ClientEntry entry) throws Exception
	{
		Common.tryCatchThrow(() -> {
			ClientsPool pool = new ClientsPool(this);
			IClientFactory factory = pool.loadClientFactory(entry.toString());
			this.controller.showPossibilities(factory.possebilities(), entry.toString());
		}, "Error on show possibilities for client entry");
	}

	public void testVersionClient() throws Exception
	{
		Common.tryCatchThrow(() -> {
			HashMap<ClientEntry, SupportedEntry> map = new HashMap<>();
			ClientsPool appPool = new ClientsPool(this);
			for (ClientEntry entry : getClientEntries())
			{
				String id = entry.toString();
				map.put(entry, new SupportedEntry(appPool.isSupported(id), appPool.requiredMajorVersion(id), appPool.requiredMinorVersion(id)));
			}
			this.controller.updateClientVersion(map);
		}, "Error on test client version");
	}

	// =======================================================================
	// service entry
	// =======================================================================
	private void displayServiceEntries() throws Exception
	{
		this.controller.displayServiceEntries();
	}

	public void addAllKnowServiceParameters(Entry entry, ArrayList<Parameter> parameters) throws Exception
	{
		addAllKnowParameters(entry, parameters, new ServicePool(this).wellKnownParameters("" + entry));
	}

	public void startService(final ServiceEntry entry) throws Exception
	{
		final String idEntry = entry.toString();
		for (ServiceEntry next : serviceMap.keySet())
		{
			if (next.toString().equals(idEntry))
			{
				return;
			}
		}
		String parametersName = startParameters;
		String title = "Start ";
		String[] strings = getServicesPool().wellKnownStartArgs(idEntry);
		Settings settings = getSettings();
		final Map<String, String> parameters = settings.getMapValues(Settings.SERVICE + idEntry, parametersName, strings);

		AbstractEvaluator evaluator = createEvaluator();
		ButtonType buttonType = DialogsHelper.showParametersDialog(title + idEntry, parameters, evaluator);
		if (buttonType == ButtonType.CANCEL)
		{
			return;
		}

		settings.setMapValues(Settings.SERVICE + idEntry, parametersName, parameters);
		settings.saveIfNeeded();
		final Map<String, Object> startParameters = new HashMap<>();
		for (Map.Entry<String, String> next : parameters.entrySet())
		{
			String name = next.getKey();
			String expression = next.getValue();
			try
			{
				Object value = evaluator.evaluate(expression);
				startParameters.put(name, value);
			}
			catch (Exception e)
			{
				throw new Exception("Error in " + name + " = " + expression + " :" + e.getMessage(), e);
			}
		}

		this.startTask = new Task<Void>()
		{
			@Override
			protected Void call() throws Exception
			{
				IServicesPool services = getServicesPool();
				controller.displayBeforeStartService();
				ServiceConnection serviceConnection = services.loadService(entry.toString());
				serviceMap.put(entry, serviceConnection);
				services.startService(context, serviceConnection, startParameters);
				return null;
			}
		};

		this.startTask.setOnSucceeded(workerStateEvent ->
		{
			controller.displayAfterStartService(entry, true, null);
		});

		this.startTask.setOnFailed(workerStateEvent ->
		{
			Throwable exception = startTask.getException();
			logger.error(exception.getMessage(), exception);
			controller.displayAfterStartService(entry, false, exception.getMessage());
		});
		Thread thread = new Thread(startTask);
		thread.setName("Start service : " + idEntry + " , thread id : " + thread.getId());
		thread.start();
	}

	public void stopService(ServiceEntry entry) throws Exception
	{
		Common.tryCatchThrow(() ->
		{
			ServiceConnection serviceConnection = this.serviceMap.remove(entry);
			if (serviceConnection != null)
			{
				getServicesPool().stopService(serviceConnection);
				this.controller.displayAfterStartService(entry, false, " stopped.");
			}
		}, "Error on stop service");
	}

	public void removeService(ServiceEntry entry) throws Exception
	{
		removeEntry(ServiceEntry.class, getServiceEntries(), "" + entry, this.controller::displayServiceEntries);
	}

	public void addNewService(String entryName) throws Exception
	{
		addNewEntry(ServiceEntry.class, getServiceEntries(), entryName, this.controller::displayServiceEntries);
	}

	public void testVersionService() throws Exception
	{
		Common.tryCatchThrow(() -> {
			HashMap<ServiceEntry, SupportedEntry> map = new HashMap<>();
			ServicePool appPool = new ServicePool(this);
			for (ServiceEntry entry : getServiceEntries())
			{
				String id = entry.toString();
				map.put(entry, new SupportedEntry(appPool.isSupported(id), appPool.requiredMajorVersion(id), appPool.requiredMinorVersion(id)));
			}
			this.controller.updateServiceVersion(map);
		}, "Error on test service version");
	}

	// =======================================================================
	// sql entry
	// =======================================================================
	private void displaySqlEntries() throws Exception
	{
		this.controller.displaySqlEntries();
	}

	public void removeSqlEntry(SqlEntry entry) throws Exception
	{
		removeEntry(SqlEntry.class, getSqlEntries(), "" + entry, this.controller::displaySqlEntries);
	}

	public void addNewSql(String entryName) throws Exception
	{
		addNewEntry(SqlEntry.class, getSqlEntries(), entryName, this.controller::displaySqlEntries);
	}

	public void testSql(SqlEntry entry) throws Exception
	{
		String s = entry.get(Configuration.entryName);
		List<Settings.SettingsValue> values = settings.getValues(Settings.GLOBAL_NS, Settings.SQL + s);
		Common.tryCatchThrow(() -> this.controller.showTestSqlPanel(entry, values), "Error on ");
	}

	
	// =======================================================================
	// Entry
	// =======================================================================
	@FunctionalInterface
	private interface DisplayFunction
	{
		void display();
	}

	private <T extends Entry> void addNewEntry(Class<T> clazz, List<T> list, String name, DisplayFunction func) throws Exception
	{
		Common.tryCatchThrow(() ->
		{
			if (name == null || name.isEmpty())
			{
				throw new Exception("Empty " + clazz.getSimpleName() + " entry name");
			}
			if (list.stream().anyMatch(entry -> entry.toString().equals(name)))
			{
				throw new Exception(String.format("%s entry with name %s is already present", clazz.getSimpleName(), name));
			}

			T entry = clazz.newInstance();
			entry.set(entryName, name);

			Command undo = () ->
			{
				list.remove(list.size() - 1);
				func.display();
			};
			Command redo = () ->
			{
				list.add(entry);
				func.display();
			};
			addCommand(undo, redo);
			super.changed(true);

		}, "Error on adding new " + clazz.getSimpleName() + " entry " + name);
	}

	private <T extends Entry> void removeEntry(Class<T> clazz, List<T> list, String name, DisplayFunction func) throws Exception
	{
		Common.tryCatchThrow(() ->
		{
			if (name == null || name.isEmpty())
			{
				throw new Exception("Empty " + clazz.getSimpleName() + " entry name");
			}
			
			List<T> lastList = new ArrayList<T>();
			lastList.addAll(list);
					
			Command undo = () -> 
			{
				list.clear();
				list.addAll(lastList);
				func.display();
			};
			Command redo = () -> 
			{
				list.removeIf(entry -> entry.toString().equals(name));
				func.display();
			};
			addCommand(undo, redo);
			super.changed(true);
		}, "Error on removing " + clazz.getSimpleName() + " entry " + name);
	}

	private void addAllKnowParameters(Entry entry, List<Parameter> parameters, String[] strings) throws Exception
	{
		Common.tryCatchThrow(() -> 
		{
			Class<?> clazz = entry.getClass();
			String name = "" + entry;
			List<Parameter> lastParameters = new ArrayList<Parameter>();
			lastParameters.addAll(entry.getParameters());
			
			for (String string : strings)
			{
				Parameter parameter = new Parameter();
				parameter.set(Configuration.parametersKey, string);
				parameter.set(Configuration.parametersValue, "");
				if (!parameters.contains(parameter))
				{
					parameters.add(parameter);
				}
			}

			Command undo = () -> 
			{
				try 
				{
					Entry e = find(clazz, name);
					e.getParameters().clear();
					e.getParameters().addAll(lastParameters);
					this.controller.updateEntry(e);
				} catch (Exception e) { }
			};
			Command redo = () -> 
			{
				try 
				{
					Entry e = find(clazz, name);
					e.getParameters().clear();
					e.getParameters().addAll(parameters);
					this.controller.updateEntry(e);
				} catch (Exception e) {	}
			};
			addCommand(undo, redo);
			
			this.controller.updateEntry(entry);
		}, "Error on adding all known parameters");
	}



	public void changeEntryPath(Entry entry, String field, String newValue) throws Exception
	{
		if (newValue == null)
		{
			return;
		}
		Common.tryCatchThrow(() -> {
			String lastValue = entry.get(field);
			String relativePath = Common.getRelativePath(newValue);
			Class<?> clazz = entry.getClass();
			String name = "" + entry;

			Command undo = () -> {
				try
				{
					Entry e = find(clazz, name);
					e.set(field, lastValue);
					this.controller.updateEntry(e);
				}
				catch (Exception e)
				{
				}
			};
			Command redo = () -> {
				try
				{
					Entry e = find(clazz, name);
					e.set(field, relativePath);
					this.controller.updateEntry(e);
				}
				catch (Exception e)
				{
				}
			};
			addCommand(undo, redo);
			super.changed(true);
		}, "Error on changing path " + newValue + " entry " + entryName + ".\n");
	}

	public void changeEntry(Entry entry, String field, Object newValue) throws Exception
	{
		Common.tryCatchThrow(() ->
		{
			String lastValue = entry.get(field);
			Class<?> clazz = entry.getClass();
			String name = "" + entry;
			if (Objects.equals(lastValue, newValue))
			{
				return;
			}
			Command undo = () ->
			{
				try
				{
					Entry e = find(clazz, name);
					e.set(field, lastValue);
					this.controller.updateEntry(e);
				} catch (Exception e) {	}
			};
			Command redo = () ->
			{
				try
				{
					Entry e = find(clazz, name);
					e.set(field, newValue);
					this.controller.updateEntry(e);
				} catch (Exception e) {	}
			};
			addCommand(undo, redo);
			super.changed(true);
		}, "Error on changing parameter " + field + " entry " + entry + ".\n");
	}

	public void removeParameters(Entry entry, List<Parameter> parameterList)
	{
		final List<Parameter> lastParameters = new ArrayList<>(entry.getParameters());
		Class<?> clazz = entry.getClass();
		String name = "" + entry;

		Command undo = () -> 
		{
			try 
			{
				Entry e = find(clazz, name);
				e.getParameters().clear();
				e.getParameters().addAll(lastParameters);
				this.controller.updateEntry(e);
			} catch (Exception e) {	}
		};
		Command redo = () -> 
		{
			try 
			{
				Entry e = find(clazz, name);
				e.getParameters().removeAll(parameterList);
				this.controller.updateEntry(e);
			} catch (Exception e) {	}
		};
		addCommand(undo, redo);
		
		super.changed(true);
	}

	public void updateParameter(Entry entry, Parameter parameter, String newValue) throws Exception
	{
		Common.tryCatchThrow(() -> 
		{
			String parameterName = parameter.getKey();
			String lastValue = parameter.get(Configuration.parametersValue);

			Class<?> clazz = entry.getClass();
			String name = "" + entry;

			Command undo = () -> 
			{
				try 
				{
					Entry e = find(clazz, name);
					e.getParameters().stream().filter(p -> p.getKey().equals(parameterName)).forEach(p -> p.setValue(lastValue));
					this.controller.updateEntry(e);
				} catch (Exception e) {	}
			};
			Command redo = () -> 
			{
				try 
				{
					Entry e = find(clazz, name);
					e.getParameters().stream().filter(p -> p.getKey().equals(parameterName)).forEach(p -> p.setValue(newValue));
					this.controller.updateEntry(e);
				} catch (Exception e) {	}
			};
			addCommand(undo, redo);
			
			super.changed(true);
			
			
		}, "Error on setting parameter " + parameter.getKey() + " new value " + newValue);
	}
	
	private Entry find (Class<?> clazz, String name) throws Exception
	{
		if (clazz == AppEntry.class)
		{
			return getAppEntry(name);
		}
		else if (clazz == ClientEntry.class)
		{
			return getClientEntry(name);
		}
		else if (clazz == LibEntry.class)
		{
			return getLibEntry(name);
		}
		else if (clazz == ServiceEntry.class)
		{
			return getServiceEntry(name);
		}
		else if (clazz == SqlEntry.class)
		{
			return getSqlEntry(name);
		}
		
		return null;
	}
	
	private void initController()
	{
		if (!this.isControllerInit)
		{
			this.controller = Common.loadController(ConfigurationFx.class.getResource("ConfigurationTab.fxml"));
			this.controller.init(this);
			this.controller.setTitle(Common.getSimpleTitle(getName()));
			this.controller.initSubControllers(this);
			this.isControllerInit = true;
		}
	}
}