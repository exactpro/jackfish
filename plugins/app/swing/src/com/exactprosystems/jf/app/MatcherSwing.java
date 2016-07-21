////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.app.exception.ElementIsNotFoundException;
import com.exactprosystems.jf.api.app.exception.ParameterIsNullException;
import com.exactprosystems.jf.api.common.Str;

import org.apache.log4j.Logger;
import org.fest.swing.core.GenericTypeMatcher;
import org.w3c.dom.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import java.awt.*;
import java.rmi.RemoteException;


public class MatcherSwing <T extends Component> extends GenericTypeMatcher<T>
{
	public static final String	itemName		= "item";

	public static final String	actionName		= "action";
	public static final String	titleName		= "title";
	public static final String	textName		= "text";
	public static final String	tooltipName		= "tooltip";
	public static final String	nameName		= "name";
	public static final String	className		= "class";
	
	public MatcherSwing(Class<T> type, Component owner, ControlKind controlKind, Locator locator) throws RemoteException
	{
		super(type);
		
		if (locator == null)
		{
			throw new ParameterIsNullException("locator");
		}
		this.locator = locator;

		String xpath = this.locator.getXpath();
		if (!Str.IsNullOrEmpty(xpath) && owner != null)
		{
			try
			{
				Document document = createDocument(owner, true, false);
				XPathFactory factory = XPathFactory.newInstance();
				XPath xPath = factory.newXPath();

				this.nodelist = (NodeList) xPath.compile(xpath).evaluate(document, XPathConstants.NODESET);
				logger.debug("Found by xpath : " + nodelist.getLength());
			}
			catch (Exception pe)
			{
				logger.error(pe.getMessage(), pe);
				throw new ElementIsNotFoundException("Wrong xpath: " + xpath, locator);
			}
		}
		
		logger.debug("=========================================");
		logger.debug("Matcher locator = " + locator);
	}

	public static Document createDocument(Component owner, boolean addItems, boolean addRectangles) throws ParserConfigurationException
	{
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.newDocument();
		buildDom(document, document, owner, addItems, addRectangles);
		
		return document;
	}
	
	public static Component find(Component owner, String xpath)
	{
		try
		{
			Document document = createDocument(owner, true, false);
			XPathFactory factory = XPathFactory.newInstance();
			XPath xPath = factory.newXPath();

			Node node = (Node) xPath.compile(xpath).evaluate(document, XPathConstants.NODE);
			if (node != null)
			{
				return (Component)node.getUserData(itemName);
			}
		}
		catch (Exception pe)
		{
			logger.error(pe.getMessage(), pe);
			return null;
		}

		return null;
	}

	public static String getAction(Component obj)
	{
		String objAction = null;
		
		if (obj instanceof JComboBox)
		{
			objAction = ((JComboBox<?>)obj).getActionCommand();
		}
		else if (obj instanceof JButton)
		{
			objAction = ((JButton)obj).getActionCommand();
		}
		else if (obj instanceof JRadioButton)
		{
			objAction = ((JRadioButton)obj).getActionCommand();
		}
		else if (obj instanceof JToggleButton)
		{
			objAction = ((JToggleButton)obj).getActionCommand();
		}
		else if (obj instanceof JMenuItem)
		{
			objAction = ((JMenuItem)obj).getActionCommand();
		}

		return objAction;
	}

	public static String getTitle(Component obj)
	{
		String objTitle = null;
		
		if (obj instanceof Frame)
		{
			objTitle = ((Frame)obj).getTitle();
		}
		else if (obj instanceof Dialog)
		{
			objTitle = ((Dialog)obj).getTitle();
		}
		return objTitle;
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
			return ((JTextComponent)obj).getText();
		}
		else if (obj instanceof AbstractButton)
		{
			return  ((AbstractButton)obj).getText();
		}
		else if (obj instanceof JLabel)
		{
			return  ((JLabel)obj).getText();
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
	
	public static String getToolTip(Component obj)
	{
		String objText = null;

		if (obj instanceof JComponent)
		{
			objText = ((JComponent)obj).getToolTipText();
		}
		
		return objText;
	}
	
	public static String getName(Component obj)
	{
		String objText = null;

		if (obj != null)
		{
			objText = obj.getName();
		}
		
		return objText;
	}
	
	public static String getClass(Component obj)
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

		//To resolve issue when dialog is hiding under frame
		if (obj instanceof Frame || obj instanceof Dialog)
		{
			((Window)obj).setAlwaysOnTop(false);			
		}
		
		boolean result = obj.isShowing() || obj instanceof JCheckBox && obj.isVisible() 
				|| obj instanceof JTabbedPane && obj.isShowing();
		Component objNew = obj;

		if (!result)
		{
			return false;
		}
		if (this.nodelist != null)
		{
			result = contains(this.nodelist, objNew);
			if (this.locator.useAbsoluteXpath())
			{
				return result;
			}
		}
		if (!Str.IsNullOrEmpty(this.locator.getClazz()))
		{
			result = part(result, false, this.locator.getClazz(),					getClass(objNew));
		}
		if (!Str.IsNullOrEmpty(this.locator.getName()))
		{
			result = part(result, this.locator.isWeak(), this.locator.getName(), 	getName(objNew));
		}
		if (!Str.IsNullOrEmpty(this.locator.getText()))
		{
			result = part(result, this.locator.isWeak(), this.locator.getText(), 	getText(objNew));
		}
		if (!Str.IsNullOrEmpty(this.locator.getTooltip()))
		{
			result = part(result, this.locator.isWeak(), this.locator.getTooltip(), getToolTip(objNew));
		}
		if (!Str.IsNullOrEmpty(this.locator.getTitle()))
		{
			result = part(result, this.locator.isWeak(), this.locator.getTitle(), 	getTitle(objNew));
		}
		if (!Str.IsNullOrEmpty(this.locator.getAction()))
		{
			result = part(result, this.locator.isWeak(), this.locator.getAction(), 	getAction(objNew));
		}
		return result;
	}

    public static void buildDom(Document document, Node current, Component component, boolean addItems, boolean addRectangles)
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
			logger.error("Error on create element with tag : '" + tagName+"'. Component class simple name : '"+ simpleName +"'.");
			throw e;
		}
		if (addItems)
    	{
    		node.setUserData(itemName, component, null);
    	}
		if (addRectangles)
		{
			node.setUserData(IRemoteApplication.rectangleName, getRect(component), null);
		}
    	
		node.setAttribute(actionName,	getAction(component));
		node.setAttribute(titleName, 	getTitle(component));
		node.setAttribute(tooltipName, 	getToolTip(component));
		node.setAttribute(nameName, 	getName(component));
		node.setAttribute(className, 	getClass(component));
		String textContent = getText(component);
		if (!Str.IsNullOrEmpty(textContent))
		{
			node.setTextContent(textContent);
		}

		current.appendChild(node);
    	if (component instanceof Container)
    	{
    		Container container = (Container)component;
    		
    		for(Component child : container.getComponents())
    		{
    			buildDom(document, node, child, addItems, addRectangles);
    		}
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
//		//see Component.class , method getLocationOnWindow
//		Point curLocation = c.getLocation();
//		for (Container parent = c.getParent(); parent != null/* && !(parent instanceof Window)*/; parent = parent.getParent())
//		{
//			curLocation.x += parent.getX();
//			curLocation.y += parent.getY();
//		}
//
//		return new Rectangle(curLocation, c.getSize());
	}

	public static void setLogger(Logger log)
	{
		logger = log;
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

	private boolean part(boolean result, boolean weak, String locatorText, String objText)
	{
		if (locatorText == null || locatorText.isEmpty())
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

	private NodeList nodelist = null;
	private Locator locator = null;

	private static Logger logger = null;
}
