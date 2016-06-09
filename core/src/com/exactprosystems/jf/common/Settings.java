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

import org.apache.log4j.Logger;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@XmlRootElement(name = "settings")
@XmlAccessorType(XmlAccessType.NONE)
public class Settings
{
	public final static String SettingsPath	= ".settings.xml";

	static
	{
		if (!new File(SettingsPath).exists())
		{
			try (	BufferedReader reader = new BufferedReader(new InputStreamReader(Settings.class.getResourceAsStream(SettingsPath)));
					 BufferedWriter writer = new BufferedWriter(new FileWriter(SettingsPath)))
			{
				String line = null;

				while ((line = reader.readLine()) != null)
				{
					writer.append(line);
					writer.newLine();
				}
			}
			catch (IOException e)
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

	public synchronized void clear()
	{
		this.values.clear();
	}

	private static final Class<?>[] jaxbContextClasses = { Settings.class, SettingsValue.class };

	private final static Logger logger = Logger.getLogger(Settings.class);

	private static Comparator<SettingsValue> comparator = (o1, o2) -> (int)(o2.time.getTime() - o1.time.getTime());

}
