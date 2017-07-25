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
import com.exactprosystems.jf.api.client.IClientsPool;
import com.exactprosystems.jf.api.client.Possibility;
import com.exactprosystems.jf.api.common.IPool;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.service.IServiceFactory;
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.api.service.ServiceStatus;
import com.exactprosystems.jf.app.ApplicationPool;
import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.common.MutableString;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.documentation.DocumentationBuilder;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ContextHelpFactory;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.*;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.listeners.RunnerListener;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.SupportedEntry;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.Reader;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@XmlRootElement(name="configuration")
public class ConfigurationFx extends Configuration
{
	//region fields
	private CompareEnum currentCompareEnum;
	private Main model;
	private ConfigurationFxController controller;
	private BorderPane pane;

	private boolean isControllerInit = false;

	private Map<String, SupportedEntry> supportedClients;
	private Map<String, SupportedEntry> supportedApps;
	private Map<String, SupportedEntry> supportedServices;

	private Map<ServiceEntry, ServiceConnection> serviceConnectionMap = new HashMap<>();
	//endregion

	//region Constructors
	public ConfigurationFx() throws Exception
	{
		this(null, null, null, null);
	}

	public ConfigurationFx(DocumentFactory factory, String fileName, RunnerListener runnerListener, Main model) throws Exception
	{
		super(fileName, factory);

		this.supportedClients = new HashMap<>();
		this.supportedApps = new HashMap<>();
		this.supportedServices = new HashMap<>();

		if (runnerListener != null)
		{
			super.runnerListener = runnerListener;
		}

		this.model = model;
	}
	//endregion

	//region Getters/Setters

	public void setPane(BorderPane pane)
	{
		this.pane = pane;
	}

	//endregion

	//region Utilities methods toString
	public String matrixToString()
	{
		return super.matricesValue.stream().map(MutableString::get).collect(Collectors.joining(SEPARATOR));
	}

	public String libraryToString()
	{
		return super.librariesValue.stream().map(MutableString::get).collect(Collectors.joining(SEPARATOR));
	}

    public String getVersionStr()
    {
        return super.versionValue.get();
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
	//endregion

	//region abstract document

	@Override
	public void refresh() throws Exception
	{
		super.refresh();
		DialogsHelper.showSuccess("Configuration was refreshed successful!");
	}

	@Override
	public void display() throws Exception
	{
		super.display();

		initController();

		displayEvaluator();
		displayFormat();
		displayMatrix();
		displayLibrary();
		displayVars();
		displayReport();
		displayGlobalHandler();
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
	}

	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);

		//		this.getServiceEntries().forEach(entry -> this.startedServices.put(entry.toString(), ConnectionStatus.NotStarted));
	}

	@Override
	public void save(String fileName) throws Exception
	{
		super.save(fileName);
		this.controller.successfulSave();
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
		storeSettings(settings);
		super.close(settings);

		if (this.model != null)
		{
			this.model.setConfiguration(null);
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
	public void commitProject() throws Exception
	{
	}

	public void updateProject() throws Exception
	{
	}

	public void newDocument(DocumentKind kind) throws Exception
	{
		switch (kind)
		{
			case MATRIX:
				this.model.newMatrix();
				break;
			case GUI_DICTIONARY:
				this.model.newDictionary();
				break;
			case SYSTEM_VARS:
				this.model.newSystemVars();
				break;
			case PLAIN_TEXT:
				this.model.newPlainText();
				break;
			case CSV:
				this.model.newCsv();
				break;
		}
	}

	public void newLibrary() throws Exception
	{
		this.model.newLibrary();
	}

	public void changeSortType(CompareEnum compareEnum) throws Exception
	{
		this.currentCompareEnum = compareEnum;
		this.display();
	}

	public Comparator<File> getFileComparator()
	{
		return this.currentCompareEnum.getComparator();
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
	public void excludeMatrixDirectory(String file)
	{
		excludeFile(file, super.matricesValue, this::displayMatrix);
	}

	public void renameMatrix(File file) throws Exception
	{
		String newName = ConfigurationTreeView.showInputDialog("Enter new name:", file.getName()).orElse(null);
		if (newName != null)
		{
			String newFilePath = getNewFilePath(file, newName);
			File tmp = new File(newFilePath);
			if (tmp.exists())
			{
				if (!DialogsHelper.showQuestionDialog(
						String.format("A file with path %s already exists. The file will rewrited", tmp.getPath())
						, "Do you want to continue?"
				))
				{
					return;
				}

			}
			CustomTab customTab = Common.checkDocument(file.getAbsolutePath());
			if (customTab != null)
			{
				Document document = customTab.getDocument();
				boolean needContinue = true;
				if (document.isChanged())
				{
					needContinue = DialogsHelper.showQuestionDialog(
							"A File was changed. Before renaming the file will save."
							,"Would you like to continue?"
					);
				}
				if (needContinue)
				{
					document.save(newFilePath);
					removeFileFromFileSystem(file, this::displayMatrix);
				}
			}
			else
			{
				this.renameFile(file, newName, this::displayMatrix);
			}
		}
	}

	public void openMatrix(File file) throws Exception
	{
		this.model.loadMatrix(path(file));
	}

	public void addNewMatrix(File parentFolder, String fileName) throws Exception
	{
        Context context = factory.createContext();
        MatrixRunner runner = context.createRunner(fileName, null, new Date(), null);
		File file = createNewFile(parentFolder, fileName, Configuration.matrixExt);
		Matrix matrix = getFactory().createMatrix(path(file), runner);
		matrix.create();
		matrix.display();
		matrix.save(path(file));
		displayMatrix();
	}

	public void removeMatrix(File matrixFile) throws Exception
	{
		removeFileFromFileSystem(matrixFile, this::displayMatrix);
	}

	public void updateMatrices()
	{
		super.refreshMatrices();
		this.displayMatrix();
	}

	public void addToToolbar(String fullPath) throws Exception
	{
		this.model.addToToolbar(fullPath);
	}

	//endregion

	//region library
	public void excludeLibraryDirectory(String file)
	{
		excludeFile(file, super.librariesValue, this::displayLibrary);
	}

	public void openLibrary(String path) throws Exception
	{
		this.openLibrary(new File(path));
	}

	public void openLibrary(File file) throws Exception
	{
		this.model.loadMatrix(path(file));
	}

	public void addNewLibrary(File parentFolder, String fileName) throws Exception
	{
		this.model.newLibrary(checkNameExtention(parentFolder.getAbsolutePath() + File.separator + fileName, Configuration.matrixExt));
		displayLibrary();
	}

	public void removeLibrary(String namespace) throws Exception
	{
		Map<String, Matrix> libs = super.libs;
		Matrix matrix = libs.get(namespace);
		File file = new File(matrix.getName());
		List<String> collect = libs.entrySet()
				.stream()
				.filter(e -> new File(e.getValue().getName()).getAbsolutePath().equals(file.getAbsolutePath()))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
		boolean needRemove = true;
		if (collect.size() > 1)
		{
			needRemove = DialogsHelper.showQuestionDialog("Current library contains many namespaces : "
							+ collect
							.stream()
							.collect(Collectors.joining(",", "[", "]"))
					, "Remove it anyway?");
		}
		if (needRemove)
		{
			File removeFile = new File(libs.get(collect.get(0)).getName());
			collect.forEach(super.libs::remove);
			removeFileFromFileSystem(removeFile, this::updateLibraries);
		}
	}

	public void updateLibraries()
	{
		refreshLibs();
		displayLibrary();
	}
	//endregion

	//region variable
	public void openVariableFile(File file) throws Exception
	{
		this.model.loadSystemVars(path(file));
	}

	public void excludeVarsFile(String file)
	{
		excludeFile(file, super.userVarsValue, this::displayVars);
	}

	public void addUserVarsFile(File file)
	{
		addFile(Common.getRelativePath(path(file)), super.userVarsValue, this::displayVars);
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
		this.model.openReport(file);
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

	public void updateReport()
	{
		super.refreshReport();
		this.displayReport();
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
		IClientFactory factory = getClientPool().loadClientFactory(entry.toString());
		this.showPossibilities(factory.possebilities(), entry.toString());
	}

	public void addAllClientParams(ClientEntry entry) throws Exception
	{
		IClientFactory factory = getClientPool().loadClientFactory("" + entry);
		addAllKnowParameters(entry, entry.getParameters(), factory.wellKnownParameters(ParametersKind.LOAD), this::displayClient);
	}

	public void testClientVersion() throws Exception
	{
		this.supportedClients.clear();
		IClientsPool clientPool = getClientPool();
		this.supportedClients.putAll(getClientEntries()
				.stream()
				.map(ClientEntry::toString)
				.collect(Collectors.toMap(
						id -> id,
						id -> new SupportedEntry(clientPool.isSupported(id))
				))
		);
		this.displayClient();
	}

	public void excludeClientDictionaryFolder(String file) throws Exception
	{
		this.excludeFile(file, super.clientDictionariesValue, this::displayClient);
	}

	public void useAsClientDictionaryFolder(String file) throws Exception
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

	public void updateClientDictionaries()
	{
		super.refreshClientDictionaries();
		this.displayClient();
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
		IServiceFactory factory = getServicesPool().loadServiceFactory("" + entry);
		addAllKnowParameters(entry, entry.getParameters(), factory.wellKnownParameters(ParametersKind.LOAD), this::displayService);
	}

	public void testServiceVersion() throws Exception
	{
		this.supportedServices.clear();
		IServicesPool servicePool = getServicesPool();
		for (ServiceEntry entry : getServiceEntries())
		{
			String id = entry.toString();
			this.supportedServices.put(entry.toString(), new SupportedEntry(servicePool.isSupported(id)));
		}
		this.displayService();
	}

	public void startService(ServiceEntry entry) throws Exception
	{
		try
		{
			final String idEntry = entry.toString();
			if (getServicesPool().getStatus(entry.toString()) == ServiceStatus.StartSuccessful)
			{
				DialogsHelper.showInfo(String.format("Entry with id '%s' already started", idEntry));
				return;
			}
			String parametersName = "StartParameters";
			String title = "Start ";
			String[] strings = getServicesPool().loadServiceFactory(idEntry).wellKnownParameters(ParametersKind.START);
			Settings settings = getFactory().getSettings();
			final Map<String, String> parameters = settings.getMapValues(Settings.SERVICE + idEntry, parametersName, strings);

			AbstractEvaluator evaluator = createEvaluator();
			ButtonType buttonType = DialogsHelper.showParametersDialog(title + idEntry, parameters, evaluator, key -> null);
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
					ServiceConnection serviceConnection = services.loadService(entry.toString());
					services.startService(getFactory().createContext(), serviceConnection, startParameters);
					serviceConnectionMap.put(entry, serviceConnection);
					return null;
				}
			};

			startTask.setOnSucceeded(workerStateEvent -> this.controller.displayService(getServiceEntries(), getStatuses(getServiceEntries())));
			startTask.setOnFailed(workerStateEvent -> this.controller.displayService(getServiceEntries(), getStatuses(getServiceEntries())));
			new Thread(startTask).start();
		}
		catch (Exception e)
		{
			this.controller.displayService(getServiceEntries(), getStatuses(getServiceEntries()));
			throw e;
		}
	}

	public void stopService(ServiceEntry entry) throws Exception
	{
		ServiceConnection serviceConnection = this.serviceConnectionMap.remove(entry);
		if (serviceConnection != null)
		{
			getServicesPool().stopService(serviceConnection);
			this.controller.displayService(getServiceEntries(), getStatuses(getServiceEntries()));
		}
	}

	public void stopService(ServiceConnection connection) throws Exception
	{
		boolean b = this.serviceConnectionMap.entrySet().removeIf(entry -> entry.getValue().equals(connection));
		if (b)
		{
			getServicesPool().stopService(connection);
			this.controller.displayService(getServiceEntries(), getStatuses(getServiceEntries()));
		}
	}

	public void stopAllServices() throws Exception
	{
		this.serviceConnectionMap.clear();
		getServicesPool().stopAllServices();
		this.controller.displayService(getServiceEntries(), getStatuses(getServiceEntries()));
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
		IApplicationFactory factory = getApplicationPool().loadApplicationFactory("" + entry);
		addAllKnowParameters(entry, entry.getParameters(), factory.wellKnownParameters(ParametersKind.LOAD), this::displayApp);
	}

	public void testAppVersion() throws Exception
	{
		this.supportedApps.clear();
		ApplicationPool AppPool = new ApplicationPool(getFactory());
		for (AppEntry entry : getAppEntries())
		{
			String id = entry.toString();
			this.supportedApps.put(entry.toString(), new SupportedEntry(AppPool.isSupported(id)));
		}
		this.displayApp();
	}

	public void openAppsDictionary(AppEntry entry) throws Exception
	{
		this.openAppsDictionary(new File(entry.get(Configuration.appDicPath)), entry.toString());
	}

	public void openAppsDictionary(File file) throws Exception
	{
		this.model.loadDictionary(path(file), null);
	}

	public void openAppsDictionary(File file, String adapter) throws Exception
	{
		this.model.loadDictionary(path(file), adapter);
	}

	public void showAppHelp(AppEntry entry) throws Exception
	{
        IApplicationFactory applicationFactory = this.getApplicationPool().loadApplicationFactory(entry.get(entryName));
        Context context = factory.createContext();
        ReportBuilder report = new ContextHelpFactory().createReportBuilder(null, null, new Date());
        MatrixItem help = DocumentationBuilder.createHelpForPlugin(report, context, entry.get(entryName), applicationFactory);
        DialogsHelper.showHelpDialog(context, entry.get(entryName), report, help);
		
	}

	public void excludeAppDictionaryFolder(String file) throws Exception
	{
		this.excludeFile(file, super.appDictionariesValue, this::displayApp);
	}

	public void useAsAppDictionaryFolder(String file) throws Exception
	{
		this.addFile(file, super.appDictionariesValue, this::displayApp);
	}

	public void updateAppDictionaries()
	{
		super.refreshAppDictionaries();
		this.displayApp();
	}
	//endregion

	//region file system
	public void useAsMatrix(String file)
	{
		addFile(file, this.matricesValue, this::displayMatrix);
	}

	public void useAsLibrary(String file)
	{
		addFile(file, super.librariesValue, this::displayLibrary);
	}

	public void addAsVars(String file)
	{
		addFile(file, super.userVarsValue, this::displayLibrary);
	}

	public void openCsv(File file) throws Exception
	{
		this.model.loadCsv(file.getAbsolutePath());
	}

	public void openPlainText(File file) throws Exception
	{
		this.model.loadPlainText(path(file));
	}

	//endregion

	public void updateHandlerValue(HandlerKind kind, String newValue) throws Exception
	{
		this.getGlobalHandler().setHandler(kind, newValue);
		this.displayGlobalHandler();
	}

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

	public <T extends Entry> SupportedEntry getSupportedEntry(T entry)
	{
		SupportedEntry supportedEntry = null;
		String id = entry.toString();
		IPool pool;
		if (entry.getClass().getSimpleName().equals(ClientEntry.class.getSimpleName()))
		{
			pool = getClientPool();
			supportedEntry = new SupportedEntry(pool.isSupported(id));
		}
		else if (entry.getClass().getSimpleName().equals(AppEntry.class.getSimpleName()))
		{
			pool = getApplicationPool();
			supportedEntry = new SupportedEntry(pool.isSupported(id));
		}
		else if (entry.getClass().getSimpleName().equals(ServiceEntry.class.getSimpleName()))
		{
			pool = getServicesPool();
			supportedEntry = new SupportedEntry(pool.isSupported(id));
		}
		return supportedEntry;
	}

	//region private methods
	private void restoreSettings()
	{
		Settings settings = getFactory().getSettings();
		Settings.SettingsValue valueOrDefault = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.CONFIG_DIALOG, Settings.CONFIG_COMPARATOR, CompareEnum.ALPHABET_0_1.name());
		String value = valueOrDefault.getValue();
		this.currentCompareEnum = CompareEnum.valueOf(value);
	}

	private void storeSettings(Settings settings)
	{
		settings.setValue(Settings.GLOBAL_NS, Settings.CONFIG_DIALOG, Settings.CONFIG_COMPARATOR, this.currentCompareEnum.name());
	}

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

		List<Parameter> newParameters = new ArrayList<>(parameters);

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
				entry.getParameters().addAll(newParameters);
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

	private void excludeFile(String filePath, List<MutableString> list, DisplayFunction displayFunction)
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
			this.displayFileSystem();
		};
		Command redo = () ->
		{
			List<MutableString> collect = new ArrayList<>(list);
			collect.add(new MutableString(filePath));
			list.clear();
			list.addAll(collect);
			displayFunction.display();
			this.displayFileSystem();
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

	private void renameFile(File file, String newName, DisplayFunction displayFunction) throws Exception
	{
		String newPath = getNewFilePath(file, newName);
		Files.move(file.toPath(), Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);
		displayFunction.display();
	}

	private String getNewFilePath(File file, String newName)
	{
		String ext = file.getName().substring(file.getName().lastIndexOf("."));
		if (!newName.endsWith(ext))
		{
			newName += ext;
		}
		return file.getAbsolutePath().replaceAll(file.getName(), newName);
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

	private Map<String, ServiceStatus> getStatuses(List<ServiceEntry> entries)
	{
		return entries.stream().collect(Collectors.toMap(Entry::toString, e -> getServicesPool().getStatus(e.toString())));
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
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setHeaderText("Possibilities for " + entryName);
		dialog.setTitle("Possibilities");
		dialog.getDialogPane().setContent(listView);
		dialog.getDialogPane().setPrefWidth(500);
		dialog.getDialogPane().setPrefHeight(300);
		dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
		dialog.show();
	}

	private static void forceDelete(File directory)
	{
		if (directory.isDirectory())
		{
			cleanDirectory(directory);
		}
		rmFromGit(directory);
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
					rmFromGit(file);
					file.delete();
				}
			}
		}
	}

	private static void rmFromGit(File file)
	{
		try
		{
			if (Main.IS_PROJECT_UNDER_GIT)
			{
				GitUtil.rmFile(file);
			}
		}
		catch (Exception e)
		{

		}
	}

	private static String checkNameExtention(String fileName, String ext)
	{
		if (fileName.endsWith(ext))
		{
			return fileName;
		}
		return fileName + ext;
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
		List<String> varsList = toStringList(super.userVarsValue);
		varsList.add(super.varsValue.get());
		this.controller.displayVars(varsList);
	}

	private void displayReport()
	{
		this.controller.displayReport(super.reportsValue.get());
	}

	private void displaySql()
	{
		this.controller.displaySql(getSqlEntries());
	}

	private void displayGlobalHandler()
	{
		this.controller.displayGlobalHandler(getGlobalHandler().getMap());
	}

	private void displayClient()
	{
		this.controller.displayClient(getClientEntries());
	}

	private void displayService()
	{
		this.controller.displayService(getServiceEntries(), getStatuses(getServiceEntries()));
	}

	private void displayApp()
	{
		this.controller.displayApp(getAppEntries());
	}

	private void displayFileSystem()
	{
		List<String> ignoreFiles = new ArrayList<>();
		ignoreFiles.addAll(toStringList(super.appDictionariesValue));
		ignoreFiles.addAll(toStringList(super.clientDictionariesValue));
		ignoreFiles.addAll(toStringList(super.matricesValue));
		ignoreFiles.addAll(toStringList(super.librariesValue));
		ignoreFiles.add(super.varsValue.get());
		ignoreFiles.addAll(toStringList(super.userVarsValue));
		ignoreFiles.add(super.reportsValue.get());

		this.controller.displayFileSystem(ignoreFiles);
	}
	//endregion

	private void initController()
	{
		if (!this.isControllerInit)
		{
			restoreSettings();
			this.isControllerInit = true;
			
			this.controller = Common.loadController(ConfigurationFx.class.getResource("config.fxml"));
			this.controller.init(this, this.pane, this.currentCompareEnum);
		}
	}
}
