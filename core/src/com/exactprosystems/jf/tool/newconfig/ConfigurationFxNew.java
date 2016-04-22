////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig;

import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.client.IClientFactory;
import com.exactprosystems.jf.api.client.Possibility;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.app.ApplicationPool;
import com.exactprosystems.jf.client.ClientsPool;
import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.listeners.RunnerListener;
import com.exactprosystems.jf.common.parser.listeners.SilenceMatrixListener;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.service.ServicePool;
import com.exactprosystems.jf.sql.SqlConnection;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.SupportedEntry;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import com.exactprosystems.jf.tool.newconfig.nodes.*;
import com.exactprosystems.jf.tool.newconfig.testing.TestingConnectionFxController;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigurationFxNew extends Configuration
{
	private Main				mainModel;

	public static final String	SEPARATOR				= ",";
	public static final File	initialFile				= new File("./");

	// ================================================================================
	// TODO replace this variables and methods from xml. It's only for test
	protected List<File>			matrixFolders				= new ArrayList<>();
	protected List<File>			libraryFoders				= new ArrayList<>();
	protected List<File>			varsFiles					= new ArrayList<>();
	protected File				reportFolder;

	// ================================================================================
	private ConfigurationNewFxController			controller;

	
	private Context									context;


	private Map<String, SupportedEntry>				supportedClients;
	private Map<String, SupportedEntry>				supportedApps;
	private Map<String, SupportedEntry>				supportedServices;
	private Map<String, ConnectionStatus>			startedServices;	// TODO why is this thing here? it should be in ServicesPool
	private List<File>								listAppsDictionaries;
	private List<File>								listClientDictionaries;

	private Map<ServiceEntry, ServiceConnection>	serviceMap	= new HashMap<>();

	public ConfigurationFxNew() throws Exception
	{
		this(null, null, null, null);
	}

	public ConfigurationFxNew(String fileName, RunnerListener runnerListener, Settings settings, Main mainModel) throws Exception
	{
		super(fileName, settings);

		this.supportedClients = new HashMap<>();
		this.supportedApps = new HashMap<>();
		this.supportedServices = new HashMap<>();
		this.startedServices = new HashMap<>();
		this.listAppsDictionaries = new ArrayList<>();
		this.listClientDictionaries = new ArrayList<>();
		this.context = createContext(new SilenceMatrixListener(), System.out);

		super.listener = DialogsHelper::showError;
		super.runnerListener = runnerListener;

		this.mainModel = mainModel;
		this.context = createContext(new SilenceMatrixListener(), System.out);
	}

	// ================================================================================
	public String matrixToString()
	{
		return this.matrixFolders.stream().map(ConfigurationFxNew::path).map(Common::getRelativePath).collect(Collectors.joining(SEPARATOR));
	}

	public String libraryToString()
	{
		return this.libraryFoders.stream().map(ConfigurationFxNew::path).map(Common::getRelativePath).collect(Collectors.joining(SEPARATOR));
	}

	public String gitRemotePath()
	{
		// TODO read this field from xml
		return "http://temp.com/project.git";
	}

	public String getReportPath()
	{
		return Common.getRelativePath(ConfigurationFxNew.path(this.reportFolder));
	}

	public String getAppDictionaries()
	{
		return this.listAppsDictionaries.stream().map(ConfigurationFxNew::path).map(Common::getRelativePath).collect(Collectors.joining(SEPARATOR));
	}

	public String getClientDictionaries()
	{
		return this.listClientDictionaries.stream().map(ConfigurationFxNew::path).map(Common::getRelativePath).collect(Collectors.joining(SEPARATOR));
	}

	public void setPane(BorderPane pane)
	{
//		pane.setTop(this.menuBar);
//		pane.setCenter(this.treeView);
//		pane.setBottom(this.tableView);
	}

	// ==============================================================================================================================
	// AbstractDocument
	// ==============================================================================================================================
	@Override
	public void display() throws Exception
	{
		super.display();
		displayEvaluator();
		displayFormat();
		displayMatrix();
		displayLibrary();
		displayVars();
		displayReport();
		displaySql();
		displayClient();
		displayService();
		displayApp();
		displayFileSystem();
	}

	@Override
	public void create() throws Exception
	{
		super.create();
//		init();
	}

	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
		this.getServiceEntries().forEach(entry -> this.startedServices.put(entry.toString(), ConnectionStatus.NotStarted));
		this.reportFolder = new File(this.get(Configuration.outputPath));
//		init();
	}

	@Override
	public void save(String fileName) throws Exception
	{
		super.save(fileName);
		// TODO
		// this.controller.saved();
		// this.controller.setTitle(Common.getSimpleTitle(fileName));
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

		// TODO
		// this.controller.close();
	}

	@Override
	protected void afterRedoUndo()
	{
//		// This need to refresh table items
//		int selectedItem = this.treeView.getSelectionModel().getSelectedIndex();
//		if (selectedItem == 0)
//		{
//			this.treeView.getSelectionModel().select(1);
//		}
//		else
//		{
//			this.treeView.getSelectionModel().selectFirst();
//		}
//		this.treeView.getSelectionModel().select(selectedItem);
	}

	// ==============================================================================================================================

	private void displayEvaluator()
	{
		try
		{
			this.controller.displayEvaluator(this.get(Configuration.evaluatorImports));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void displayFormat()
	{
		try
		{
			this.controller.displayFormat(this.get(timeFormat), this.get(dateFormat), this.get(dateTimeFormat), this.get(additionFormats));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void displayMatrix()
	{
		this.controller.displayMatrix(this.matrixFolders);
	}
	
	private void displayLibrary()
	{
		this.controller.displayLibrary(this.libraryFoders);
	}
	
	private void displayVars()
	{
		this.controller.displayVars(this.varsFiles);
	}
	
	private void displayReport()
	{
		this.controller.displayReport(this.reportFolder);
	}
	
	private void displaySql()
	{
		this.controller.displaySql(getSqlEntries());
	}
	
	private void displayClient()
	{
		this.controller.displayClient(getClientEntries());
	}
	
	private void displayService()
	{
		this.controller.displayService(getServiceEntries());
	}
	
	private void displayApp()
	{
		this.controller.displayApp(getAppEntries());
	}
	
	private void displayFileSystem()
	{
		List<File> ignoreFiles = new ArrayList<>(this.matrixFolders);
		ignoreFiles.addAll(this.libraryFoders);
		ignoreFiles.addAll(this.varsFiles);
		ignoreFiles.addAll(this.listAppsDictionaries);
		ignoreFiles.addAll(this.listClientDictionaries);
		ignoreFiles.add(this.reportFolder);
//		Common.tryCatch(() -> this.fileSystemTreeNode.display(this.initialFile.listFiles(), ignoreFiles), "Error on display sql entries");

	}

	// ============================================================
	// configuration
	// ============================================================
	public void refresh() throws Exception
	{
		this.display();
	}

	public void commitProject() throws Exception
	{
		System.out.println("commit task");
	}

	public void updateProject() throws Exception
	{
		System.out.println("update task");
	}

	// ============================================================
	// evaluator
	// ============================================================
	public void addNewEvaluatorImport(String newImport) throws Exception
	{
		String lastImports = this.get(Configuration.evaluatorImports);
		String newImports = lastImports + SEPARATOR + newImport;
		this.changeImports(lastImports, newImports);
	}

	public void removeImport(String evaluatorImport) throws Exception
	{
		String lastImports = this.get(Configuration.evaluatorImports);
		String newImports = Arrays.stream(lastImports.split(SEPARATOR)).filter(str -> !evaluatorImport.equals(str)).collect(Collectors.joining(SEPARATOR));
		this.changeImports(lastImports, newImports);
	}

	public void replaceEvaluatorImport(String oldValue, String newValue) throws Exception
	{
		this.changeImports(this.get(Configuration.evaluatorImports), this.get(Configuration.evaluatorImports).replaceAll(oldValue, newValue));
	}

	private void changeImports(String lastImports, String newImports) throws Exception
	{
		if (Str.areEqual(lastImports, newImports))
		{
			return;
		}
		Command undo = () ->
		{
			change(Configuration.evaluatorImports, lastImports);
			displayEvaluator();
		};
		Command redo = () ->
		{
			change(Configuration.evaluatorImports, newImports);
			displayEvaluator();
		};
		addCommand(undo, redo);
		super.changed(true);
	}

	// ============================================================
	// format
	// ============================================================
	public void changeFormat(String key, String newValue) throws Exception
	{
		String oldValue = this.get(key);
		if (Str.areEqual(oldValue, newValue))
		{
			return;
		}
		Command undo = () ->
		{
			change(key, oldValue);
			displayFormat();
		};
		Command redo = () ->
		{
			change(key, newValue);
			displayFormat();
		};
		addCommand(undo, redo);
		super.changed(true);
	}

	public void addNewAdditionalFormat(String newFormat) throws Exception
	{
		changeAdditionalFormats(this.get(additionFormats), this.get(additionFormats) + "|" + newFormat);
	}

	public void removeAdditionalFormat(String removeFormat) throws Exception
	{
		String oldAdditionalFormats = this.get(additionFormats);
		String newAdditionalFormats = Arrays.stream(oldAdditionalFormats.split("\\|")).filter(str -> !removeFormat.equals(str))
				.collect(Collectors.joining("|"));
		changeAdditionalFormats(oldAdditionalFormats, newAdditionalFormats);
	}

	public void replaceAdditionalFormat(String oldFormat, String newFormat) throws Exception
	{
		this.changeAdditionalFormats(this.get(additionFormats), this.get(additionFormats).replaceAll(oldFormat, newFormat));
	}

	private void changeAdditionalFormats(String oldFormats, String newFormats)
	{
		if (Str.areEqual(oldFormats, newFormats))
		{
			return;
		}
		Command undo = () ->
		{
			change(Configuration.additionFormats, oldFormats);
			displayFormat();
		};
		Command redo = () ->
		{
			change(Configuration.additionFormats, newFormats);
			displayFormat();
		};
		addCommand(undo, redo);
		super.changed(true);
	}

	// ============================================================
	// matrix
	// ============================================================
	public void removeMatrixDirectory(File file)
	{
		removeFile(file, this.matrixFolders, this::displayMatrix);
	}

	public void openMatrix(File file)
	{
		System.out.println(String.format("MATRIX FILE %S ARE OPENED", file.getName()));
	}

	public void addNewMatrix(File parentFolder, String fileName) throws Exception
	{
		String newFileName = fileName;
		if (!newFileName.endsWith(Configuration.matrixExt))
		{
			newFileName += Configuration.matrixExt;
		}
		File where = parentFolder;
		if (!parentFolder.isDirectory())
		{
			where = new File(path(parentFolder)).getParentFile();
		}
		File newMatrixFile = new File(path(where) + File.separator + newFileName);
		boolean newFile = newMatrixFile.createNewFile();
		if (newFile)
		{
			System.out.println(String.format("MATRIX WITH NAME '%s' WAS ADDED ON FOLDER '%s'", newFileName, path(where)));
			displayMatrix();
//			this.matrixTreeNode.select(newMatrixFile, item -> this.treeView.getSelectionModel().select(item));
		}
	}

	public void removeMatrix(File matrixFile) throws Exception
	{
		forceDelete(matrixFile);
		System.out.println("removed");
		displayMatrix();
	}

	// ============================================================
	// library
	// ============================================================
	public void removeLibraryDirectory(File file)
	{
		removeFile(file, this.libraryFoders, this::displayLibrary);
	}

	public void openLibrary(File file)
	{
		System.out.println(String.format("LIBRARY FILE %S ARE OPENED", file.getName()));
	}

	public void addNewLibrary(File parentFolder, String fileName) throws Exception
	{
		String newFileName = fileName;
		if (!newFileName.endsWith(Configuration.matrixExt))
		{
			newFileName += Configuration.matrixExt;
		}
		File where = parentFolder;
		if (!parentFolder.isDirectory())
		{
			where = new File(path(parentFolder)).getParentFile();
		}
		File newLibraryFile = new File(path(where) + File.separator + newFileName);
		boolean newFile = newLibraryFile.createNewFile();
		if (newFile)
		{
			System.out.println(String.format("Library WITH NAME '%s' WAS ADDED ON FOLDER '%s'", newFileName, path(where)));
			this.displayLibrary();
//			this.libTreeNode.select(newLibraryFile, item -> this.treeView.getSelectionModel().select(item));
		}
	}

	public void removeLibrary(File libraryFile) throws Exception
	{
		// TODO think about undo/redo files
		forceDelete(libraryFile);
		System.out.println("removed");
		this.displayLibrary();
	}

	// ============================================================
	// variable
	// ============================================================
	public void openVariableFile(File file)
	{
		System.out.println(String.format("VARS FILE %S ARE OPENED", file.getName()));
	}

	public void removeVarsFile(File file)
	{
		removeFile(file, this.varsFiles, this::displayVars);
	}

	// ============================================================
	// report
	// ============================================================
	public void setReportFolder(File file) throws Exception
	{
		File lastFile = this.reportFolder;
		Command undo = () ->
		{
			this.reportFolder = lastFile;
			this.displayReport();
			this.displayFileSystem();
		};
		Command redo = () ->
		{
			this.reportFolder = file;
			this.displayReport();
			this.displayFileSystem();
		};
		super.addCommand(undo, redo);
		super.changed(true);
	}

	public void openReport(File file) throws Exception
	{
		System.out.println(String.format("OPENED REPORT '%s'", path(file)));
	}

	public void removeReport(File file) throws Exception
	{
		File[] files = this.reportFolder.listFiles();
		if (files != null)
		{
			this.removeFile(file, Arrays.stream(files).collect(Collectors.toList()), this::displayReport);
		}
	}

	public void clearReportFolder() throws Exception
	{
		ConfigurationFxNew.cleanDirectory(this.reportFolder);
		this.displayReport();
	}

	// ============================================================
	// sql
	// ============================================================
	public void addNewSqlEntry(String sqlName) throws Exception
	{
		addNewEntry(SqlEntry.class, getSqlEntries(), sqlName, this::displaySql);
	}

	public void removeSqlEntry(SqlEntry entry) throws Exception
	{
		removeEntry(SqlEntry.class, getSqlEntries(), "" + entry, new HashMap<>(), this::displaySql);
	}

	public void changeSql(SqlEntry sqlEntry, String key, String value) throws Exception
	{
		changeEntry(sqlEntry, key, value);
	}

	// ============================================================
	// client
	// ============================================================
	public void addNewClientEntry(String clientName) throws Exception
	{
		addNewEntry(ClientEntry.class, getClientEntries(), clientName, this::displayClient);
	}

	public void removeClientEntry(ClientEntry entry) throws Exception
	{
		removeEntry(ClientEntry.class, getClientEntries(), "" + entry, this.supportedClients, this::displayClient);
	}

	public void showPossibilities(ClientEntry entry) throws Exception
	{
		ClientsPool pool = new ClientsPool(this);
		IClientFactory factory = pool.loadClientFactory(entry.toString());
		this.showPossibilities(factory.possebilities(), entry.toString());
	}

	public void addAllClientParams(ClientEntry entry) throws Exception
	{
		addAllKnowParameters(entry, entry.getParameters(), new ClientsPool(this).wellKnownParameters("" + entry), this::displayClient);
	}

	public void testClientVersion() throws Exception
	{
		this.supportedClients.clear();
		ClientsPool clientPool = new ClientsPool(this);
		for (ClientEntry entry : getClientEntries())
		{
			String id = entry.toString();
			this.supportedClients.put(entry.toString(),
					new SupportedEntry(clientPool.isSupported(id), clientPool.requiredMajorVersion(id), clientPool.requiredMinorVersion(id)));
		}
		this.displayClient();
	}

	public void removeClientDictionaryFolder(File file) throws Exception
	{
		this.removeFile(file, this.listClientDictionaries, this::displayClient);
	}

	public void addClientDictionaryFolder(File file) throws Exception
	{
		this.addFile(file, this.listClientDictionaries, this::displayClient);
	}

	public void openClientDictionary(ClientEntry entry) throws Exception
	{
		this.openClientDictionary(new File(entry.get(Configuration.clientDictionary)));
	}

	public void openClientDictionary(File file) throws Exception
	{
		System.out.println(String.format("CLIENT DICTIONARY PATH '%s' ARE OPENED", path(file)));
	}

	// ============================================================
	// service
	// ============================================================
	public void addNewServiceEntry(String clientName) throws Exception
	{
		addNewEntry(ServiceEntry.class, getServiceEntries(), clientName, this::displayService);
	}

	public void removeServiceEntry(ServiceEntry entry) throws Exception
	{
		removeEntry(ServiceEntry.class, getServiceEntries(), "" + entry, this.supportedServices, this::displayService);
	}

	public void addAllServiceParams(ServiceEntry entry) throws Exception
	{
		addAllKnowParameters(entry, entry.getParameters(), new ServicePool(this).wellKnownParameters("" + entry), this::displayService);
	}

	public void testServiceVersion() throws Exception
	{
		this.supportedServices.clear();
		ServicePool servicePool = new ServicePool(this);
		for (ServiceEntry entry : getServiceEntries())
		{
			String id = entry.toString();
			this.supportedServices.put(entry.toString(),
					new SupportedEntry(servicePool.isSupported(id), servicePool.requiredMajorVersion(id), servicePool.requiredMinorVersion(id)));
		}
		this.displayService();
	}

	public void startService(final ServiceEntry entry) throws Exception
	{
		try
		{
			final String idEntry = entry.toString();
			for (ServiceEntry next : serviceMap.keySet())
			{
				if (next.toString().equals(idEntry))
				{
					DialogsHelper.showInfo(String.format("Entry with id '%s' already started", idEntry));
					return;
				}
			}
			String parametersName = "StartParameters";
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

			Task<Void> startTask = new Task<Void>()
			{
				@Override
				protected Void call() throws Exception
				{
					IServicesPool services = getServicesPool();
					controller.displayService(getServiceEntries());
					ServiceConnection serviceConnection = services.loadService(entry.toString());
					serviceMap.put(entry, serviceConnection);
					services.startService(context, serviceConnection, startParameters);
					return null;
				}
			};

			startTask.setOnSucceeded(workerStateEvent ->
			{
				startedServices.replace(entry.toString(), ConnectionStatus.StartSuccessful);
				this.controller.displayService(getServiceEntries());
			});

			startTask.setOnFailed(workerStateEvent ->
			{
				startedServices.replace(entry.toString(), ConnectionStatus.StartFailed);
				this.controller.displayService(getServiceEntries());
			});
			new Thread(startTask).start();
		}
		catch (Exception e)
		{
			this.startedServices.replace(entry.toString(), ConnectionStatus.StartFailed);
			this.controller.displayService(getServiceEntries());
			throw e;
		}
	}

	public void stopService(ServiceEntry entry) throws Exception
	{
		ServiceConnection serviceConnection = this.serviceMap.remove(entry);
		if (serviceConnection != null)
		{
			getServicesPool().stopService(serviceConnection);
			this.startedServices.remove(entry.toString());
			this.controller.displayService(getServiceEntries());
		}
	}

	// ============================================================
	// app
	// ============================================================
	public void addNewAppEntry(String clientName) throws Exception
	{
		addNewEntry(AppEntry.class, getAppEntries(), clientName, this::displayApp);
	}

	public void removeAppEntry(AppEntry entry) throws Exception
	{
		removeEntry(AppEntry.class, getAppEntries(), "" + entry, this.supportedApps, this::displayApp);
	}

	public void addAllAppParams(AppEntry entry) throws Exception
	{
		addAllKnowParameters(entry, entry.getParameters(), new ApplicationPool(this).wellKnownParameters("" + entry), this::displayApp);
	}

	public void testAppVersion() throws Exception
	{
		this.supportedApps.clear();
		ApplicationPool AppPool = new ApplicationPool(this);
		for (AppEntry entry : getAppEntries())
		{
			String id = entry.toString();
			this.supportedApps.put(entry.toString(),
					new SupportedEntry(AppPool.isSupported(id), AppPool.requiredMajorVersion(id), AppPool.requiredMinorVersion(id)));
		}
		this.displayApp();
	}

	public void openAppsDictionary(AppEntry entry) throws Exception
	{
		this.openAppsDictionary(new File(entry.get(Configuration.appDicPath)));
	}

	public void openAppsDictionary(File file) throws Exception
	{
		System.out.println(String.format("APP DICTIONARY PATH '%s' ARE OPENED", path(file)));
	}

	public void showAppHelp(AppEntry entry) throws Exception
	{
		IApplicationFactory iApplicationFactory = this.getApplicationPool().loadApplicationFactory(entry.get(entryName));
		String help = iApplicationFactory.getHelp();
		DialogsHelper.showAppHelp(help);
	}

	public void removeAppDictionaryFolder(File file) throws Exception
	{
		this.removeFile(file, this.listAppsDictionaries, this::displayApp);
	}

	public void addAppDictionaryFolder(File file) throws Exception
	{
		this.addFile(file, this.listAppsDictionaries, this::displayApp);
	}

	// ============================================================
	// file system
	// ============================================================
	public void addAsMatrix(File file)
	{
		addFile(file, this.matrixFolders, this::displayMatrix);
	}

	public void addAsLibrary(File file)
	{
		addFile(file, this.libraryFoders, this::displayLibrary);
	}

	public void addAsVars(File file)
	{
		addFile(file, this.varsFiles, this::displayLibrary);
	}

	// ============================================================
	// abstract entry
	// ============================================================
	public void changeEntry(Entry entry, String key, Object newValue) throws Exception
	{
		String lastValue = entry.get(key);
		if (Objects.equals(lastValue, newValue))
		{
			return;
		}
		Command undo = () ->
		{
			Common.tryCatch(() -> entry.set(key, lastValue), "");
		};
		Command redo = () ->
		{
			Common.tryCatch(() -> entry.set(key, newValue), "");
		};
		addCommand(undo, redo);
		super.changed(true);
	}

	// ============================================================

	public static String path(File file)
	{
		try
		{
			return file.getCanonicalPath();
		}
		catch (Exception e)
		{
			e.printStackTrace();

		}
		return "";
	}

	public static String path(String string)
	{
		return path(new File(string));
	}

	// ============================================================
	// private methods
	// ============================================================
	private <T extends Entry> void addNewEntry(Class<T> clazz, List<T> list, String name, DisplayFunction func) throws Exception
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
	}

	private <T extends Entry> void removeEntry(Class<T> clazz, List<T> list, String name, Map<String, SupportedEntry> supportedMap, DisplayFunction func)
			throws Exception
	{
		if (name == null || name.isEmpty())
		{
			throw new Exception("Empty " + clazz.getSimpleName() + " entry name");
		}

		List<T> lastList = new ArrayList<>();
		lastList.addAll(list);

		HashMap<String, SupportedEntry> lastMap = new HashMap<>();
		lastMap.putAll(supportedMap);

		Command undo = () ->
		{
			list.clear();
			list.addAll(lastList);
			supportedMap.clear();
			supportedMap.putAll(lastMap);
			func.display();
		};
		Command redo = () ->
		{
			list.removeIf(entry -> entry.toString().equals(name));
			supportedMap.remove(name);
			func.display();
		};
		addCommand(undo, redo);
		super.changed(true);
	}

	private void addAllKnowParameters(Entry entry, List<Parameter> parameters, String[] strings, DisplayFunction func) throws Exception
	{
		List<Parameter> lastParameters = new ArrayList<>();
		lastParameters.addAll(entry.getParameters());

		for (String string : strings)
		{
			Configuration.Parameter parameter = new Configuration.Parameter();
			parameter.set(Configuration.parametersKey, string);
			parameter.set(Configuration.parametersValue, "");
			if (!parameters.contains(parameter))
			{
				parameters.add(parameter);
			}
		}

		Command undo = () ->
		{
			Common.tryCatch(() ->
			{
				entry.getParameters().clear();
				entry.getParameters().addAll(lastParameters);
				func.display();
			}, "");
		};
		Command redo = () ->
		{
			Common.tryCatch(() ->
			{
				entry.getParameters().clear();
				entry.getParameters().addAll(parameters);
				func.display();
			}, "");
		};
		addCommand(undo, redo);
		super.changed(true);
	}

	private void removeFile(File file, List<File> list, DisplayFunction displayFunction)
	{
		// TODO need remove file from fileSystem
		List<File> oldFiles = new ArrayList<>(list);
		Command undo = () ->
		{
			list.clear();
			list.addAll(oldFiles);
			displayFunction.display();
			this.displayFileSystem();
		};
		Command redo = () ->
		{
			List<File> collect = list.stream().filter(f -> !path(file).equals(path(f))).collect(Collectors.toList());
			list.clear();
			list.addAll(collect);
			displayFunction.display();
			this.displayFileSystem();
		};
		super.addCommand(undo, redo);
		super.changed(true);
	}

	private void addFile(File file, List<File> list, DisplayFunction displayFunction)
	{
		List<File> oldFiles = new ArrayList<>(list);
		Command undo = () ->
		{
			list.clear();
			list.addAll(oldFiles);
			displayFunction.display();
			this.displayFileSystem();
		};
		Command redo = () ->
		{
			List<File> collect = new ArrayList<>(list);
			collect.add(file);
			list.clear();
			list.addAll(collect);
			displayFunction.display();
			this.displayFileSystem();
		};
		super.addCommand(undo, redo);
		super.changed(true);
	}

	private void select(TreeNode startNode)
	{

	}

	@FunctionalInterface
	private interface DisplayFunction
	{
		void display();
	}

	private void showPossibilities(Set<Possibility> possibilities, String entryName)
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

	private void change(String value, String newValue)
	{
		Common.tryCatch(() -> set(value, newValue), "Error on change " + value + " to set " + newValue);
	}

	private static void forceDelete(File directory)
	{
		if (directory.isDirectory())
		{
			cleanDirectory(directory);
		}
		directory.delete();
	}

	private static void cleanDirectory(File directory)
	{
		File[] files = directory.listFiles();
		if (files != null)
		{
			for (File file : files)
			{
				if (file.isDirectory())
				{
					forceDelete(file);
				}
				else
				{
					file.delete();
				}
			}
		}
	}

	public static String getExtension(String fileName)
	{
		int index = fileName.lastIndexOf(".");
		return index == -1 ? "" : fileName.substring(index + 1);
	}

	private void initController()
	{
		this.controller = Common.loadController(ConfigurationFxNew.class.getResource("config.fxml"));
		this.controller.init(this);
	}

}
