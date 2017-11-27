////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.wizard;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.Str;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WizardMatcher
{
    public WizardMatcher(PluginInfo pluginInfo)
    {
        this.pluginInfo = pluginInfo;
    }

	public List<Node> findAll(Node from, Locator locator, String nodeName) throws Exception
	{
		if (from == null || locator == null)
		{
			return Collections.emptyList();
		}
		Visibility visibility = locator.getVisibility();
		String xpathStr = xpathFromControl(locator.getControlKind(), locator, nodeName);

		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression compile = xpath.compile(xpathStr);
		NodeList nodeList = (NodeList) compile.evaluate(from, XPathConstants.NODESET);
		return IntStream.range(0, nodeList.getLength())
				.mapToObj(nodeList::item)
				.filter(n ->
				{
					if (visibility == Visibility.Visible)
					{
						return true;
					}
					Boolean visible = (Boolean)n.getUserData(IRemoteApplication.visibleName);
					return visible != null && visible.booleanValue();
				})
				.collect(Collectors.toList());
	}

	public List<Node> findAll(Node from, Locator locator) throws Exception
    {
		return this.findAll(from, locator, null);
	}
    
    private String xpathFromControl(ControlKind controlKind, Locator locator, String nodeName)
    {
        if (locator.getXpath() != null)
        {
            return locator.getXpath();
        }
        if (controlKind == null)
        {
            return null;
        }
        Set<String> setNodes = this.pluginInfo.nodeByControlKind(controlKind);
		if (!Str.IsNullOrEmpty(nodeName))
		{
			setNodes.add(nodeName);
		}
		String[] nodes = setNodes.toArray(new String[setNodes.size()]);
		return complexXpath(locator, nodes);
    }

    private String complexXpath(Locator locator, String ... strings)
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
			sb.append(this.full(idName, locator.getUid()));
        }
        if (locator.getClazz() != null)
        {
            for (String part : locator.getClazz().split(" "))
            {
                sb.append(separator); separator = " and ";

                if (part.startsWith("!"))
                {
					sb.append(String.format("not (%s)", this.contains(className, part.substring(1))));
                }
                else
                {
					sb.append(this.contains(className, part));
                }
            }
        }
        if (locator.getName() != null)
        {
            sb.append(separator); separator = " and ";
			sb.append(this.full(nameName, locator.getName()));
        }
        if (locator.getTitle() != null)
        {
            sb.append(separator); separator = " and ";
			sb.append(this.contains(titleName, locator.getTitle()));
        }
        if (locator.getAction() != null)
        {
            sb.append(separator); separator = " and ";
			sb.append(this.full(actionName, locator.getAction()));
        }
        if (locator.getText() != null)
        {
            sb.append(separator); separator = " and ";
            if (locator.isWeak())
            {
				sb.append(String.format("contains(.,'%s') or contains(@%s,'%s')", locator.getText(), textName, locator.getText()));
			}
            else
            {
                sb.append(String.format("(.='%s') or @%s='%s' or text()='%s'", locator.getText(), textName, locator.getText(), locator.getText()));
            }
        }
        if (locator.getTooltip() != null)
        {
            sb.append(separator);
			sb.append(this.full(tooltipName, locator.getTooltip()));
        }
        return sb.length() == 0 ? "*" : sb.toString();
    }

	private String full(String key, String value)
	{
		return String.format("@%s='%s'", key, value);
	}

	private String contains(String key, String value)
	{
		return String.format("contains(@%s, '%s')", key, value);
	}

	private PluginInfo pluginInfo;
}
