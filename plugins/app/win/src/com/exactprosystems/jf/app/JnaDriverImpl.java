package com.exactprosystems.jf.app;

import com.sun.jna.Native;
import com.sun.jna.Platform;

import java.io.File;

public class JnaDriverImpl
{
	//TODO path need be relative
	private static final String dllDir = "C:\\workspaces\\cs\\UIAdapter\\UIAdapter\\bin\\x64\\Release\\UIAdapter.dll";

	public JnaDriverImpl() throws Exception
	{
		if (Platform.is64Bit())
		{}
		
		if (new File(dllDir).exists())
		{
			this.driver = (JnaDriver) Native.loadLibrary(dllDir, JnaDriver.class);
		}
		else
		{
			throw new Exception("Dll is  not found");
		}
	}

	public void connect(String title) throws Exception
	{
		this.driver.connect(title);
		checkError();
	}
	
	public void run(String exec, String workDir, String param) throws Exception
	{
		this.driver.run(exec, workDir, param);
		checkError();
	}

	public void stop() throws Exception
	{
		this.driver.stop();
		checkError();
	}

	public void refresh() throws Exception
	{
		this.driver.refresh();
		checkError();
	}

	public String title() throws Exception
	{
		String title = this.driver.title();
		checkError();
		return title;
	}

	public String listAll(int[] ownerId, int controlKindId, String uid, String xpath, String clazz, String name, String title, String text) throws Exception
	{
		String result = this.driver.listAll(ownerId, controlKindId, uid, xpath, clazz, name, title, text);
		checkError();
		return result;
	}

	public String elementAttribute(int[] id, int partId) throws Exception
	{
		String result = this.driver.elementAttribute(id, partId);
		checkError();
		return result;
	}

	public int elementByCoords(int[] resultId, int controlKindId, int x, int y) throws Exception
	{
		int result = this.driver.elementByCoords(resultId, controlKindId, x, y);
		checkError();
		return result;
	}

	public void sendKeys(String key) throws Exception
	{
		this.driver.sendKeys(key);
		checkError();
	}

	public void mouse(int[] id, int actionId, int x, int y) throws Exception
	{
		this.driver.mouse(id, actionId, x, y);
		checkError();
	}

	public int findAllForLocator(int[] arr, int len, int[] ownerId, int controlKindId, String uid, String xpath, String clazz, String name, String title, String text) throws Exception
	{
		int result = this.driver.findAllForLocator(arr, len, ownerId, controlKindId, uid, xpath, clazz, name, title, text);
		checkError();
		return result;
	}

	public int findAll(int[] arr, int len, int[] id, int scopeId, long propertyId, Object value) throws Exception
	{
		int result = this.driver.findAll(arr, len, id, scopeId, propertyId, value);
		checkError();
		return result;
	}

	public String doPatternCall(int[] id, int patternId, String method, Object[] args) throws Exception
	{
		String res = this.driver.doPatternCall(id, patternId, method, args);
		checkError();
		return res;
	}

	public String getProperty(int[] id, int propertyId) throws Exception
	{
		String result = this.driver.getProperty(id, propertyId);
		checkError();
		return result;
	}

	public int getPatterns(int[] arr, int len, int[] id) throws Exception
	{
		int result = this.driver.getPatterns(arr, len, id);
		checkError();
		return result;
	}

	public int getImage(int[] arr, int len, int[] id) throws Exception
	{
		int result = this.driver.getImage(arr, len, id);
		checkError();
		return result;
	}

	private void checkError() throws Exception
	{
		String error = this.driver.lastError();
		if (error != null)
		{
			throw new Exception(error);
		}
	}

	private JnaDriver driver;

}
