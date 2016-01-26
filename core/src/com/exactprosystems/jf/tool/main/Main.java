////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.main;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.*;
import com.exactprosystems.jf.common.Settings.SettingsValue;
import com.exactprosystems.jf.common.evaluator.SystemVars;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.listeners.MatrixListener;
import com.exactprosystems.jf.common.parser.listeners.RunnerListener;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.common.xml.gui.GuiDictionary;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.configuration.ConfigurationFx;
import com.exactprosystems.jf.tool.csv.CsvFx;
import com.exactprosystems.jf.tool.custom.store.StoreVariable;
import com.exactprosystems.jf.tool.dictionary.DictionaryFx;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.helpers.DialogsHelper.OpenSaveMode;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.schedule.RunnerScheduler;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import com.exactprosystems.jf.tool.settings.Theme;
import com.exactprosystems.jf.tool.systemvars.SystemVarsFx;
import com.exactprosystems.jf.tool.text.PlainTextFx;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Main extends Application
{
	private static final Logger logger = Logger.getLogger(Main.class);

	public static final String	MAX_FILES_COUNT		= "maxFilesCount";
	public static final String	TIME_NOTIFICATION	= "timeNotification";
	public static final String	USE_FULL_SCREEN		= "useFullScreen";
	public static final String	THEME				= "theme";
	public static final String	USE_DEFAULT_BROWSER	= "useDefaultBrowser";
	public static final String	USE_SMALL_WINDOW	= "useSmallWindow";
	public static final String 	OPENED 				= "OPENED";
	public static final String 	MAIN_NS 			= "MAIN";

	public static final String	DEFAULT_MAX_FILES_COUNT		= "3";

	private Preloader preloader;
	private MainController controller;
	private RunnerListener runnerListener;

	private Configuration config;
	private Settings settings;
	private List<Document> docs = new ArrayList<Document>();

	public void setConfiguration(Configuration config)
	{
		this.config = config;
		this.runnerListener.setConfiguration(this.config);
		if (this.controller != null)
		{
			this.controller.disableMenu(this.config == null);
		}
	}
	
	//----------------------------------------------------------------------------------------------
	// Application
	//----------------------------------------------------------------------------------------------
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
						loadConfiguration(args.get(0));
						
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

		load.setOnSucceeded(workerStateEvent -> Common.tryCatch(() ->
		{
			controller.display();
			if (preloader != null)
			{
				preloader.hide();
			}
			controller.initShortcuts();
			try
			{
				final List<String> args = getParameters().getRaw();
				if (args.size() == 2)
				{
					try
					{
						loadMatrix(args.get(1));
					}
					catch (Exception e)
					{
						DialogsHelper.showError("Error on load start matrix " + e.getMessage());
						logger.error("Error on load start matrix");
						logger.error(e.getMessage(), e);
					}
				}
				for (SettingsValue item : settings.getValues(MAIN_NS, OPENED))
				{
					DocumentKind kind = DocumentKind.valueOf(item.getValue());
					if (kind != null)
					{
						String filePath = item.getKey();
						switch (kind)
						{
							case MATRIX:
								loadMatrix(filePath);
								break;

							case GUI_DICTIONARY:
								loadDocument(new File(filePath), new DictionaryFx(filePath, config, null), DocumentKind.GUI_DICTIONARY);
								break;

							case SYSTEM_VARS:
								loadDocument(new File(filePath), new SystemVarsFx(filePath, config), DocumentKind.SYSTEM_VARS);
								break;

							case PLAIN_TEXT:
								loadDocument(new File(filePath), new PlainTextFx(filePath, settings, config), DocumentKind.PLAIN_TEXT);
								break;

							case CSV:
								loadDocument(new File(filePath), new CsvFx(filePath, settings, config), DocumentKind.CSV);
								break;
								
							default:
								break;
						}
					}
				}
			}
			catch (Exception e)
			{
				logger.error("Error on restore opened documents");
				logger.error(e.getMessage(), e);
			}
			Common.setNeedSelectedTab(true);
//			this.controller.selectConfig();
		}, "Error on task succeed on"));
		
		Thread thread = new Thread(load);
		thread.setName("Load " + thread.getId());
		thread.start();
	}
	
	//----------------------------------------------------------------------------------------------
	// Event handlers
	//----------------------------------------------------------------------------------------------
	public void loadConfiguration(String filePath) throws Exception
	{
		Optional<File> optional = chooseFile(Configuration.class, filePath, DialogsHelper.OpenSaveMode.OpenFile);
		if (optional.isPresent())
		{
			File file = optional.get();
			if (this.config != null)
			{
				if (this.config.canClose())
				{
					this.config.close();
					setConfiguration(null);
				}
				else
				{
					return;
				}
			}

			ConfigurationFx config = new ConfigurationFx(file.getPath(), this.runnerListener, this.settings, Main.this);

			Document doc = loadDocument(file, config, DocumentKind.CONFIGURATION);
			if (doc instanceof Configuration)
			{
				setConfiguration(config);
			}
		}
	}
	
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

	public void newConfiguration() throws Exception
	{
		if (this.config != null)
		{
			if (this.config.canClose())
			{
				this.config.close();
				setConfiguration(null);
			}
			else
			{
				return;
			}
		}
		ConfigurationFx config = new ConfigurationFx(newName(Configuration.class), this.runnerListener, this.settings, Main.this);

		createDocument(config);
		setConfiguration(config);
	}
	
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
		String text = copyright.getValue().replaceAll("\\\\n", "\n");
		doc.addCopyright(text);
		docs.add(doc);
		doc.display();
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

	public void openReport() throws Exception
	{
		File file = DialogsHelper.showOpenSaveDialog("Choose report", "HTML files (*.html)", "*.html", OpenSaveMode.OpenFile);
		Optional.ofNullable(file).ifPresent(f -> DialogsHelper.displayReport(f, null, this.config));
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

	public void clearFileLastOpenMatrix() throws Exception
	{
		this.settings.removeAll(MAIN_NS, DocumentKind.MATRIX.toString());
		this.settings.saveIfNeeded();

		this.controller.clearLastMatrixMenu();
	}

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
		if (document.getName() == null || !(new File(document.getName()).exists()) || !document.hasName())
		{
			documentSaveAs(document);
		}
		else
		{
			document.save(document.getName());
			document.saved();
		}
		
		DialogsHelper.showInfo(document.getName() + " is saved successfully.");
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

	public boolean closeApplication()
	{
		try
		{
			if (this.config != null)
			{
				if(this.config.canClose())
				{
					this.config.close();
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
	//----------------------------------------------------------------------------------------------

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
		return this.controller.checkFile(file) ? Optional.empty() :Optional.ofNullable(file);
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
	
}
