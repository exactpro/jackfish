package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.MouseAction;
import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.api.error.app.*;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.*;
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
	private final static String pdbDir = "bin/UIAdapter.pdb";

	public static void main(String[] args) throws Exception
	{
		JnaDriverImpl driver = new JnaDriverImpl(Logger.getLogger(JnaDriverImpl.class));
		driver.connect("", 351, 425, Integer.MIN_VALUE, ControlKind.Panel, 5000);
		System.out.println("title : " + driver.title());
		int l = 100 * 100;
		int a[] = new int[l];
		String id = "42,4458408";
		System.out.println(driver.getProperty(new UIProxyJNA(new int[]{42, 4458408}), WindowProperty.NameProperty));
	}

	public JnaDriverImpl(Logger logger) throws Exception
	{
		this.logger = logger;
		if (Platform.is64Bit())
		{
		}
		Path pathDll = Paths.get("UIAdapter.dll");
		Path pathPdb = Paths.get("UIAdapter.pdb");
		try (
				InputStream inDll = getClass().getResourceAsStream(dllDir);
				InputStream inPdb = getClass().getResourceAsStream(pdbDir)
		)
		{
			Files.copy(inDll, pathDll, StandardCopyOption.REPLACE_EXISTING);
			Files.copy(inPdb, pathPdb, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (AccessDeniedException ex)
		{
			//nothing
		}
		catch (Exception e)
		{
			throw new ProxyException(e.getMessage(), "internal error", e);
		}
		String dll = pathDll.toString();

		if (new File(dll).exists())
		{
			this.driver = (JnaDriver) Native.loadLibrary(dll, JnaDriver.class);
			//this.driver = (JnaDriver) Native.loadLibrary("C:\\jackfish\\AppWinGui\\src\\com\\exactprosystems\\jf\\app\\bin\\UIAdapter.dll", JnaDriver.class);
		}
		else
		{
			throw new InternalErrorException("Dll is  not found");
		}
	}

	//region utils methods
	public Framework getFrameworkId() throws Exception
	{
		long start = System.currentTimeMillis();
		String frameworkId = this.driver.getFrameworkId();
		this.logger.info(String.format("getFrameworkId() = %s, time (ms) : %d", frameworkId, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return Framework.byId(frameworkId);
	}

	public void maxTimeout(int timeout) throws Exception
	{
		long start = System.currentTimeMillis();
		this.driver.maxTimeout(timeout);
		this.logger.info(String.format("maxTimeout(%d), time (ms) : %d", timeout, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
	}

	public void createLogger(String logLevel) throws Exception
	{
		long start = System.currentTimeMillis();
		this.driver.createLogger(logLevel);
		this.logger.info(String.format("createLogLevel(%s), time : (ms) : %d", logLevel, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
	}
	//endregion

	//region application methods
	public int connect(String title, int height, int width, int pid, ControlKind controlKind, int timeout) throws Exception
	{
		long start = System.currentTimeMillis();
		int ret = this.driver.connect(title, height, width, pid, controlKind == null ? Integer.MIN_VALUE : controlKind.ordinal(), timeout);
		this.logger.info(String.format("connect(%s), time (ms) : %d", title, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		
		return ret;
	}

	public int run(String exec, String workDir, String param) throws Exception
	{
		long start = System.currentTimeMillis();
		int ret = this.driver.run(exec, workDir, param);
		this.logger.info(String.format("start(%s,%s,%s), time (ms) : %d", exec, workDir, param, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		
		return ret;
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
		this.logger.info(String.format("title() = %s, time (ms) : %d", title, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return title;
	}

	//endregion

	//region find methods

	public String listAll(UIProxyJNA owner, ControlKind kind, String uid, String xpath, String clazz, String name, String title, String text, boolean many) throws Exception
	{
		long start = System.currentTimeMillis();
		String result = this.driver.listAll(owner.getIdString(), kind.ordinal(), uid, xpath, clazz, name, title, text, many);
		this.logger.info(String.format("listAll(%s,%s,%s,%s,%s,%s,%s,%s,%b), time (ms) : %d", owner, kind, uid, xpath, clazz, name, title, text, many, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public int findAllForLocator(int[] arr, UIProxyJNA owner, ControlKind kind, String uid, String xpath, String clazz, String name, String title, String text, boolean many) throws Exception
	{
		long start = System.currentTimeMillis();
		int result = this.driver.findAllForLocator(arr, arr.length, owner.getIdString(), kind.ordinal(), uid, xpath, clazz, name, title, text, many);
		this.logger.info(String.format("findAllForLocator(%s,%d,%s,%s,%s,%s,%s,%s,%s,%s,%b) = %d, time (ms) : %d", Arrays.toString(arr), arr.length, owner, kind, uid, xpath, clazz, name, title, text, many, result, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public int findAll(int[] arr, UIProxyJNA owner, WindowTreeScope scope, WindowProperty property, String value) throws Exception
	{
		long start = System.currentTimeMillis();
		int result = this.driver.findAll(arr, arr.length, owner.getIdString(), scope.getValue(), property.getId(), value);
		this.logger.info(String.format("findAll(%s,%d,%s,%s,%s,%s) = %s, time (ms) : %d", Arrays.toString(arr), arr.length, owner, scope, property, value, result, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public int elementByCoords(int[] resultId, ControlKind kind, int x, int y) throws Exception
	{
		long start = System.currentTimeMillis();
		int result = this.driver.elementByCoords(resultId, resultId.length, kind.ordinal(), x, y);
		this.logger.info(String.format("elementByCoords(%s,%s,%d,%d) = %d, time (ms) : %d", Arrays.toString(resultId), kind, x, y, result, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	//endregion

	public String elementAttribute(UIProxyJNA element, AttributeKind kind) throws Exception
	{
		long start = System.currentTimeMillis();
		String result = this.driver.elementAttribute(element.getIdString(), kind.ordinal());
		this.logger.info(String.format("elementAttribute(%s,%s) = %s, time (ms) : %d", element, kind, result, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public void sendKeys(String key) throws Exception
	{
		long start = System.currentTimeMillis();
		this.driver.sendKey(key);
		this.logger.info(String.format("key(%s), time (ms) : %d", key, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
	}

	public void upAndDown(String key, boolean isDown) throws Exception
	{
		long start = System.currentTimeMillis();
		this.driver.upAndDown(key, isDown);
		this.logger.info(String.format("upAndDown (%s, %b), time (ms) : %d", key, isDown, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
	}

	public void mouse(UIProxyJNA element, MouseAction action, int x, int y) throws Exception
	{
		long start = System.currentTimeMillis();
		this.driver.mouse(element.getIdString(), action.getId(), x, y);
		this.logger.info(String.format("mouse(%s,%s,%d,%d), time (ms) : %d", element, action, x, y, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
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
		this.logger.info(String.format("doPatternCall(%s,%s,%s,%s,%d) = %s, time (ms) : %d", element, pattern, method, args, c, result, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public void setText(UIProxyJNA element, String text) throws Exception
	{
		long start = System.currentTimeMillis();
		this.driver.setText(element.getIdString(), text);
		this.logger.info(String.format("setText(%s,%s)", element, text));
		checkCSharpTimes();
		checkError();
	}

	public String getProperty(UIProxyJNA element, WindowProperty property) throws Exception
	{
		long start = System.currentTimeMillis();
		String result = this.driver.getProperty(element.getIdString(), property.getId());
		this.logger.info(String.format("getProperty(%s,%s) = %s, time (ms) : %d", element, property, result, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public int getPatterns(int[] arr, UIProxyJNA element) throws Exception
	{
		long start = System.currentTimeMillis();
		int result = this.driver.getPatterns(arr, arr.length, element.getIdString());
		this.logger.info(String.format("getPatterns(%s,%s,%s) = %d, time (ms) : %d", Arrays.toString(arr), arr.length, element, result, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public int getImage(int[] arr, UIProxyJNA element) throws Exception
	{
		long start = System.currentTimeMillis();
		int result = this.driver.getImage(arr, arr.length, element.getIdString());
		this.logger.info(String.format("getImage(%d,%s) = %s, time (ms) : %d", arr.length, element.getIdString(), result,
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public void clearCache() throws Exception
	{
		long start = System.currentTimeMillis();
		this.driver.clearCache();
		this.logger.info(String.format("clearCache, time (ms) : %d", 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
	}

	//region table methods
	public String getValueTableCell(UIProxyJNA table, int column, int row) throws Exception
	{
		long start = System.currentTimeMillis();
		String result = this.driver.getValueTableCell(table.getIdString(), column, row);
		this.logger.info(String.format("getValueTableCell(%s,%d,%d) time(ms) : %d", table, column, row, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public void mouseTableCell(UIProxyJNA table, int column, int row, MouseAction mouseAction) throws Exception
	{
		long start = System.currentTimeMillis();
		this.driver.mouseTableCell(table.getIdString(), column, row, mouseAction.getId());
		this.logger.info(String.format("mouseTableCell(%s,%d,%d, %s) time(ms) : %d", table, column, row, mouseAction, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
	}

	public void textTableCell(UIProxyJNA table, int column, int row, String text) throws Exception
	{
		long start = System.currentTimeMillis();
		this.driver.textTableCell(table.getIdString(), column, row, text);
		this.logger.info(String.format("textTableCell(%s,%d,%d, %s) time(ms) : %d", table, column, row, text, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
	}

	public String getRowByConditions(UIProxyJNA table, boolean useNumericHeader, Condition condition, String columns) throws Exception
	{
		long start = System.currentTimeMillis();
		String stringCondition = condition.serialize();
		String res = this.driver.getRowByCondition(table.getIdString(), useNumericHeader, stringCondition, columns);
		this.logger.info(String.format("getRowByConditions(%s,%b,%s) : %s, time(ms) : %d", table, useNumericHeader, stringCondition, res, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return res;
	}

	public String getList(UIProxyJNA element) throws Exception
	{
		long start = System.currentTimeMillis();
		String result = this.driver.getList(element.getIdString());
		this.logger.info(String.format("getList(%s) = %s, time (ms) : %d", element, result, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return result;
	}

	public String getRowIndexes(UIProxyJNA table, boolean useNumericHeader, ICondition condition, String columns) throws Exception
	{
		long start = System.currentTimeMillis();
		String stringCondition = condition.serialize();
		String res = this.driver.getRowIndexes(table.getIdString(), useNumericHeader, stringCondition, columns);
		this.logger.info(String.format("getRowIndexes(%s,%b,%s) : %s, time(ms) : %d", table, useNumericHeader, stringCondition, res, 
				System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return res;
	}

	public String getRowByIndex(UIProxyJNA table, boolean useNumericHeader, int index) throws Exception
	{
		long start = System.currentTimeMillis();
		String res = this.driver.getRowByIndex(table.getIdString(), useNumericHeader, index);
		this.logger.info(String.format("getRowByIndex(%s,%b,%s) : %s, time(ms) : %d", table, useNumericHeader, index, res, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return res;
	}

	public String getTable(UIProxyJNA table, boolean useNumericHeader) throws Exception
	{
		long start = System.currentTimeMillis();
		String res = this.driver.getTable(table.getIdString(), useNumericHeader);
		this.logger.info(String.format("getTable(%s,%b) : %s, time(ms) : %d", table, useNumericHeader, res, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return res;
	}

	public int getTableSize(UIProxyJNA table) throws Exception
	{
		long start = System.currentTimeMillis();
		int res = this.driver.getTableSize(table.getIdString());
		this.logger.info(String.format("getTableSize(%s) : %d, time(ms) : %d", table, res, System.currentTimeMillis() - start));
		checkCSharpTimes();
		checkError();
		return res;
	}

//	private String conditionToString(StringCondition condition)
//	{
//		return condition == null ? "" : condition.getName() + "," + condition.getValue() + "," + condition.isIgnoreCase();
//	}
//
	private void checkError() throws RemoteException
	{
		String error = this.driver.lastError();
		int errorNumber = this.driver.lastErrorNumber();
		if (error != null)
		{
			switch (errorNumber)
			{
				case 0: throw new FeatureNotSupportedException(error);
				case 1: throw new NullParameterException(error);
				case 2: throw new WrongParameterException(error);
				case 3: throw new OperationNotAllowedException(error);
				case 4: throw new ElementNotFoundException(error);
				case 5: throw new TooManyElementsException(error);
				case 6: throw new InternalErrorException(error);
				case 7:
					throw new TimeoutException(error);
			}
		}
	}
	
	private void checkCSharpTimes()
	{
		String methodTime = this.driver.methodTime();
		if (methodTime != null)
		{
			//this.logger.info("method time {" + methodTime + "}");
		}
		String uiAutomationTIme = this.driver.uiAutomationTime();
		if (uiAutomationTIme != null)
		{
			//this.logger.info("uiAutomation time : {" + uiAutomationTIme + "}");
		}
	}
	//endregion

	private JnaDriver driver;

}
