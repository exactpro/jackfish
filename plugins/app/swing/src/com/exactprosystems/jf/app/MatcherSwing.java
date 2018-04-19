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

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.NullParameterException;
import org.apache.log4j.Logger;
import org.fest.swing.core.GenericTypeMatcher;
import org.w3c.dom.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.Set;

public class MatcherSwing<T extends Component> extends GenericTypeMatcher<T>
{
    public static final String itemName = "item";

    private PluginInfo         info;

    public MatcherSwing(PluginInfo info, Class<T> type, Component owner, ControlKind controlKind, Locator locator)
            throws RemoteException
    {
        super(type);

        if (locator == null)
        {
            throw new NullParameterException("locator");
        }
        this.info = info;
        this.locator = locator;

        String xpath = this.locator.getXpath();
        if (!Str.IsNullOrEmpty(xpath) && owner != null)
        {
            try
            {
                Document document = createDocument(this.info, owner, true, false);
                XPathFactory factory = XPathFactory.newInstance();
                XPath xPath = factory.newXPath();
                Node root = document;
                try
                {
                    XPathExpression compile = xPath.compile("/*");
                    root = (Node) compile.evaluate(document, XPathConstants.NODE);
                }
                catch (XPathExpressionException e)
                {
                }

                this.nodelist = (NodeList) xPath.compile(xpath).evaluate(root, XPathConstants.NODESET);
                logger.debug("Found by xpath : " + nodelist.getLength());
            }
            catch (Exception pe)
            {
                logger.error(pe.getMessage(), pe);
                throw new ElementNotFoundException(String.format(R.MATCHER_SWING_WRONG_XPATH.get(), xpath), locator);
            }
        }

        logger.debug("=========================================");
        logger.debug("Matcher locator = " + locator);
    }

    public static void setLogger(Logger log)
    {
        logger = log;
    }

    public static Document createDocument(PluginInfo info, Component owner, boolean addItems, boolean addRectangles)
            throws ParserConfigurationException
    {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.newDocument();
        buildDom(info, document, document, owner, addItems, addRectangles);

        return document;
    }

    public static String getText(Component obj)
    {
        if (obj instanceof JTable)
        {
            StringBuilder sb = new StringBuilder();
            JTable table = (JTable) obj;
            for (int row = 0; row < table.getRowCount(); ++row)
            {
                for (int column = 0; column < table.getColumnCount(); ++column)
                {
                    String someText = String.valueOf(table.getValueAt(row, column));
                    if (someText != null && !someText.isEmpty())
                    {
                        sb.append(someText);
                        sb.append('|');
                    }
                }
                sb.append('\n');
            }
            return sb.toString();
        }
        else if (obj instanceof JTextComponent)
        {
            return ((JTextComponent) obj).getText();
        }
        else if (obj instanceof AbstractButton)
        {
            return ((AbstractButton) obj).getText();
        }
        else if (obj instanceof JLabel)
        {
            return ((JLabel) obj).getText();
        }
        else if (obj instanceof JToolTip)
        {
            return ((JToolTip) obj).getTipText();
        }
        else
        {
            return null;
        }
    }

    private static String getAction(Component obj)
    {
        String objAction = null;

        if (obj instanceof JComboBox)
        {
            objAction = ((JComboBox<?>) obj).getActionCommand();
        }
        else if (obj instanceof JButton)
        {
            objAction = ((JButton) obj).getActionCommand();
        }
        else if (obj instanceof JRadioButton)
        {
            objAction = ((JRadioButton) obj).getActionCommand();
        }
        else if (obj instanceof JToggleButton)
        {
            objAction = ((JToggleButton) obj).getActionCommand();
        }
        else if (obj instanceof JMenuItem)
        {
            objAction = ((JMenuItem) obj).getActionCommand();
        }

        return objAction;
    }

    private static String getTitle(Component obj)
    {
        String objTitle = null;

        if (obj instanceof Frame)
        {
            objTitle = ((Frame) obj).getTitle();
        }
        else if (obj instanceof Dialog)
        {
            objTitle = ((Dialog) obj).getTitle();
        }
        return objTitle;
    }

    private static String getToolTip(Component obj)
    {
        String objText = null;

        if (obj instanceof JComponent)
        {
            objText = ((JComponent) obj).getToolTipText();
        }

        return objText;
    }

    private static String getName(Component obj)
    {
        String objText = null;

        if (obj != null)
        {
            objText = obj.getName();
        }

        return objText;
    }

    private static String getClass(Component obj)
    {
        String objText = null;

        if (obj != null)
        {
            objText = obj.getClass().getName();
        }

        return objText;
    }

    @Override
    protected boolean isMatching(Component obj)
    {
        if (obj == null)
        {
            return false;
        }

        // To resolve issue when dialog is hiding under frame
        if (obj instanceof Frame || obj instanceof Dialog)
        {
            ((Window) obj).setAlwaysOnTop(false);
        }

        boolean result = isVisible(obj);
        if(this.locator.getVisibility() == Visibility.Visible)
        {
            result = true;
        }
        Component objNew = obj;

        if (!result)
        {
            return false;
        }
        if (this.nodelist != null)
        {
            result = contains(this.nodelist, objNew);
            if (this.locator.getXpath() != null && !this.locator.getXpath().isEmpty())
            {
                return result;
            }
        }
        
        result = part(result, this.locator, LocatorFieldKind.CLAZZ,   getClass(objNew));
        result = part(result, this.locator, LocatorFieldKind.NAME,    getName(objNew));
        result = part(result, this.locator, LocatorFieldKind.TEXT,    getText(objNew));
        result = part(result, this.locator, LocatorFieldKind.TOOLTIP, getToolTip(objNew));
        result = part(result, this.locator, LocatorFieldKind.TITLE,   getTitle(objNew));
        result = part(result, this.locator, LocatorFieldKind.ACTION,  getAction(objNew));

        return result;
    }

    private static boolean isVisible(Component obj)
    {
        boolean result = obj.isShowing() || obj instanceof JCheckBox && obj.isVisible()
                || obj instanceof JTabbedPane && obj.isShowing();
        return result;
    }

    private static void buildDom(PluginInfo info, Document document, Node current, Component component, boolean addItems, boolean addRectangles)
    {
        if (component == null)
        {
            return;
        }

        Element node = null;
        String simpleName = component.getClass().getSimpleName();
        if (simpleName.isEmpty())
        {
            simpleName = component.getClass().getName();
        }
        String tagName = simpleName.replaceAll("\\$", "_");
        try
        {
            node = document.createElement(tagName);
        }
        catch (DOMException e)
        {
            logger.error("Current component : " + component);
            logger.error("Error on create element with tag : '" + tagName + "'. Component class simple name : '"
                    + simpleName + "'.");
            throw e;
        }
        if (addItems)
        {
            node.setUserData(itemName, component, null);
        }
        if (addRectangles)
        {
			node.setAttribute(IRemoteApplication.rectangleName, Converter.rectangleToString(getRect(component)));
			node.setAttribute(IRemoteApplication.visibleName, "" + isVisible(component));

			//			node.setUserData(IRemoteApplication.rectangleName, getRect(component), null);
			//            boolean visible = isVisible(component);
			//            node.setUserData(IRemoteApplication.visibleName, visible, null);
		}

        String className    = info.attributeName(LocatorFieldKind.CLAZZ);
        String nameName     = info.attributeName(LocatorFieldKind.NAME);
        String titleName    = info.attributeName(LocatorFieldKind.TITLE);
        String actionName   = info.attributeName(LocatorFieldKind.ACTION);
        String tooltipName  = info.attributeName(LocatorFieldKind.TOOLTIP);

        node.setAttribute(actionName,   getAction(component));
        node.setAttribute(titleName,    getTitle(component));
        node.setAttribute(tooltipName,  getToolTip(component));
        node.setAttribute(nameName,     getName(component));
        node.setAttribute(className,    getClass(component));

		addBaseClass(node, component, info);

        String textContent = getText(component);
        if (!Str.IsNullOrEmpty(textContent))
        {
            node.setTextContent(textContent);
        }

        current.appendChild(node);
        if (component instanceof Container)
        {
            Container container = (Container) component;

            for (Component child : container.getComponents())
            {
                buildDom(info, document, node, child, addItems, addRectangles);
            }
        }
    }

	private static void addBaseClass(Element node, Component component, PluginInfo info)
	{
		Set<Class<?>> allParents = SwingAppFactory.ALL_PARENETS;
		Class<?> clazz = component.getClass();
		while (clazz != null)
		{
			if (allParents.contains(clazz))
			{
				node.setAttribute(IRemoteApplication.baseParnetName, clazz.getName());
				break;
			}
			clazz = clazz.getSuperclass();
		}
	}

	static Rectangle getRect(Component c)
    {
        logger.debug("getRect is showing ? " + c.isShowing());
        logger.debug("component " + c);
        logger.debug("component hc" + c.hashCode());
        if (c.isShowing())
        {
            return new Rectangle(c.getLocationOnScreen(), c.getSize());
        }
        return new Rectangle(0, 0, 0, 0);
    }

    private boolean contains(NodeList nodelist, Component objNew)
    {
        for (int i = 0; i < nodelist.getLength(); i++)
        {
            Node n = nodelist.item(i);
            if (n.getUserData(itemName).equals(objNew))
            {
                return true;
            }
        }

        return false;
    }

    private boolean part(boolean result, Locator locator, LocatorFieldKind kind, String objText)
    {
        if (kind == null)
        {
            return result;
        }
        boolean weak = locator.isWeak();
        String locatorText = Str.asString(locator.get(kind));
        if (Str.IsNullOrEmpty(locatorText))
        {
            return result;
        }

        if (objText != null)
        {
            if (weak)
            {
                result = result && objText.contains(locatorText);
            }
            else
            {
                result = result && objText.equals(locatorText);
            }
        }
        else
        {
            return false;
        }

        return result;
    }


    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("MatcherSwing :").append("\n");
        if (nodelist != null)
        {
            builder.append("found by xpath : ").append(nodelist.getLength()).append("\n");
        }
        if (locator != null)
        {
            builder.append("locator : ").append(locator);
        }
        return builder.toString();
    }

    private NodeList      nodelist = null;
    private Locator       locator  = null;

    private static Logger logger   = null;
}
