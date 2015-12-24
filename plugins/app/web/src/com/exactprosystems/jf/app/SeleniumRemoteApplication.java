////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.app.js.JSInjection;
import com.exactprosystems.jf.app.js.JSInjectionFactory;
import org.apache.log4j.*;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;

public class SeleniumRemoteApplication extends RemoteApplication
{
	private static final String TAG_FIELD = "tag";
	private static final String ATTRIBUTES_FIELD = "attributes";
	private static final String ELEMENT_FIELD = "elem";
	private static final String ELEMENT_PARENT_FIELD = "parent";
	private static final String ATTRIBUTE_NAME_FIELD = "attrName";
	private static final String ATTRIBUTE_VALUE_FIELD = "attrValue";
	private static final String ELEMENT_TEXT_FIELD = "text";
	private static final String ELEMENT_USER_DATA = "element";
	private static final String RECTANGLE_X = "x";
	private static final String RECTANGLE_Y = "y";
	private static final String RECTANGLE_W = "w";
	private static final String RECTANGLE_H = "h";



	private static final String SCRIPT =
					"function check(e) {\n" +
					"    if (e.tagName === undefined) {\n" +
					"        return false;\n" +
					"    }\n" +
					"    return true;\n" +
					"};\n" +
					"\n" +
					"function text(e) {\n" +
					"    if (e.tagName === 'input') {\n" +
					"        return e.value;\n" +
					"    }\n" +
					"    return e.text;\n" +
					"}\n" +
					"\n" +
					"function attrsOfElement(e) {\n" +
					"    var a = [];\n" +
					"    var attrs = e.attributes;\n" +
					"    for(var i = 0; i < attrs.length; i++) {\n" +
					"        var t = attrs[i];\n" +
					"        console.log(t.nodeValue);\n" +
					"        a.push({\n" +
					"            "+ATTRIBUTE_NAME_FIELD+" : t.nodeName,\n" +
					"            "+ATTRIBUTE_VALUE_FIELD+" : t.nodeValue\n" +
					"        });\n" +
					"    }\n" +
					"    return a;\n" +
					"}\n" +
					"\n" +
					"function rect(e) {\n" +
					"    if (check(e)) {\n" +
					"    var rect = e.getBoundingClientRect(); \n" +
					"        return {\n" +
					"            "+RECTANGLE_X+" : rect.left,\n" +
					"            "+RECTANGLE_Y+" : rect.top,\n" +
					"            "+RECTANGLE_H+" : rect.height,\n" +
					"            "+RECTANGLE_W+" : rect.width \n" +
					"        }\n" +
					"    }\n" +
					"    return null;\n" +
					"};\n" +
					"\n" +
					"function go(element, array, needRoot) {\n" +
					"    if (check(element)) {\n" +
					"        var temp = {\n" +
					"            "+TAG_FIELD+" : element.tagName,\n" +
					"            "+ATTRIBUTES_FIELD+" : attrsOfElement(element),\n" +
					"            "+IRemoteApplication.rectangleName+" : rect(element),\n" +
					"            "+ELEMENT_TEXT_FIELD+" : text(element),\n" +
					"            "+ELEMENT_FIELD+" : element\n" +
					"        };\n" +
					"        if (needRoot === true) {\n" +
					"            temp."+ELEMENT_PARENT_FIELD+" = element.parentElement;\n" +
					"        }\n" +
					"        else {\n" +
					"            temp."+ELEMENT_PARENT_FIELD+" = element;\n" +
					"        }\n" +
					"        array.push(temp);\n" +
					"        var els = element.childNodes;\n" +
					"        for(var i=0; i<els.length; i++) {\n" +
					"            go(els[i],array,true);\n" +
					"        }\n" +
					"    }\n" +
					"};\n" +
					"\n" +
					"function q(root) {\n" +
					"    var rectangles = [];\n" +
					"    console.log('start')\n" +
					"    go(root, rectangles, false);\n" +
					"    console.log(\"end. found : \" + rectangles.length + \" elements\");\n" +
					"    return rectangles;\n" +
					"};\n" +
					"return q(arguments[0]);"
			;

	private static Map<String, ArrayList<ControlKind>> mapTagsControlKind = new HashMap<>();

	public static final String itemName 	= "item";

	public static final String idName 		= "id";
	public static final String titleName 	= "title";
	public static final String textName 	= "text";
	public static final String tooltipName 	= "tooltip";
	public static final String nameName 	= "name";
	public static final String className 	= "class";

	static
	{
		mapTagsControlKind.put("button",	new SimpleArrayBuilder<ControlKind>().
				add(ControlKind.Button).
				add(ControlKind.CheckBox).
				add(ControlKind.TabPanel).build());
		mapTagsControlKind.put("span",	new SimpleArrayBuilder<ControlKind>().add(ControlKind.Label).build());
		mapTagsControlKind.put("a",			new SimpleArrayBuilder<ControlKind>().add(ControlKind.Button).build());
		mapTagsControlKind.put("img",		new SimpleArrayBuilder<ControlKind>().add(ControlKind.Button).build());
		mapTagsControlKind.put("input",		new SimpleArrayBuilder<ControlKind>().
				add(ControlKind.TextBox).
				add(ControlKind.Button).
				add(ControlKind.CheckBox).
				add(ControlKind.ComboBox).
				add(ControlKind.RadioButton).
				add(ControlKind.ToggleButton).build());
		mapTagsControlKind.put("select",	new SimpleArrayBuilder<ControlKind>().add(ControlKind.ComboBox).build());
		mapTagsControlKind.put("form",		new SimpleArrayBuilder<ControlKind>().
				add(ControlKind.Dialog).
				add(ControlKind.Frame).build());
		mapTagsControlKind.put("body",		new SimpleArrayBuilder<ControlKind>().add(ControlKind.Frame).build());
		mapTagsControlKind.put("label",		new SimpleArrayBuilder<ControlKind>().add(ControlKind.Label).build());
		mapTagsControlKind.put("li",		new SimpleArrayBuilder<ControlKind>().
				add(ControlKind.MenuItem).
				add(ControlKind.Menu).build());
		mapTagsControlKind.put("div",		new SimpleArrayBuilder<ControlKind>().add(ControlKind.Panel).build());
		mapTagsControlKind.put("tr",		new SimpleArrayBuilder<ControlKind>().add(ControlKind.Row).build());
		mapTagsControlKind.put("table",		new SimpleArrayBuilder<ControlKind>().add(ControlKind.Table).build());
		mapTagsControlKind.put("textarea",	new SimpleArrayBuilder<ControlKind>().add(ControlKind.TextBox).build());
		mapTagsControlKind.put("ul",		new SimpleArrayBuilder<ControlKind>().add(ControlKind.ListView).build());
		mapTagsControlKind.put("*",			new SimpleArrayBuilder<ControlKind>().add(ControlKind.Any).build());

	}

	public SeleniumRemoteApplication()
	{
	}

	@Override
	protected void createLoggerDerived(String logName, String serverLogLevel, String serverLogPattern) throws Exception
	{
		logger = Logger.getLogger(SeleniumRemoteApplication.class);

		Layout layout = new PatternLayout(serverLogPattern);
		Appender appender = new FileAppender(layout, logName);
		logger.addAppender(appender);
		logger.setLevel(Level.toLevel(serverLogLevel, Level.ALL));

		MatcherSelenium.setLogger(logger);
	}

	@Override
	protected void connectDerived(Map<String, String> args) throws Exception
	{
		logger.info("##########################################################################################################");
		throw new Exception("Not supported yet.");
	}

	@Override
	protected void runDerived(Map<String, String> args) throws Exception
	{
		try
		{
			logger.info("##########################################################################################################");

			String browserName = args.get(WebAppFactory.browserName);
			String url = args.get(WebAppFactory.urlName);

			String chromeDriverPath = args.get(WebAppFactory.chromeDriverPathName);
			if (chromeDriverPath != null && !chromeDriverPath.isEmpty())
			{
				logger.info(WebAppFactory.chromeDriverPathName + " = " + chromeDriverPath);
				System.setProperty("webdriver.chrome.driver", chromeDriverPath);
			}
			String ieDriverPath = args.get(WebAppFactory.ieDriverPathName);
			if (ieDriverPath != null && !ieDriverPath.isEmpty())
			{
				logger.info(WebAppFactory.ieDriverPathName + " = " + ieDriverPath);
				System.setProperty("webdriver.ie.driver", ieDriverPath);
			}
			String chromeDriverBinary = args.get(WebAppFactory.chromeDriverBinary);
			if (chromeDriverBinary != null && !chromeDriverBinary.isEmpty())
			{
				logger.info(WebAppFactory.chromeDriverBinary + " = " + chromeDriverBinary);
			}

			logger.info("Starting " + browserName + " on " + url);

			if (browserName == null)
			{
				throw new Exception("browser is null");
			}

			if (url == null)
			{
				throw new Exception("url is null");
			}
			Browser browser = Browser.valueOf(browserName.toUpperCase());
			this.driver = new EventFiringWebDriver(browser.createDriver(chromeDriverBinary));
			this.jsInjection = JSInjectionFactory.getJSInjection(browser);
			this.operationExecutor = new SeleniumOperationExecutor(this.driver, this.logger);

			this.driver.get(url);
			this.driver.manage().window().maximize();
			needTune = true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(),  e);
			throw e;
		}
	}

	@Override
	protected void stopDerived() throws Exception
	{
		try
		{
			if (this.driver != null)
			{
				for (String handle : this.driver.getWindowHandles())
				{
					this.driver.switchTo().window(handle);
					break;
				}
				this.driver.quit();
			}
		}
		catch (UnreachableBrowserException e)
		{
			logger.error("Browser has been closed");
		}
	}

	@Override
	protected void refreshDerived() throws Exception
	{
		this.driver.navigate().refresh();

		//		Actions actions = new Actions(driver);
		//		actions.keyDown(Keys.CONTROL).sendKeys(Keys.F5).perform();
	}

	@Override
	protected Collection<String> titlesDerived() throws Exception
	{
		final List<String> res = new ArrayList<String>();

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					String current = driver.getWindowHandle();
					Set<String> set = driver.getWindowHandles();
					for (String handle : set)
					{
						driver.switchTo().window(handle);
						res.add(driver.getTitle());
					}

					driver.switchTo().window(current);
				}
				catch (Exception e)
				{
					logger.error("Error on thread");
					logger.error(e.getMessage(), e);
				}
			}
		});
		thread.start();
		thread.join(3000);
		return res;
	}

	@Override
	protected void resizeDerived(int height, int width, boolean maximize, boolean minimize) throws Exception
	{
		if (maximize)
		{
			this.driver.manage().window().maximize();
		}
		else if (minimize)
		{
			throw new UnsupportedOperationException();
		}
		else
		{
			this.driver.manage().window().setSize(new Dimension(width,  height));
		}
	}
	
	@Override
	protected String switchToDerived(final String title) throws Exception
	{
		final String[] result = new String[]{""};

		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					Set<String> set = driver.getWindowHandles();

					for (String handle : set)
					{
						driver.switchTo().window(handle);
						result[0] = driver.getTitle();
						if (driver.getTitle().contains(title))
						{
							driver.manage().window().maximize();
							return;
						}
					}
				}
				catch (Exception e)
				{
					logger.error("Error on thread");
					logger.error(e.getMessage(), e);
				}
			}
		});
		thread.start();
		thread.join(10000);
		needTune = true;
		return result[0];
	}

	@Override
	protected Locator getLocatorDerived(Locator owner, ControlKind controlKind, int x, int y) throws RemoteException
	{
		if (needTune)
		{
			return tuneDisplay();
		}
		WebElement o = (WebElement) this.driver.executeScript("return document.elementFromPoint(" + (x - offsetX) + ", " + (y - offsetY) + ");");
		return getLocator(controlKind, o);
	}

	private Locator tuneDisplay() throws RemoteException
	{
		if (needTune)
		{
			try
			{
				this.jsInjection.injectJSLocation(driver);
				Robot robot = new Robot();
				Thread.sleep(1000);
				robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

				Object o = this.driver.executeScript(JSInjectionFactory.returnLocation);
				ArrayList<?> longs = (ArrayList<?>) o;
				long xL = ((Long) longs.get(0));
				long yL = ((Long) longs.get(1));

				java.awt.Point location = MouseInfo.getPointerInfo().getLocation();
				double x1 = location.getX();
				double y1 = location.getY();
				offsetX = x1 - xL;
				offsetY = y1 - yL;
			}
			catch (Exception e)
			{
				logger.error("Error on tune display");
				logger.error(e.getMessage(),e );
				throw new ProxyException("Error on tune display", e.getMessage(), e);
			}
		}
		needTune = false;
		return null;
	}

	private Locator getLocator(ControlKind kind, WebElement e)
	{
		ControlKind controlKind;
		WebElement element;
		if (kind == null || kind == ControlKind.Any)
		{
			controlKind = mapTagsControlKind.get(e.getTagName()) == null ? ControlKind.Any : mapTagsControlKind.get(e.getTagName()).get(0);
			element = e;
		}
		else
		{
			element = parentKind(e, kind);
			controlKind = kind;
		}
		String idElement = element.getAttribute("id");
		String id = idElement == null ? controlKind.name() + element.getTagName() : idElement;

		Locator locator = new Locator(null, id, controlKind);
		locator
			.uid(idElement)
			.clazz(element.getAttribute("class"))
			.name(element.getAttribute("name"))
			.title(element.getAttribute("title"))
			.action(element.getAttribute("action"))
			.text(element.getText());
		return locator;
	}

	private WebElement parentKind(WebElement e, ControlKind kind)
	{
		ArrayList<ControlKind> controlKinds = mapTagsControlKind.get(e.getTagName());

		if (controlKinds != null)
		{
			for (ControlKind ck : controlKinds)
			{
				if (kind == ck)
				{
					return e;
				}
			}
			return parentKind(findParent(e), kind);
		}
		else
		{
			WebElement parent = findParent(e);
			if (parentKind(parent, kind) != null)
			{
				return parent;
			}
		}
		return null;
	}

	private WebElement findParent(WebElement e)
	{
		logger.debug("e : " + getElementString(e));
		WebElement r = e.findElement(By.xpath(".."));
		logger.debug("r : " + getElementString(r));
		return r;
	}

	@Override
	protected Collection<String> findAllDerived(Locator owner, Locator locator) throws Exception
	{
		WebElement ownerElement = null;
		if (owner != null)
		{
			By byOwner = new MatcherSelenium(owner.getControlKind(), owner);
			List<WebElement> owners = this.driver.findElements(byOwner);
			if (owners.isEmpty())
			{
				throw new RemoteException("Owner was not found.");
			}

			if (owners.size() > 1)
			{
				throw new RemoteException(owners.size() + " owners were found instead 1.");
			}
			ownerElement = owners.get(0);
		}

		By by = new MatcherSelenium(locator.getControlKind(), locator);
		List<WebElement> elements = (ownerElement == null ? this.driver.findElements(by) : ownerElement.findElements(by));

		List<String> result = new ArrayList<>();
		for(WebElement element : elements)
		{
			result.add(getElementString(element));
		}
		return result;
	}

	static String getElementString(WebElement element)
	{
		String s = element.getAttribute("outerHTML");
		return s.substring(0, s.indexOf(">")+1);
	}

	@Override
	protected ImageWrapper getImageDerived(Locator owner, Locator element) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				//method webElement.getScreenshotAs not working in 2.48.2
				File screenshot = this.driver.getScreenshotAs(OutputType.FILE);
				BufferedImage fullImg = ImageIO.read(screenshot);
				if (element == null)
				{
					return new ImageWrapper(fullImg);
				}
				WebElement component = this.operationExecutor.find(owner, element);
				Point point = component.getLocation();
				int eleWidth = component.getSize().getWidth();
				int eleHeight = component.getSize().getHeight();
				BufferedImage image = fullImg.getSubimage(point.getX(), point.getY(), eleWidth, eleHeight);

				return new ImageWrapper(image);
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumRemoteApplication : " + repeat);
			}
			catch (Exception e)
			{
				logger.error("EXCEPTION : " + e.getMessage(), e);
				throw new Exception(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	protected Rectangle getRectangleDerived(Locator owner, Locator element) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				WebElement webElement = this.operationExecutor.find(owner, element);
				return this.operationExecutor.getRectangle(webElement);
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumRemoteApplication : " + repeat);
			}
			catch (Exception e)
			{
				logger.error("EXCEPTION : " + e.getMessage(), e);
				throw new Exception(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	@Override
	protected OperationResult operateDerived(Locator owner, Locator element, Locator rows, Locator header, Operation operation) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				return operation.operate(this.operationExecutor, owner, element, rows, header);
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumRemoteApplication : " + repeat);
			}
			catch (Exception e)
			{
				logger.error("EXCEPTION : " + e.getMessage(), e);
				throw new Exception(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}
	
	@Override
	protected CheckingLayoutResult checkLayoutDerived(Locator owner, Locator element, Spec spec) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				return spec.perform(this.operationExecutor, owner, element);
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug("Element is no longer attached to the DOM. Try in SeleniumRemoteApplication : " + repeat);
			}
			catch (Exception e)
			{
				logger.error("EXCEPTION : " + e.getMessage(), e);
				throw new Exception(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}


	@Override
	protected void newInstanceDerived(Map<String, String> args) throws Exception
	{
		String url 		= args.get(WebAppFactory.urlName);
		if (url == null)
		{
			throw new Exception("url is null");
		}
		String tab 		= args.get("Tab");
		boolean flag = false;
		if (tab != null)
		{
			flag = tab.equalsIgnoreCase("true");
		}
		this.driver.executeScript("function createDoc(){var w = window.open('"+url+"'"+ (flag ? ",'_blank'" : "")+")}; createDoc();");
		needTune = true;
	}

	@Override
	protected int closeAllDerived(Locator element, Collection<LocatorAndOperation> operations) throws Exception
	{
		List<WebElement> dialogs = this.operationExecutor.findAll(ControlKind.Any, null, element);
		
		for (WebElement dialog : dialogs)
		{
			for (LocatorAndOperation pair : operations)
			{
				Locator locator = pair.getLocator();
				
				List<WebElement> components = this.operationExecutor.findAll(locator.getControlKind(), dialog, locator);
				if (components.size() == 1)
				{
					WebElement component = components.get(0);
					Operation operation = pair.getOperation();
					operation.operate(this.operationExecutor, locator, component);
					
				}
			}
		}
		
		return dialogs.size();
	}

	@Override
	protected String closeWindowDerived() throws Exception
	{
		String title = this.driver.getTitle();
		this.driver.close();
		return title;
	}

	@Override
	protected Document getTreeDerived(Locator owner) throws Exception
	{
		WebElement ownerElement;
		if (owner == null)
		{
			ownerElement = this.driver.findElement(By.tagName("body"));
		}
		else
		{
			ownerElement = this.operationExecutor.find(null, owner);
		}
		try
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document document = docBuilder.newDocument();
			Object returnObject = driver.executeScript(SCRIPT, ownerElement);
			/**
			 * every row from o - is object, string value of them like this
			 * [elem=[org.openqa.selenium.remote.RemoteWebElement@67502cce -> unknown locator], parent=[org.openqa.selenium.remote.RemoteWebElement@67502cce -> unknown locator], attributes=[{attrValue=position:absolute;width:500px;height:270px;top:0px;left:0px, attrName=style}, {attrValue=html1, attrName=id}], rectangle={w=500, x=0, h=270, y=0}, tag=DIV, id=html1, text=null, parentId=html1]
			 * where	[] - list/array;
			 * 			{} - map;
			 * 	[org.openqa.selenium.remote.RemoteWebElement@67502cce -> unknown locator] - RemoteWebElement.toString();
			 */
			List<Map<String, Object>> list = (List<Map<String, Object>>) returnObject;

			/**
			 * we get first element of this list - this is root element;
			 */
			Map<String, Object> rootElement = list.get(0);
			Element rootElem = document.createElement(((String) rootElement.get(TAG_FIELD)).toLowerCase());
			setAttributes(rootElem, ((List<Map<String, String>>) rootElement.get(ATTRIBUTES_FIELD)));
			rootElem.setUserData(IRemoteApplication.rectangleName, createRectangle((Map<String, String>) rootElement.get(IRemoteApplication.rectangleName)), null);
			rootElem.setUserData(ELEMENT_USER_DATA, rootElement.get(ELEMENT_FIELD), null);
			document.appendChild(rootElem);
			String text = (String) rootElement.get(ELEMENT_TEXT_FIELD);
			if (!Str.IsNullOrEmpty(text))
			{
				rootElem.setTextContent(text);
			}

			/**
			 * for remaining rows we create element, find parent for them and put in document with attributes and text
			 */
			for (int i = 1; i < list.size(); i++)
			{
				Map<String, Object> el = list.get(i);
				Element element = document.createElement(((String) el.get(TAG_FIELD)).toLowerCase());
				setAttributes(element, ((List<Map<String, String>>) el.get(ATTRIBUTES_FIELD)));
				element.setUserData(ELEMENT_USER_DATA, el.get(ELEMENT_FIELD), null);
				String textElement = (String) el.get(ELEMENT_TEXT_FIELD);
				if (!Str.IsNullOrEmpty(textElement))
				{
					element.setTextContent(textElement);
				}
				WebElement parent1 = (WebElement) el.get(ELEMENT_PARENT_FIELD);
				Node parent = findParent(rootElem, parent1);
				if (parent == null)
				{
					rootElem.appendChild(element);
				}
				else
				{
					parent.appendChild(element);
				}
				element.setUserData(IRemoteApplication.rectangleName, createRectangle(((Map<String, String>) el.get(IRemoteApplication.rectangleName))), null);
			}
			clearDocument(document);
			return document;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * remove all WebElement's from document
	 */
	private void clearDocument(Node document)
	{
		document.setUserData(ELEMENT_USER_DATA, null, null);
		for(int i =0; i < document.getChildNodes().getLength(); i++)
		{
			Node item = document.getChildNodes().item(i);
			item.setUserData(ELEMENT_USER_DATA, null, null);
			clearDocument(item);
		}
	}

	private void setAttributes(Element element, List<Map<String, String>> map)
	{
		for (Map<String, String> att : map)
		{
			element.setAttribute(att.get(ATTRIBUTE_NAME_FIELD), att.get(ATTRIBUTE_VALUE_FIELD));
		}
	}

	private Node findParent(Node root, WebElement element)
	{
		NodeList childNodes = root.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++)
		{
			Node item = childNodes.item(i);
			if (element.equals(item.getUserData(ELEMENT_USER_DATA)))
			{
				return item;
			}
			Node parent = findParent(item, element);
			if (parent != null)
			{
				return parent;
			}
		}
		return null;
	}

	private java.awt.Rectangle createRectangle(Map<String, String> map)
	{
		int x = Integer.parseInt(String.valueOf(map.get(RECTANGLE_X)));
		int y = Integer.parseInt(String.valueOf(map.get(RECTANGLE_Y)));
		int h = Integer.parseInt(String.valueOf(map.get(RECTANGLE_H)));
		int w = Integer.parseInt(String.valueOf(map.get(RECTANGLE_W)));
		return new java.awt.Rectangle(x, y, w, h);
	}

	@Override
	protected void startGrabbingDerived() throws Exception
	{
		this.jsInjection.injectJSHighlight(this.driver);
	}

	@Override
	protected void endGrabbingDerived() throws Exception
	{
		this.jsInjection.stopInject(this.driver);
	}

	@Override
	protected void highlightDerived(Locator owner, String xpath) throws Exception
	{
		SearchContext ownerElement = this.driver;
		if (owner != null)
		{
			ownerElement = this.operationExecutor.find(null, owner);
		}
		WebElement currentElement = ownerElement.findElement(By.xpath(xpath));
		this.jsInjection.startHighLight(this.driver, currentElement);
		try
		{
			Thread.sleep(1000);
		}
		catch (Exception e)
		{
			//
		}
		this.jsInjection.stopHighLight(this.driver, currentElement);
	}

	private EventFiringWebDriver driver;

	private boolean needTune = true;
	private double offsetX;
	private double offsetY;
	private final int repeatLimit = 5;
	private JSInjection jsInjection;
	public OperationExecutor<WebElement> operationExecutor;
	private Logger logger = null;
}
