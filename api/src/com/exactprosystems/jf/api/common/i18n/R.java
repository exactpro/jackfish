package com.exactprosystems.jf.api.common.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

//this class contains i18n constants
public enum R
{
	//region general
	DRAG_N_DROP_LIST_TOOLTIP("DragNDropListTooltip"),
	//endregion

	//region tool.wizard
	WIZARD_SUCCESS("WizardSuccess_1"),

	//region tool.wizard.all
	WIZARD_START_CONNECTION("Start"),
	WIZARD_SCAN("Scan"),
	WIZARD_CHECK_TABLE("CheckTable"),
	WIZARD_SELECT_CONNECTION_INFO("SelectConnectionInfo"),
	WIZARD_SELECT_DIALOG("SelectDialog"),
	WIZARD_SELECT_STORED_CONN("SelectStoredConn"),

	//endregion

	//endregion
	;

	private static final ResourceBundle bundle = ResourceBundle.getBundle(Constants.RESOURCE_BUNDLE.get(), new UTF8Control());
	private final String string;

	R(String s)
	{
		this.string = s;
	}

	/**
	 * use this method for I18n
	 * @return string by default locales
	 */
	public static String get(R r)
	{
		return bundle.getString(r.string);
	}

	public String get()
	{
		return bundle.getString(this.string);
	}

	private static class UTF8Control extends ResourceBundle.Control
	{
		public ResourceBundle newBundle
				(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException, IOException
		{
			// The below is a copy of the default implementation.
			String bundleName = toBundleName(baseName, locale);
			String resourceName = toResourceName(bundleName, "properties");
			ResourceBundle bundle = null;
			InputStream stream = null;
			if (reload) {
				URL url = loader.getResource(resourceName);
				if (url != null) {
					URLConnection connection = url.openConnection();
					if (connection != null) {
						connection.setUseCaches(false);
						stream = connection.getInputStream();
					}
				}
			} else {
				stream = loader.getResourceAsStream(resourceName);
			}
			if (stream != null) {
				try {
					// Only this line is changed to make it to read properties files as UTF-8.
					bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
				} finally {
					stream.close();
				}
			}
			return bundle;
		}
	}

	//this class contains non i18n constants
	enum Constants
	{
		RESOURCE_BUNDLE("com/exactprosystems/jf/ToolResourceBundle");

		private final String string;
		Constants(String s)
		{
			this.string = s;
		}

		public String get()
		{
			return this.string;
		}

	}
}
