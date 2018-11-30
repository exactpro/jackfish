/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.CheckingLayoutResult;
import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.CookieBean;
import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.app.LocatorAndOperation;
import com.exactprosystems.jf.api.app.NavigateKind;
import com.exactprosystems.jf.api.app.Operation;
import com.exactprosystems.jf.api.app.OperationResult;
import com.exactprosystems.jf.api.app.PerformKind;
import com.exactprosystems.jf.api.app.PluginInfo;
import com.exactprosystems.jf.api.app.RemoteApplication;
import com.exactprosystems.jf.api.app.Resize;
import com.exactprosystems.jf.api.app.Spec;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.app.FeatureNotSupportedException;
import com.exactprosystems.jf.api.error.app.TimeoutException;
import com.exactprosystems.jf.app.WebAppFactory.WhereToOpen;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class SeleniumRemoteApplication extends RemoteApplication
{
	private static final String TAG_FIELD = "tag";
	private static final String ATTRIBUTES_FIELD = "attr";
	private static final String ELEMENT_CHILD_FIELD = "child";
	private static final String ATTRIBUTE_NAME_FIELD = "name";
	private static final String ATTRIBUTE_VALUE_FIELD = "val";
	private static final String ELEMENT_TEXT_FIELD = "text";

	private static final String SCRIPT = "function attrs(e) { \n" +
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

	private Alert currentAlert;
	private	Exception threadException = null;
	private static long time = System.currentTimeMillis();

	private WebDriverListenerNew driver;
	private PluginInfo info;

	private final int repeatLimit = 5;
	private SeleniumOperationExecutor operationExecutor;
	private static Logger logger = Logger.getLogger(SeleniumRemoteApplication.class);

	public SeleniumRemoteApplication()
	{
	}

	@Override
	protected void createLoggerDerived(String logName, String serverLogLevel, String serverLogPattern) throws Exception
	{
		System.out.println(String.format("createLoggerDerived(%s, %s, %s) is called", logName, serverLogLevel, serverLogPattern));
		Logger rootLogger = Logger.getRootLogger();
		Layout layout = new PatternLayout(serverLogPattern);
		Appender appender = new FileAppender(layout, logName);
		rootLogger.addAppender(appender);
		rootLogger.setLevel(Level.toLevel(serverLogLevel, Level.ALL));
	}

    @Override
    protected void setPluginInfoDerived(PluginInfo info) throws Exception
    {
        this.info = info;
        this.operationExecutor.setPluginInfo(info);
    }

	@Override
	public Serializable getProperty(String name, Serializable prop) throws RemoteException
	{
		if (this.driver != null)
		{
			switch (name)
			{
				case WebAppFactory.propertyUrlName:
					return this.driver.getCurrentUrl();

				case WebAppFactory.propertyTitle:
					return this.driver.getTitle();
					
                case WebAppFactory.propertyAllTitles:
                    return new ArrayList<>(titles());

                case WebAppFactory.propertyCookie:
			        Cookie cookie = this.driver.manage().getCookieNamed("" + prop);
			        return new CookieBean(cookie.getName(), cookie.getValue())
                            .setPath(cookie.getPath())
                            .setDomain(cookie.getDomain())
                            .setSecure(cookie.isSecure())
                            .setHttpOnly(cookie.isHttpOnly())
                            .setExpary(cookie.getExpiry());

                case WebAppFactory.propertyAllCookies:
                    Set<Cookie> set = this.driver.manage().getCookies();
                    return set.stream().map(c -> new CookieBean(c.getName(), c.getValue())
                                .setPath(c.getPath())
                                .setDomain(c.getDomain())
                                .setSecure(c.isSecure())
                                .setHttpOnly(c.isHttpOnly())
                                .setExpary(c.getExpiry())
                    ).collect(Collectors.toCollection(ArrayList::new));
			}
		}
		return null;
	}

    @Override
    public void setProperty(String name, Serializable prop) throws RemoteException
    {
		try
		{
			if (this.driver != null)
			{
				switch (name)
				{
					case WebAppFactory.propertyAddCookie:
						if (prop instanceof CookieBean)
						{
							CookieBean bean = (CookieBean)prop;
							Cookie cookie = new Cookie.Builder(bean.name, bean.value)
									.path(bean.path)
									.domain(bean.domain)
									.expiresOn(bean.expiry)
									.isSecure(bean.isSecure)
									.isHttpOnly(bean.isHttpOnly)
									.build();
							this.driver.manage().addCookie(cookie);
						}
						break;

					case WebAppFactory.propertyRemoveCookie:
						this.driver.manage().deleteCookieNamed(prop.toString());
						break;

					case WebAppFactory.propertyRemoveAllCookies:
						this.driver.manage().deleteAllCookies();
						break;

					case WebAppFactory.propertyUrlName:
						this.driver.get(prop.toString());
						break;
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
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
			// XXX change to something meaningful
			logger.info("##########################################################################################################");
			
			WebPluginArguments arguments = new WebPluginArguments();
			arguments.setBrowserName(args.get(WebAppFactory.browserName));
			arguments.setStartUrl(args.get(WebAppFactory.urlName));
			arguments.setChromeDriverPath(args.get(WebAppFactory.chromeDriverPathName));
			arguments.setChromeDriverBinary(args.get(WebAppFactory.chromeDriverBinary));
			arguments.setGeckoDriverPath(args.get(WebAppFactory.geckoDriverPathName));
			arguments.setIEDriverPath(args.get(WebAppFactory.ieDriverPathName));
			arguments.setFirefoxProfileDirectory(args.get(WebAppFactory.firefoxProfileDir));
			arguments.setUsePrivateMode(Boolean.valueOf(args.get(WebAppFactory.usePrivateMode))); // TODO allow to omit it
			arguments.setDriverLogging(Boolean.valueOf(args.get(WebAppFactory.isDriverLogging))); // TODO allow to omit it
			arguments.setAdditionalParameters(args.get(WebAppFactory.additionalParameters));

			if (arguments.getBrowserName() == null)
			{
				throw new Exception(R.SELENIUM_REMOTE_APP_EMPTY_BROWSER_EXCEPTION.get());
			}

			logger.info("Starting " + arguments.getBrowserName() + " on " + arguments.getStartUrl());

            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                    	Browser browser = null;
                    	try
                    	{
            				browser = Browser.valueOf(arguments.getBrowserName().toUpperCase());
            			}
                    	catch (Exception e)
                    	{
            				throw new Exception(R.SELENIUM_REMOTE_APP_WRONG_BROWSER_NAME.get());
            			}

                        driver = new WebDriverListenerNew(browser.createDriver(arguments));
                        operationExecutor = new SeleniumOperationExecutor(driver, SeleniumRemoteApplication.super.useTrimText);

                        logger.debug("Before driver.get(" + arguments.getStartUrl() + ")");
						if (!Str.IsNullOrEmpty(arguments.getStartUrl())) {
							driver.get(arguments.getStartUrl());
						}
						logger.debug("After driver.get(). Current url : " + driver.getCurrentUrl());
                        if (browser != Browser.ANDROIDBROWSER && browser != Browser.ANDROIDCHROME && !Str.IsNullOrEmpty(arguments.getStartUrl()))
                        {
                            driver.manage().window().maximize();
                            logger.debug("After driver.maximize()");
                        }
						logger.info("Selenium session id : " + ((RemoteWebDriver) driver.getWrappedDriver()).getSessionId());
					}
                    catch (Exception e)
                    {
                    	threadException = e;
                    }
                }
            });
            try
            {
                t.start();
                logger.debug("Before join");
                t.join(60 * 1000);
                if (threadException != null)
                {
                	Exception ex = threadException;
                	threadException = null;
					throw ex;
				}
                logger.debug("After join");
            }
            catch (InterruptedException e)
            { 
                // ignore
                logger.warn("Selenium driver start up thread was interrupted", e);
            }
            if (t.isAlive())
            { 
                // Thread is still alive. It means that timeout is expired and we need to abort the thread 
                logger.debug("Before interrupt");
                t.interrupt();
                this.driver = null;
                logger.debug("Before throw");
                throw new TimeoutException(R.SELENIUM_REMOTE_APP_PAGE_LOADING.get());
            }			
            
            if (this.driver == null || this.operationExecutor == null)
            {
                throw new Exception(R.SELENIUM_REMOTE_APP_FAIL_DRIVER_CREATION.get());
            }
		}
		catch (Exception e)
		{
			logger.error("Unable to start selenium driver", e);
			throw new Exception("Unable to start selenium driver: " + e.getMessage());
		}
		
		return -1;
	}

	@Override
	protected void stopDerived(boolean needKill) throws Exception
	{
		if (driver != null) {
			try {
				for (String handle : this.driver.getWindowHandles()) {
					this.driver.switchTo().window(handle);
					break;
				}
			}
			catch(WebDriverException e)
			{
				logger.error("Browser has been closed");
				logger.error(e.getMessage(), e);
			}
			finally {
				this.driver.quit();
			}
		}
	}

	@Override
	protected void refreshDerived() throws Exception
	{
		this.driver.navigate().refresh();
	}

	@Override
	protected String getAlertTextDerived() throws Exception
	{
		try
		{
			this.currentAlert = this.driver.switchTo().alert();
		}
		catch (NoAlertPresentException e)
		{
			throw new RemoteException(R.SELENIUM_REMOTE_APP_ALERT_IS_NOT_PRESENT.get());
		}
		return this.currentAlert.getText();
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
				this.driver.navigate().forward();
				break;
		}
	}

	@Override
	protected void setAlertTextDerived(String text, PerformKind performKind) throws Exception
	{
		logger.debug(String.format("setAlertTextDerived(%s,%s)", text, performKind));
		if (this.currentAlert == null)
		{
			throw new RemoteException(R.SELENIUM_REMOTE_APP_ALERT_IS_NOT_PRESENT.get());
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
	protected void resizeDerived(Resize resize, int height, int width) throws Exception
	{
		if (resize != null)
		{
			switch (resize)
			{
				case Maximize: this.driver.manage().window().maximize(); return;
				case Minimize: throw new UnsupportedOperationException();
				case Normal: return;
			}
		}
		else
		{
			this.driver.manage().window().setSize(new Dimension(width, height));
		}
	}

    @Override
    protected void resizeDialogDerived(Locator element, Resize resize, int height, int width) throws Exception
    {
		// The action isn't relevant to the web-plugin
    }

	@Override
	protected java.awt.Dimension getDialogSizeDerived(Locator owner) throws Exception
	{
		throw new FeatureNotSupportedException(R.SELENIUM_REMOTE_APP_GET_DIALOG_SIZE.get());
	}

	@Override
	protected java.awt.Point getDialogPositionDerived(Locator owner) throws Exception
	{
		throw new FeatureNotSupportedException(R.SELENIUM_REMOTE_APP_GET_DIALOG_POSITION.get());
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
                            String actual = String.valueOf(getProperty(name, null));
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
	protected void switchToFrameDerived(Locator owner, Locator element) throws Exception
	{
		Exception real = null;
		int repeat = 1;
		do
		{
			try
			{
				if (element == null)
				{
					this.driver.switchTo().defaultContent();
				}
				else
				{
					WebElement component = this.operationExecutor.find(owner, element);
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
				throw new RemoteException(R.SELENIUM_REMOTE_APP_OWNER_NOT_FOUND.get());
			}

			if (owners.size() > 1)
			{
				throw new RemoteException(String.format(R.SELENIUM_REMOTE_APP_TOO_MUCH_OWNERS.get(), owners.size()));
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
					BufferedImage fullImage = getImage();
					return new ImageWrapper(fullImage);
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
					throw new Exception(R.SELENIUM_OPERATION_EXECUTOR_ELEMENT_OUT_OF_SCREEN.get());
				}

				BufferedImage bufferedImage = getImage();
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

	private BufferedImage getImage() throws IOException
	{
		byte[] bytes = this.driver.getScreenshotAs(OutputType.BYTES);
		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes))
		{
			return ImageIO.read(bais);
		}
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
			throw new Exception(R.SELENIUM_REMOTE_APP_URL_IS_NULL.get());
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

	//changed cause if only one window was opened in the browser, the browser was closed, but the process remained even after the application was stopped
	@Override
	protected String closeWindowDerived() throws Exception
	{
		String title = this.driver.getTitle();
		if (this.driver.getWindowHandles().size() > 1) {
			this.driver.close();
			//todo think about switch to another window after close current
		}
		else {
			this.driver.quit();
		}
		return title;
	}

	@Override
	protected void startNewDialogDerived() throws Exception
	{
		this.operationExecutor.clearModifiers();
	}

	@Override
	protected void moveWindowDerived(int x, int y) throws Exception
	{
		throw new FeatureNotSupportedException("moveWindow");
	}

	@Override
	protected void moveDialogDerived(Locator owner, int x, int y) throws Exception {
		throw new FeatureNotSupportedException("moveDialog");
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
}
