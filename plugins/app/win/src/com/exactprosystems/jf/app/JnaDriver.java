package com.exactprosystems.jf.app;

import com.sun.jna.Library;

public interface JnaDriver extends Library {
	String lastError();

	void connect(String title);
	void run(String exec, String workDir, String param);
	void stop();
	void refresh();
	String title();
	String listAll(String ownerId, int controlKindId, String uid, String xpath, String clazz, String name, String title, String text);
	String elementAttribute(String elementId, int partId);
	int elementByCoords(int[] resultId, int controlKindId, int x, int y);
	void sendKeys(String key);
	void mouse(String elementId, int actionId, int x, int y);
	int findAllForLocator(int[] arr, int len, String ownerId, int controlKindId, String uid, String xpath, String clazz, String name, String title, String text);
	int findAll(int[] arr, int len, String elementId, int scopeId, long propertyId, Object value);
	String doPatternCall(String elementId, int patternId, String method, Object[] args);
	String getProperty(String elementId, int propertyId);
	int getPatterns(int[] arr, int len, String elementId);
	int getImage(int[] arr, int len, String id);
}