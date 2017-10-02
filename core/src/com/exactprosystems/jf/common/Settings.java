////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common;

import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.matrix.parser.ScreenshotKind;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.MutableArrayList;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.newconfig.CompareEnum;
import com.exactprosystems.jf.tool.settings.Theme;
import com.exactprosystems.jf.api.common.i18n.Locales;
import com.exactprosystems.jf.tool.wizard.WizardSettings;
import javafx.scene.input.KeyCombination;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XmlRootElement(name = "settings")
@XmlAccessorType(XmlAccessType.NONE)
public class Settings
{
	public final static String SETTINGS_PATH	= ".settings.xml";

	public static final String	FONT			= "Font";
	public static final String	SETTINGS 		= "Main";
	public static final String	LOGS_NAME		= "Logs";
	public static final String	SHORTCUTS_NAME	= "Shortcuts";
	public static final String	MATRIX_COLORS	= "MatrixColors";
	public static final String	GIT				= "Git";
	public static final String	MATRIX_NAME		= "Matrix";
	public static final String	WIZARD_NAME		= "Wizard";

	//region Shortcuts
	//other shortcuts
	public static final String SHOW_ALL_TABS	= "ShowAllTabs";
	public static final String SEARCH			= "Search";

	//document shortcuts
	public static final String SAVE_DOCUMENT	= "SaveDocument";
	public static final String SAVE_DOCUMENT_AS	= "SaveDocumentAs";
	public static final String UNDO				= "Undo";
	public static final String REDO				= "Redo";

	//matrix navigation shortcuts
	public static final String ADD_ITEMS		= "Add";
	public static final String ALL_PARAMETERS	= "AllParameters";
	public static final String BREAK_POINT		= "BreakPoint";
	public static final String ADD_PARAMETER	= "AddParameter";
	public static final String HELP				= "Help";
	public static final String GO_TO_LINE		= "GoToLine";
	public static final String SHOW_ALL			= "ShowAll";
	public static final String DELETE_ITEM		= "DeleteItem";
	public static final String COPY_ITEMS		= "CopyItems";
	public static final String CUT_ITEMS		= "CutItems";
	public static final String PASTE_ITEMS		= "PasteItems";

	//matrix actions shortcuts
	public static final String START_MATRIX		= "StartMatrix";
	public static final String STOP_MATRIX		= "StopMatrix";
	public static final String PAUSE_MATRIX		= "PauseMatrix";
	public static final String STEP_MATRIX		= "StepMatrix";
	public static final String SHOW_RESULT		= "ShowResult";
	public static final String SHOW_WATCH		= "ShowWatch";
	public static final String TRACING			= "Tracing";
	public static final String FIND_ON_MATRIX	= "FindOnMatrix";
	//endregion

	//region Main
	public static final String	MAX_LAST_COUNT = "maxFilesCount";
	public static final String	TIME_NOTIFICATION = "timeNotification";
	public static final String	THEME = "theme";
	public static final String	USE_EXTERNAL_REPORT_VIEWER = "useExternalReportViewer";
	public static final String	USE_FULL_SCREEN	= "useFullScreen";
	public static final String	USE_FULLSCREEN_XPATH = "useFullScreenXpath";
	public static final String	COPYRIGHT = "copyright";
	public static final String	LANGUAGE = "language";
	//endregion

	//region Logs
	public static final String	ALL = "ALL";
	public static final String	DEBUG = "DEBUG";
	public static final String	ERROR = "ERROR";
	public static final String	FATAL = "FATAL";
	public static final String	INFO = "INFO";
	public static final String	TRACE = "TRACE";
	public static final String	WARN = "WARN";
	//endregion

	//region Git
	//git
	public static final String GIT_SSH_IDENTITY	= "gitSshIdentity";
	public static final String GIT_KNOWN_HOST	= "gitKnownHost";
	//endregion

	//region Matrix
	public static final String MATRIX_DEFAULT_SCREENSHOT = "matrixDefaultScreenshot";
	public static final String	MATRIX_POPUPS				= "matrixPopups";
	public static final String MATRIX_FOLD_ITEMS = "foldNewItems";
	//endregion

	public static final String	THRESHOLD			= "threshold";
	public static final String	MAX					= "_MAX";
	public static final String	MIN					= "_MIN";

	public static final String 	OPENED 				= "OPENED";
	public static final String 	MAIN_NS 			= "MAIN";
	public static final String	MATRIX_TOOLBAR		= "MATRIX_TOOLBAR";

	//region Search
	public static final String TEXT = "searchText";
	public static final String MASK = "fileMask";
	//endregion

	private static final Class<?>[] jaxbContextClasses = { Settings.class, SettingsValue.class };

	static
	{
		if (!new File(SETTINGS_PATH).exists())
		{
			try
			{
				defaultSettings().save(SETTINGS_PATH);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public final static String GLOBAL_NS	= "GLOBAL";
	public final static String APPLICATION	= "APP_";
	public static final String SERVICE 		= "SRV_";
	public static final String CLIENT 		= "CLN_";
	public static final String SQL			= "SQL_";
	public static final String WATCHER		= "WATCHER";

	public static final String CONFIG_DIALOG = "CONFIGURATION";
	public static final String CONFIG_COMPARATOR = "COMPARATOR";

	@XmlRootElement(name = "value")
	@XmlAccessorType(XmlAccessType.NONE)
	public static class SettingsValue implements Mutable
	{
		@XmlAttribute
		private String ns;

		@XmlAttribute
		private String dialog;

		@XmlAttribute
		private String key;

		@XmlAttribute
		private Date time;

		@XmlValue
		private String value;

		@XmlTransient
		private boolean changed;

		public SettingsValue()
		{
			this(null, null, null);
		}

		public SettingsValue(String ns, String dialog, String key)
		{
			this.changed = false;
			this.ns = ns;
			this.dialog = dialog;
			this.key = key;
			this.time = new Date();
		}

		@Override
		public String toString()
		{
			return "{" + this.ns + ":" + this.dialog + ":" + this.key + "=" + this.value + "}";
		}

		public String getNs()
		{
			return this.ns;
		}

		public String getDialog()
		{
			return this.dialog;
		}

		public String getKey()
		{
			return this.key;
		}

		public Date getTime()
		{
			return this.time;
		}

		public String getValue()
		{
			return this.value;
		}

		public void setValue(String value)
		{
			this.changed = true;
			this.value = value;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			SettingsValue that = (SettingsValue) o;

			if (!ns.equals(that.ns))
				return false;
			if (!dialog.equals(that.dialog))
				return false;
			return key.equals(that.key);

		}

		@Override
		public int hashCode()
		{
			int result = ns.hashCode();
			result = 31 * result + dialog.hashCode();
			result = 31 * result + key.hashCode();
			return result;
		}

		@Override
		public boolean isChanged()
		{
			return this.changed;
		}

		@Override
		public void saved()
		{
			this.changed = false;
		}
	}

	@XmlElement(name = "value")
	protected MutableArrayList<SettingsValue> values;

	@XmlTransient
	private String fileName;

	public Settings()
	{
		this.values = new MutableArrayList<>();
	}

	public static Settings load(String fileName)
	{
		File file = new File(fileName);
		Settings settings = null;
		Settings defaultSettings = defaultSettings();
		if (file.exists())
		{
			try(Reader reader = CommonHelper.readerFromFile(file))
			{
				JAXBContext jaxbContext = JAXBContext.newInstance(jaxbContextClasses);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				unmarshaller.setEventHandler(event -> {
					logger.error("Error in settings : " + event);
					return false;
				});
				settings = (Settings) unmarshaller.unmarshal(reader);
			}
			catch (Exception e)
			{
				settings = defaultSettings;
			}
		}
		else
		{
			settings = defaultSettings;
		}
		Settings finalSettings = settings;
		defaultSettings.values.stream()
				.filter(sv -> !finalSettings.values.contains(sv))
				.forEach(finalSettings.values::add);
		finalSettings.fileName = fileName;
		return finalSettings;
	}

	//region default settings
	private static Settings DEFAULT_SETTINGS;

	public static Settings defaultSettings()
	{
		if (DEFAULT_SETTINGS == null)
		{
			DEFAULT_SETTINGS = new Settings();
			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, CONFIG_DIALOG, mapOf(
					CONFIG_COMPARATOR, CompareEnum.ALPHABET_0_1.name()
			));
			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, SETTINGS, mapOf(
					MAX_LAST_COUNT,"10",
					TIME_NOTIFICATION,"5",
					THEME, Theme.WHITE.name(),
					USE_FULL_SCREEN,"false",
					USE_EXTERNAL_REPORT_VIEWER, "false",
					USE_FULLSCREEN_XPATH,"false",
					LANGUAGE, Locales.ENGLISH.name(),
					COPYRIGHT,"",
					FONT, "System$13"
			));

			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, SHORTCUTS_NAME, mapOf(
					//Document
					SAVE_DOCUMENT,"Ctrl+S",
					SAVE_DOCUMENT_AS,"Shift+Ctrl+S",
					UNDO,"Ctrl+Z",
					REDO,"Shift+Ctrl+Z",

					//Matrix navigation
					ADD_ITEMS, "Insert",
					ALL_PARAMETERS, "Ctrl+D",
					COPY_ITEMS, "Ctrl+C",
					CUT_ITEMS, "Ctrl+X",
					PASTE_ITEMS, "Ctrl+V",
					HELP,"Ctrl+F1",
					DELETE_ITEM,"Delete",
					SHOW_ALL,"Ctrl+Q",
					GO_TO_LINE,"Ctrl+G",
					ADD_PARAMETER, "Ctrl+P",
					BREAK_POINT,"Ctrl+B",

					//Matrix actions
					SHOW_WATCH, "F2",
					TRACING, "Ctrl+F3",
					START_MATRIX, "Ctrl+F4",
					STOP_MATRIX, "Ctrl+F5",
					PAUSE_MATRIX, "F6",
					STEP_MATRIX, "F7",
					SHOW_RESULT, "F8",
					FIND_ON_MATRIX,"Ctrl+F",

					//Other
					SHOW_ALL_TABS,"Ctrl+E",
					SEARCH, "Ctrl+Shift+F"
			));

			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, LOGS_NAME, mapOf(
					ALL, "0x000000ff",
					DEBUG, "0x334db3ff",
					ERROR, "0xcc3333ff",
					FATAL, "0xcc3333ff",
					INFO, "0x336633ff",
					TRACE, "0x8066ccff",
					WARN, "0xe64d4dff"
			));

			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, MATRIX_COLORS, mapOf(
					ActionGroups.App.name(), "rgba(118,145,39,1.0)",
					ActionGroups.Matrix.name(), "rgba(85,183,183,1.0)",
					ActionGroups.GUI.name(), "rgba(82,168,100,1.0)",
					ActionGroups.Messages.name(), "rgba(231,116,52,1.0)",
					ActionGroups.Tables.name(), "rgba(209,73,73,1.0)",
					ActionGroups.Text.name(), "rgba(84,174,227,1.0)",
					ActionGroups.Clients.name(), "rgba(170,142,206,1.0)",
					ActionGroups.Services.name(), "rgba(101,177,170,1.0)",
					ActionGroups.SQL.name(), "rgba(211,52,114,1.0)",
					ActionGroups.System.name(), "rgba(237,173,52,1.0)",
					ActionGroups.Report.name(), "rgba(73,149,182,1.0)",
					ActionGroups.XML.name(), "rgba(201,138,205,1.0)",

					Tokens.Assert.name(), "rgba(205,80,122,1.0)",
					Tokens.NameSpace.name(), "rgba(87,149,27,1.0)",
					Tokens.TestCase.name(), "rgba(80,158,228,1.0)",
					Tokens.RawTable.name(), "rgba(209,73,73,1.0)",
					Tokens.RawMessage.name(), "rgba(231,116,52,1.0)",
					Tokens.SubCase.name(), "rgba(81,159,226,1.0)",
					Tokens.RawText.name(), "rgba(84,174,227,1.0)",
					Tokens.Step.name(), "rgba(81,159,226,1.0)",
					Tokens.Let.name(), "rgba(76,131,76,1.0)"
			));


			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, MATRIX_NAME, mapOf(
					MATRIX_DEFAULT_SCREENSHOT, ScreenshotKind.Never.name(),
					MATRIX_POPUPS, "false",
					MATRIX_FOLD_ITEMS, "false"
			));

			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, GIT, mapOf(
					GIT_KNOWN_HOST, "",
					GIT_SSH_IDENTITY, ""
			));

			DEFAULT_SETTINGS.setMapValues(GLOBAL_NS, WIZARD_NAME, mapOf(
					WizardSettings.Kind.TYPE.name()+MAX, "1",
					WizardSettings.Kind.TYPE.name()+MIN, "0",

					WizardSettings.Kind.PATH.name()+MAX, "1",
					WizardSettings.Kind.PATH.name()+MIN, "0",

					WizardSettings.Kind.SIZE.name()+MAX, "1",
					WizardSettings.Kind.SIZE.name()+MIN, "0",

					WizardSettings.Kind.POSITION.name()+MAX, "1",
					WizardSettings.Kind.POSITION.name()+MIN, "0",

					WizardSettings.Kind.ATTR.name()+MAX, "1",
					WizardSettings.Kind.ATTR.name()+MIN, "0",

					THRESHOLD, "0.6"
			));
		}
		return DEFAULT_SETTINGS;
	}
	//endregion

	public synchronized void save(String fileName) throws Exception
	{
		try (Writer writer = CommonHelper.writerToFileName(fileName))
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(jaxbContextClasses);

			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(this, writer);

			this.fileName = fileName;
			this.values.saved();
		}
	}

	public synchronized void saveIfNeeded() throws Exception
	{
		//TODO always values is changed, because we not use method saved();
		if (this.values.isChanged())
		{
			save();
		}
	}

	private void save() throws Exception
	{
		if (this.fileName != null)
		{
			save(this.fileName);
		}
		else
		{
			save(SETTINGS_PATH);
		}
	}

	public synchronized Set<String> getNamespaces()
	{
		Set<String> set = new HashSet<String>();
		for (SettingsValue value : this.values)
		{
			if (value.getNs() != null)
			{
				set.add(value.getNs());
			}
		}
		return set;
	}

	public Map<String, String> getMapValues(String ns, String dialog, String[] names)
	{
		Map<String, String> res = new HashMap<String, String>();
		for (String s : names)
		{
			res.put(s, null);
		}

		for (SettingsValue value : getValues(ns, dialog))
		{
			res.put(value.getKey(), value.getValue());
		}
		return res;
	}

	public synchronized List<SettingsValue> getValues(String ns, String dialog)
	{
		List<SettingsValue> res = new ArrayList<SettingsValue>();
		for (SettingsValue value : this.values)
		{
			if (Str.areEqual(ns, value.getNs())
					&& Str.areEqual(dialog, value.getDialog()))
			{
				res.add(value);
			}
		}
		return res;
	}

	public SettingsValue getValueOrDefault(String ns, String dialog, String key)
	{
		SettingsValue result = getValue(ns, dialog, key);
		if (result == null)
		{
			result = defaultSettings().getValue(ns, dialog, key);
		}
		return Optional.ofNullable(result).orElseThrow(() -> new IllegalArgumentException("No default settings for key " + key));
	}

	public synchronized SettingsValue getValue(String ns, String dialog, String key)
	{
		for (SettingsValue value : this.values)
		{
			if (Str.areEqual(ns, value.getNs())
					&& Str.areEqual(dialog, value.getDialog())
					&& Str.areEqual(key, value.getKey()))
			{
				return value;
			}
		}
		return null;
	}

	public synchronized void setValue(String ns, String dialog, String key, int max, String newValue)
	{
		List<SettingsValue> list = getValues(ns, dialog);
		Collections.sort(list, comparator);
		for (SettingsValue value : list)
		{
			if (value.getKey() != null && value.getKey().equals(key))
			{
				value.setValue(newValue);
				return;
			}
		}
		while (list.size() >= max)
		{
			list.remove(list.size() - 1);
		}
		removeAll(ns, dialog);
		this.values.addAll(list);
		SettingsValue settingsValue = new SettingsValue(ns, dialog, key);
		settingsValue.setValue(newValue);

		this.values.add(settingsValue);
	}

	public synchronized void setValue(String ns, String dialog, String key, String newValue)
	{
		for (SettingsValue value : this.values)
		{
			if (Str.areEqual(ns, value.getNs())
					&& Str.areEqual(dialog, value.getDialog())
					&& Str.areEqual(key, value.getKey()))
			{
				value.setValue(newValue);
				return;
			}
		}

		SettingsValue value = new SettingsValue(ns, dialog, key);
		value.setValue(newValue);

		this.values.add(value);
	}

	public void setMapValues(String ns, String dialog, Map<String, String> values)
	{
		for (Entry<String, String> entry : values.entrySet())
		{
			setValue(ns, dialog, entry.getKey(), entry.getValue());
		}
	}

	public synchronized void removeAll(String ns, String dialog)
	{
		this.values = this.values.stream()
				.filter(value -> !(Str.areEqual(ns, value.getNs()) && Str.areEqual(dialog, value.getDialog())))
				.collect(Collectors.toCollection(MutableArrayList::new));
	}

	public synchronized void remove(String ns, String dialog, String key)
	{
		this.values = this.values.stream()
				.filter(value -> !(Str.areEqual(ns, value.getNs()) && Str.areEqual(dialog, value.getDialog()) && Str.areEqual(key, value.getKey())))
				.collect(Collectors.toCollection(MutableArrayList::new));
	}

	private static Map<String, String> mapOf(String... args)
	{
		Map<String, String> map = new LinkedHashMap<>();
		Iterator<String> iterator = Arrays.asList(args).iterator();
		while (iterator.hasNext())
		{
			String key = iterator.next();
			if (iterator.hasNext())
			{
				String value = iterator.next();
				map.put(key, value);
			}
			else
			{
				break;
			}
		}
		return map;
	}

	public synchronized void clear()
	{
		this.values.clear();
	}

	public synchronized List<KeyCombination> getRemovedShortcuts()
	{
		return Stream.of(
				SAVE_DOCUMENT,
				ADD_ITEMS,
				ALL_PARAMETERS,
				BREAK_POINT,
				ADD_PARAMETER,
				HELP,
				GO_TO_LINE,
				SHOW_ALL,
				DELETE_ITEM,
				COPY_ITEMS,
				CUT_ITEMS,
				PASTE_ITEMS,
				START_MATRIX,
				STOP_MATRIX,
				PAUSE_MATRIX,
				STEP_MATRIX,
				SHOW_RESULT,
				SHOW_WATCH,
				TRACING,
				FIND_ON_MATRIX,
				SEARCH
		)
				.map(s -> Common.getShortcut(this, s))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private final static Logger logger = Logger.getLogger(Settings.class);

	private static Comparator<SettingsValue> comparator = (o1, o2) -> (int)(o2.time.getTime() - o1.time.getTime());

}
