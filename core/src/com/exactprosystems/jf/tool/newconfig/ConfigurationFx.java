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
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.app.ApplicationPool;
import com.exactprosystems.jf.client.ClientsPool;
import com.exactprosystems.jf.common.MutableString;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.listeners.MatrixListener;
import com.exactprosystems.jf.common.parser.listeners.RunnerListener;
import com.exactprosystems.jf.common.parser.listeners.SilenceMatrixListener;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.documents.config.*;
import com.exactprosystems.jf.service.ServicePool;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.SupportedEntry;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import com.exactprosystems.jf.tool.newconfig.nodes.TreeNode;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigurationFx extends Configuration
{
	private Main mainModel;
	private ConfigurationFxController controller;
	private BorderPane pane;

	// ================================================================================

	private Map<String, SupportedEntry> supportedClients;
	private Map<String, SupportedEntry> supportedApps;
	private Map<String, SupportedEntry> supportedServices;
	private Map<String, ConnectionStatus> startedServices;    // TODO why is this thing here? it should be in ServicesPool

	private Map<ServiceEntry, ServiceConnection> serviceMap = new HashMap<>();


	public ConfigurationFx() throws Exception
	{
		this(null, null, null, null, null);
	}

	public ConfigurationFx(String fileName, RunnerListener runnerListener, Settings settings, Main mainModel, BorderPane pane) throws Exception
	{
		super(fileName, settings);

		this.supportedClients = new HashMap<>();
		this.supportedApps = new HashMap<>();
		this.supportedServices = new HashMap<>();
		this.startedServices = new HashMap<>();

		super.listener = DialogsHelper::showError;
		super.runnerListener = runnerListener;

		this.mainModel = mainModel;
		this.pane = pane;
	}

	// ================================================================================
	public String matrixToString()
	{
		return super.matricesValue.stream().map(MutableString::get).collect(Collectors.joining(SEPARATOR));
	}

	public String libraryToString()
	{
		return super.librariesValue.stream().map(MutableString::get).collect(Collectors.joining(SEPARATOR));
	}

	public String gitRemotePath()
	{
		return super.gitValue.get();
	}

	public String getReportPath()
	{
		return super.reportsValue.get();
	}

	public String getAppDictionaries()
	{
		return super.appDictionariesValue.stream().map(MutableString::get).collect(Collectors.joining(SEPARATOR));
	}

	public String getClientDictionaries()
	{
		return this.clientDictionariesValue.stream().map(MutableString::get).collect(Collectors.joining(SEPARATOR));
	}

	//region abstract document
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
		initController();
	}

	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);

		this.getServiceEntries().forEach(entry -> this.startedServices.put(entry.toString(), ConnectionStatus.NotStarted));
		initController();
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
	//endregion

	//region configuration
	public void refresh() throws Exception
	{
		this.display();
	}

	public void commitProject() throws Exception
	{
	}

	public void updateProject() throws Exception
	{
	}
	//endregion

	//region evaluator
	public void addNewEvaluatorImport(String newImport) throws Exception
	{
		this.addString(newImport, super.importsValue, this::displayEvaluator);
	}

	public void removeImport(String evaluatorImport) throws Exception
	{
		this.removeString(evaluatorImport, super.importsValue, this::displayEvaluator);
	}

	public void replaceEvaluatorImport(String oldEvaluator, String newEvaluator) throws Exception
	{
		this.replaceString(oldEvaluator, newEvaluator, super.importsValue, this::displayEvaluator, false);
	}
	//endregion

	//region format

	public void changeFormat(String key, String newValue) throws Exception
	{
		//TODO we need use parameter on table mutable string
//		String oldValue = this.formatsValue.get(index); 
//		if (Str.areEqual(oldValue, newValue))
//		{
//			return;
//		}
//		Command undo = () -> {
//			//			change(key, oldValue);
//			displayFormat();
//		};
//		Command redo = () -> {
//			//			change(key, newValue);
//			displayFormat();
//		};
//		addCommand(undo, redo);
		super.changed(true);
	}

	public void addNewAdditionalFormat(String newFormat) throws Exception
	{
		this.addString(newFormat, super.formatsValue, this::displayFormat);
	}

	public void removeAdditionalFormat(String removeFormat) throws Exception
	{
		this.removeString(removeFormat, super.formatsValue, this::displayFormat);
	}

	public void replaceAdditionalFormat(String oldFormat, String newFormat) throws Exception
	{
		this.replaceString(oldFormat, newFormat, super.formatsValue, this::displayFormat, false);
	}

	//endregion

	//region matrix
	public void removeMatrixDirectory(String file)
	{
		removeFile(file, super.matricesValue, this::displayMatrix);
	}

	public void openMatrix(File file)
	{
		Common.tryCatch(() -> this.mainModel.loadMatrix(path(file)), "Error on open matrix file");
	}

	public void addNewMatrix(File parentFolder, String fileName) throws Exception
	{
		File file = createNewFile(parentFolder, fileName, Configuration.matrixExt);
		Matrix matrix = new Matrix(path(file), this, new MatrixListener());
		matrix.create();
		matrix.save(path(file));
		displayMatrix();
	}

	public void removeMatrix(File matrixFile) throws Exception
	{
		removeFileFromFileSystem(matrixFile, this::displayMatrix);
	}

	//endregion

	//region library
	public void removeLibraryDirectory(String file)
	{
		removeFile(file, super.librariesValue, this::displayLibrary);
	}

	public void openLibrary(String path)
	{
		this.openLibrary(new File(path));
	}

	public void openLibrary(File file)
	{
		//TODO we need method to open library
		Common.tryCatch(() -> this.mainModel.loadMatrix(path(file)), "Error on open library");
	}

	public void addNewLibrary(File parentFolder, String fileName) throws Exception
	{
		createNewFile(parentFolder, fileName, Configuration.matrixExt);
		displayLibrary();
	}

	public void removeLibrary(File libraryFile) throws Exception
	{
		removeFileFromFileSystem(libraryFile, this::displayLibrary);
	}
	//endregion

	//region variable
	public void openVariableFile(File file)
	{
		Common.tryCatch(() -> this.mainModel.loadSystemVars(path(file)), "Error on load system variable");
	}

	public void removeVarsFile(String file)
	{
		removeFile(file, super.userVarsValue, this::displayVars);
	}
	//endregion

	//region report
	public void setReportFolder(String file) throws Exception
	{
		String lastFile = super.reportsValue.get();
		Command undo = () -> {
			super.reportsValue.set(lastFile);
			this.displayReport();
			this.displayFileSystem();
		};
		Command redo = () -> {
			super.reportsValue.set(file);
			this.displayReport();
			this.displayFileSystem();
		};
		super.addCommand(undo, redo);
		super.changed(true);
	}

	public void openReport(File file) throws Exception
	{
		Common.tryCatch(() -> this.mainModel.openReport(file), "Error on open report");
	}

	public void removeReport(File file) throws Exception
	{
		removeFileFromFileSystem(file, this::displayReport);
	}

	public void clearReportFolder() throws Exception
	{
		File reportFolder = new File(super.reportsValue.get());
		Optional.ofNullable(reportFolder.listFiles()).ifPresent(files -> removeFilesFromFileSystem(Arrays.asList(files), this::displayReport));
	}
	//endregion

	//region sql
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
	//endregion

	//region clients
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
		// TODO move to the clients pool
		this.supportedClients.clear();
		ClientsPool clientPool = new ClientsPool(this);
		for (ClientEntry entry : getClientEntries())
		{
			String id = entry.toString();
			this.supportedClients.put(entry.toString(), new SupportedEntry(clientPool.isSupported(id), clientPool.requiredMajorVersion(id), clientPool.requiredMinorVersion(id)));
		}
		this.displayClient();
	}

	public void removeClientDictionaryFolder(String file) throws Exception
	{
		this.removeFile(file, super.clientDictionariesValue, this::displayClient);
	}

	public void addClientDictionaryFolder(String file) throws Exception
	{
		this.addFile(file, super.clientDictionariesValue, this::displayClient);
	}

	public void openClientDictionary(ClientEntry entry) throws Exception
	{
		this.openClientDictionary(new File(entry.get(Configuration.clientDictionary)));
	}

	public void openClientDictionary(File file) throws Exception
	{
		System.out.println(String.format("CLIENT DICTIONARY PATH '%s' ARE OPENED", path(file)));
	}
	//endregion

	//region services
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
			this.supportedServices.put(entry.toString(), new SupportedEntry(servicePool.isSupported(id), servicePool.requiredMajorVersion(id), servicePool.requiredMinorVersion(id)));
		}
		this.displayService();
	}

	public void startService(ServiceEntry entry) throws Exception
	{
		// TODO move to service pool
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
					services.startService(createContext(new SilenceMatrixListener(), System.out), serviceConnection, startParameters);
					return null;
				}
			};

			startTask.setOnSucceeded(workerStateEvent -> {
				startedServices.replace(entry.toString(), ConnectionStatus.StartSuccessful);
				this.controller.displayService(getServiceEntries());
			});

			startTask.setOnFailed(workerStateEvent -> {
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
	//endregion

	//region application
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
			this.supportedApps.put(entry.toString(), new SupportedEntry(AppPool.isSupported(id), AppPool.requiredMajorVersion(id), AppPool.requiredMinorVersion(id)));
		}
		this.displayApp();
	}

	public void openAppsDictionary(AppEntry entry) throws Exception
	{
		this.openAppsDictionary(new File(entry.get(Configuration.appDicPath)));
	}

	public void openAppsDictionary(File file) throws Exception
	{
		Common.tryCatch(() -> this.mainModel.loadDictionary(path(file), null),"Error on load dictionary");
	}

	public void showAppHelp(AppEntry entry) throws Exception
	{
		IApplicationFactory iApplicationFactory = this.getApplicationPool().loadApplicationFactory(entry.get(entryName));
		String help = iApplicationFactory.getHelp();
		DialogsHelper.showAppHelp(help);
	}

	public void removeAppDictionaryFolder(String file) throws Exception
	{
		this.removeFile(file, super.appDictionariesValue, this::displayApp);
	}

	public void addAppDictionaryFolder(String file) throws Exception
	{
		this.addFile(file, super.appDictionariesValue, this::displayApp);
	}

	//endregion

	//region file system
	public void addAsMatrix(String file)
	{
		addFile(file, this.matricesValue, this::displayMatrix);
	}

	public void addAsLibrary(String file)
	{
		addFile(file, super.librariesValue, this::displayLibrary);
	}

	public void addAsVars(String file)
	{
		addFile(file, super.userVarsValue, this::displayLibrary);
	}
	//endregion

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

	//region private methods
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

		Command undo = () -> {
			list.remove(list.size() - 1);
			func.display();
		};
		Command redo = () -> {
			list.add(entry);
			func.display();
		};
		addCommand(undo, redo);
		super.changed(true);
	}

	private <T extends Entry> void removeEntry(Class<T> clazz, List<T> list, String name, Map<String, SupportedEntry> supportedMap, DisplayFunction func) throws Exception
	{
		if (name == null || name.isEmpty())
		{
			throw new Exception("Empty " + clazz.getSimpleName() + " entry name");
		}

		List<T> lastList = new ArrayList<>();
		lastList.addAll(list);

		HashMap<String, SupportedEntry> lastMap = new HashMap<>();
		lastMap.putAll(supportedMap);

		Command undo = () -> {
			list.clear();
			list.addAll(lastList);
			supportedMap.clear();
			supportedMap.putAll(lastMap);
			func.display();
		};
		Command redo = () -> {
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
			Parameter parameter = new Parameter();
			parameter.setKey(string).setValue("");
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

	private void removeString(String value, List<MutableString> list, DisplayFunction displayFunction)
	{
		List<MutableString> oldFiles = new ArrayList<>(list);
		Command undo = () ->
		{
			list.clear();
			list.addAll(oldFiles);
			displayFunction.display();
			this.displayFileSystem();
		};
		Command redo = () ->
		{
			List<MutableString> collect = list.stream().filter(f -> !f.equals(value)).collect(Collectors.toList());
			list.clear();
			list.addAll(collect);
			displayFunction.display();
			this.displayFileSystem();
		};
		super.addCommand(undo, redo);
		super.changed(true);
	}

	private void removeFile(String filePath, List<MutableString> list, DisplayFunction displayFunction)
	{
		// TODO need remove file from fileSystem
		List<MutableString> oldFiles = new ArrayList<>(list);
		Command undo = () ->
		{
			list.clear();
			list.addAll(oldFiles);
			displayFunction.display();
			this.displayFileSystem();
		};
		Command redo = () ->
		{
			List<MutableString> collect = list.stream().filter(f -> !path(filePath).equals(path(f.get()))).collect(Collectors.toList());
			list.clear();
			list.addAll(collect);
			displayFunction.display();
			this.displayFileSystem();
		};
		super.addCommand(undo, redo);
		super.changed(true);
	}

	private void addString(String file, List<MutableString> list, DisplayFunction displayFunction)
	{
		List<MutableString> oldFiles = new ArrayList<>(list);
		Command undo = () ->
		{
			list.clear();
			list.addAll(oldFiles);
			displayFunction.display();
		};
		Command redo = () ->
		{
			List<MutableString> collect = new ArrayList<>(list);
			collect.add(new MutableString(file));
			list.clear();
			list.addAll(collect);
			displayFunction.display();
		};
		super.addCommand(undo, redo);
		super.changed(true);
	}

	private void addFile(String filePath, List<MutableString> list, DisplayFunction displayFunction)
	{
		List<MutableString> oldFiles = new ArrayList<>(list);
		Command undo = () ->
		{
			list.clear();
			list.addAll(oldFiles);
			displayFunction.display();
		};
		Command redo = () ->
		{
			List<MutableString> collect = new ArrayList<>(list);
			collect.add(new MutableString(filePath));
			list.clear();
			list.addAll(collect);
			displayFunction.display();
		};
		super.addCommand(undo, redo);
		super.changed(true);
	}

	private void replaceString(String oldValue, String newValue, List<MutableString> list, DisplayFunction displayFunction, boolean needUpdateFileSystem)
	{
		if (oldValue.equals(newValue))
		{
			return;
		}
		List<MutableString> oldValues = new ArrayList<>(list);
		Command undo = () ->
		{
			list.clear();
			list.addAll(oldValues);
			displayFunction.display();
			if (needUpdateFileSystem)
			{
				this.displayFileSystem();
			}
		};
		Command redo = () ->
		{
			List<MutableString> collect = new ArrayList<>(list);
			collect.set(collect.indexOf(new MutableString(oldValue)), new MutableString(newValue));
			list.clear();
			list.addAll(collect);
			displayFunction.display();
			if (needUpdateFileSystem)
			{
				this.displayFileSystem();
			}
		};
		super.addCommand(undo, redo);
		super.changed(true);
	}

	private File createNewFile(File parentFolder, String nameOfFile, String ext) throws Exception
	{
		String newFileName = nameOfFile;
		if (!newFileName.endsWith(ext))
		{
			newFileName += ext;
		}
		File where = parentFolder;
		if (!parentFolder.isDirectory())
		{
			where = new File(path(parentFolder)).getParentFile();
		}
		File newFile = new File(path(where) + File.separator + newFileName);
		if (!newFile.createNewFile())
		{
			throw new Exception("Can't create new file");
		}
		return newFile;
	}

	private void removeFileFromFileSystem(File removeFile, DisplayFunction displayFunction)
	{
		// TODO think about undo/redo files
		forceDelete(removeFile);
		displayFunction.display();
	}

	private void removeFilesFromFileSystem(List<File> files, DisplayFunction displayFunction)
	{
		files.forEach(ConfigurationFx::forceDelete);
		displayFunction.display();
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
		return index == -1 ? "" : fileName.substring(index);
	}
	//endregion

	//region display methods
	private void displayEvaluator()
	{
		this.controller.displayEvaluator(toStringList(super.importsValue));
	}

	private void displayFormat()
	{
		this.controller.displayFormat(super.timeValue.get(), super.dateValue.get(), super.dateTimeValue.get(), toStringList(super.formatsValue));
	}

	private void displayMatrix()
	{
		this.controller.displayMatrix(toStringList(this.matricesValue));
	}

	private void displayLibrary()
	{
		this.controller.displayLibrary(super.libs);
	}

	private void displayVars()
	{
		this.controller.displayVars(toStringList(super.userVarsValue));
	}

	private void displayReport()
	{
		this.controller.displayReport(super.reportsValue.get());
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
		List<String> ignoreFiles = new ArrayList<>();

		ignoreFiles.addAll(toStringList(super.matricesValue));
		ignoreFiles.addAll(toStringList(super.librariesValue));
		ignoreFiles.add(super.varsValue.get());
		ignoreFiles.add(super.reportsValue.get());

		this.controller.displayFileSystem(ignoreFiles);
	}
	//endregion

	private void initController()
	{
		this.controller = Common.loadController(ConfigurationFx.class.getResource("config.fxml"));
		this.controller.init(this, this.pane);
	}
}
