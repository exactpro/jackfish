////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.matrix.parser.items.MutableArrayList;
import com.exactprosystems.jf.tool.Common;
import javafx.scene.input.KeyCombination;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XmlRootElement(name = "settings")
@XmlAccessorType(XmlAccessType.NONE)
public class Settings
{
	public final static String SettingsPath	= ".settings.xml";

	public static final String	FONT			= "Font";
	public static final String	SETTINGS 		= "Main";
	public static final String	LOGS_NAME		= "Logs";
	public static final String	SHORTCUTS_NAME	= "Shortcuts";
	public static final String	MATRIX_COLORS	= "MatrixColors";
	public static final String	GIT				= "Git";

	//region Shortcuts
	//other shortcuts
	public static final String SHOW_ALL_TABS	= "ShowAllTabs";

	//document shortcuts
	public static final String SAVE_DOCUMENT	= "SaveDocument";
	public static final String SAVE_DOCUMENT_AS	= "SaveDocumentAs";
	public static final String UNDO				= "Undo";
	public static final String REDO				= "Redo";

	//matrix navigation shortcuts
	public static final String ADD_CHILD		= "AddChild";
	public static final String ADD_BEFORE		= "AddBefore";
	public static final String ADD_AFTER		= "AddAfter";
	public static final String BREAK_POINT		= "BreakPoint";
	public static final String ADD_PARAMETER	= "AddParameter";
	public static final String HELP				= "Help";
	public static final String GO_TO_LINE		= "GoToLine";
	public static final String SHOW_ALL			= "ShowAll";
	public static final String DELETE_ITEM		= "DeleteItem";
	public static final String COPY_ITEMS		= "CopyItems";
	public static final String PASTE_ITEMS_CHILD = "PasteItemsToChild";
	public static final String PASTE_ITEMS_AFTER = "PasteItemsAfter";
	public static final String PASTE_ITEMS_BEFORE = "PasteItemsBefore";
	public static final String COLLAPSE_ALL		= "CollapseAll";
	public static final String COLLAPSE_ONE		= "CollapseOne";
	public static final String EXPAND_ALL		= "ExpandAll";
	public static final String EXPAND_ONE		= "ExpandOne";

	//matrix actions shortcuts
	public static final String START_MATRIX		= "StartMatrix";
	public static final String STOP_MATRIX		= "StopMatrix";
	public static final String PAUSE_MATRIX		= "PauseMatrix";
	public static final String SHOW_RESULT		= "ShowResult";
	public static final String SHOW_WATCH		= "ShowWatch";
	public static final String TRACING			= "Tracing";
	public static final String FIND_ON_MATRIX	= "FindOnMatrix";
	//endregion

	//region Main
	public static final String	MAX_LAST_COUNT = "maxFilesCount";
	public static final String	TIME_NOTIFICATION = "timeNotification";
	public static final String	THEME = "theme";
	public static final String	USE_FULL_SCREEN	= "useFullScreen";
	public static final String	USE_COMPACT_MODE = "useCompactMode";
	public static final String	USE_FULLSCREEN_XPATH = "useFullScreenXpath";
	public static final String	COPYRIGHT = "copyright";
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

	public static final String 	OPENED 				= "OPENED";
	public static final String 	MAIN_NS 			= "MAIN";
	public static final String	MATRIX_TOOLBAR		= "MATRIX_TOOLBAR";

	private static final Class<?>[] jaxbContextClasses = { Settings.class, SettingsValue.class };

	static
	{
		if (!new File(SettingsPath).exists())
		{
			try
			{
				defaultSettings().save(SettingsPath);
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
		this.values = new MutableArrayList<SettingsValue>();
	}

	public static Settings load(String fileName)
	{
		File file = new File(fileName);
		Settings settings = null;
		if (file.exists())
		{
			try(Reader reader = new FileReader(file))
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
				settings = new Settings();
			}
		}
		else
		{
			settings = new Settings();
		}

		settings.fileName = fileName;
		return settings;
	}

	public static Settings defaultSettings()
	{
		//TODO add all default shortcuts and other settings.
		Settings settings = new Settings();
		settings.setMapValues(GLOBAL_NS, "Logs", mapOf(
				ALL, "0x000000ff",
				DEBUG, "0x334db3ff",
				ERROR, "0xcc3333ff",
				FATAL, "0xcc3333ff",
				INFO, "0x336633ff",
				TRACE, "0x8066ccff",
				WARN, "0xe64d4dff"
		));
		settings.setMapValues(GLOBAL_NS, "Main", mapOf(
				MAX_LAST_COUNT,"10",
				USE_FULL_SCREEN,"false",
				USE_COMPACT_MODE,"false",
				TIME_NOTIFICATION,"5",
				THEME,"WHITE",
				COPYRIGHT,"//==============================================\\n//  Copyright (c) 2009-2016, Exactpro Systems, LLC\\n//  Quality Assurance &amp; Related Development for Innovative " +
						"Trading Systems.\\n//  All rights reserved.\\n//  This is unpublished, licensed software, confidential and proprietary\\n//  information which is the property of Exactpro Systems, LLC or its licensors.\\n//==============================================",
				USE_FULLSCREEN_XPATH,"false"
		));
		settings.setValue(GLOBAL_NS, "Main", FONT, "System$13");
		settings.setMapValues(GLOBAL_NS, "Shortcuts", mapOf(
				FIND_ON_MATRIX,"Ctrl+F",
				SHOW_ALL_TABS,"Ctrl+E",
				SAVE_DOCUMENT,"Ctrl+S",
				SAVE_DOCUMENT_AS,"Shift+Ctrl+S",
				BREAK_POINT,"Ctrl+B",
				DELETE_ITEM,"Delete",
				EXPAND_ONE,"Ctrl+Equals",
				COLLAPSE_ALL,"Shift+Ctrl+Minus",
				COLLAPSE_ONE,"Ctrl+Minus",
				EXPAND_ALL,"Shift+Ctrl+Equals",
				SHOW_ALL,"Ctrl+Q",
				GO_TO_LINE,"Ctrl+G",
				UNDO,"Ctrl+Z",
				REDO,"Shift+Ctrl+Z"
		));
		return settings;
	}

	public synchronized void save(String fileName) throws Exception
	{
		try (Writer writer = new FileWriter(new File(fileName)))
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
			save(SettingsPath);
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

	public SettingsValue getValueOrDefault(String ns, String dialog, String key, String defaultValue)
	{
		SettingsValue result = getValue(ns, dialog, key);
		if (result == null)
		{
			result = new SettingsValue(ns, dialog, key);
			result.setValue(defaultValue);
		}

		return result;
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
				ADD_CHILD,
				ADD_BEFORE,
				ADD_AFTER,
				BREAK_POINT,
				ADD_PARAMETER,
				HELP,
				GO_TO_LINE,
				SHOW_ALL,
				DELETE_ITEM,
				COPY_ITEMS,
				PASTE_ITEMS_CHILD,
				PASTE_ITEMS_AFTER,
				PASTE_ITEMS_BEFORE,
				COLLAPSE_ALL,
				COLLAPSE_ONE,
				EXPAND_ALL,
				EXPAND_ONE,
				START_MATRIX,
				STOP_MATRIX,
				PAUSE_MATRIX,
				SHOW_RESULT,
				SHOW_WATCH,
				TRACING,
				FIND_ON_MATRIX
		)
				.map(s -> Common.getShortcut(this, s))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private final static Logger logger = Logger.getLogger(Settings.class);

	private static Comparator<SettingsValue> comparator = (o1, o2) -> (int)(o2.time.getTime() - o1.time.getTime());

}
