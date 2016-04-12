package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.MouseAction;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.rmi.RemoteException;
import java.util.Arrays;


/*
	TODO  we need do all operations inside this class.
	for example recall some methods, if returned length > initial length
*/
public class JnaDriverImpl
{
	private final Logger logger;
	private final static String dllDir = "bin/UIAdapter.dll";

    public static void main(String[] args) throws Exception {
	    JnaDriverImpl driver = new JnaDriverImpl(Logger.getLogger(JnaDriverImpl.class));
	    driver.connect("Form1");
	    System.out.println(driver.title());
	    int l = 100 * 100;
	    int a[] = new int[l];
	    String id = "42,4458408";
	    System.out.println(driver.getProperty(new UIProxyJNA(new int[]{42,4458408}), WindowProperty.NameProperty));
    }

	public JnaDriverImpl(Logger logger) throws Exception
	{
		this.logger = logger;
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
		long start = System.currentTimeMillis();
		this.driver.connect(title);
		this.logger.info(String.format("connect(%s), time (ms) : %d", title, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
	}

	public void run(String exec, String workDir, String param) throws Exception
	{
		long start = System.currentTimeMillis();
		this.driver.run(exec, workDir, param);
		this.logger.info(String.format("start(%s,%s,%s), time (ms) : %d", exec, workDir, param, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
	}

	public void stop() throws Exception
	{
		long start = System.currentTimeMillis();
		this.driver.stop();
		this.logger.info(String.format("stop(), time (ms) : %d", System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
	}

	public void refresh() throws Exception
	{
		long start = System.currentTimeMillis();
		this.driver.refresh();
		this.logger.info(String.format("refresh(), time (ms) : %d", System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
	}

	public String title() throws Exception
	{
		long start = System.currentTimeMillis();
		String title = this.driver.title();
		this.logger.info(String.format("title() = %s, time (ms) : %d",title, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return title;
	}

	public String listAll(UIProxyJNA owner, ControlKind kind, String uid, String xpath, String clazz, String name, String title, String text) throws Exception
	{
		long start = System.currentTimeMillis();
		String result = this.driver.listAll(owner.getIdString(), kind.ordinal(), uid, xpath, clazz, name, title, text);
		this.logger.info(String.format("listAll(%s,%s,%s,%s,%s,%s,%s,%s), time (ms) : %d", owner, kind, uid, xpath, clazz, name, title, text, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public String elementAttribute(UIProxyJNA element, AttributeKind kind) throws Exception
	{
		long start = System.currentTimeMillis();
		String result = this.driver.elementAttribute(element.getIdString(), kind.ordinal());
		this.logger.info(String.format("elementAttribute(%s,%s) = %s, time (ms) : %d", element, kind, result, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public int elementByCoords(int[] resultId, ControlKind kind, int x, int y) throws Exception
	{
		long start = System.currentTimeMillis();
		int result = this.driver.elementByCoords(resultId, resultId.length, kind.ordinal(), x, y);
		this.logger.info(String.format("elementByCoords(%s,%s,%d,%d) = %d, time (ms) : %d", Arrays.toString(resultId), kind, x, y, result, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public void sendKeys(String key) throws Exception
	{
		long start = System.currentTimeMillis();
		this.driver.sendKey(key);
		this.logger.info(String.format("key(%s), time (ms) : %d", key, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
	}

	public void mouse(UIProxyJNA element, MouseAction action, int x, int y) throws Exception
	{
		long start = System.currentTimeMillis();
		this.driver.mouse(element.getIdString(), action.getId(), x, y);
		this.logger.info(String.format("mouse(%s,%s,%d,%d), time (ms) : %d", element, action, x, y, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
	}

	public int findAllForLocator(int[] arr, UIProxyJNA owner, ControlKind kind, String uid, String xpath, String clazz, String name, String title, String text) throws Exception
	{
		long start = System.currentTimeMillis();
		int result = this.driver.findAllForLocator(arr, arr.length, owner.getIdString(), kind.ordinal(), uid, xpath, clazz, name, title, text);
		this.logger.info(String.format("findAllForLocator(%s,%d,%s,%s,%s,%s,%s,%s,%s,%s) = %d, time (ms) : %d", Arrays.toString(arr), arr.length, owner, kind, uid, xpath, clazz, name, title, text,result, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public int findAll(int[] arr, UIProxyJNA owner, WindowTreeScope scope, WindowProperty property, String value) throws Exception
	{
		long start = System.currentTimeMillis();
		int result = this.driver.findAll(arr, arr.length, owner.getIdString(), scope.getValue(), property.getId(), value);
		this.logger.info(String.format("findAll(%s,%d,%s,%s,%s,%s) = %s, time (ms) : %d", Arrays.toString(arr), arr.length, owner, scope, property, value, result, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	/**
	 * if @param c == -1 -> arg is null;<br>
	 * if @param c == 0 -> arg is array of string with separator %<br>
	 * if @param c == 1 -> arg is array of int with separator %<br>
	 * if @param c == 2 -> arg is array of double with separator %<br>
	 */
	public String doPatternCall(UIProxyJNA element, WindowPattern pattern, String method, String args, int c) throws Exception
	{
		long start = System.currentTimeMillis();
		String result = this.driver.doPatternCall(element.getIdString(), pattern.getId(), method, args, c);
		this.logger.info(String.format("doPatternCall(%s,%s,%s,%s,%d) = %s, time (ms) : %d", element, pattern, method, args, c, result, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public String getProperty(UIProxyJNA element, WindowProperty property) throws Exception
	{
		long start = System.currentTimeMillis();
		String result = this.driver.getProperty(element.getIdString(), property.getId());
		this.logger.info(String.format("getProperty(%s,%s) = %s, time (ms) : %d", element, property, result, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public int getPatterns(int[] arr, UIProxyJNA element) throws Exception
	{
		long start = System.currentTimeMillis();
		int result = this.driver.getPatterns(arr, arr.length, element.getIdString());
		this.logger.info(String.format("getPatterns(%s,%s,%s) = %d, time (ms) : %d", Arrays.toString(arr), arr.length, element, result, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public int getImage(int[] arr, UIProxyJNA element) throws Exception
	{
		long start = System.currentTimeMillis();
		int result = this.driver.getImage(arr, arr.length, element.getIdString());
		this.logger.info(String.format("getImage(%s,%d,%s) = %s, time (ms) : %d", Arrays.toString(arr), arr.length, element.getIdString(), result, System.currentTimeMillis() - start));
		checkCSharpTimes();
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

	private void checkCSharpTimes()
	{
		String methodTime = this.driver.methodTime();
		if (methodTime != null)
		{
			this.logger.info("method time {" + methodTime + "}");
		}
		String uiAutomationTIme = this.driver.uiAutomationTime();
		if (uiAutomationTIme != null)
		{
			this.logger.info("uiAutomation time : {" + uiAutomationTIme + "}");
		}
	}

	private JnaDriver driver;

}
