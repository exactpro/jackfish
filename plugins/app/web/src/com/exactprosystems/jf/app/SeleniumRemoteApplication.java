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
import org.jsoup.Jsoup;
import org.openqa.selenium.*;
import org.openqa.selenium.Point;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeleniumRemoteApplication extends RemoteApplication
{
	private static Map<ControlKind, ArrayList<String>> mapControlKindTags = new HashMap<>(); // TODO what for?
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
		mapControlKindTags.put(ControlKind.Any,			new SimpleArrayBuilder<String>().add("*").build());
		mapControlKindTags.put(ControlKind.Button,		new SimpleArrayBuilder<String>().add("button").add("input").add("a").add("img").build());
		mapControlKindTags.put(ControlKind.CheckBox,	new SimpleArrayBuilder<String>().add("button").add("input").build());
		mapControlKindTags.put(ControlKind.ComboBox,	new SimpleArrayBuilder<String>().add("select").add("input").build());
		mapControlKindTags.put(ControlKind.Dialog,		new SimpleArrayBuilder<String>().add("form").build());
		mapControlKindTags.put(ControlKind.Frame,		new SimpleArrayBuilder<String>().add("form").add("body").build());
		mapControlKindTags.put(ControlKind.Label,		new SimpleArrayBuilder<String>().add("label").build());
		mapControlKindTags.put(ControlKind.MenuItem,	new SimpleArrayBuilder<String>().add("li").build());
		mapControlKindTags.put(ControlKind.Panel,		new SimpleArrayBuilder<String>().add("div").build());
		mapControlKindTags.put(ControlKind.RadioButton,	new SimpleArrayBuilder<String>().add("input").build());
		mapControlKindTags.put(ControlKind.Row,			new SimpleArrayBuilder<String>().add("tr").build());
		mapControlKindTags.put(ControlKind.Table,		new SimpleArrayBuilder<String>().add("table").build());
		mapControlKindTags.put(ControlKind.TabPanel,	new SimpleArrayBuilder<String>().add("button").build());
		mapControlKindTags.put(ControlKind.TextBox,		new SimpleArrayBuilder<String>().add("input").add("textarea").build());
		mapControlKindTags.put(ControlKind.ToggleButton,new SimpleArrayBuilder<String>().add("input").build());
		mapControlKindTags.put(ControlKind.ListView,	new SimpleArrayBuilder<String>().add("ul").build());
		mapControlKindTags.put(ControlKind.Tree,		new SimpleArrayBuilder<String>().add("").build());
		mapControlKindTags.put(ControlKind.Wait,		new SimpleArrayBuilder<String>().add("*").build());
		mapControlKindTags.put(ControlKind.Tooltip,		new SimpleArrayBuilder<String>().add("*").build());
		mapControlKindTags.put(ControlKind.Menu,		new SimpleArrayBuilder<String>().add("li").build());
		mapControlKindTags.put(ControlKind.TreeItem,	new SimpleArrayBuilder<String>().add("").build());

		mapTagsControlKind.put("button",	new SimpleArrayBuilder<ControlKind>().
				add(ControlKind.Button).
				add(ControlKind.CheckBox).
				add(ControlKind.TabPanel).build());
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
			this.operationExecutor = new SeleniumOperationExecutor(driver, this.logger);

			driver.get(url);
			driver.manage().window().maximize();
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
		if (driver != null)
		{
			for (String handle : driver.getWindowHandles())
			{
				driver.switchTo().window(handle);
				break;
			}
			driver.quit();
		}
	}

	@Override
	protected void refreshDerived() throws Exception
	{
		driver.navigate().refresh();

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
		thread.join(1000);
		return res;
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
		WebElement o = (WebElement) driver.executeScript("return document.elementFromPoint(" + (x - offsetX) + ", " + (y - offsetY) + ");");
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

				Object o = driver.executeScript(JSInjectionFactory.returnLocation);
				ArrayList longs = (ArrayList) o;
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
			List<WebElement> owners = driver.findElements(byOwner);
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
		List<WebElement> elements = (ownerElement == null ? driver.findElements(by) : ownerElement.findElements(by));

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
		File screenshot = driver.getScreenshotAs(OutputType.FILE);
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
		driver.executeScript("function createDoc(){var w = window.open('"+url+"'"+ (flag ? ",'_blank'" : "")+")}; createDoc();");
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
		WebElement element;
		if (owner == null)
		{
			element = driver.findElement(By.tagName("body"));
		}
		else
		{
			element = this.operationExecutor.find(null, owner);
		}
		try
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document document = docBuilder.newDocument();

			String outerHtml = element.getAttribute("outerHTML");
			org.jsoup.nodes.Document doc = Jsoup.parse(outerHtml);
			org.jsoup.nodes.Element body = doc.getElementsByTag("body").get(0);

			int[] count = new int[]{0};
			long begin = System.currentTimeMillis();
			buildDom(document, document, body, count);
			logger.debug("Created document, time : " + (System.currentTimeMillis() - begin) + " ms, total items : " + count[0]);
			return document;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private void setNodeAttributes(Element node, org.jsoup.nodes.Element element)
	{
		String s = element.attributes().toString();
		if (!s.isEmpty())
		{
			s = s.substring(1);
		}

		String attributes = s;
		Pattern pattern = Pattern.compile("([^\\s]+?)=\"(.*?)\"\\s?");
		Matcher matcher = pattern.matcher(attributes);
		while (matcher.find())
		{
			String attrName = matcher.group(1);
			String attrValue = matcher.group(2);
			try
			{
				node.setAttribute(attrName, attrValue);
			} 
			catch (DOMException e)
			{ } // nothing to do - just to validate attrName
		}
		String ownText = element.ownText();
		if (!Str.IsNullOrEmpty(ownText))
		{
			node.setTextContent(ownText);
		}
	}

	private void buildDom(Document document, Node current, org.jsoup.nodes.Element element, int[] count)
	{
		count[0]++;
		Element node = document.createElement(element.tagName());
		setNodeAttributes(node, element);
		current.appendChild(node);
		for (org.jsoup.nodes.Element child : element.children())
		{
			buildDom(document, node, child, count);
		}
	}

	@Override
	protected void startGrabbingDerived() throws Exception
	{
		this.jsInjection.injectJSHighlight(driver);
	}

	@Override
	protected void endGrabbingDerived() throws Exception
	{
		this.jsInjection.stopInject(driver);
	}

	@Override
	protected void highlightDerived(Locator owner, String xpath) throws Exception
	{
		SearchContext ownerElement = driver;
		if (owner != null)
		{
			ownerElement = this.operationExecutor.find(null, owner);
		}
		WebElement currentElement = ownerElement.findElement(By.xpath(xpath));
		this.jsInjection.startHighLight(driver, currentElement);
		try
		{
			Thread.sleep(1000);
		}
		catch (Exception e)
		{
			//
		}
		this.jsInjection.stopHighLight(driver, currentElement);
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
