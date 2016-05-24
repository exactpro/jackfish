////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.main;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.MainRunner;
import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.Settings.SettingsValue;
import com.exactprosystems.jf.common.evaluator.SystemVars;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.listeners.MatrixListener;
import com.exactprosystems.jf.common.parser.listeners.RunnerListener;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.common.xml.gui.GuiDictionary;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.DisplayableTask;
import com.exactprosystems.jf.tool.csv.CsvFx;
import com.exactprosystems.jf.tool.custom.store.StoreVariable;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.git.CredentialBean;
import com.exactprosystems.jf.tool.git.CredentialDialog;
import com.exactprosystems.jf.tool.git.GitUtil;
import com.exactprosystems.jf.tool.git.clone.GitClone;
import com.exactprosystems.jf.tool.git.commit.GitCommit;
import com.exactprosystems.jf.tool.git.pull.GitPull;
import com.exactprosystems.jf.tool.git.status.GitStatus;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.helpers.DialogsHelper.OpenSaveMode;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.schedule.RunnerScheduler;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.newconfig.wizard.WizardConfiguration;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import com.exactprosystems.jf.tool.settings.Theme;
import com.exactprosystems.jf.tool.systemvars.SystemVarsFx;
import com.exactprosystems.jf.tool.text.PlainTextFx;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application
{
	private static ExecutorService executorService = Executors.newSingleThreadExecutor();

	private static final Logger logger = Logger.getLogger(Main.class);

	public static final String	MAX_FILES_COUNT		= "maxFilesCount";
	public static final String	TIME_NOTIFICATION	= "timeNotification";
	public static final String	USE_FULL_SCREEN		= "useFullScreen";
	public static final String	THEME				= "theme";
	public static final String	USE_SMALL_WINDOW	= "useSmallWindow";
	public static final String 	OPENED 				= "OPENED";
	public static final String 	MAIN_NS 			= "MAIN";

	public static final String	DEFAULT_MAX_FILES_COUNT		= "3";

	private static String configName = null;

	private Preloader preloader;
	private MainController controller;
	private RunnerListener runnerListener;

	private Configuration config;
	private Settings settings;
	private List<Document> docs = new ArrayList<Document>();

	//TODO not use now
	private String username;
	private String password;


	public static String getConfigName()
	{
		String temp = configName;
		configName = null;
		return temp;
	}

	//region public methods
	public void setConfiguration(Configuration config)
	{
		this.config = config;
		this.runnerListener.setConfiguration(this.config);
		if (this.controller != null)
		{
			this.controller.disableMenu(this.config == null);
		}
	}

	public <T> void displayableTask(DisplayableTask<T> task, String title, String error)
	{
		this.controller.startTask(title);
		task.setExecutor(executorService);
		task.setOnFailed(event -> {
			Throwable exception = event.getSource().getException();
			logger.error(exception.getMessage(), exception);
			DialogsHelper.showError(exception.getMessage() + "\n" + error);
			task.getOnFail().ifPresent(consumer -> consumer.accept(event.getSource().getException()));
			this.controller.endTask();
		});
		task.setOnSucceeded(event -> {
			task.getOnSuccess().ifPresent(t -> t.accept((T) event.getSource().getValue()));
			this.controller.endTask();
		});
		task.start();
	}
	//endregion

	//region Application
	@Override
	public void start(final Stage stage) throws Exception
	{
		try
		{
			this.settings = Settings.load(Settings.SettingsPath);
			DialogsHelper.setTimeNotification(Integer.parseInt(this.settings.getValueOrDefault(Settings.GLOBAL_NS, SettingsPanel.SETTINGS, Main.TIME_NOTIFICATION, "5").getValue()));
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			DialogsHelper.showError("Settings are invalid. Using empty settings.");
			this.settings = new Settings();
		}
		Common.node = stage;
		Settings.SettingsValue theme = this.settings.getValueOrDefault(Settings.GLOBAL_NS, SettingsPanel.SETTINGS, Main.THEME, Theme.WHITE.name());
		Common.setTheme(Theme.valueOf(theme.getValue().toUpperCase()));
		this.preloader = new Preloader();
		this.preloader.show();
		this.runnerListener = new RunnerScheduler();
		Task<Object> load = new Task<Object>()
		{
			@Override
			protected Object call() throws Exception
			{
				try
				{
					controller = Common.loadController(Main.class.getResource("tool.fxml"));
					controller.init(Main.this, settings, stage, ((RunnerScheduler) runnerListener));
					controller.disableMenu(true);
					
					final List<String> args = getParameters().getRaw();

					if (args.size() > 0)
					{
						Main.this.username = args.size() > 1 ? args.get(1) : null;
						Main.this.password = args.size() > 2 ? args.get(2) : null;

						openProject(args.get(0), controller.projectPane); // TODO

						controller.clearLastMatrixMenu();

						Collection<SettingsValue> list = settings.getValues(MAIN_NS, DocumentKind.MATRIX.toString());
						controller.updateFileLastMatrix(list);
					}
					return null;
				}
				catch (Exception e)
				{
					logger.error("Error on load tool");
					logger.error(e.getMessage(), e);
				}
				return null;
			}
		};

		load.setOnSucceeded(workerStateEvent -> Common.tryCatch(() -> {
			controller.display();
			if (preloader != null)
			{
				preloader.hide();
			}
			controller.initShortcuts();
			try
			{
				for (SettingsValue item : settings.getValues(MAIN_NS, OPENED))
				{
					DocumentKind kind = DocumentKind.valueOf(item.getValue());
					if (kind != null)
					{
						String filePath = item.getKey();
						File file = new File(filePath);
						try
						{
							switch (kind)
							{
								case MATRIX:
									loadDocument(file, new MatrixFx(filePath, config, new MatrixListener()), kind);
									break;

								case GUI_DICTIONARY:
									loadDocument(file, new DictionaryFx(filePath, config, null), DocumentKind.GUI_DICTIONARY);
									break;

								case SYSTEM_VARS:
									loadDocument(file, new SystemVarsFx(filePath, config), DocumentKind.SYSTEM_VARS);
									break;

								case PLAIN_TEXT:
									loadDocument(file, new PlainTextFx(filePath, settings, config), DocumentKind.PLAIN_TEXT);
									break;

								case CSV:
									loadDocument(file, new CsvFx(filePath, settings, config), DocumentKind.CSV);
									break;

								default:
									break;
							}
						}
						catch (FileNotFoundException e)
						{
							settings.remove(MAIN_NS, OPENED, file.getAbsolutePath());
							settings.saveIfNeeded();
						}
					}
				}
			}
			catch (Exception e)
			{
				logger.error("Error on restore opened documents");
				logger.error(e.getMessage(), e);
			}
			//			this.controller.selectConfig();
		}, "Error on task succeed on"));

		Thread thread = new Thread(load);
		thread.setName("Load main gui" + thread.getId());
		thread.start();
	}
	//endregion

	//region Configuration
	public void openProject(String filePath, BorderPane pane) throws Exception
	{
		Optional<File> optional = chooseFile(Configuration.class, filePath, DialogsHelper.OpenSaveMode.OpenFile);
		if (optional.isPresent())
		{
			File file = optional.get();
			if (this.config != null)
			{
				if (this.config.canClose())
				{
					this.config.close(this.config.getSettings());
					setConfiguration(null);
				}
				else
				{
					return;
				}
			}

			Path path = MainRunner.needToChangeDirectory(file.getPath());
			if (path != null)
			{
				configName = file.getAbsolutePath();
				this.controller.close();
			}

			ConfigurationFx config = new ConfigurationFx(file.getPath(), this.runnerListener, this.settings, Main.this, pane);

			Document doc = loadDocument(file, config, DocumentKind.CONFIGURATION);
			if (doc instanceof Configuration)
			{
				setConfiguration(config);
			}
		}
	}

	public void createNewProject(BorderPane pane) throws Exception
	{
		WizardConfiguration wizard = new WizardConfiguration(this);
		String fullPath = wizard.display();
		if (fullPath != null)
		{
			if (this.config != null)
			{
				if (this.config.canClose())
				{
					this.config.close(this.config.getSettings());
					setConfiguration(null);
				}
				else
				{
					return;
				}
			}
			File newFolder = new File(fullPath);
			String configName = newFolder.getName();
			String configurePath = fullPath + File.separator + configName + ".xml";

			Configuration newConfig = Configuration.createNewConfiguration(configName, this.settings);
			newConfig.save(configurePath);
			openProject(configurePath, pane);
		}
	}

	public void projectFromGit(BorderPane projectPane) throws Exception
	{
		GitClone cloneWindow = new GitClone(this);
		String fullPath = cloneWindow.display();
		if (fullPath != null)
		{
			openProject(fullPath, projectPane);
		}
	}
	//endregion

	//region Git
	public CredentialBean getCredential()
	{
		checkCredential();
		SettingsValue idRsa = this.settings.getValueOrDefault("GLOBAL", SettingsPanel.GIT, SettingsPanel.GIT_SSH_IDENTITY, "");
		SettingsValue knownHosts = this.settings.getValueOrDefault("GLOBAL", SettingsPanel.GIT, SettingsPanel.GIT_KNOWN_HOST, "");
		return new CredentialBean(this.username, this.password, idRsa.getValue(), knownHosts.getValue());
	}

	public void saveCredential(String username, String password)
	{
		this.username = username;
		this.password = password;
	}

	public void changeCredential()
	{
		new CredentialDialog(this::storeCredential).display(this.username, this.password);
	}

	public void gitStatus() throws Exception
	{
		new GitStatus(this).display(GitUtil.gitStatus(getCredential()));
	}

	public void gitPull() throws Exception
	{
		try (Git git = git())
		{
			PullResult pull = git.pull()
					.setCredentialsProvider(getCredentialsProvider())
					.call();


			new GitPull(this)
					.display("Some title", new ArrayList<>());
		}
	}

	public void gitCommit() throws Exception
	{
		new GitCommit(this).display();
	}

	public void gitReset() throws Exception
	{

	}

	public void revertFiles(List<File> files) throws Exception
	{
		try (Git git = git())
		{

		}
	}
	//endregion

	//region Load documents
	public void loadDictionary(String filePath, String entryName) throws Exception
	{
		checkConfig();
		Optional<File> optional = chooseFile(GuiDictionary.class, filePath, DialogsHelper.OpenSaveMode.OpenFile);
		if (optional.isPresent())
		{
			loadDocument(optional.get(), new DictionaryFx(optional.get().getPath(), this.config, entryName), DocumentKind.GUI_DICTIONARY);
		}
	}

	public void loadMatrix(String filePath) throws Exception
	{
		checkConfig();
		Optional<File> optional = chooseFile(Matrix.class, filePath, DialogsHelper.OpenSaveMode.OpenFile);
		if (optional.isPresent())
		{
			loadDocument(optional.get(), new MatrixFx(optional.get().getPath(), this.config, new MatrixListener()), DocumentKind.MATRIX);
		}
	}

	public void loadSystemVars(String filePath) throws Exception
	{
		checkConfig();
		Optional<File> optional = chooseFile(SystemVars.class, filePath, DialogsHelper.OpenSaveMode.OpenFile);
		if (optional.isPresent())
		{
			loadDocument(optional.get(), new SystemVarsFx(optional.get().getPath(), this.config), DocumentKind.SYSTEM_VARS);
		}
	}

	public void loadPlainText(String filePath) throws Exception
	{
		checkConfig();
		Optional<File> optional = chooseFile(PlainTextFx.class, filePath, DialogsHelper.OpenSaveMode.OpenFile);
		if (optional.isPresent())
		{
			loadDocument(optional.get(), new PlainTextFx(optional.get().getPath(), this.settings, this.config), DocumentKind.PLAIN_TEXT);
		}
	}

	public void loadCsv(String filePath) throws Exception
	{
		checkConfig();
		Optional<File> optional = chooseFile(CsvFx.class, filePath, DialogsHelper.OpenSaveMode.OpenFile);
		if (optional.isPresent())
		{
			loadDocument(optional.get(), new CsvFx(optional.get().getPath(), this.settings, this.config), DocumentKind.CSV);
		}
	}
	//endregion

	//region Create documents
	public void newDictionary() throws Exception
	{
		checkConfig();
		createDocument(new DictionaryFx(newName(GuiDictionary.class), this.config));
	}

	public void newMatrix() throws Exception
	{
		checkConfig();
		MatrixFx doc = new MatrixFx(newName(Matrix.class), this.config, new MatrixListener());
		doc.create();
		Settings.SettingsValue copyright = settings.getValueOrDefault(Settings.GLOBAL_NS, "Main", "copyright", "");
		String text = copyright.getValue().replaceAll("\\\\n", System.lineSeparator());
		doc.addCopyright(text);
		docs.add(doc);
		doc.display();
	}

	public void newLibrary(String fullPath) throws Exception
	{
		checkConfig();
		MatrixFx doc = new MatrixFx(fullPath, this.config, new MatrixListener());
		doc.create();
		doc.createLibrary();
		Settings.SettingsValue copyright = settings.getValueOrDefault(Settings.GLOBAL_NS, "Main", "copyright", "");
		String text = copyright.getValue().replaceAll("\\\\n", System.lineSeparator());
		doc.addCopyright(text);
		docs.add(doc);
		if (new File(fullPath).exists())
		{
			doc.save(fullPath);
		}
		doc.display();
	}

	public void newLibrary() throws Exception
	{
		newLibrary(newName(Matrix.class));
	}

	public void newSystemVars() throws Exception
	{
		checkConfig();
		createDocument(new SystemVarsFx(newName(SystemVars.class), this.config));
	}

	public void newPlainText() throws Exception
	{
		checkConfig();
		createDocument(new PlainTextFx(newName(PlainTextFx.class), this.settings, this.config));
	}

	public void newCsv() throws Exception
	{
		checkConfig();
		createDocument(new CsvFx(newName(CsvFx.class), this.settings, this.config));
	}
	//endregion

	//region Documents
	public void documentSaveAs(Document document) throws Exception
	{
		if (document == null)
		{
			DialogsHelper.showInfo("Nothing to save");
			return;
		}
		File file = DialogsHelper.showSaveAsDialog(document);
		if (file != null)
		{
			String lastName = document.getName();
			String newName = file.getPath();

			document.save(file.getPath());
			document.saved();
			DialogsHelper.showInfo(document.getName() + " is saved successfully.");
			if (document instanceof Matrix)
			{
				this.settings.remove(MAIN_NS, DocumentKind.MATRIX.name(), new File(lastName).getName());
				this.settings.setValue(MAIN_NS, DocumentKind.MATRIX.name(), new File(document.getName()).getName(), newName);
				this.settings.saveIfNeeded();
				this.controller.updateFileLastMatrix(this.settings.getValues(MAIN_NS, DocumentKind.MATRIX.name()));
			}
		}
	}

	public void documentSave(Document document) throws Exception
	{
		if (document == null)
		{
			return;
		}

		if (document.getName() == null || !(new File(document.getName()).exists()) || !document.hasName())
		{
			documentSaveAs(document);
		}
		else
		{
			document.save(document.getName());
			document.saved();
			DialogsHelper.showInfo(document.getName() + " is saved successfully.");
		}
	}

	public void documentsSaveAll() throws Exception
	{
		for (Document document : this.docs)
		{
			documentSave(document);
		}
		DialogsHelper.showSuccess("All files successful saved");
	}

	public void undo(Document document) throws Exception
	{
		document.undo();
	}

	public void redo(Document document) throws Exception
	{
		document.redo();
	}

	public void changeDocument(Document document)
	{
		StringBuilder sb = new StringBuilder(Configuration.projectName);
		sb.append(" ").append(VersionInfo.getVersion());
		if (document != null)
		{
			File file = new File(document.getName());
			String absolutePath = file.getAbsolutePath();
			sb.append(" [ ").append(absolutePath).append(" ]");
		}
		this.controller.displayTitle(sb.toString());
	}
	//endregion

	//region Reports
	public void openReport() throws Exception
	{
		File file = DialogsHelper.showOpenSaveDialog("Choose report", "HTML files (*.html)", "*.html", OpenSaveMode.OpenFile);
		openReport(file);
	}

	public void openReport(File file) throws Exception
	{
		Optional.ofNullable(file).ifPresent(f -> DialogsHelper.displayReport(f, null, this.config));
	}
	//endregion

	//region Matrix
	public void runMatrixFromFile() throws Exception
	{
		Optional<File> optional = chooseFile(Matrix.class, null, DialogsHelper.OpenSaveMode.OpenFile);
		if (optional.isPresent())
		{
			try (Context context = config.createContext(new MatrixListener(), System.out);
				 MatrixRunner runner = new MatrixRunner(context, optional.get(), null, null)
			)
			{
				runner.start();
			}
		}
	}

	public void stopMatrix(Document document) throws Exception
	{
		if (document instanceof MatrixFx)
		{
			((MatrixFx) document).stopMatrix();
		}
	}

	public void startMatrix(Document document) throws Exception
	{
		if (document instanceof MatrixFx)
		{
			((MatrixFx) document).startMatrix();
		}
	}
	//endregion

	public void clearFileLastOpenMatrix() throws Exception
	{
		this.settings.removeAll(MAIN_NS, DocumentKind.MATRIX.toString());
		this.settings.saveIfNeeded();

		this.controller.clearLastMatrixMenu();
	}

	public boolean closeApplication()
	{
		try
		{
			if (this.config != null)
			{
				if (this.config.canClose())
				{
					this.config.close(this.config.getSettings());
					setConfiguration(null);
					this.controller.close();

					return true;
				}
				return false;
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return true;
	}

	public void showCalculator() throws Exception
	{
		checkConfig();
		this.controller.showCalculator(this.config.createEvaluator());
	}

	public void store() throws Exception
	{
		new StoreVariable(this.config).show();
	}

	//region Files
	public void createFolder(File parentFolder, String folderName)
	{
		new File(parentFolder.getAbsolutePath() + File.separator + folderName).mkdir();
	}

	public void createFile(File parentFolder, String fileName) throws Exception
	{
		new File(parentFolder.getAbsolutePath() + File.separator + fileName).createNewFile();
	}

	public void copyVarsFile(File newFolder) throws Exception
	{
		try (InputStream stream = new FileInputStream(this.config.getVars().get()))
		{
			Files.copy(stream, Paths.get(newFolder.getAbsolutePath() + File.separator + "vars.ini"));
		}
	}
	//endregion

	//region private methods
	private void checkConfig() throws Exception
	{
		if (this.config == null)
		{
			throw new Exception("Open or create a configuration at first.");
		}
	}

	private String newName(Class<? extends Document> clazz) throws Exception
	{
		DocumentInfo annotation = clazz.getAnnotation(DocumentInfo.class);
		if (annotation == null)
		{
			throw new Exception("Unknown type of document: " + clazz);
		}

		return annotation.newName();
	}

	private Optional<File> chooseFile(Class<? extends Document> clazz, String filePath, OpenSaveMode mode) throws Exception
	{
		File file;
		if (Str.IsNullOrEmpty(filePath))
		{
			DocumentInfo annotation = clazz.getAnnotation(DocumentInfo.class);
			if (annotation == null)
			{
				throw new Exception("Unknown type of document: " + clazz);
			}
	
			String title		= String.format("Choose %s file", annotation.description());
			String filter		= String.format("%s files (*.%s)", annotation.extentioin(), annotation.extentioin());
			String extension	= String.format("*.%s", annotation.extentioin());
	
			file = DialogsHelper.showOpenSaveDialog(title, filter, extension, mode);
		}
		else
		{
			file = new File(filePath);
		}
		return this.controller.checkFile(file) ? Optional.empty() : Optional.ofNullable(file);
	}

	private Document loadDocument(File file, Document doc, DocumentKind kind) throws Exception
	{
		if (doc == null)
		{
			return null;
		}
		if (!file.exists())
		{
			DialogsHelper.showError(String.format("File with name %s not found", file.getAbsoluteFile()));
			throw new FileNotFoundException();
		}
		try
		{
			try (Reader reader = new FileReader(file))
			{
				doc.load(reader);
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				DialogsHelper.showError(e.getMessage());

				doc = new PlainTextFx(doc.getName(), this.settings, this.config);
				try (Reader reader = new FileReader(file))
				{
					doc.load(reader);
				}
			}

			docs.add(doc);
			doc.display();
			doc.saved();
			SettingsValue maxSettings = this.settings.getValueOrDefault(Settings.GLOBAL_NS, SettingsPanel.SETTINGS, MAX_FILES_COUNT, DEFAULT_MAX_FILES_COUNT);
			int max = Integer.parseInt(maxSettings.getValue());
			this.settings.setValue(MAIN_NS, kind.toString(), new File(doc.getName()).getName(), max, doc.getName());
			this.settings.saveIfNeeded();
			if (kind == DocumentKind.MATRIX)
			{
				this.controller.updateFileLastMatrix(this.settings.getValues(MAIN_NS, kind.toString()));
			}
			return doc;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			DialogsHelper.showError("Error on load " + doc.getClass().getSimpleName() + "'" + file + "'");
		}

		return null;
	}

	public void removeMatrixFromSettings(String key) throws Exception
	{
		this.settings.remove(MAIN_NS, DocumentKind.MATRIX.name(), key);
		this.settings.saveIfNeeded();
		this.controller.updateFileLastMatrix(this.settings.getValues(MAIN_NS, DocumentKind.MATRIX.name()));
	}

	private void createDocument(Document doc) throws Exception
	{
		if (doc == null)
		{
			return;
		}
		doc.create();
		docs.add(doc);
		doc.display();
	}

	//region private git methods
	private void storeCredential(String username, String password)
	{
		this.username = username;
		this.password = password;
	}

	private void checkCredential()
	{
		if (Str.IsNullOrEmpty(this.username) || Str.IsNullOrEmpty(this.password))
		{
			new CredentialDialog(this::storeCredential).display(this.username, this.password);
		}
	}

	private Git git() throws Exception
	{
		setSSH();
		Repository localRepo = new FileRepositoryBuilder().findGitDir().build();
		return new Git(localRepo);
	}

	private void setSSH()
	{
		CustomJschConfigSessionFactory jschConfigSessionFactory = new CustomJschConfigSessionFactory(this.settings);
		if (jschConfigSessionFactory.isValid())
		{
			SshSessionFactory.setInstance(jschConfigSessionFactory);
		}
	}

	private CredentialsProvider getCredentialsProvider()
	{
		checkCredential();
		return new CredentialsProvider()
		{
			@Override
			public boolean isInteractive()
			{
				return true;
			}

			@Override
			public boolean supports(CredentialItem... credentialItems)
			{
				return true;
			}

			@Override
			public boolean get(URIish urIish, CredentialItem... credentialItems) throws UnsupportedCredentialItem
			{
				for (CredentialItem item : credentialItems)
				{
					if (item instanceof CredentialItem.StringType)
					{
						((CredentialItem.StringType) item).setValue(password);
						continue;
					}
				}
				return true;
			}
		};
	}

	public static class CustomJschConfigSessionFactory extends JschConfigSessionFactory
	{
		private final Settings settings;
		private boolean isValid = true;
		private String pathToIdRsa;
		private String pathToKnownHosts;

		public CustomJschConfigSessionFactory(Settings settings)
		{
			this.settings = settings;
			SettingsValue idRsa = this.settings.getValue("GLOBAL", SettingsPanel.GIT, SettingsPanel.GIT_SSH_IDENTITY);
			SettingsValue knownHosts = this.settings.getValue("GLOBAL", SettingsPanel.GIT, SettingsPanel.GIT_KNOWN_HOST);
			this.isValid = idRsa != null && knownHosts != null;
			if (this.isValid)
			{
				this.pathToIdRsa = idRsa.getValue();
				this.pathToKnownHosts = knownHosts.getValue();
			}
		}

		public boolean isValid()
		{
			return isValid;
		}

		@Override
		protected void configure(OpenSshConfig.Host host, Session session)
		{
			session.setConfig("StrictHostKeyChecking", "yes");
		}

		@Override
		protected JSch getJSch(final OpenSshConfig.Host hc, FS fs) throws JSchException
		{
			JSch jsch = super.getJSch(hc, fs);
			jsch.removeAllIdentity();
			jsch.addIdentity(this.pathToIdRsa);
			jsch.setKnownHosts(this.pathToKnownHosts);
			return jsch;
		}
	}
	//endregion

	//endregion
}
