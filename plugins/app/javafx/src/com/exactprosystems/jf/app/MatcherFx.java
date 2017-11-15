////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.NullParameterException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MatcherFx
{
	private static Logger logger = null;
	private Locator locator;
	static final String itemName = "item";
	private PluginInfo info;
	private NodeList   nodelist;

	private EventTarget owner;

	private static Set<Class<?>> parents;

	public MatcherFx(PluginInfo info, Locator locator, EventTarget owner) throws RemoteException
	{
		if (locator == null)
		{
			throw new NullParameterException("locator");
		}
		this.locator = locator;
		this.info = info;
		this.owner = owner;

		String locatorXpath = this.locator.getXpath();
		if (!Str.IsNullOrEmpty(locatorXpath) && owner != null)
		{
			try
			{
				org.w3c.dom.Document document = createDocument(this.info, owner, true, false);

				XPathFactory factory = XPathFactory.newInstance();
				XPath xPath = factory.newXPath();
				org.w3c.dom.Node root = document;

				try
				{
					XPathExpression compile = xPath.compile("/*");
					root = (org.w3c.dom.Node) compile.evaluate(document, XPathConstants.NODE);
				}
				catch (XPathExpressionException e)
				{
				}

				this.nodelist = (NodeList) xPath.compile(locatorXpath).evaluate(root, XPathConstants.NODESET);
				logger.debug("Found by xpath : " + nodelist.getLength());
			}
			catch (Exception pe)
			{
				logger.error(pe.getMessage(), pe);
				throw new ElementNotFoundException("Wrong xpath: " + locatorXpath, locator);
			}
		}

		logger.debug("=========================================");
		logger.debug("Matcher locator = " + locator);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("MatcherFx :").append("\n");
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

	//region work with find Nodes
	public List<EventTarget> findAll()
	{
		List<EventTarget> nodes = new ArrayList<>();
		collect(this.owner, nodes);
		return nodes.stream()
				.filter(this::isMatches)
				.collect(Collectors.toList());
	}

	private boolean isMatches(EventTarget target)
	{
		if (target == null)
		{
			return false;
		}
		if (target instanceof Stage)
		{
			Platform.runLater(((Stage) target)::toFront);
		}
		boolean result = isVisible(target);
		if (this.locator.getVisibility() == Visibility.Visible)
		{
			result = true;
		}
		if (!result)
		{
			return false;
		}

		if (this.nodelist != null)
		{
			result = IntStream.range(0, nodelist.getLength())
					.mapToObj(nodelist::item)
					.anyMatch(n -> n.getUserData(itemName).equals(target));
			if (!Str.IsNullOrEmpty(this.locator.getXpath()))
			{
				return result;
			}
		}

		//we need check that target is instance of locator.getControlKind()
		boolean classIsFound = checkClass(target);

		if (!classIsFound)
		{
			return false;
		}

		result = part(result, this.locator, LocatorFieldKind.UID,     getId(target));
		result = part(result, this.locator, LocatorFieldKind.CLAZZ,   getClass(target));
		result = part(result, this.locator, LocatorFieldKind.TEXT,    getText(target));
		result = part(result, this.locator, LocatorFieldKind.TOOLTIP, getToolTip(target));
		result = part(result, this.locator, LocatorFieldKind.TITLE,   getTitle(target));

		return result;
	}

	private boolean checkClass(EventTarget target)
	{
		Set<String> strClasses = this.info.nodeByControlKind(locator.getControlKind());
		for (String strClass : strClasses)
		{
			if (strClass.equals(PluginInfo.ANY_TYPE))
			{
				return true;
			}
			try
			{
				Class<?> clazz = Class.forName(strClass);
				boolean classIsFound = clazz.isAssignableFrom(target.getClass());
				if (classIsFound)
				{
					return true;
				}
			}
			catch (ClassNotFoundException ignored)
			{}
		}
		return false;
	}

	private void collect(EventTarget eventTarget, List<EventTarget> nodes)
	{
		nodes.add(eventTarget);
		if (eventTarget instanceof RootContainer)
		{
			//this means that we pass owner as null for searching of locator
			RootContainer rootContainer = (RootContainer) eventTarget;
			rootContainer.getStages().forEach(s -> collect(s, nodes));
		}
		else if (eventTarget instanceof Stage)
		{
			collect(((Stage) eventTarget).getScene().getRoot(), nodes);
		}
		else if (eventTarget instanceof Parent)
		{
			ObservableList<Node> children = ((Parent) eventTarget).getChildrenUnmodifiable();
			children.forEach(child -> collect(child, nodes));
		}
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
	//endregion

	//region work with xml document
	private static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException
	{
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}

	public static org.w3c.dom.Document createDocument(PluginInfo info, EventTarget owner, boolean addItems, boolean addRectangles) throws ParserConfigurationException
	{
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		org.w3c.dom.Document document = builder.newDocument();
		buildDom(info, document, document, owner, addItems, addRectangles);

		return document;
	}

	private static void buildDom(PluginInfo info, org.w3c.dom.Document document, org.w3c.dom.Node current, EventTarget component, boolean addItems, boolean addRectangles)
	{
		if (component == null)
		{
			return;
		}

		Element node;
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
			logger.error("Error on create element with tag : '" + tagName + "'. Component class simple name : '" + simpleName + "'.");
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
		}

		String className = info.attributeName(LocatorFieldKind.CLAZZ);
		String idName = info.attributeName(LocatorFieldKind.UID);
		String titleName = info.attributeName(LocatorFieldKind.TITLE);
		String tooltipName = info.attributeName(LocatorFieldKind.TOOLTIP);


		setNodeAttribute(node, titleName, getTitle(component));
		setNodeAttribute(node, tooltipName, getToolTip(component));
		setNodeAttribute(node, idName, getId(component));
		setNodeAttribute(node, className, getClass(component));
		addBaseClass(node, component, info);
		String textContent = getText(component);
		if (!Str.IsNullOrEmpty(textContent))
		{
			node.setTextContent(textContent);
		}

		current.appendChild(node);
		if (component instanceof Stage)
		{
			buildDom(info, document, node, ((Stage) component).getScene().getRoot(), addItems, addRectangles);
		}
		else if (component instanceof RootContainer)
		{
			RootContainer rootContainer = (RootContainer) component;
			rootContainer.getStages().forEach(stage -> buildDom(info, document, node, stage, addItems, addRectangles));
		}
		else if (component instanceof Parent)
		{
			Parent container = (Parent) component;

			for (Node child : container.getChildrenUnmodifiable())
			{
				buildDom(info, document, node, child, addItems, addRectangles);
			}
		}
	}

	private static void setNodeAttribute(Element node, String attrName, String attrValue)
	{
		if (!Str.IsNullOrEmpty(attrValue))
		{
			node.setAttribute(attrName, attrValue);
		}
	}

	private static void addBaseClass(Element node, EventTarget target, PluginInfo info)
	{
		Set<Class<?>> allParents = getAllParents(info);
		Class<?> clazz = target.getClass();
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

	private static Set<Class<?>> getAllParents(PluginInfo info)
	{
		if (parents == null)
		{
			parents = Arrays.stream(ControlKind.values())
					.map(info::nodeByControlKind)
					.filter(Objects::nonNull)
					.flatMap(Set::stream)
					.map(str ->
					{
						try
						{
							return Class.forName(str);
						}
						catch (ClassNotFoundException ignored) {;}
						return null;
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
		}
		return parents;
	}

	//endregion

	//region public static methods
	public static String getText(EventTarget target)
	{
		if (target instanceof TableView)
		{
			StringBuilder sb = new StringBuilder();
			TableView table = (TableView) target;
			for (int rowNum = 0; rowNum < table.getItems().size(); ++rowNum)
			{
				for (int colNum = 0; colNum < table.getColumns().size(); ++colNum)
				{
					TableColumn column = ((TableColumn) ((TableView) target).getColumns().get(colNum));
					String someText = String.valueOf(column.getCellObservableValue(rowNum).getValue());
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
		else if (target instanceof Text)
		{
			return ((Text) target).getText();
		}
		else if (target instanceof Button)
		{
			return ((Button) target).getText();
		}
		else if (target instanceof Label)
		{
			return ((Label) target).getText();
		}
		else if (target instanceof Tooltip)
		{
			return ((Tooltip) target).getText();
		}
		else if (target instanceof TextInputControl)
		{
			return ((TextInputControl) target).getText();
		}
		else if (target instanceof Pane)
		{
			StringBuilder sb = new StringBuilder();
			collectAllText(((Pane) target), sb);
			return sb.toString();
		}
		else if (target instanceof Stage)
		{
			return ((Stage) target).getTitle();
		}
		else if (target instanceof Dialog)
		{
			return ((Dialog) target).getTitle();
		}
		else if (target instanceof Labeled)
		{
			return ((Labeled) target).getText();
		}
		else
		{
			return null;
		}
	}

	private static void collectAllText(Pane pane, StringBuilder sb)
	{
		ObservableList<Node> children = pane.getChildren();
		children.forEach(node -> sb.append(getText(node)));
	}

	public static String getAction(EventTarget target)
	{
		String objAction = null;

		if (target instanceof ComboBox)
		{
			objAction = ((ComboBox<?>) target).getOnAction().toString();
		}
		else if (target instanceof Button)
		{
			objAction = ((Button) target).getOnAction().toString();
		}
		else if (target instanceof RadioButton)
		{
			objAction = ((RadioButton) target).getOnAction().toString();
		}
		else if (target instanceof ToggleButton)
		{
			objAction = ((ToggleButton) target).getOnAction().toString();
		}
		else if (target instanceof MenuItem)
		{
			objAction = ((MenuItem) target).getOnAction().toString();
		}

		return objAction;
	}

	public static String getTitle(EventTarget obj)
	{
		String objTitle = null;

		if (obj instanceof Stage)
		{
			objTitle = ((Stage) obj).getTitle();
		}
		else if (obj instanceof Dialog<?>)
		{
			objTitle = ((Dialog) obj).getTitle();
		}
		return objTitle;
	}

	public static String getToolTip(EventTarget obj)
	{
		String objText = null;

		if (obj instanceof Control)
		{
			Tooltip tooltip = ((Control) obj).getTooltip();
			if (tooltip != null)
			{
				objText = tooltip.getText();
			}
		}

		return objText;
	}

	public static String getId(EventTarget target)
	{
		String objText = null;

		if (target instanceof Node)
		{
			objText = ((Node) target).getId();
		}

		return objText;
	}

	public static String getClass(EventTarget target)
	{
		String objText = null;

		if (target instanceof Node)
		{
			objText = ((Node) target).getStyleClass().stream().collect(Collectors.joining(" "));
		}

		return objText;
	}

	public static boolean isVisible(EventTarget target)
	{
		if (target instanceof Scene)
		{
			return ((Scene) target).getWindow().isShowing();
		}
		if (target instanceof Dialog)
		{
			return ((Dialog) target).isShowing();
		}
		if (target instanceof MenuItem)
		{
			return ((MenuItem) target).isVisible();
		}
		if (target instanceof Node)
		{
			return ((Node) target).isVisible();
		}
		if (target instanceof Window)
		{
			return ((Window) target).isShowing();
		}
		return false;
	}

	public static Rectangle getRect(EventTarget target)
	{
		if (target instanceof Window)
		{
			return new Rectangle(
					(int) ((Window) target).getX()
					, (int) ((Window) target).getY()
					, (int) ((Window) target).getWidth()
					, (int) ((Window) target).getHeight()
			);
		}
		if (!(target instanceof Node) || target instanceof RootContainer)
		{
			return new Rectangle(0, 0, 0, 0);
		}
		Node node = (Node) target;
		if (node.isVisible())
		{
			Bounds screenBounds = node.localToScreen(node.getBoundsInLocal());
			int x = (int) screenBounds.getMinX();
			int y = (int) screenBounds.getMinY();
			int width = (int) screenBounds.getWidth();
			int height = (int) screenBounds.getHeight();
			return new Rectangle(x, y, width, height);
		}
		return new Rectangle(0, 0, 0, 0);
	}

	public static String targetToString(EventTarget target)
	{
		if (target instanceof Stage)
		{
			return "Stage : " + ((Stage) target).getTitle();
		}
		else if (target instanceof Dialog)
		{
			return "Dialog : " + ((Dialog) target).getTitle();
		}
		else if (target instanceof Scene)
		{
			return "SceneRoot : " + ((Scene) target).getRoot();
		}
		return target.toString();
	}
	//endregion

	static void setLogger(Logger logger)
	{
		MatcherFx.logger = logger;
	}
}