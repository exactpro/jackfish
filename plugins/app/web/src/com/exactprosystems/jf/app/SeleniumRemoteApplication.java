////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.app.js.JSInjection;
import com.exactprosystems.jf.app.js.JSInjectionFactory;
import org.apache.log4j.*;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;

public class SeleniumRemoteApplication extends RemoteApplication
{
	private static final String TAG_FIELD = "tag";
	private static final String ATTRIBUTES_FIELD = "attr";
	private static final String ELEMENT_CHILD_FIELD = "child";
	private static final String ATTRIBUTE_NAME_FIELD = "name";
	private static final String ATTRIBUTE_VALUE_FIELD = "val";
	private static final String ELEMENT_TEXT_FIELD = "text";

	private Alert currentAlert;

	private static final String SCRIPT = 
		" \n" +
		"function attrs(e) { \n" +
		"    var a = []; \n" +
		"    var attrs = e.attributes; \n" +
		"    for(var i = 0; i < attrs.length; i++) { \n" + 
		"        var t = attrs[i]; \n" +
		"        a.push({ \n" +
		"            " + ATTRIBUTE_NAME_FIELD + " : t.nodeName, \n" +
		"            " + ATTRIBUTE_VALUE_FIELD + " : t.value \n" +
		"        }); \n" +
		"    } \n" +
		"    return a; \n" +
		"}; \n" +
		" \n" +
		"function go(e) { \n" +
		"    if (e.tagName !== undefined) { \n" +
		"        var child = []; \n" +
		"        var els = e.childNodes; \n" +
		"        for(var i=0; i<els.length; i++) { \n" +
		"            var ch = go(els[i]); \n" +
		"            if (ch != null) { \n" +
		"                child.push(ch); \n" +
		"            } \n" +
		"        } \n" +
		"        var temp = { \n" +
		"            " + TAG_FIELD + " : e.tagName, \n" +
		"            " + ATTRIBUTES_FIELD + " : attrs(e), \n" +
		"            " + IRemoteApplication.rectangleName + " : { \n" +
		"            " + "    left : e.getBoundingClientRect().left, \n" +
		"            " + "    top : e.getBoundingClientRect().top, \n" +
		"            " + "    height : e.getBoundingClientRect().height, \n" +
		"            " + "    width : e.getBoundingClientRect().width, \n" +
		"            " + "}, \n" +
		"            " + ELEMENT_TEXT_FIELD + " : (e.tagName === 'input') ? e.value : (e.firstChild !== null) ? e.firstChild.data : undefined, \n" +
		"            " + ELEMENT_CHILD_FIELD + " : child \n" +
		"        }; \n" +
		"        return temp; \n" +
		"    } \n" +
		"    return null; \n" +
		"}; \n" +
		" \n" +
		"return go(arguments[0]); \n";

	private static Map<String, ArrayList<ControlKind>> mapTagsControlKind = new HashMap<>();

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
	protected void connectDerived(Map<String, String> args, MetricsCounter metricsCounter) throws Exception
	{
		logger.info("##########################################################################################################");
		throw new Exception("Not supported yet.");
	}

	@Override
	protected void runDerived(Map<String, String> args, MetricsCounter metricsCounter) throws Exception
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

			String firefoxProfileDirectory = args.get(WebAppFactory.firefoxProfileDir);
			if (!Str.IsNullOrEmpty(firefoxProfileDirectory))
			{
				logger.info(WebAppFactory.firefoxProfileDir + " = " + firefoxProfileDirectory);
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
			this.driver = new WebDriverListenerNew(browser.createDriver(chromeDriverBinary, firefoxProfileDirectory), metricsCounter);
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
		catch (WebDriverException e)
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
	protected SerializablePair<String, Boolean> getAlertTextDerived() throws Exception
	{
		try
		{
			this.currentAlert = this.driver.switchTo().alert();
		}
		catch (NoAlertPresentException e)
		{
			throw new RemoteException("Alert is not present");
		}
		return new SerializablePair<>(this.currentAlert.getText(), true);
	}

	@Override
	protected void setAlertTextDerived(String text, PerformKind performKind) throws Exception
	{
		logger.debug(String.format("setAlertTextDerived(%s,%s)", text, performKind));
		if (this.currentAlert == null)
		{
			throw new RemoteException("Alert is not present");
		}
		if (!Str.IsNullOrEmpty(text))
		{
			this.currentAlert.sendKeys(text);
		}
		switch (performKind)
		{

			case Accept:
				this.currentAlert.accept();
				break;
			case Dismiss:
				this.currentAlert.dismiss();
				break;
			case Nothing:
				break;
		}
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
	protected String switchToDerived(final String title, final boolean softCondition) throws Exception
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
						if (softCondition && driver.getTitle().contains(title)
								|| !softCondition && driver.getTitle().equals(title) )
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
	protected void switchToFrameDerived(Locator owner) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				if (owner == null)
				{
					this.driver.switchTo().defaultContent();
				}
				else
				{
					WebElement component = this.operationExecutor.find(null, owner);
					this.driver.switchTo().frame(component);
				}
				return;
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

	//	public static void main(String[] args)
	//	{
	//		WebDriver driver = new FirefoxDriver();
	//		try
	//		{
	//			driver.get("file:///home/andrey.bystrov/Projects/JackFish/ActionsLibrary/testdata/resources/mock/mock.html");
	//			WebElement iframe = driver.findElement(By.tagName("iframe"));
	//			WebDriver frame = driver.switchTo().frame(iframe);
	//			System.out.println(frame == driver);
	//			File screenshotAs = ((TakesScreenshot) frame).getScreenshotAs(OutputType.FILE);
	//			BufferedImage fullImg = ImageIO.read(screenshotAs);
	//			WebElement component = frame.findElement(By.id("myId"));
	//			Point location = component.getLocation();
	//			int eleWidth = component.getSize().getWidth();
	//			int eleHeight = component.getSize().getHeight();
	//			BufferedImage image = fullImg.getSubimage(location.getX(), location.getY(), eleWidth, eleHeight);
	//			ImageIO.write(image, "jpeg", new File("tempimg.jpeg"));
	//			ImageIO.write(fullImg, "jpeg", new File("full.jpeg"));
	//		}
	//		catch (Exception e)
	//		{
	//			e.printStackTrace();
	//		}
	//		finally
	//		{
	//			driver.quit();
	//		}
	//	}

	@Override
	protected ImageWrapper getImageDerived(Locator owner, Locator element) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				log("before image");
				
				//method webElement.getScreenshotAs not working in 2.48.2
				File screenshot = this.driver.getScreenshotAs(OutputType.FILE);
				BufferedImage fullImg = ImageIO.read(screenshot);
				if (element == null)
				{
					return new ImageWrapper(fullImg);
				}
				WebElement component = this.operationExecutor.find(owner, element);
				Point point = component.getLocation();
				
				logger.error(">>> point=" + point);
				logger.error(">>> size=" + component.getSize());
				
				Point realPoint = new Point(Math.max(point.x, 0), Math.max(point.y, 0));
				int eleWidth = component.getSize().getWidth() + Math.min(point.x, 0);
				int eleHeight = component.getSize().getHeight() + Math.min(point.y, 0);

				logger.error(">>> realPoint=" + realPoint);
				logger.error(">>> size=" + eleWidth + "x" + eleHeight);

				//TODO add exception about image is very big/small.
				BufferedImage image = fullImg.getSubimage(realPoint.getX(), realPoint.getY(), eleWidth, eleHeight);

				log("after image");
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
				WebElement webElement;
				if (element == null)
				{
					webElement = this.driver.findElements(By.xpath("//html")).get(0);
				}
				else
				{
					webElement = this.operationExecutor.find(owner, element);
				}
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
		log("start");
		
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
			log("before script");
			Object returnObject = driver.executeScript(SCRIPT, ownerElement);
			log("after script");
			/**
			 * every row from o - is object, string value of them like this
			 * [elem=[org.openqa.selenium.remote.RemoteWebElement@67502cce -> unknown locator], parent=[org.openqa.selenium.remote.RemoteWebElement@67502cce -> unknown locator], attributes=[{attrValue=position:absolute;width:500px;height:270px;top:0px;left:0px, attrName=style}, {attrValue=html1, attrName=id}], rectangle={w=500, x=0, h=270, y=0}, tag=DIV, id=html1, text=null, parentId=html1]
			 * where	[] - list/array;
			 * 			{} - map;
			 * 	[org.openqa.selenium.remote.RemoteWebElement@67502cce -> unknown locator] - RemoteWebElement.toString();
			 */
			Map<String, Object> rootElement = (Map<String, Object>) returnObject;

			transform (rootElement, document, document);
			log("buid doc");
			return document;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private void outToLog(StringBuilder sb, Map<String, Object> map, int level)
	{
		String spaces = "";
		for (int i = 0; i < level; i++)
		{
			spaces += "    ";
		}
		
		sb.append(spaces + "tag  : " + map.get(TAG_FIELD)).append('\n');
		sb.append(spaces + "attr : " + map.get(ATTRIBUTES_FIELD)).append('\n');
		sb.append(spaces + "text : " + map.get(ELEMENT_TEXT_FIELD)).append('\n');
		sb.append(spaces + "rec  : " + map.get(IRemoteApplication.rectangleName)).append('\n');
		
		Object childMap = map.get(ELEMENT_CHILD_FIELD);

		if (childMap != null)
		{
			int i = 0;
			for (Map<String, Object> ch : (List<Map<String, Object>>) childMap)
			{
				sb.append(spaces + "child[" + i++ + "]").append('\n');
				outToLog(sb, ch, level + 1);
			}
		}
	}

	private void transform(Map<String, Object> map, Document document, Node element)
	{
		Object tag = map.get(TAG_FIELD);
		Element newElement = document.createElement(String.valueOf(tag).toLowerCase());
		element.appendChild(newElement);
		element = newElement;
		
		
		setAttributes(element, ((List<Map<String, String>>) map.get(ATTRIBUTES_FIELD)));
		String textElement = (String) map.get(ELEMENT_TEXT_FIELD);
		if (!Str.IsNullOrEmpty(textElement))
		{
			element.setTextContent(textElement);
		}
		Object rec = map.get(IRemoteApplication.rectangleName);
		if (rec != null)
		{
			element.setUserData(IRemoteApplication.rectangleName, createRectangle((Map<String, String>)rec), null);
		}

		Object childMap = map.get(ELEMENT_CHILD_FIELD);

		if (childMap != null)
		{
			for (Map<String, Object> ch : (List<Map<String, Object>>) childMap)
			{
				transform(ch, document, element);
			}
		}
	}

	private void setAttributes(Node element, List<Map<String, String>> map)
	{
		if (map != null && element instanceof Element)
		{
			for (Map<String, String> att : map)
			{
				((Element)element).setAttribute(att.get(ATTRIBUTE_NAME_FIELD), att.get(ATTRIBUTE_VALUE_FIELD));
			}
		}
	}

	private java.awt.Rectangle createRectangle(Map<String, String> map)
	{
		double x = Double.parseDouble(String.valueOf(map.get("left")));
		double y = Double.parseDouble(String.valueOf(map.get("top")));
		double h = Double.parseDouble(String.valueOf(map.get("height")));
		double w = Double.parseDouble(String.valueOf(map.get("width")));
		return new java.awt.Rectangle((int)x, (int)y, (int)w, (int)h);
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

	private void log(String message)
	{
		long newTime = System.currentTimeMillis();
		logger.info(message + " " + (newTime - time));
		time = newTime;
	}

	private static long time = System.currentTimeMillis();

	private WebDriverListenerNew driver;

	private boolean needTune = true;
	private double offsetX;
	private double offsetY;
	private final int repeatLimit = 5;
	private JSInjection jsInjection;
	public OperationExecutor<WebElement> operationExecutor;
	private Logger logger = null;
}
