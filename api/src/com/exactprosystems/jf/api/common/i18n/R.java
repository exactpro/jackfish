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
	DEFAULT("Default"),
	DRAG_N_DROP_LIST_TOOLTIP("DragNDropListTooltip"),
	//endregion

	//region actions.app
	APP_CONNECTION_NAME_DESCRIPTION("AppConnectionDescription"),
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
	WIZARD_CONNECT("Connect"),
	WIZARD_STATUS_FAILED("Failed"),
	WIZARD_STOP_APPLICATION("Stop"),
	WIZARD_ERROR_ON_APPLICATION_STOP("ErrorOnApplicationStop"),
	WIZARD_VARIABLE_WITH_NAME("VariableWithNameExist"),
	WIZARD_STATUS_SUCCESS("Success"),
	WIZARD_STATUS_LOADING("Loading"),
	WIZARD_ERROR_ON_CLOSE_WIZARD("ErrorOnCloseWizard"),
	WIZARD_TOOLTIP_NAME_OF_VAR("EnterNameOfVar"),
	WIZARD_LABEL_STATUS("Status"),
	WIZARD_LABEL_STORE_AS("StoreAs"),
	WIZARD_SELECT_CONVERTER_FROM_COMBOBOX("SelectConverterFromCombobox"),
	WIZARD_STATUS_SCANNING("Scanning"),
	WIZARD_APP_RESIZE_DESCRIPTION("AppResizeConverterDescription"),
	WIZARD_LABEL_DIALOG("Dialog"),
	WIZARD_LABEL_SELFID("SelfID"),
	WIZARD_ERROR_ON_SET_SELF_ID("ErrorOnSetSelfId"),
	WIZARD_LABEL_ON_OPEN("OnOpen"),
	WIZARD_LABEL_ON_CLOSE("OnClose"),
	WIZARD_SELF_ID_NOT_FOUND("SelfIdNotFound"),
	WIZARD_SELF_CONTROL_IS_EMPTY("SelfControlIsEmpty"),
	WIZARD_APPLICATION_NOT_STARTED("ApplicationNotStarted"),
	WIZARD_DICTIONARY_EXCEPTION("DictionaryWizardException"),
	WIZARD_ERROR_ON_GENERATE_ON_OPEN("ErrorOnGenerateOnOpen"),
	WIZARD_ERROR_ON_GENERATE_ON_CLOSE("ErrorOnGenerateOnClose"),
	WIZARD_NOTHING_TO_UPDATE_INFO("NothingToUpdate"),
	WIZARD_UPDATING_ELEMENTS("UpdatingElements"),
	WIZARD_START_UPDATING_ITEMS("StartUpdatingItemOf"),
	WIZARD_ERROR_ON_ARRANGE_ONE("ErrorOnArrangeOne"),
	WIZARD_END_UPDATING("EndUpdating"),
	WIZARD_ERROR_ON_FIND("ErrorOnFindElements"),
	WIZARD_LABEL_CONNECTION("Connection"),
	WIZARD_NUMBER("Number"),
	WIZARD_ABOUT("About"),
	WIZARD_LESS("Less"),
	WIZARD_GREAT("Great"),
	WIZARD_BETWEEN("Between"),
	WIZARD_LABEL_SELECT_DISTANCE("SelectDistance"),
	WIZARD_ALL("All"),
	WIZARD_SIGNIFICANT("Significant"),
	WIZARD_USE_DISTANCE("UseDistance"),
	WIZARD_SELECT_FUNCTIONS("SelectFunctions"),
	WIZARD_ALL_OK("AllOk"),
	WIZARD_CHECKING("Checking"),
	WIZARD_SELECT_MORE_ELEMENTS("SelectMoreElements"),
	WIZARD_CREATING_TABLE("CreatingTable"),
	WIZARD_RELATION("Relation"),
	WIZARD_CHECK("Check"),
	WIZARD_SAVE("Save"),
	WIZARD_CANT_SAVE_INVALID_DOSPEC("CantSaveInvalidDoSpecFunc"),
	WIZARD_CANT_CHECK_INVALID_DOSPEC("CantCheckInvalidDoSpecFunc"),
	WIZARD_NAMESPACES("Namespases"),
	WIZARD_WHERE_TO_MOVE("WhereToMove"),
	WIZARD_INVOKE_FROM_NAMESPACE("WizardMustBeInvokedFromNamespace"),
	WIZARD_MATRIX_CONTAINS_REFERENCES_2("MatrixContainsReferences"),
	WIZARD_NO_CHANGES_NEEDED("NoChangesNeeded"),
	WIZARD_ERROR_ON_CREATE_COPY("ERROR_ON_CREATE_COPY"),
	WIZARD_OLD_VALUE_1("OLD_VALUE_1"),
	WIZARD_SELECT_NEW_VALUE("SELECT_NEW_VALUE"),
	WIZARD_DICTIONARY_NAME_1("WIZARD_DICTIONARY_NAME_1"),
	WIZARD_DICTIONARY_OK("DICTIONARY_OK"),
	WIZARD_ERROR_ON_TABLE_CREATE("ERROR_ON_TABLE_CREATE"),
	WIZARD_NO_FILE_NAME("WIZARD_NO_FILE_NAME"),
	WIZARD_ERROR_IN_FILENAME("WIZARD_ERROR_IN_FILENAME"),
	WIZARD_FILE_NOT_CSV_1("WIZARD_FILE_NOT_CSV"),
	WIZARD_FILE_READ_ONLY_1("WIZARD_FILE_READ_ONLY_1"),
	WIZARD_DELIMITER_INCORRECT("WIZARD_DELIMETR_INCORRECT"),
	WIZARD_ENTER_XPATH("WIZARD_ENTER_XPATH"),
	WIZARD_HELPER("WIZARD_HELPER"),

	WIZARD_RELATIVE("WIZARD_RELATIVE"),
	WIZARD_USE_TEXT("WIZARD_USE_TEXT"),


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
	public enum Constants
	{
		RESOURCE_BUNDLE("com/exactprosystems/jf/ToolResourceBundle"),
		DEFAULT(""),

		//region jf.actions.app
		APP_CONNECTION_NAME("AppConnection"),

		//endregion


		;

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
