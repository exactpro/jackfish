package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.ControlKind;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.util.Arrays;

public class JnaDriverImpl
{
	private final static String dllDir = "bin/UIAdapter.dll";

    public static void main(String[] args) throws Exception {
	    JnaDriverImpl driver = new JnaDriverImpl();
	    driver.connect("Calc");
	    System.out.println(driver.title());
	    int l = 100 * 100;
	    int a[] = new int[l];
	    String id = "42,4458408";
	    System.out.println(driver.getProperty(id, WindowProperty.NameProperty.getId()));
    }

	public JnaDriverImpl() throws Exception
	{
		if (Platform.is64Bit())
		{}
		Path path = Paths.get("tempFile.dll");
		try (InputStream in = getClass().getResourceAsStream(dllDir)) {
			Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			throw new RemoteException(e.getMessage(), e);
		}
		String dll = path.toString();
		// TODO if i committed this string, please remove it, because this string only for debuging onyl on my computer
		// dll = "C:\\jackfish\\AppWinGui\\src\\com\\exactprosystems\\jf\\app\\bin\\UIAdapter.dll";
		System.out.println("dll path : " + dll);
		if (new File(dll).exists())
		{
			this.driver = (JnaDriver) Native.loadLibrary(dll, JnaDriver.class);
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

	public String listAll(String ownerId, int controlKindId, String uid, String xpath, String clazz, String name, String title, String text) throws Exception
	{
		String result = this.driver.listAll(ownerId, controlKindId, uid, xpath, clazz, name, title, text);
		checkError();
		return result;
	}

	public String elementAttribute(String elementId, int partId) throws Exception
	{
		String result = this.driver.elementAttribute(elementId, partId);
		checkError();
		return result;
	}

	public int elementByCoords(int[] resultId, int length, int controlKindId, int x, int y) throws Exception
	{
		System.out.println("result id : " + Arrays.toString(resultId));
		System.out.println("length : " + length);
		System.out.println("x : " + x + " y : " + y);
		int result = this.driver.elementByCoords(resultId, length, controlKindId, x, y);
		checkError();
		return result;
	}

	public void sendKeys(String key) throws Exception
	{
		this.driver.sendKey(key);
		checkError();
	}

	public void mouse(String elementId, int actionId, int x, int y) throws Exception
	{
		this.driver.mouse(elementId, actionId, x, y);
		checkError();
	}

	public int findAllForLocator(int[] arr, int len, String ownerId, int controlKindId, String uid, String xpath, String clazz, String name, String title, String text) throws Exception
	{
		int result = this.driver.findAllForLocator(arr, len, ownerId, controlKindId, uid, xpath, clazz, name, title, text);
		checkError();
		return result;
	}

	public int findAll(int[] arr, int len, String elementId, int scopeId, int propertyId, String value) throws Exception
	{
		int result = this.driver.findAll(arr, len, elementId, scopeId, propertyId, value);
		checkError();
		return result;
	}

	public String doPatternCall(String elementId, int patternId, String method, String args, int c) throws Exception
	{
		String res = this.driver.doPatternCall(elementId, patternId, method, args, c);
		checkError();
		return res;
	}

	public String getProperty(String elementId, int propertyId) throws Exception
	{
		String result = this.driver.getProperty(elementId, propertyId);
		checkError();
		return result;
	}

	public int getPatterns(int[] arr, int len, String elementId) throws Exception
	{
		int result = this.driver.getPatterns(arr, len, elementId);
		checkError();
		return result;
	}

	public int getImage(int[] arr, int len, String id) throws Exception
	{
        System.out.println("arr : " + Arrays.toString(arr));
        System.out.println("len : " +  len);
        System.out.println("id : " + id);
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
