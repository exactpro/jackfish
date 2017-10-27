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
public enum R {
	//region general
	DEFAULT,
	DRAG_N_DROP_LIST_TOOLTIP,
	//endregion

	//region actions
	DEFAULT_OUTPUT_DESCRIPTION,

	//region actions.app
	APP_CONNECTION_NAME_DESCRIPTION,
	APP_CLOSE_WINDOW_GENERAL_DESCRIPTION,
	APP_CLOSE_WINDOW_OUTPUT_DESCRIPTION,
	APP_CLOSE_WINDOW_EXAMPLES,
	APP_CONNECT_TO_GENERAL_DESC,
	APP_CONNECT_TO_ADDITIONAL_DESC,
	APP_CONNECT_TO_OUTPUT_DESC,
	APP_CONNECT_TO_EXAMPLE,
	APPLICATION_CONNECT_TO_ID,
	APPLICATION_CONNECT_TO_CONNECTION,
	APP_GET_PROPERTIES_GENERAL_DESC,
	APP_GET_PROPERTIES_ADDITIONAL_DESC,
	APP_GET_PROPERTIES_OUTPUT_DESC,
	APP_GET_PROPERTIES_EXAMPLE,
	APPLICATION_GET_PROPERTIES_CONNECTION,
	APP_MOVE_GENERAL_DESC,
	APPLICATION_MOVE_CONNECTION,
	APPLICATION_MOVE_X,
	APPLICATION_MOVE_Y,
	APP_NAVIGATE_GENERAL_DESC,
	APP_NAVIGATE_ADDITIONAL_DESC,
	APP_NAVIGATE_EXAMPLE,
	APPLICATION_NAVIGATE_CONNECTION,
	APPLICATION_NAVIGATE_KIND,
	APP_NEW_INSTANCE_GENERAL_DESC,
	APP_NEW_INSTANCE_ADDITIONAL_DESC,
	APP_NEW_INSTANCE_EXAMPLE,
	APPLICATION_NEW_INSTANCE_CONNECTION,
	APP_REFRESH_GENERAL_DESC,
	APP_REFRESH_EXAMPLE,
	APPLICATION_REFRESH_CONNECTION,
	APP_RESIZE_GENERAL_DESC,
	APP_RESIZE_EXAMPLE,
	APPLICATION_RESIZE_CONNECTION,
	APPLICATION_RESIZE_HEIGHT,
	APPLICATION_RESIZE_WIDTH,
	APPLICATION_RESIZE_RESIZE,
	APP_SET_PROPERTIES_GENERAL_DESC,
	APP_SET_PROPERTIES_ADDITIONAL_DESC,
	APP_SET_PROPERTIES_EXAMPLE,
	APPLICATION_SET_PROPERTIES_CONNECTION,
	APP_START_GENERAL_DESC,
	APP_START_ADDITIONAL_DESC,
	APP_START_OUTPUT_DESC,
	APP_START_EXAMPLE,
	APPLICATION_START_APP_ID,
	APP_STOP_GENERAL_DESC,
	APP_STOP_EXAMPLE,
	APPLICATION_STOP_CONNECTION,
	APPLICATION_STOP_NEED_KILL,
	APP_SWITCH_TO_GENERAL_DESC,
	APP_SWITCH_TO_ADDITIONAL_DESC,
	APP_SWITCH_TO_OUTPUT_DESC,
	APP_SWITCH_TO_EXAMPLE,
	APPLICATION_SWITCH_TO_CONNECTION,
	APPLICATION_SWITCH_TO_SOFT_CONDITION,
	//endregion

	//region actions.clients
	CLIENT_CHECK_FIELDS_GENERAL_DESC,
	CLIENT_CHECK_FIELDS_OUTPUT_DESC,
	CLIENT_CHECK_FIELDS_EXAMPLE,
	CLIENT_CHECK_FIELDS_MESSAGE,
	CLIENT_CHECK_FIELDS_CONNECTION,
	CLIENT_CHECK_MESSAGE_GENERAL_DESC,
	CLIENT_CHECK_MESSAGE_ADDITIONAL_DESC,
	CLIENT_CHECK_MESSAGE_EXAMPLE,
	CLIENT_CHECK_MESSAGE_CONNECTION,
	CLIENT_CHECK_MESSAGE_ACTUAL,
	CLIENT_CHECK_MESSAGE_MESSAGE_TYPE,
	CLIENT_CLEAR_MESSAGES_GENERAL_DESC,
	CLIENT_CLEAR_MESSAGES_EXAMPLE,
	CLIENT_CLEAR_MESSAGES_CONNECTION,
	CLIENT_CONNECT_GENERAL_DESC,
	CLIENT_CONNECT_EXAMPLE,
	CLIENT_CONNECT_ADDITIONAL_DESC,
	CLIENT_CONNECT_OUTPUT_DESC,
	CLIENT_CONNECT_CONNECTION,
	CLIENT_CONNECT_SOCKET,
	CLIENT_COUNT_MESSAGES_GENERAL_DESC,
	CLIENT_COUNT_MESSAGES_OUTPUT_DESC,
	CLIENT_COUNT_MESSAGES_ADDITIONAL_DESC,
	CLIENT_COUNT_MESSAGES_EXAMPLE,
	CLIENT_COUNT_MESSAGES_CONDITIONS,
	CLIENT_COUNT_MESSAGES_MESSAGE_TYPE,
	CLIENT_COUNT_MESSAGES_CONNECTION,
	CLIENT_DECODE_GENERAL_DESC,
	CLIENT_DECODE_OUTPUT_DESC,
	CLIENT_DECODE_EXAMPLE,
	CLIENT_DECODE_ARRAY,
	CLIENT_DECODE_CONNECTION,
	CLIENT_ENCODE_GENERAL_DESC,
	CLIENT_ENCODE_OUTPUT_DESC,
	CLIENT_ENCODE_EXAMPLE,
	CLIENT_ENCODE_MESSAGE,
	CLIENT_ENCODE_CONNECTION,
	CLIENT_GET_MESSAGE_GENERAL_DESC,
	CLIENT_GET_MESSAGE_ADDITIONAL_DESC,
	CLIENT_GET_MESSAGE_OUTPUT_DESC,
	CLIENT_GET_MESSAGE_EXAMPLE,
	CLIENT_GET_MESSAGE_CONNECTION,
	CLIENT_GET_MESSAGE_MESSAGE_TYPE,
	CLIENT_GET_MESSAGE_CONDITIONS,
	CLIENT_GET_MESSAGE_TIMEOUT,
	CLIENT_GET_MESSAGE_REMOVE,
	CLIENT_LOAD_GENERAL_DESC,
	CLIENT_LOAD_OUTPUT_DESC,
	CLIENT_LOAD_EXAMPLE,
	CLIENT_LOAD_ID,
	CLIENT_SEND_MAP_MESSAGE_GENERAL_DESC,
	CLIENT_SEND_MAP_MESSAGE_EXAMPLE,
	CLIENT_SEND_MAP_MESSAGE_CONNECTION,
	CLIENT_SEND_MAP_MESSAGE_MESSAGE,
	CLIENT_SEND_MAP_MESSAGE_CHECK,
	CLIENT_SEND_MESSAGE_GENERAL_DESC,
	CLIENT_SEND_MESSAGE_OUTPUT_DESC,
	CLIENT_SEND_MESSAGE_ADDITIONAL_DESC,
	CLIENT_SEND_MESSAGE_EXAMPLE,
	CLIENT_SEND_MESSAGE_CONNECTION,
	CLIENT_SEND_MESSAGE_MESSAGE_TYPE,
	CLIENT_SEND_MESSAGE_CHECK,
	CLIENT_SEND_RAW_MESSAGE_EXAMPLE,
	CLIENT_SEND_RAW_MESSAGE_GENERAL_DESC,
	CLIENT_SEND_RAW_MESSAGE_CONNECTION,
	CLIENT_SEND_RAW_MESSAGE_DATA,
	CLIENT_SET_PROPERTIES_ADDITIONAL_DESC,
	CLIENT_SET_PROPERTIES_EXAMPLE,
	CLIENT_SET_PROPERTIES_GENERAL_DESC,
	CLIENT_SET_PROPERTIES_CONNECTION,
	CLIENT_START_GENERAL_DESC,
	CLIENT_START_OUTPUT_DESC,
	CLIENT_START_ADDITIONAL_DESC,
	CLIENT_START_EXAMPLE,
	CLIENT_START_CONNECTION,
	CLIENT_STOP_GENERAL_DESC,
	CLIENT_STOP_EXAMPLE,
	CLIENT_STOP_CONNECTION,
	//endregion

	//region actions.tables
	TABLE_CREATE_GENERAL_DESC,
	TABLE_CREATE_ADDITIONAL_DESC,
	TABLE_CREATE_OUTPUT_DESC,
	TABLE_CREATE_EXAMPLE,
	TABLE_ADD_COLUMNS_GENERAL_DESC,
	TABLE_ADD_COLUMNS_EXAMPLES,
	TABLE_ADD_COLUMNS_TABLE,
	TABLE_ADD_COLUMNS_COLUMNS,
	TABLE_ADD_COLUMNS_INDEX,
	TABLE_ADD_VALUE_EXAMPLE,
	TABLE_ADD_VALUE_GENERAL_DESC,
	TABLE_ADD_VALUE_ADDITIONAL_DESC,
	TABLE_ADD_VALUE_TABLE,
	TABLE_ADD_VALUE_INDEX,
	TABLE_COLUMN_AS_LIST_GENERAL_DESC,
	TABLE_COLUMN_AS_LIST_OUTPUT_DESC,
	TABLE_COLUMN_AS_LIST_TABLE,
	TABLE_COLUMN_AS_LIST_COLUMN,
	TABLE_COLUMN_AS_LIST_GET_VALUES,
	TABLE_COLUMN_RENAME_GENERAL_DESC,
	TABLE_COLUMN_RENAME_ADDITIONAL_DESC,
	TABLE_COLUMN_RENAME_TABLE,
	TABLE_COLUMN_RENAME_EXAMPLE,
	TABLE_COMPARE_TWO_GENERAL_DESC,
	TABLE_COMPARE_TWO_EXAMPLE,
	TABLE_COMPARE_TWO_OUTPUT_DESC,
	TABLE_COMPARE_TWO_ACTUAL,
	TABLE_COMPARE_TWO_EXPECTED,
	TABLE_COMPARE_TWO_EXCLUDE,
	TABLE_COMPARE_TWO_IGNORE_ROWS_ORDER,
	TABLE_COMPARE_TWO_COMPARE_VALUES,
	TABLE_CONSIDER_COLUMN_AS_GENERAL_DESC,
	TABLE_CONSIDER_COLUMN_AS_EXAMPLE,
	TABLE_CONSIDER_COLUMN_AS_TABLE,
	TABLE_CONSIDER_COLUMN_AS_STRING,
	TABLE_CONSIDER_COLUMN_AS_BOOLEAN,
	TABLE_CONSIDER_COLUMN_AS_INTEGER,
	TABLE_CONSIDER_COLUMN_AS_DOUBLE,
	TABLE_CONSIDER_COLUMN_AS_BIG_DECIMAL,
	TABLE_CONSIDER_COLUMN_AS_DATE,
	TABLE_CONSIDER_COLUMN_AS_EXPRESSION,
	TABLE_CONSIDER_COLUMN_AS_GROUP,
	TABLE_CONSIDER_COLUMN_AS_HYPERLINK,
	TABLE_CONSIDER_COLUMN_AS_COLORED,
	TABLE_EDIT_GENERAL_DESC,
	TABLE_EDIT_ADDITIONAL_DESC,
	TABLE_EDIT_EXAMPLE,
	TABLE_EDIT_OUTPUT_DESC,
	TABLE_EDIT_TITLE,
	TABLE_EDIT_TABLE,
	TABLE_GET_ROW_INDEXES_GENERAL_DESC,
	TABLE_GET_ROW_INDEXES_ADDITIONAL_DESC,
	TABLE_GET_ROW_INDEXES_OUTPUT_DESC,
	TABLE_GET_ROW_INDEXES_EXAMPLE,
	TABLE_GET_ROW_INDEXES_TABLE,
	TABLE_LEFT_JOIN_GENERAL_DESC,
	TABLE_LEFT_JOIN_ADDITIONAL_DESC,
	TABLE_LEFT_JOIN_OUTPUT_DESC,
	TABLE_LEFT_JOIN_EXAMPLE,
	TABLE_LEFT_JOIN_RIGHT_TABLE,
	TABLE_LEFT_JOIN_LEFT_TABLE,
	TABLE_LEFT_JOIN_RIGHT_ALIAS,
	TABLE_LEFT_JOIN_LEFT_ALIAS,
	TABLE_LEFT_JOIN_CONDITION,
	TABLE_LOAD_FROM_DIR_GENERAL_DESC,
	TABLE_LOAD_FROM_DIR_OUTPUT_DESC,
	TABLE_LOAD_FROM_DIR_EXAMPLE,
	TABLE_LOAD_FROM_DIR_DIRECTORY,
	TABLE_LOAD_FROM_FILE_GENERAL_DESC,
	TABLE_LOAD_FROM_FILE_OUTPUT_DESC,
	TABLE_LOAD_FROM_FILE_EXAMPLE,
	TABLE_LOAD_FROM_FILE_FILE,
	TABLE_LOAD_FROM_FILE_DELIMITER,
	TABLE_REMOVE_COLUMNS_GENERAL_DESC,
	TABLE_REMOVE_COLUMNS_EXAMPLE,
	TABLE_REMOVE_COLUMNS_TABLE,
	TABLE_REMOVE_COLUMNS_COLUMNS,
	TABLE_REMOVE_ROW_GENERAL_DESC,
	TABLE_REMOVE_ROW_OUTPUT_DESC,
	TABLE_REMOVE_ROW_EXAMPLE,
	TABLE_REMOVE_ROW_TABLE,
	TABLE_REMOVE_ROW_ROW,
	TABLE_REPLACE_GENERAL_DESC,
	TABLE_REPLACE_EXAMPLE,
	TABLE_REPLACE_TABLE,
	TABLE_REPLACE_COLUMNS,
	TABLE_REPLACE_REPLACE,
	TABLE_REPLACE_SEARCH,
	TABLE_REPLACE_REGEXP,
	TABLE_REPLACE_MATCH_CELL,
	TABLE_REPORT_GENERAL_DESC,
	TABLE_REPORT_ADDITIONAL_DESC,
	TABLE_REPORT_EXAMPLE,
	TABLE_REPORT_TABLE,
	TABLE_REPORT_TITLE,
	TABLE_REPORT_TO_REPORT,
	TABLE_REPORT_COLUMNS,
	TABLE_REPORT_WIDTHS,
	TABLE_REPORT_BEFORE_TEST_CASE,
	TABLE_REPORT_REPORT_VALUES,
	TABLE_REPORT_WITH_NUMBERS,
	TABLE_SAVE_TO_FILE_GENERAL_DESC,
	TABLE_SAVE_TO_FILE_OUTPUT_DESC,
	TABLE_SAVE_TO_FILE_EXAMPLE,
	TABLE_SAVE_TO_FILE_TABLE,
	TABLE_SAVE_TO_FILE_FILE_NAME,
	TABLE_SAVE_TO_FILE_DELIMITER,
	TABLE_SAVE_TO_FILE_SAVE_VALUES,
	TABLE_SELECT_GENERAL_DESC,
	TABLE_SELECT_ADDITIONAL_DESC,
	TABLE_SELECT_OUTPUT_DESC,
	TABLE_SELECT_EXAMPLE,
	TABLE_SELECT_TABLE,
	TABLE_SET_VALUE_GENERAL_DESC,
	TABLE_SET_VALUE_ADDITIONAL_DESC,
	TABLE_SET_VALUE_EXAMPLE,
	TABLE_SET_VALUE_TABLE,
	TABLE_SET_VALUE_INDEX,
	TABLE_SORT_GENERAL_DESC,
	TABLE_SORT_OUTPUT_DESC,
	TABLE_SORT_EXAMPLE,
	TABLE_SORT_TABLE,
	TABLE_SORT_COLUMN_INDEX,
	TABLE_SORT_ASCENDING,
	TABLE_SORT_IGNORE_CASE,
	TABLE_UNION_GENERAL_DESC,
	TABLE_UNION_EXAMPLE,
	TABLE_UNION_MAIN_TABLE,
	TABLE_UNION_UNITED_TABLE,

	//endregion

	//region actions.gui
	DESKTOP_SCREENSHOT_GENERAL_DESC,
	DESKTOP_SCREENSHOT_OUTPUT_DESC,
	DESKTOP_SCREENSHOT_EXAMPLE,
	DESKTOP_SCREENSHOT_DESCRIPTION_PARAM,
	DIALOG_ALERT_GENERAL_DESC,
	DIALOG_ALERT_OUTPUT_DESC,
	DIALOG_ALERT_EXAMPLE,
	DIALOG_ALERT_APP_CONNECTION,
	DIALOG_ALERT_PERFORM_KIND,
	DIALOG_ALERT_TEXT,
	DIALOG_CHECK_LAYOUT_GENERAL_DESC,
	DIALOG_CHECK_LAYOUT_APP_CONNECTION,
	DIALOG_CHECK_LAYOUT_DIALOG,
	DIALOG_CHECK_LAYOUT_DO_NOT_OPEN,
	DIALOG_CHECK_LAYOUT_DO_NOT_CLOSE,
	DIALOG_CHECK_LAYOUT_FIELDS,
	DIALOG_CHECK_LAYOUT_TABLE,
	DIALOG_CLOSE_GENERAL_DESC,
	DIALOG_CLOSE_OUTPUT_DESC,
	DIALOG_CLOSE_EXAMPLE,
	DIALOG_CLOSE_APP_CONNECTION,
	DIALOG_CLOSE_DIALOG,
	DIALOG_FILL_EXAMPLE,
	DIALOG_FILL_OUTPUT_DESC,
	DIALOG_FILL_GENERAL_DESC,
	DIALOG_FILL_APP_CONNECTION,
	DIALOG_FILL_DIALOG,
	DIALOG_FILL_DO_NOT_OPEN,
	DIALOG_FILL_DO_NOT_CLOSE,
	DIALOG_FILL_STOP_ON_FAIL,
	DIALOG_FILL_FIELDS,
	DIALOG_SWITCH_TO_WINDOW_GENERAL_DESC,
	DIALOG_SWITCH_TO_WINDOW_EXAMPLE,
	DIALOG_SWITCH_TO_WINDOW_APP_CONNECTION,
	DIALOG_SWITCH_TO_WINDOW_DIALOG,
	DIALOG_SWITCH_TO_WINDOW_FRAME,
	DIALOG_VALIDATE_GENERAL_DESC,
	DIALOG_VALIDATE_ADDITIONAL_DESC,
	DIALOG_VALIDATE_EXAMPLE,
	DIALOG_VALIDATE_APP_CONNECTION,
	DIALOG_VALIDATE_DIALOG,
	DIALOG_VALIDATE_DO_NOT_OPEN,
	DIALOG_VALIDATE_DO_NOT_CLOSE,
	IMAGE_GET_GENERAL_DESC,
	IMAGE_GET_OUTPUT_DESC,
	IMAGE_GET_EXAMPLE,
	IMAGE_GET_APP_CONNECTION,
	IMAGE_GET_DIALOG,
	IMAGE_GET_NAME,
	IMAGE_GET_DESCRIPTION,
	IMAGE_GET_X1,
	IMAGE_GET_Y1,
	IMAGE_GET_X2,
	IMAGE_GET_Y2,
	IMAGE_REPORT_GENERAL_DESC,
	IMAGE_REPORT_OUTPUT_DESC,
	IMAGE_REPORT_EXAMPLE,
	IMAGE_REPORT_AS_LINK,
	IMAGE_REPORT_IMAGE,
	IMAGE_REPORT_TO_REPORT,
	IMAGE_REPORT_BEFORE_TESTCASE,
	IMAGE_REPORT_TITLE,
	IMAGE_SAVE_GENERAL_DESC,
	IMAGE_SAVE_OUTPUT_DESC,
	IMAGE_SAVE_EXAMPLE,
	IMAGE_SAVE_IMAGE,
	IMAGE_SAVE_DIR,
	IMAGE_SAVE_FILE,
	//endregion

	//region actions.matrix
	MATRIX_RUN_GENERAL_DESC,
	MATRIX_RUN_OUTPUT_DESC,
	MATRIX_RUN_EXAMPLE,
	MATRIX_RUN_MATRIX,
	MATRIX_RUN_AT,
	MATRIX_RUN_PARAMETER,
	MATRIX_RUN_FROM_TEXT_GENERAL_DESC,
	MATRIX_RUN_FROM_TEXT_OUTPUT_DESC,
	MATRIX_RUN_FROM_TEXT_EXAMPLE,
	MATRIX_RUN_FROM_TEXT_TEXT,
	MATRIX_RUN_FROM_TEXT_AT,
	MATRIX_RUN_FROM_TEXT_PARAMETER,
	MATRIX_WAIT_GENERAL_DESC,
	MATRIX_WAIT_EXAMPLE,
	MATRIX_WAIT_MATRIX,
	MATRIX_WAIT_TIME,
	//endregion

	//region actions.message
	MESSAGE_CHECK_GENERAL_DESC,
	MESSAGE_CHECK_ADDITIONAL_DESC,
	MESSAGE_CHECK_EXAMPLE,
	MESSAGE_CHECK_ACTUAL,
	MESSAGE_COMPARE_TWO_GENERAL_DESC,
	MESSAGE_COMPARE_TWO_EXAMPLE,
	MESSAGE_COMPARE_TWO_ACTUAL,
	MESSAGE_COMPARE_TWO_EXPECTED,
	MESSAGE_COMPARE_TWO_EXCLUDE,
	MESSAGE_CREATE_GENERAL_DESC,
	MESSAGE_CREATE_OUTPUT_DESC,
	MESSAGE_CREATE_EXAMPLE,
	MESSAGE_CREATE_ADDITIONAL_DESC,
	MESSAGE_CREATE_MESSAGE_TYPE,
	MESSAGE_CREATE_SOURCE,
	MESSAGE_CREATE_FIELDS,
	MESSAGE_REPORT_GENERAL_DESC,
	MESSAGE_REPORT_MESSAGE,
	MESSAGE_REPORT_TITLE,
	MESSAGE_REPORT_TO_REPORT,
	MESSAGE_REPORT_BEFORE_TESTCASE,
	//endregion

	//region actions.report
	CHART_REPORT_GENERAL_DESC,
	CHART_REPORT_ADDITIONAL_DESC,
	CHART_REPORT_EXAMPLE,
	CHART_REPORT_TITLE,
	CHART_REPORT_TABLE,
	CHART_REPORT_CHART_TYPE,
	CHART_REPORT_TO_REPORT,
	CHART_REPORT_COLORS,
	CHART_REPORT_BEFORE_TEST_CASE,
	REPORT_GENERAL_DESC,
	REPORT_ADDITIONAL_DESC,
	REPORT_EXAMPLE,
	REPORT_TO_REPORT,
	REPORT_BEFORE_TEST_CASE,
	REPORT_MESSAGE,
	REPORT_FINISH_EXAMPLE,
	REPORT_FINISH_GENERAL_DESC,
	REPORT_FINISH_REPORT,
	REPORT_FINISH_PASSED,
	REPORT_FINISH_FAILED,
	REPORT_FINISH_START_TIME,
	REPORT_FINISH_FINISH_TIME,
	REPORT_NAME_GENERAL_DESC,
	REPORT_NAME_EXAMPLE,
	REPORT_NAME_NAME,
	REPORT_NAME_FAILED_STEPS,
	REPORT_NAME_PASSED_STEPS,
	REPORT_SHOW_GENERAL_DESC,
	REPORT_SHOW_EXAMPLE,
	REPORT_SHOW_REPORT,
	REPORT_START_GENERAL_DESC,
	REPORT_START_OUTPUT_DESC,
	REPORT_START_EXAMPLE,
	REPORT_START_REPORT_NAME,
	REPORT_START_VERSION,
	//endregion

	//region actions.services
	SERVICE_LOAD_GENERAL_DESC,
	SERVICE_LOAD_OUTPUT_DESC,
	SERVICE_LOAD_EXAMPLE,
	SERVICE_LOAD_ID,
	SERVICE_START_GENERAL_DESC,
	SERVICE_START_ADDITIONAL_DESC,
	SERVICE_START_OUTPUT_DESC,
	SERVICE_START_EXAMPLE,
	SERVICE_START_CONNECTION,
	SERVICE_STOP_GENERAL_DESC,
	SERVICE_STOP_EXAMPLE,
	SERVICE_STOP_CONNECTION,
	//endregion

	//region actions.sql
	SQL_CONNECT_GENERAL_DESC,
	SQL_CONNECT_OUTPUT_DESC,
	SQL_CONNECT_EXAMPLE,
	SQL_CONNECT_SQL,
	SQL_CONNECT_SERVER,
	SQL_CONNECT_BASE,
	SQL_CONNECT_USER,
	SQL_CONNECT_PASSWORD,
	SQL_DISCONNECT_GENERAL_DESC,
	SQL_DISCONNECT_EXAMPLE,
	SQL_DISCONNECT_CONNECTION,
	SQL_EXECUTE_GENERAL_DESC,
	SQL_EXECUTE_ADDITIONAL_DESC,
	SQL_EXECUTE_OUTPUT_DESC,
	SQL_EXECUTE_EXAMPLE,
	SQL_EXECUTE_CONNECTION,
	SQL_EXECUTE_QUERY,
	SQL_INSERT_ADDITIONAL_DESC,
	SQL_INSERT_OUTPUT_DESC,
	SQL_INSERT_EXAMPLE,
	SQL_INSERT_QUERY,
	SQL_INSERT_CONNECTION,
	SQL_INSERT_GENERAL_DESC,
	SQL_SELECT_GENERAL_DESC,
	SQL_SELECT_ADDITIONAL_DESC,
	SQL_SELECT_OUTPUT_DESC,
	SQL_SELECT_EXAMPLE,
	SQL_SELECT_QUERY,
	SQL_SELECT_CONNECTION,
	SQL_TABLE_UPLOAD_EXAMPLE,
	SQL_TABLE_UPLOAD_GENERAL_DESC,
	SQL_TABLE_UPLOAD_CONNECTION,
	SQL_TABLE_UPLOAD_TABLE,
	SQL_TABLE_UPLOAD_DATA,
	//endregion

	//region actions.system
	CHECK_GENERAL_DESC,
	CHECK_OUTPUT_DESC,
	CHECK_ADDITIONAL_DESC,
	CHECK_EXAMPLE,
	CHECK_DONT_FAIL,
	CHECK_ACTUAL,
	COMPARE_GENERAL_DESC,
	COMPARE_OUTPUT_DESC,
	COMPARE_ADDITIONAL_DESC,
	COMPARE_EXAMPLE,
	COMPARE_ACTUAL,
	COMPARE_EXPECTED,
	COMPARE_DONT_FAIL,
	EXECUTE_GENERAL_DESC,
	EXECUTE_OUTPUT_DESC,
	EXECUTE_EXAMPLE,
	EXECUTE_COMMAND,
	EXECUTE_WAIT,
	EXECUTE_WORK_DIR,
	INPUT_GENERAL_DESC,
	INPUT_OUTPUT_DESC,
	INPUT_TITLE,
	INPUT_DEFAULT_VALUE,
	INPUT_HELP_KIND,
	INPUT_DATA_SOURCE,
	INPUT_TIMEOUT,
	PRINT_GENERAL_DESC,
	PRINT_ADDITIONAL_DESC,
	PRINT_EXAMPLE,
	PRINT_MESSAGE,
	RESTORE_GENERAL_DESC,
	RESTORE_OUTPUT_DESC,
	RESTORE_EXAMPLE,
	RESTORE_NAME,
	RESTORE_AS_VAR,
	RESULT_TABLE_GENERAL_DESC,
	RESULT_TABLE_OUTPUT_DESC,
	RESULT_TABLE_EXAMPLE,
	RESULT_TABLE_DECORATED,
	RESULT_TABLE_MATRIX,
	RESULT_TABLE_USER_VALUE_GENERAL_DESC,
	RESULT_TABLE_USER_VALUE_ADDITIONAL_DESC,
	RESULT_TABLE_USER_VALUE_EXAMPLE,
	RESULT_TABLE_USER_VALUE_MATRIX,
	SHOW_GENERAL_DESC,
	SHOW_EXAMPLE,
	SHOW_NOTIFIER,
	SHOW_TEXT,
	STORE_GENERAL_DESC,
	STORE_EXAMPLE,
	STORE_NAME,
	STORE_VALUE,
	WAIT_GENERAL_DESC,
	WAIT_EXAMPLE,
	WAIT_TIMEOUT,
	WAIT_BY_TIME,
	//endregion

	//region actions.text
	TEXT_ADD_LINE_GENERAL_DESC,
	TEXT_ADD_LINE_EXAMPLE,
	TEXT_ADD_LINE_TEXT,
	TEXT_ADD_LINE_LINE,
	TEXT_CREATE_GENERAL_DESC,
	TEXT_CREATE_OUTPUT_DESC,
	TEXT_CREATE_EXMAPLE,
	TEXT_CREATE_CONTENT,
	TEXT_LOAD_FROM_FILE_EXAMPLE,
	TEXT_LOAD_FROM_FILE_GENERAL_DESC,
	TEXT_LOAD_FROM_FILE_OUTPUT_DESC,
	TEXT_LOAD_FROM_FILE_FILE,
	TEXT_PERFORM_GENERAL_DESC,
	TEXT_PERFORM_OUTPUT_DESC,
	TEXT_PERFORM_EXAMPLE,
	TEXT_PERFORM_TEXT,
	TEXT_REPORT_GENERAL_DESC,
	TEXT_REPORT_EXAMPLE,
	TEXT_REPORT_TEXT,
	TEXT_REPORT_TITLE,
	TEXT_REPORT_BEFORE_TESTCASE,
	TEXT_REPORT_TO_REPORT,
	TEXT_SAVE_TO_FILE_GENERAL_DESC,
	TEXT_SAVE_TO_FILE_OUTPUT_DESC,
	TEXT_SAVE_TO_FILE_EXAMPLE,
	TEXT_SAVE_TO_FILE_TEXT,
	TEXT_SAVE_TO_FILE_FILE,
	TEXT_SET_VALUE_GENERAL_DESC,
	TEXT_SET_VALUE_EXAMPLE,
	TEXT_SET_VALUE_TEXT,
	TEXT_SET_VALUE_LINE,
	TEXT_SET_VALUE_INDEX,
	//endregion

	//region actions.xml
	XML_ADD_NODE_EXAMPLE,
	XML_ADD_NODE_GENERAL_DESC,
	XML_ADD_NODE_ADDITIONAL_DESC,
	XML_ADD_NODE_XML,
	XML_ADD_NODE_NODE_NAME,
	XML_ADD_NODE_CONTENT_NAME,
	XML_ADD_NODE_NEW_XML,
	XML_CHILDREN_GENERAL_DESC,
	XML_CHILDREN_OUTPUT_DESC,
	XML_CHILDREN_EXAMPLE,
	XML_CHILDREN_XML,
	XML_COMPARE_GENERAL_DESC,
	XML_COMPARE_OUTPUT_DESC,
	XML_COMPARE_EXAMPLE,
	XML_COMPARE_ACTUAL,
	XML_COMPARE_EXPECTED,
	XML_COMPARE_IGNORE_NODE_ORDER,
	XML_CREATE_GENERAL_DESC,
	XML_CREATE_EXAMPLE,
	XML_FIND_FIRST_GENERAL_DESC,
	XML_FIND_FIRST_OUTPUT_DESC,
	XML_FIND_FIRST_EXAMPLE,
	XML_FIND_FIRST_XML,
	XML_FIND_FIRST_X_PATH,
	XML_FROM_TEXT_GENERAL_DESC,
	XML_FROM_TEXT_OUTPUT_DESC,
	XML_FROM_TEXT_EXAMPLE,
	XML_FROM_TEXT_TEXT,
	XML_LOAD_FROM_FILE_GENERAL_DESC,
	XML_LOAD_FROM_FILE_OUTPUT_DESC,
	XML_LOAD_FROM_FILE_EXAMPLE,
	XML_LOAD_FROM_FILE_FILE,
	XML_REMOVE_GENERAL_DESC,
	XML_REMOVE_EXAMPLE,
	XML_REMOVE_XML,
	XML_REMOVE_XPATH,
	XML_REPORT_GENERAL_DESC,
	XML_REPORT_EXAMPLE,
	XML_REPORT_XML,
	XML_REPORT_TITLE,
	XML_REPORT_TO_REPORT,
	XML_REPORT_BEFORE_TESTCASE,
	XML_SAVE_TO_FILE_EXAMPLE,
	XML_SAVE_TO_FILE_GENERAL_DESC,
	XML_SAVE_TO_FILE_OUTPUT_DESC,
	XML_SAVE_TO_FILE_XML,
	XML_SAVE_TO_FILE_FILE_NAME,
	XML_SELECT_GENERAL_DESC,
	XML_SELECT_OUTPUT_DESC,
	XML_SELECT_EXAMPLE,
	XML_SELECT_FIRST_GENERAL_DESC,
	XML_SELECT_FIRST_OUTPUT_DESC,
	XML_SELECT_FIRST_EXAMPLE,
	XML_SELECT_XML,
	XML_SELECT_NODE_NAME,
	XML_SELECT_X_PATH,
	XML_SELECT_FIRST_X_PATH,
	XML_SELECT_FIRST_XML,
	XML_SELECT_FIRST_NODE_NAME,
	XML_SET_NODE_GENERAL_DESC,
	XML_SET_NODE_ADDITIONAL_DESC,
	XML_SET_NODE_EXAMPLE,
	XML_SET_NODE_TEXT,
	XML_SET_NODE_XML,

	//endregion

	//endregion

	//region tool

	//region tool.wizard
	WIZARD_SUCCESS,
	WIZARD_ERROR_ON_CONFIG_REFRESH,
	WIZARD_ERROR_ON_SHOW_ELEMENT,
	WIZARD_ERROR_ON_DISPLAY_WINDOW,
	WIZARD_ERROR_ON_CREATE_DOCUMENT_2,
	WIZARD_ERROR_ON_LOAD_DOC_1,
	WIZARD_ERROR_ON_CREATE_DOC_1,
	WIZARD_WIZARD,
	WIZARDS,
	WIZARD_NO_ONE_ACCESSIBLE,
	WIZARD_CHOOSE_WIZARD,
	WIZARD_HELP,
	WIZARD_ACCEPT,
	WIZARD_REFUSE,
	//endregion

	//region tool.wizard.all
	WIZARD_START_CONNECTION,
	WIZARD_SCAN,
	WIZARD_CHECK_TABLE,
	WIZARD_SELECT_CONNECTION_INFO,
	WIZARD_SELECT_DIALOG,
	WIZARD_SELECT_STORED_CONN,
	WIZARD_CONNECT,
	WIZARD_STATUS_FAILED,
	WIZARD_STOP_APPLICATION,
	WIZARD_ERROR_ON_APPLICATION_STOP,
	WIZARD_VARIABLE_WITH_NAME,
	WIZARD_STATUS_SUCCESS,
	WIZARD_STATUS_LOADING,
	WIZARD_ERROR_ON_CLOSE_WIZARD,
	WIZARD_TOOLTIP_NAME_OF_VAR,
	WIZARD_LABEL_STATUS,
	WIZARD_LABEL_STORE_AS,
	WIZARD_SELECT_CONVERTER_FROM_COMBOBOX,
	WIZARD_STATUS_SCANNING,
	WIZARD_APP_RESIZE_DESCRIPTION,
	WIZARD_LABEL_DIALOG,
	WIZARD_LABEL_SELFID,
	WIZARD_ERROR_ON_SET_SELF_ID,
	WIZARD_LABEL_ON_OPEN,
	WIZARD_LABEL_ON_CLOSE,
	WIZARD_SELF_ID_NOT_FOUND,
	WIZARD_SELF_CONTROL_IS_EMPTY,
	WIZARD_APPLICATION_NOT_STARTED,
	WIZARD_DICTIONARY_EXCEPTION,
	WIZARD_ERROR_ON_GENERATE_ON_OPEN,
	WIZARD_ERROR_ON_GENERATE_ON_CLOSE,
	WIZARD_NOTHING_TO_UPDATE_INFO,
	WIZARD_UPDATING_ELEMENTS,
	WIZARD_START_UPDATING_ITEMS,
	WIZARD_ERROR_ON_ARRANGE_ONE,
	WIZARD_END_UPDATING,
	WIZARD_ERROR_ON_FIND,
	WIZARD_LABEL_CONNECTION,
	WIZARD_NUMBER,
	WIZARD_ABOUT,
	WIZARD_LESS,
	WIZARD_GREAT,
	WIZARD_BETWEEN,
	WIZARD_LABEL_SELECT_DISTANCE,
	WIZARD_ALL,
	WIZARD_SIGNIFICANT,
	WIZARD_USE_DISTANCE,
	WIZARD_SELECT_FUNCTIONS,
	WIZARD_ALL_OK,
	WIZARD_CHECKING,
	WIZARD_SELECT_MORE_ELEMENTS,
	WIZARD_CREATING_TABLE,
	WIZARD_RELATION,
	WIZARD_CHECK,
	WIZARD_SAVE,
	WIZARD_CANT_SAVE_INVALID_DOSPEC,
	WIZARD_CANT_CHECK_INVALID_DOSPEC,
	WIZARD_NAMESPACES,
	WIZARD_WHERE_TO_MOVE,
	WIZARD_INVOKE_FROM_NAMESPACE,
	WIZARD_MATRIX_CONTAINS_REFERENCES_2,
	WIZARD_NO_CHANGES_NEEDED,
	WIZARD_ERROR_ON_CREATE_COPY,
	WIZARD_OLD_VALUE_1,
	WIZARD_SELECT_NEW_VALUE,
	WIZARD_DICTIONARY_NAME_1,
	WIZARD_DICTIONARY_OK,
	WIZARD_ERROR_ON_TABLE_CREATE,
	WIZARD_NO_FILE_NAME,
	WIZARD_ERROR_IN_FILENAME,
	WIZARD_FILE_NOT_CSV_1,
	WIZARD_FILE_READ_ONLY_1,
	WIZARD_DELIMITER_INCORRECT,
	WIZARD_ENTER_XPATH,
	WIZARD_HELPER,
	WIZARD_RELATIVE,
	WIZARD_USE_TEXT,
	//endregion

	//region tool.custom.helper
	FORMULA_INTERPRETER,
	PARAMETERS_NAME,
	ALL_VARIABLES_FOR_CURRENT_MATRIX,
	//endregion

	//region tool.dictionary.actions
	SEND_KEYS,
	//endregion
	//endregion

	//region plugins
	PLUGIN_COMMON_DESCRIPTION,

	//region plugins.app.web
	WEB_PLUGIN_DESCRIPTION,
	WEB_PLUGIN_DIFFERENCE,
	WEB_PLUGIN_LOG_LEVEL,
	WEB_PLUGIN_JRE_EXEC,
	WEB_PLUGIN_JRE_ARGS,
	WEB_PLUGIN_CHROME_DRIVER,
	WEB_PLUGIN_GECKO_DRIVER,
	WEB_PLUGIN_IE_DRIVER,
	WEB_PLUGIN_CHROME_DRIVER_BINARY,
	WEB_PLUGIN_FIREFOX_PROFILE,
	WEB_PLUGIN_PRIVATE_MODE,
	WEB_PLUGIN_BROWSER,
	WEB_PLUGIN_URL,
	WEB_PLUGIN_WHERE_OPEN,
	WEB_PLUGIN_PROPERTY_URL,
	WEB_PLUGIN_PROPERTY_TITLE,
	WEB_PLUGIN_PROPERTY_ALL_TITLES,
	WEB_PLUGIN_PROPERTY_COOKIE,
	WEB_PLUGIN_PROPERTY_ALL_COOKIES,
	WEB_PLUGIN_PROPERTY_ADD_COOKIE,
	WEB_PLUGIN_PROPERTY_REMOVE_COOKIE,
	WEB_PLUGIN_PROPERTY_REMOVE_ALL_COOKIES,
	WEB_PLUGIN_PROPERTY_TAB,
	//endregion

	//region plugins.app.win
	WIN_PLUGIN_DESCRIPTION,
	WIN_PLUGIN_DIFFERENCE,
	WIN_PLUGIN_LOG_LEVEL,
	WIN_PLUGIN_JRE_EXEC,
	WIN_PLUGIN_JRE_ARGS,
	WIN_PLUGIN_MAX_TIMEOUT,
	WIN_PLUGIN_ALWAYS_TO_FRONT,
	WIN_PLUGIN_MAIN_WINDOW,
	WIN_PLUGIN_HEIGHT,
	WIN_PLUGIN_WIDTH,
	WIN_PLUGIN_PID,
	WIN_PLUGIN_CONTROL_KIND,
	WIN_PLUGIN_TIMEOUT,
	WIN_PLUGIN_EXEC,
	WIN_PLUGIN_WORK_DIR,
	WIN_PLUGIN_ARGS,
	WIN_PLUGIN_RECTANGLE,
	WIN_PLUGIN_TITLE,
	//endregion

	//region plugins.app.swing
	SWING_PLUGIN_DESCRIPTION,
	SWING_PLUGIN_DIFFERENCE,
	SWING_PLUGIN_LOG_LEVEL,
	SWING_PLUGIN_JRE_EXEC,
	SWING_PLUGIN_JRE_ARGS,
	SWING_PLUGIN_MAIN_CLASS,
	SWING_PLUGIN_JAR,
	SWING_PLUGIN_ARGS,
	SWING_PLUGIN_URL,
	SWING_PLUGIN_TITLE,
	//endregion

	//region plugins.app.swing
	JAVAFX_PLUGIN_DESCRIPTION,
	JAVAFX_PLUGIN_DIFFERENCE,
	JAVAFX_PLUGIN_LOG_LEVEL,
	JAVAFX_PLUGIN_JRE_EXEC,
	JAVAFX_PLUGIN_JRE_ARGS,
	JAVAFX_PLUGIN_MAIN_CLASS,
	JAVAFX_PLUGIN_JAR,
	JAVAFX_PLUGIN_ARGS,
	JAVAFX_PLUGIN_URL,
	JAVAFX_PLUGIN_TITLE,
	//endregion

	//endregion

	//region common.documentation
	SUPPORTED_CONTROLS,
	DIFFERENCES,
	PARAMETER,
	DESCRIPTION,
	EXAMPLE,
	//endregion

	//region api.common
	PARAMETERS_KIND_LOAD,
	PARAMETERS_KIND_START,
	PARAMETERS_KIND_CONNECT,
	PARAMETERS_KIND_STOP,
	PARAMETERS_KIND_GET_PROPERTY,
	PARAMETERS_KIND_SET_PROPERTY,
	PARAMETERS_KIND_ENCODE,
	PARAMETERS_KIND_PROPS,
	PARAMETERS_KIND_NEW_INSTANCE,;
	//endregion


	public static final String RESOURCE_BUNDLE_PATH = "com/exactprosystems/jf/ToolResourceBundle";
	private static final ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_PATH, new UTF8Control());


	/**
	 * use this method for I18n
	 *
	 * @return string by default locales
	 */
	public static String get(R r) {
		return bundle.getString(r.name());
	}

	public String get() {
		return bundle.getString(this.name());
	}

	@Override
	public String toString() {
		return bundle.getString(this.name());
	}


	private static class UTF8Control extends ResourceBundle.Control {
		@Override
		public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
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
}
