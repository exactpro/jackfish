package com.exactprosystems.jf.app;

import com.sun.jna.Library;

public interface JnaDriver extends Library {
	//region util methods
	String lastError();
	String methodTime();
	String uiAutomationTime();
	String getFrameworkId();
	void maxTimeout(int timeout);
	//endregion

	//region application methods
	int connect(String title, int height, int width, int pid, int controlKind);
	int run(String exec, String workDir, String param);
	void stop();
	void refresh();
	String title();
	//endregion

	//region find methods
	String listAll(String ownerId, int controlKindId, String uid, String xpath, String clazz, String name, String title, String text, boolean many);
	int findAllForLocator(int[] arr, int len, String ownerId, int controlKindId, String uid, String xpath, String clazz, String name, String title, String text, boolean many);
	int findAll(int[] arr, int len, String elementId, int scopeId, int propertyId, String value);
	int elementByCoords(int[] resultId, int length, int controlKindId, int x, int y);
	//endregion

	String elementAttribute(String elementId, int partId);
	void sendKey(String key);
	void mouse(String elementId, int actionId, int x, int y);

	/**
	 * if @param c == -1 -> arg is null;
	 * if @param c == 0 -> arg is array of string with separator %
	 * if @param c == 1 -> arg is array of int with separator %
	 * if @param c == 2 -> arg is array of double with separator %
	 */
	String doPatternCall(String elementId, int patternId, String method, String arg, int c);
	void setText(String elementId, String text);
	String getProperty(String elementId, int propertyId);
	int getPatterns(int[] arr, int len, String elementId);
	int getImage(int[] arr, int len, String id);

	void clearCache();

	//region table methods
	String getValueTableCell(String elementId, int column, int row);
	void mouseTableCell(String elementId, int column, int row, int mouseAction);
	void textTableCell(String elementId, int column, int row, String text);
	String getRowByCondition(String tableId, boolean useNumericHeader, String condition);
	String getRowIndexes(String tableId, boolean useNumericHeader, String condition);
	String getRowByIndex(String tableId, boolean useNumericHeader, int index);
	String getTable(String tableId, boolean useNumericHeader);
	int getTableSize(String tableId);
	//endregion

}