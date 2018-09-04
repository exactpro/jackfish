/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.app;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.app.LocatorFieldKind;
import com.exactprosystems.jf.api.app.PluginInfo;
import com.exactprosystems.jf.api.app.Visibility;
import com.exactprosystems.jf.api.error.app.NullParameterException;

public class MatcherSelenium extends By
{
    public PluginInfo pluginInfo = null;
    
	public MatcherSelenium(PluginInfo info, ControlKind controlKind, Locator locator) throws RemoteException
	{
        this.pluginInfo = info;
		if (locator == null)
		{
			throw new NullParameterException("locator");
		}
		
        this.xpath = xpathFromControl(controlKind, locator);
		this.visibility = locator.getVisibility();

		logger.debug("=========================================");
		logger.debug("Matcher control = " + controlKind);
		logger.debug("Matcher locator = " + locator);
		logger.debug("Matcher xpath   = " + xpath);
	}

	@Override
	public List<WebElement> findElements(SearchContext context)
	{
		logger.debug("-----------------------------------------");
		List<WebElement> found = context.findElements(By.xpath(this.xpath));
		int count = 0;
		int countEnabledElement = 0;
		List<WebElement> result = new ArrayList<WebElement>();
		if (found != null)
		{
			for(WebElement element : found)
			{
				if (element != null)
				{
					if (element.isDisplayed())
					{
						logger.debug("Found element : " + SeleniumRemoteApplication.getElementString(element));
						result.add(element);
					}
					else if (visibility != null)
					{
						if (visibility == Visibility.Visible)
						{
							result.add(element);
						}
					}
					if (element.isEnabled())
					{
						countEnabledElement++;
					}
				}
				else
				{
					count++;
				}
			}
		}

		logger.debug("Matcher found " + result.size() + " elements. Not displayed " + count + " elements. Enabled element : " + countEnabledElement);

		return result;
	}

	//TODO the same code from WizardMatcher
    private String xpathFromControl(ControlKind controlKind, Locator locator)
    {
        if (locator.getXpath() != null && !locator.getXpath().isEmpty())
        {
            return locator.getXpath();
        }
        if (controlKind == null)
        {
            return null;
        }
        Set<String> setNodes = this.pluginInfo.nodeByControlKind(controlKind);
        String[] nodes = setNodes.toArray(new String[setNodes.size()]);
        if (nodes != null)
        {
            return complexXpath(locator, nodes);
        }
        return complexXpath(locator, "*");
    }

    private String complexXpath(Locator locator, String... strings)
    {
        if (strings == null || strings.length == 0)
        {
            return null;
        }

        String separator = "";
        StringBuilder sb = new StringBuilder();
        for (String str : strings)
        {
            String filter = filterForLocator(locator);

            sb.append(separator);
            sb.append(String.format(".//%s[%s]", str, filter));
            separator = " | ";
        }

        return sb.toString();
    }

    private String filterForLocator(Locator locator)
    {
        String idName       = this.pluginInfo.attributeName(LocatorFieldKind.UID);
        String className    = this.pluginInfo.attributeName(LocatorFieldKind.CLAZZ);
        String nameName     = this.pluginInfo.attributeName(LocatorFieldKind.NAME);
        String titleName    = this.pluginInfo.attributeName(LocatorFieldKind.TITLE);
        String actionName   = this.pluginInfo.attributeName(LocatorFieldKind.ACTION);
        String textName     = this.pluginInfo.attributeName(LocatorFieldKind.TEXT);
        String tooltipName  = this.pluginInfo.attributeName(LocatorFieldKind.TOOLTIP);
        
        StringBuilder sb = new StringBuilder();
        String separator = "";

        if (locator.getUid() != null)
        {
            sb.append(separator); separator = " and ";
            sb.append(String.format("@" + idName + "='%s'", locator.getUid()));
        }
        if (locator.getClazz() != null)
        {
            for (String part : locator.getClazz().split(" "))
            {
                sb.append(separator); separator = " and ";

                if (part.startsWith("!"))
                {
                    sb.append(String.format("not (contains(@" + className + ", '%s'))", part.substring(1)));
                }
                else
                {
                    sb.append(String.format("contains(@" + className + ", '%s')", part));
                }
            }
        }
        if (locator.getName() != null)
        {
            sb.append(separator); separator = " and ";
            sb.append(String.format("@" + nameName + "='%s'", locator.getName()));
        }
        if (locator.getTitle() != null)
        {
            sb.append(separator); separator = " and ";
            sb.append(String.format("contains(@" + titleName + ",'%s')", locator.getTitle()));
        }
        if (locator.getAction() != null)
        {
            sb.append(separator); separator = " and ";
            sb.append(String.format("@" + actionName + "='%s'", locator.getAction()));
        }
        if (locator.getText() != null)
        {
            sb.append(separator); separator = " and ";
            if (locator.isWeak())
            {
                sb.append(String.format("contains(.,'%s') or contains(@" + textName + ",'%s')", locator.getText(), locator.getText()));
            }
            else
            {
                sb.append(String.format("(.='%s') or @" + textName + "='%s'", locator.getText(), locator.getText()));
            }
        }
        if (locator.getTooltip() != null)
        {
            sb.append(separator);
            sb.append(String.format("@" + tooltipName + "='%s'", locator.getTooltip()));
        }
        return sb.length() == 0 ? "*" : sb.toString();
    }	
	
	private String xpath;
	private Visibility visibility;
	public static void setLogger(Logger log)
	{
		logger = log;
	}

	protected static Logger logger = null;

}
