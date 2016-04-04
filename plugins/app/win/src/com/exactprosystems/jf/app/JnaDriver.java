package com.exactprosystems.jf.app;

import com.sun.jna.Library;

public interface JnaDriver extends Library
{
	String lastError();

	void connect(String title);
    void run(String exec, String workDir, String param);
    void stop();
	void refresh();
	String title();
	String listAll(int[] ownerId, int controlKindId, String uid, String xpath, String clazz, String name, String title, String text);
	String elementAttribute(int[] id, int partId);
	int elementByCoords(int[] resultId, int controlKindId, int x, int y);
	void sendKeys(String key);
	void mouse(int[] id, int actionId, int x, int y);
	int findAllForLocator(int[] arr, int len, int[] ownerId, int controlKindId, String uid, String xpath, String clazz, String name, String title, String text);
	int findAll(int[] arr, int len, int[] id, int scopeId, long propertyId, Object value);
	String doPatternCall(int[] id, int patternId, String method, Object[] args);
	String getProperty(int[] id, int propertyId);
	int getPatterns(int[] arr, int len, int[] id);
	int getImage(int[] arr, int len, int[] id);
}