////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.app.FeatureNotSupportedException;
import com.exactprosystems.jf.api.error.app.TimeoutException;
import com.exactprosystems.jf.app.WebAppFactory.WhereToOpen;
import org.apache.log4j.*;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

public class SeleniumRemoteApplication extends RemoteApplication
{
	private static final String TAG_FIELD = "tag";
	private static final String ATTRIBUTES_FIELD = "attr";
	private static final String ELEMENT_CHILD_FIELD = "child";
	private static final String ATTRIBUTE_NAME_FIELD = "name";
	private static final String ATTRIBUTE_VALUE_FIELD = "val";
	private static final String ELEMENT_TEXT_FIELD = "text";

	private Alert currentAlert;

	private	Exception te = null;

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
		"            " + IRemoteApplication.visibleName + " : (e.offsetWidth > 0 || e.offsetHeight > 0), \n" +
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
    protected void setPluginInfoDerived(PluginInfo info) throws Exception
    {
        this.info = info;
        this.operationExecutor.setPluginInfo(info);
    }

	@Override
	public Serializable getProperty(String name) throws RemoteException
	{
		if (this.driver != null)
		{
			switch (name)
			{
				case WebAppFactory.propertyUrlName:
					return this.driver.getCurrentUrl();

				case WebAppFactory.propertyTitle:
					return this.driver.getTitle();
			}
		}
		return null;
	}

	@Override
	protected int connectDerived(Map<String, String> args) throws Exception
	{
		logger.info("##########################################################################################################");
		throw new FeatureNotSupportedException("Connect");
	}

	@Override
	protected int runDerived(Map<String, String> args) throws Exception
	{
		try
		{
			logger.info("##########################################################################################################");
			String browserName = args.get(WebAppFactory.browserName);
			String url = args.get(WebAppFactory.urlName);
			if (Str.IsNullOrEmpty(browserName) || browserName.equals("null"))
			{
				throw new Exception("Browser can't be null or empty.");
			}
			if (Str.IsNullOrEmpty(url) || url.equals("null"))
			{
				throw new Exception("URL can't be null or empty.");
			}

			String chromeDriverPath = args.get(WebAppFactory.chromeDriverPathName);
			if (chromeDriverPath != null && !chromeDriverPath.isEmpty())
			{
				logger.info(WebAppFactory.chromeDriverPathName + " = " + chromeDriverPath);
				System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, chromeDriverPath);
			}

			String geckoDriverPath = args.get(WebAppFactory.geckoDriverPathName);
			if (geckoDriverPath != null && !geckoDriverPath.isEmpty())
			{
				logger.info(WebAppFactory.geckoDriverPathName + " = " + geckoDriverPath);
				System.setProperty(GeckoDriverService.GECKO_DRIVER_EXE_PROPERTY, geckoDriverPath);
			}
			
			String ieDriverPath = args.get(WebAppFactory.ieDriverPathName);
			if (ieDriverPath != null && !ieDriverPath.isEmpty())
			{
				logger.info(WebAppFactory.ieDriverPathName + " = " + ieDriverPath);
				System.setProperty(InternetExplorerDriverService.IE_DRIVER_EXE_PROPERTY, ieDriverPath);
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

			boolean usePrivateMode = args.get(WebAppFactory.usePrivateMode) != null;
			if (usePrivateMode)
			{
				logger.info("Use private mode for browser");
			}


			logger.info("Starting " + browserName + " on " + url);

            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {

                    	Browser browser = null;
                    	try {
							browser = Browser.valueOf(browserName.toUpperCase());
						} catch (Exception e){
							te =  new Exception("Wrong browser name.");
							throw te;
						}
                        driver = new WebDriverListenerNew(browser.createDriver(chromeDriverBinary, firefoxProfileDirectory, usePrivateMode));
                        operationExecutor = new SeleniumOperationExecutor(driver, logger);

                        logger.info("Before driver.get(" + url + ")");
                        driver.get(url);
                        logger.info("After driver.get(" + url + ")");
                        if(!browser.equals(Browser.ANDROIDBROWSER) && !browser.equals(Browser.ANDROIDCHROME))
                        {
                            driver.manage().window().maximize();
                            logger.info("After driver.maximize()");
                        }
                    }
                    catch (Exception e)
                    {
                    	te = e;
                        logger.error(e.getMessage(), e);
                    }
                    
                }
            });
            try
            {
                t.start();
                logger.info("Before join");
                t.join(60000);
                if (te != null){
                	Exception ne = te;
                	te = null;
					throw ne;
				}
                logger.info("After join");
            }
            catch (InterruptedException e)
            { 
                // ignore
                logger.error(e.getMessage());
            }
            if (t.isAlive())
            { 
                // Thread still alive, we need to abort and it means that timeout expired
                logger.info("Before interrupt");
                t.interrupt();
                this.driver = null;
                logger.info("Before throw");
                throw new TimeoutException("Page loading");
            }			
            
            if (this.driver == null || this.operationExecutor == null)
            {
                throw new Exception("Driver creation is failed");
            }
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
		
		return -1;
	}

	@Override
	protected void stopDerived(boolean needKill) throws Exception
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
	protected void navigateDerived(NavigateKind kind) throws Exception
	{
		switch (kind)
		{

			case BACK:
				this.driver.navigate().back();
				break;
			case FORWARD:
				this.driver.navigate().back();
				break;
		}
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
	protected void resizeDerived(int height, int width, boolean maximize, boolean minimize, boolean normal) throws Exception
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
			this.driver.manage().window().setSize(new Dimension(width, height));
		}
	}

	@Override
	protected String switchToDerived(final Map<String, String> criteria, final boolean softCondition) throws Exception
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

                        StringBuilder sb = new StringBuilder();
                        boolean res = true;
                        for (Entry<String, String> entry : criteria.entrySet())
                        {
                            String name = entry.getKey();
                            String expected = entry.getValue();
                            String actual = String.valueOf(getProperty(name));
                            sb.append(name).append(":").append(actual).append(" ");

                            res = res && (softCondition && actual.contains(expected) || !softCondition && actual.equals(expected));
                        }
                        if (res)
                        {
                            result[0] = sb.toString(); 
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
				logger.debug(msgElementNotLonger(repeat));
			}
			catch (RemoteException e)
			{
				throw e;
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
	protected Collection<String> findAllDerived(Locator owner, Locator locator) throws Exception
	{
		WebElement ownerElement = null;
		if (owner != null)
		{
			By byOwner = new MatcherSelenium(this.info, owner.getControlKind(), owner);
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

		By by = new MatcherSelenium(this.info, locator.getControlKind(), locator);
		List<WebElement> elements = (ownerElement == null ? this.driver.findElements(by) : ownerElement.findElements(by));

		List<String> result = new ArrayList<>();
		for (WebElement element : elements)
		{
			result.add(getElementString(element));
		}
		return result;
	}

	static String getElementString(WebElement element)
	{
		String s = element.getAttribute("outerHTML");
		return s.substring(0, s.indexOf(">") + 1);
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
				log("before image");

				if (element == null)
				{
					File screenshot = this.driver.getScreenshotAs(OutputType.FILE);
					BufferedImage fullImg = ImageIO.read(screenshot);
					return new ImageWrapper(fullImg);
				}

				WebElement component = this.operationExecutor.find(owner, element);

				Point location = component.getLocation();
				Dimension size = component.getSize();

				logger.debug("Location of element : " + location);
				logger.debug("Size of element : " + size);

				java.awt.Rectangle componentRectangle = new Rectangle(location.x, location.y, size.width, size.height);

				Number startX = (Number) driver.executeScript("return document.documentElement.getBoundingClientRect().left");
				Number startY = (Number) driver.executeScript("return document.documentElement.getBoundingClientRect().top");

				Number width = (Number) driver.executeScript(
						"return window.innerWidth || document.documentElement.getBoundingClientRect().right - document.documentElement.getBoundingClientRect().left");
				Number height = (Number) driver.executeScript(
						"return window.innerHeight || document.documentElement.getBoundingClientRect().bottom - document.documentElement.getBoundingClientRect().top");

				Rectangle windowRectangle = new Rectangle(Math.abs(startX.intValue()), Math.abs(startY.intValue()), width.intValue(), height.intValue());
				Rectangle returnRectangle = windowRectangle.intersection(componentRectangle);

				if (!(driver.getWrappedDriver() instanceof FirefoxDriver))
				{
					returnRectangle.setLocation(returnRectangle.x - Math.abs(startX.intValue()), returnRectangle.y - Math.abs(startY.intValue()));
				}

				logger.debug("evaluated rectangle : " + returnRectangle);

				if (returnRectangle.isEmpty())
				{
					throw new Exception("Element out of screen");
				}

				File image = driver.getScreenshotAs(OutputType.FILE);
				BufferedImage bufferedImage = ImageIO.read(image);

				BufferedImage subimage = bufferedImage.getSubimage(returnRectangle.x, returnRectangle.y, returnRectangle.width, returnRectangle.height);
				return new ImageWrapper(subimage);
			}
			catch (StaleElementReferenceException e)
			{
				real = e;
				logger.debug(msgElementNotLonger(repeat));
			}
			catch (RemoteException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				throw new Exception(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}

	private String msgElementNotLonger(int repeat)
	{
		return "Element is no longer attached to the DOM. Try in SeleniumRemoteApplication : " + repeat;
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
				logger.debug(msgElementNotLonger(repeat));
			}
			catch (RemoteException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
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
				logger.debug(msgElementNotLonger(repeat));
			}
			catch (RemoteException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
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
				logger.debug(msgElementNotLonger(repeat));
			}
			catch (RemoteException e)
			{
				throw e;
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				throw new Exception(e.getMessage());
			}
		}
		while (++repeat < repeatLimit);
		throw real;
	}


	@Override
	protected void newInstanceDerived(Map<String, String> args) throws Exception
	{
		String url = args.get(WebAppFactory.urlName);
		if (url == null)
		{
			throw new Exception("url is null");
		}
		
		WhereToOpen whereToOpen = WhereToOpen.OpenInTab;
		
		String inTab = args.get(WebAppFactory.tabName);
		if (inTab != null)
		{
		    whereToOpen = inTab.equals("" + true) ? WhereToOpen.OpenInTab : WhereToOpen.OpenInWindow;
		}
		    
		String whereOpen = args.get(WebAppFactory.whereOpenName);
		if (whereOpen != null)
		{
		    whereToOpen = WhereToOpen.valueOf(whereOpen);
		}
		
		boolean flag = false;
		switch (whereToOpen)
		{
			case OpenNewUrl:
				this.driver.get(url);
				break;

			case OpenInWindow:
				flag = true;
			case OpenInTab:
				this.driver.executeScript(String.format(
						"function createDoc(){"
						+ "var w = window.open('%s' %s,'height='+window.outerHeight+',width='+window.outerWidth)"
						+ "}; createDoc();", url, flag ? ",'_blank'" : "")
				);
				break;

			default:
				this.driver.executeScript(String.format(
						"function createDoc(){"
								+ "var w = window.open('%s')"
								+ "}; createDoc();", url)
				);
		}
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
	protected void startNewDialogDerived() throws Exception
	{

	}

	@Override
	protected void moveWindowDerived(int x, int y) throws Exception
	{
		throw new FeatureNotSupportedException("moveWindow");
	}

	@Override
	protected Document getTreeDerived(Locator owner) throws Exception
	{
		log("start");

		WebElement ownerElement;
		if (owner == null)
		{
			ownerElement = this.driver.findElement(By.tagName("html"));
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

			transform(rootElement, document, document);
			log("buid doc");
			return document;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		return null;
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
		setUserData(map, element);

		Object childMap = map.get(ELEMENT_CHILD_FIELD);

		if (childMap != null)
		{
			for (Map<String, Object> ch : (List<Map<String, Object>>) childMap)
			{
				transform(ch, document, element);
			}
		}
	}

	private void setUserData(Map<String, Object> map, Node element)
	{
		Object rec = map.get(IRemoteApplication.rectangleName);
		if (rec != null)
		{
			((Element) element).setAttribute(IRemoteApplication.rectangleName, Converter.rectangleToString(createRectangle((Map<String, String>) rec)));
		}
		Object vis = map.get(IRemoteApplication.visibleName);
		if (vis != null)
		{
			((Element) element).setAttribute(IRemoteApplication.visibleName, "" + vis);
		}
	}

	private void setAttributes(Node element, List<Map<String, String>> map)
	{
		if (map != null && element instanceof Element)
		{
			for (Map<String, String> att : map)
			{
				try
				{
					((Element) element).setAttribute(att.get(ATTRIBUTE_NAME_FIELD), att.get(ATTRIBUTE_VALUE_FIELD));
				}
				catch (DOMException e)
				{
					//nothing;
				}
			}
		}
	}

	private java.awt.Rectangle createRectangle(Map<String, String> map)
	{
		double x = Double.parseDouble(String.valueOf(map.get("left")));
		double y = Double.parseDouble(String.valueOf(map.get("top")));
		double h = Double.parseDouble(String.valueOf(map.get("height")));
		double w = Double.parseDouble(String.valueOf(map.get("width")));
		return new java.awt.Rectangle((int) x, (int) y, (int) w, (int) h);
	}

	private void log(String message)
	{
		long newTime = System.currentTimeMillis();
		logger.info(message + " " + (newTime - time));
		time = newTime;
	}

	private static long time = System.currentTimeMillis();

	private WebDriverListenerNew driver;
	private PluginInfo info;

	private final int repeatLimit = 5;
	public OperationExecutor<WebElement> operationExecutor;
	private Logger logger = null;
}
