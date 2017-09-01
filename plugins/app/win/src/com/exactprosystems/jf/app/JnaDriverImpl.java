package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.client.ICondition;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.api.error.app.*;
import com.sun.jna.Native;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class JnaDriverImpl
{
	private final Logger logger;
	private final static String dllDir = "bin/UIAdapter.dll";
	private final static String pdbDir = "bin/UIAdapter.pdb";
	private JnaDriver jnaDriver;

	public JnaDriverImpl(Logger logger) throws Exception
	{
		this.logger = logger;
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
			this.jnaDriver = (JnaDriver) Native.loadLibrary(dll, JnaDriver.class);
		}
		else
		{
			throw new InternalErrorException("Dll is  not found");
		}
	}

	//region utils methods

	/**
	 * @return current framework identifier
	 * @throws Exception if some problem in c# side
	 */
	public Framework getFrameworkId() throws Exception
	{
		long start = time();
		String frameworkId = this.jnaDriver.getFrameworkId();
		this.logger.info(String.format("getFrameworkId() = %s, time (ms) : %d", frameworkId, time() - start));
		checkError();
		return Framework.byId(frameworkId);
	}

	/**
	 * @param timeout - max timeout for finding elements. If elements will found more than timeout, will throw TimeoutException
	 * @throws Exception
	 */
	public void maxTimeout(int timeout) throws Exception
	{
		long start = time();
		this.jnaDriver.maxTimeout(timeout);
		this.logger.info(String.format("maxTimeout(%d), time (ms) : %d", timeout, time() - start));
		checkError();
	}

	/**
	 * create logger on c# side
	 */
	public void createLogger(String logLevel) throws Exception
	{
		long start = time();
		this.jnaDriver.createLogger(logLevel);
		this.logger.info(String.format("createLogLevel(%s), time : (ms) : %d", logLevel, time() - start));
		checkError();
	}

	public void setPluginInfo(PluginInfo pluginInfo) throws Exception
	{
		long start = time();
		String pluginString = pluginInfoToString(pluginInfo);
		this.jnaDriver.setPluginInfo(pluginString);
		this.logger.info(String.format("setPluginInfo(%s), time : (ms) : %d", pluginString, time() - start));
		checkError();
	}
	//endregion

	//region application methods

	/**
	 * @return process id of a connected application
	 */
	public int connect(String title, int height, int width, int pid, ControlKind controlKind, int timeout, boolean alwaysToFront) throws Exception
	{
		long start = time();
		title = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(title);
		int ret = this.jnaDriver.connect(title, height, width, pid, controlKind == null ? Integer.MIN_VALUE : controlKind.ordinal(), timeout, alwaysToFront);
		this.logger.info(String.format("connect(%s), time (ms) : %d", title, time() - start));
		checkError();
		
		return ret;
	}

	/**
	 * @return process id of a started application
	 */
	public int run(String exec, String workDir, String param, boolean alwaysToFront) throws Exception
	{
		long start = time();
		int ret = this.jnaDriver.run(exec, workDir, param, alwaysToFront);
		this.logger.info(String.format("start(%s,%s,%s,%s), time (ms) : %d", exec, workDir, param, alwaysToFront, time() - start));
		checkError();
		return ret;
	}

	public void stop(boolean needKill) throws Exception
	{
		long start = time();
		this.jnaDriver.stop(needKill);
		this.logger.info(String.format("stop(), time (ms) : %d", time() - start));
		checkError();
	}

	public void refresh() throws Exception
	{
		long start = time();
		this.jnaDriver.refresh();
		this.logger.info(String.format("refresh(), time (ms) : %d", time() - start));
		checkError();
	}

	public String title() throws Exception
	{
		long start = time();
		String title = ConvertString.replaceUnicodeSubStringsToCharSymbols(this.jnaDriver.title());
		this.logger.info(String.format("title() = %s, time (ms) : %d", title, time() - start));
		checkError();
		return title;
	}

	//endregion

	//region find methods

	public List<String> listAll(UIProxyJNA owner,  Locator locator) throws Exception
	{
		long start = time();

		String uid = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(locator.getUid());
		String clazz = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(locator.getClazz());
		String xpath = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(locator.getXpath());
		String name = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(locator.getName());
		String title = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(locator.getTitle());
		String text = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(locator.getText());

		String result = this.jnaDriver.listAll(owner.getIdString(), locator.getControlKind().ordinal(), uid, xpath, clazz, name, title, text, locator.getAddition() == Addition.Many, locator
			.getVisibility()	== Visibility.Visible);
		checkError();

		List<String> list = new ArrayList<>();
		String[] split = result.split("#####");
		for(int i = 0; i < split.length && !split[i].isEmpty(); i++)
		{
			list.add(split[i]);
		}

		this.logger.info(String.format("listAll(%s, %s) = %s, time (ms) : %d", owner, locator, list.size(), time() - start));
		return list;
	}

	public List<UIProxyJNA> findAllForLocator(UIProxyJNA window, ControlKind kind, Locator locator ) throws Exception
	{
		List<UIProxyJNA> list = new ArrayList<>();
		long start = time();

		String uid = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(locator.getUid());
		String clazz = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(locator.getClazz());
		String xpath = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(locator.getXpath());
		String name = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(locator.getName());
		String title = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(locator.getTitle());
		String text = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(locator.getText());

		int length = 100;
		int[] result = new int[length];
		boolean many = locator.getAddition() != null && locator.getAddition() == Addition.Many;
		UIProxyJNA owner = window == null ? new UIProxyJNA() : window;

		boolean addVisible = locator.getVisibility() == Visibility.Visible;
		int count = this.jnaDriver.findAllForLocator(result, result.length, owner.getIdString(), kind.ordinal(), uid, xpath,clazz, name, title, text, many, addVisible);

		checkError();
		if (count > length)
		{
			length = count;
			result = new int[length];
			this.jnaDriver.findAllForLocator(result, result.length, owner.getIdString(), kind.ordinal(), uid, xpath,clazz, name, title, text, many, addVisible);
		}
		checkError();

		int foundElementCount = result[0];
		int currentPosition = 1;
		for (int i = 0; i < foundElementCount; i++)
		{
			int currentArrayLength = result[currentPosition++];
			int[] elem = new int[currentArrayLength];
			for (int j = 0; j < currentArrayLength; j++)
			{
				elem[j] = result[currentPosition++];
			}
			list.add(new UIProxyJNA(elem));
		}

		this.logger.info(String.format("findAllForLocator(%s,%s, (count) %s) = %s, time (ms) : %d"
				,window, kind, locator, result[0], time() - start
		));

		return list;
	}

	public int findAll(int[] arr, UIProxyJNA owner, WindowTreeScope scope, WindowProperty property, String value) throws Exception
	{
		long start = time();
		value = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(value);

		int result = this.jnaDriver.findAll(arr, arr.length, owner.getIdString(), scope.getValue(), property.getId(), value);
		this.logger.info(String.format("findAll(%s,%d,%s,%s,%s,%s) = %s, time (ms) : %d", Arrays.toString(arr), arr.length, owner, scope, property, value, result, time() - start));
		checkError();
		return result;
	}



	//endregion

	public String getXMLFromTree(UIProxyJNA element) throws Exception
	{
		long start = time();
		String result = ConvertString.replaceUnicodeSubStringsToCharSymbols(this.jnaDriver.getXMLFromTree(element.getIdString()));
		this.logger.info(String.format("getXMLFromTree(%s) = %s, time (ms) : %d", element, result, time() - start));
		checkError();
		return result;
	}

	public String elementAttribute(UIProxyJNA element, AttributeKind kind) throws Exception
	{
		long start = time();
		String result = ConvertString.replaceUnicodeSubStringsToCharSymbols(this.jnaDriver.elementAttribute(element.getIdString(), kind.ordinal()));
		this.logger.info(String.format("elementAttribute(%s,%s) = %s, time (ms) : %d", element, kind, result, time() - start));
		checkError();
		return result;
	}

	public void sendKeys(UIProxyJNA component, String key) throws Exception
	{
		long start = time();
		this.jnaDriver.sendKey(component.getIdString(), key);
		this.logger.info(String.format("key(%s), time (ms) : %d", key, time() - start));
		checkError();
	}

	public void upAndDown(UIProxyJNA component, String key, boolean isDown) throws Exception
	{
		long start = time();
		this.jnaDriver.upAndDown(component.getIdString(), key, isDown);
		this.logger.info(String.format("upAndDown (%s, %b), time (ms) : %d", key, isDown, time() - start));
		checkError();
	}

	public void mouse(UIProxyJNA element, MouseAction action, int x, int y) throws Exception
	{
		long start = time();
		this.jnaDriver.mouse(element.getIdString(), action.getId(), x, y);
		this.logger.info(String.format("mouse(%s,%s,%d,%d), time (ms) : %d", element, action, x, y, time() - start));
		checkError();
	}

	public void dragNdrop(int x1, int y1, int x2, int y2) throws Exception
	{
		long start = time();
		this.jnaDriver.dragNdrop(x1, y1, x2, y2);
		this.logger.info(String.format("dragNdrop(%d,%d,%d,%d), time (ms) : %d", x1, y1, x2, y2, time() - start));
		checkError();
	}

	/**
	 * if @param c == -1 -> arg is null;<br>
	 * if @param c == 0 -> arg is array of string with separator %<br>
	 * if @param c == 1 -> arg is array of int with separator %<br>
	 * if @param c == 2 -> arg is array of double with separator %<br>
	 * if @param c == 3 -> arg is window state <br>
	 */
	public String doPatternCall(UIProxyJNA element, WindowPattern pattern, String method, String args, int c) throws Exception
	{
		long start = time();
		String result = this.jnaDriver.doPatternCall(element.getIdString(), pattern.getId(), method, args, c);
		this.logger.info(String.format("doPatternCall(%s,%s,%s,%s,%d) = %s, time (ms) : %d", element, pattern, method, args, c, result, time() - start));
		checkError();
		return result;
	}

	public void setText(UIProxyJNA element, String text) throws Exception
	{
		long start = time();
		text = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(text);
		this.jnaDriver.setText(element.getIdString(), text);
		this.logger.info(String.format("setText(%s,%s), time (ms) : %d", element, text, time() - start));
		checkError();
	}

	public String getProperty(UIProxyJNA element, WindowProperty property) throws Exception
	{
		long start = time();
		String result = ConvertString.replaceUnicodeSubStringsToCharSymbols(this.jnaDriver.getProperty(element.getIdString(), property.getId()));
		this.logger.info(String.format("getProperty(%s,%s) = %s, time (ms) : %d", element, property, result, time() - start));
		checkError();
		return result;
	}

	public int getPatterns(int[] arr, UIProxyJNA element) throws Exception
	{
		long start = time();
		int result = this.jnaDriver.getPatterns(arr, arr.length, element.getIdString());
		this.logger.info(String.format("getPatterns(%s,%s,%s) = %d, time (ms) : %d", Arrays.toString(arr), arr.length, element, result, time() - start));
		checkError();
		return result;
	}

	public List<WindowPattern> getAvailablePatterns(UIProxyJNA element) throws Exception
	{
		long start = time();

		int length = 100;
		int[] arr = new int[length];
		int count = this.jnaDriver.getPatterns(arr, arr.length, element.getIdString());
		checkError();
		if (count > length)
		{
			length = count;
			arr = new int[length];
			count = this.jnaDriver.getPatterns(arr, arr.length, element.getIdString());
		}
		checkError();
		int patternsCount = count;
		int[] patterns = new int[patternsCount];
		System.arraycopy(arr, 0, patterns, 0, patternsCount);

		List<WindowPattern> patternList = new ArrayList<>();

		for (int pattern : patterns)
		{
			patternList.add(WindowPattern.byId(pattern));
		}

		this.logger.info(String.format("getAvailablePatterns(%s) = %s, time(ms) : %d", element, patternList, time() - start));
		return patternList;
	}

	public ImageWrapper getImage(UIProxyJNA element)  throws Exception
	{
		int length = 100 * 100;
		int[] arr = new int[length];
		int count = this.jnaDriver.getImage(arr, arr.length, element.getIdString());
		if (count > length)
		{
			length = count;
			arr = new int[length];
			this.jnaDriver.getImage(arr, arr.length, element.getIdString());
		}
		int[] result = new int[arr.length - 2];
		System.arraycopy(arr, 2, result, 0, arr.length - 2);
		return new ImageWrapper(arr[0], arr[1], result);
	}

	public void clearCache() throws Exception
	{
		long start = time();
		this.jnaDriver.clearCache();
		this.logger.info(String.format("clearCache, time (ms) : %d", time() - start));
		checkError();
	}

	//region table methods
	public String getValueTableCell(UIProxyJNA table, int column, int row) throws Exception
	{
		long start = time();
		String result = ConvertString.replaceUnicodeSubStringsToCharSymbols(this.jnaDriver.getValueTableCell(table.getIdString(), column, row));
		this.logger.info(String.format("getValueTableCell(%s,%d,%d) time(ms) : %d", table, column, row, time() - start));
		checkError();
		return result;
	}

	public void mouseTableCell(UIProxyJNA table, int column, int row, MouseAction mouseAction) throws Exception
	{
		long start = time();
		this.jnaDriver.mouseTableCell(table.getIdString(), column, row, mouseAction.getId());
		this.logger.info(String.format("mouseTableCell(%s,%d,%d, %s) time(ms) : %d", table, column, row, mouseAction, time() - start));
		checkError();
	}

	public void textTableCell(UIProxyJNA table, int column, int row, String text) throws Exception
	{
		long start = time();
		text = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(text);
		this.jnaDriver.textTableCell(table.getIdString(), column, row, text);
		this.logger.info(String.format("textTableCell(%s,%d,%d, %s) time(ms) : %d", table, column, row, text, time() - start));
		checkError();
	}

	public String getRowByConditions(UIProxyJNA table, boolean useNumericHeader, Condition condition, String columns) throws Exception
	{
		long start = time();
		columns = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(columns);
		String stringCondition = condition.serialize();
		String res = ConvertString.replaceUnicodeSubStringsToCharSymbols(this.jnaDriver.getRowByCondition(table.getIdString(), useNumericHeader, stringCondition, columns));
		this.logger.info(String.format("getRowByConditions(%s,%b,%s) : %s, time(ms) : %d", table, useNumericHeader, stringCondition, res, time() - start));
		checkError();
		return res;
	}

	public String getList(UIProxyJNA element, boolean onlyVisible) throws Exception
	{
		long start = time();
		String result = ConvertString.replaceUnicodeSubStringsToCharSymbols(this.jnaDriver.getList(element.getIdString(), onlyVisible));
		this.logger.info(String.format("getList(%s) = %s, time (ms) : %d", element, result, time() - start));
		checkError();
		return result;
	}

	public String getRowIndexes(UIProxyJNA table, boolean useNumericHeader, ICondition condition, String columns) throws Exception
	{
		long start = time();
		columns = ConvertString.replaceNonASCIISymbolsToUnicodeSubString(columns);
		String stringCondition = condition.serialize();
		String res = ConvertString.replaceUnicodeSubStringsToCharSymbols(this.jnaDriver.getRowIndexes(table.getIdString(), useNumericHeader, stringCondition, columns));
		this.logger.info(String.format("getRowIndexes(%s,%b,%s) : %s, time(ms) : %d", table, useNumericHeader, stringCondition, res, time() - start));
		checkError();
		return res;
	}

	public String getRowByIndex(UIProxyJNA table, boolean useNumericHeader, int index) throws Exception
	{
		long start = time();
		String res = ConvertString.replaceUnicodeSubStringsToCharSymbols(this.jnaDriver.getRowByIndex(table.getIdString(), useNumericHeader, index));
		this.logger.info(String.format("getRowByIndex(%s,%b,%s) : %s, time(ms) : %d", table, useNumericHeader, index, res, time() - start));
		checkError();
		return res;
	}

	public String getTable(UIProxyJNA table, boolean useNumericHeader) throws Exception
	{
		long start = time();
		String res = ConvertString.replaceUnicodeSubStringsToCharSymbols(this.jnaDriver.getTable(table.getIdString(), useNumericHeader));
		this.logger.info(String.format("getTable(%s,%b) : %s, time(ms) : %d", table, useNumericHeader, res, time() - start));
		checkError();
		return res;
	}

	public int getTableSize(UIProxyJNA table) throws Exception
	{
		long start = time();
		int res = this.jnaDriver.getTableSize(table.getIdString());
		this.logger.info(String.format("getTableSize(%s) : %d, time(ms) : %d", table, res, time() - start));
		checkError();
		return res;
	}

	private void checkError() throws RemoteException
	{
		String error = this.jnaDriver.lastError();
		if (!Str.IsNullOrEmpty(error))
		{
			int errorNumber = this.jnaDriver.lastErrorNumber();
			this.logger.error(error);
			switch (errorNumber)
			{
				case 0: throw new FeatureNotSupportedException(error);
				case 1: throw new NullParameterException(error);
				case 2: throw new WrongParameterException(error);
				case 3: throw new OperationNotAllowedException(error);
				case 4: throw new ElementNotFoundException(error);
				case 5: throw new TooManyElementsException(error);
				case 6: throw new InternalErrorException(error);
				case 7:	throw new TimeoutException(error);
			}
		}
	}

	//endregion

	//region private methods
	private long time()
	{
		return System.currentTimeMillis();
	}

	private static String pluginInfoToString(PluginInfo info)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("KindMap{");
		String bigSep = "";
		for (ControlKind kind : ControlKind.values()) {
			Set<String> strings = info.nodeByControlKind(kind);
			if (strings != null) {
				builder.append(bigSep).append(kind.ordinal()).append(":");
				String sep = "";
				for (String s : strings) {
					builder.append(sep).append(ControlType.get(s).getId());
					sep = ",";
				}
				bigSep = ";";
			}
		}
		builder.append("}\n");

		bigSep = "";
		builder.append("LocatorMap{");
		for (LocatorFieldKind kind : LocatorFieldKind.values()) {
			String s = info.attributeName(kind);
			if (s != null) {
				builder.append(bigSep).append(kind.ordinal()).append(":").append(s); // todo fix me, if I'm wrong
				bigSep = ";";
			}
		}

		builder.append("}");
		return builder.toString();
	}
	//endregion

}
