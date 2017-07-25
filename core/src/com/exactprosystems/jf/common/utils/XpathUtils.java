////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.utils;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.guidic.Attr;
import com.exactprosystems.jf.tool.custom.xpath.XpathViewer;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class XpathUtils
{
    private XpathUtils()
    {
    }

    public static void applyOffset(Node node, int xOffset, int yOffset)
    {
        boolean isDocument = node.getNodeType() == Node.DOCUMENT_NODE;
        IntStream.range(0, node.getChildNodes().getLength()).mapToObj(node.getChildNodes()::item)
                .filter(item -> item.getNodeType() == Node.ELEMENT_NODE)
                .forEach(item -> applyOffset(item, xOffset, yOffset));
        if (!isDocument)
        {
            Rectangle rec = (Rectangle) node.getUserData(IRemoteApplication.rectangleName);
            if (rec != null)
            {
                rec.x -= xOffset;
                rec.y -= yOffset;
                node.setUserData(IRemoteApplication.rectangleName, rec, null);
            }
        }
    }

    public static List<Rectangle> collectAllRectangles(Node node)
    {
        List<Rectangle> collect = new ArrayList<>();
        boolean isDocument = node.getNodeType() == Node.DOCUMENT_NODE;
        IntStream.range(0, node.getChildNodes().getLength()).mapToObj(node.getChildNodes()::item)
                .filter(item -> item.getNodeType() == Node.ELEMENT_NODE)
                .forEach(item -> collect.addAll(collectAllRectangles(item)));
        if (!isDocument)
        {
            Rectangle rec = (Rectangle) node.getUserData(IRemoteApplication.rectangleName);
            if (rec != null)
            {
                collect.add(rec);
            }
        }
        return collect;
    }

    public static String fullXpath(String relativeXpath, Node relative, Node node, boolean useText, List<String> parameters, boolean longPath)
    {
    	if (node == null)
    	{
    		return "//*";
    	}
    
    	if (relative == null)
    	{
    		if (!longPath)
    		{
    			return "/" + XpathUtils.xpath(node.getParentNode(), node, useText, parameters);
    		}
    		return XpathUtils.xpath(null, node, useText, parameters);
    	}
    	else
    	{
    		Node common = XpathUtils.commonAncestor(relative, node);
    		Node current = relative;
    		String backPath = "";
    		while (current != null && !current.equals(common))
    		{
    			current = current.getParentNode();
    			backPath += "/..";
    		}
    
    		if (!longPath)
    		{
    			return relativeXpath + backPath + "/" + XpathUtils.xpath(node.getParentNode(), node, useText, parameters);
    		}
    		return relativeXpath + backPath + XpathUtils.xpath(common, node, useText, parameters);
    	}
    }

    public static List<Node> evaluate(Node node, String xpathStr)
    {
    	if (xpathStr == null)
    	{
    		return null;
    	}
    	XPath xpath = XPathFactory.newInstance().newXPath();
    	try
    	{
    		XPathExpression compile = xpath.compile(xpathStr);
    		NodeList nodeList = (NodeList) compile.evaluate(node, XPathConstants.NODESET);
    		return IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item).collect(Collectors.toList());
    	}
    	catch (XPathExpressionException e)
    	{
    	}
    
    	return null;
    }

    public static String xpath(Node parent, Node node, boolean useText, List<String> parameters)
    {
    	if (node instanceof Document)
    	{
    		return "";
    	}
    	if (node == null || node.equals(parent))
    	{
    		return "";
    	}
    	return xpath(parent, node.getParentNode(), false, null) + "/" + node.getNodeName() + (parameters != null && !parameters.isEmpty() || useText ?
    			"[" + XpathUtils.getParameters(node, useText, parameters) + "]" : (XpathUtils.hasSiblings(node) ? "[" + XpathUtils.getIndexNode(node) + "]" : ""));
    }

    public static Node getFirst(Node node, String xpathStr)
    {
        List<Node> nodes = evaluate(node, xpathStr);
        return nodes == null || nodes.isEmpty() ? null : nodes.get(0);
    }

    public static Node commonAncestor(Node node1, Node node2)
    {
    	if (node1 == null || node2 == null)
    	{
    		return null;
    	}
    	Iterator<Node> iterator1 = XpathUtils.ancestors(node1).iterator();
    	Iterator<Node> iterator2 = XpathUtils.ancestors(node2).iterator();
    	Node res = null;
    	while (iterator1.hasNext() && iterator2.hasNext())
    	{
    		Node ancestor1 = iterator1.next();
    		Node ancestor2 = iterator2.next();
    		if (!ancestor1.equals(ancestor2))
    		{
    			break;
    		}
    		res = ancestor1;
    	}
    	return res;
    }

    public static List<Node> ancestors(Node node)
    {
    	List<Node> res = new ArrayList<Node>();
    	Node current = node;
    	while (current != null)
    	{
    		res.add(0, current);
    		current = current.getParentNode();
    	}
    	return res;
    }

    public static String getParameters(Node node, boolean useText, List<String> parameters)
    {
    	String res = "";
    	NamedNodeMap attr = node.getAttributes();
    	if (attr != null)
    	{
    		res = parameters.stream().filter(p -> attr.getNamedItem(p) != null).map(p -> "@" + attr.getNamedItem(p)).collect(Collectors.joining(" and "));
    	}
    	if (useText)
    	{
    		res = res + (res.isEmpty() ? "" : " and ") + "contains(text(), \"" + text(node) + "\")";
    	}
    	return res;
    }

    public static String text(Node node)
    {
        if (node == null)
        {
            return null;
        }
    
        StringBuilder sb = new StringBuilder();
        IntStream.range(0, node.getChildNodes().getLength()).mapToObj(i -> node.getChildNodes().item(i)).filter(item -> item.getNodeType() == Node.TEXT_NODE).map(Node::getNodeValue).filter(
                value -> value != null).map(value -> value.trim().replace('\n', ' ')).forEach(sb::append);
        return sb.toString();
    }

    public static int getIndexNode(Node node)
    {
    	int result = 0;
    	Node parentNode = node.getParentNode();
    	NodeList childNodes = parentNode.getChildNodes();
    	for (int i = 0; i < childNodes.getLength(); i++)
    	{
    		Node item = childNodes.item(i);
    		if (item.getNodeName().equals(node.getNodeName()))
    		{
    			result++;
    		}
    		if (item.equals(node))
    		{
    			return result;
    		}
    	}
    	return result;
    }

    public static boolean hasSiblings(Node node)
    {
    	int res = 0;
    	Node parentNode = node.getParentNode();
    	if (parentNode == null)
    	{
    		return false;
    	}
    	NodeList childNodes = parentNode.getChildNodes();
    	for (int i = 0; i < childNodes.getLength(); i++)
    	{
    		if (childNodes.item(i).getNodeName().equals(node.getNodeName()))
    		{
    			if (++res > 1)
    			{
    				return true;
    			}
    		}
    	}
    	return false;
    }

	public static List<String> getAllNodeAttribute(Node node)
	{
		ArrayList<String> params = new ArrayList<>();
		NamedNodeMap attributes = node.getAttributes();
		Optional.ofNullable(attributes)
				.ifPresent(attr -> IntStream.range(0, attr.getLength())
						.mapToObj(attr::item)
						.map(Node::getNodeName)
						.forEach(params::add)
				);
		return params;
	}

	public static boolean isStable(String identifier)
	{
		if (Str.IsNullOrEmpty(identifier))
		{
			return false;
		}
		if (identifier.split(" ").length > 2)
		{
			return false;
		}
		return identifier.matches("^[a-zA-Z\\s]+$");
	}

	public static <T> List<T> shuffle(int mask, List<T> source)
	{
		List<T> res = new ArrayList<T>();
		int oneBit = 0;
		while((oneBit = Integer.lowestOneBit(mask)) != 0)
		{
			res.add(source.get(Integer.numberOfTrailingZeros(mask)));
			mask ^= oneBit;
		}
		return res;
	}


	public static void passTree(Node node, Consumer<Node> func)
	{
		func.accept(node);
		IntStream.range(0, node.getChildNodes().getLength())
				.mapToObj(node.getChildNodes()::item)
				.filter(item -> item.getNodeType() == Node.ELEMENT_NODE)
				.forEach(item -> passTree(item, func));
	}

	public static List<Attr> extractAttributes(Node node)
	{
		List<Attr> attributes = new ArrayList<>();
		NamedNodeMap attrs = node.getAttributes();
		if (attrs != null)
		{
			for (int index = 0; index < attrs.getLength(); index++)
			{
				Node attr = attrs.item(index);
				attributes.add(new Attr(attr.getNodeName(), attr.getNodeValue()));
			}
		}
		return attributes;
	}

	public static class FindLocator
	{
		private static final int        MAX_TRIES = 128;

		private boolean findById;
		private boolean findByAttrs;
		private boolean findByXpath;

		private ToIntBiFunction<Locator, Node> findFunction;
		private String id;
		private ControlKind kind;
		private Node node;

		private PluginInfo pluginInfo;

		private Node owner;

		private FindLocator(ToIntBiFunction<Locator,Node> findFunction, String id, ControlKind kind, Node node, PluginInfo pluginInfo)
		{
			this.findFunction = findFunction;
			this.id = id;
			this.kind = kind;
			this.node = node;
			this.pluginInfo = pluginInfo;
		}

		public static FindLocator start(ToIntBiFunction<Locator,Node> findFunction, String id, ControlKind kind, Node node, PluginInfo pluginInfo)
		{
			return new FindLocator(findFunction, id, kind, node, pluginInfo);
		}

		public FindLocator findById()
		{
			this.findById = true;
			this.pluginInfo = pluginInfo;
			return this;
		}

		public FindLocator findByAttrs()
		{
			this.findByAttrs = true;
			return this;
		}

		public FindLocator findByXpath(Node owner)
		{
			this.owner = owner;
			this.findByXpath = true;
			return this;
		}

		public Locator build()
		{
			Locator locator = null;
			if (this.findById)
			{
				locator = this.locatorById();
				if (locator != null)
				{
					return locator;
				}
			}
			if (this.findByAttrs)
			{
				locator = locatorByAttrs();
				if (locator != null)
				{
					return locator;
				}
			}
			if (this.findByXpath)
			{
				locator = locatorByExtendAttrs();
				if (locator != null)
				{
					return locator;
				}
				locator = locatorByXpath(this.node);
				if (locator != null)
				{
					return locator;
				}
				locator = locatorByRelativeXpath();
				if (locator != null)
				{
					return locator;
				}
			}
			return null;
		}

		//region private methods
		private Locator locatorById()
		{
			if (this.node.hasAttributes())
			{
				String idName = this.pluginInfo.attributeName(LocatorFieldKind.UID);
				if (idName == null)
				{
					return null;
				}

				Node nodeId = this.node.getAttributes().getNamedItem(idName);
				if (nodeId != null)
				{
					String uid = nodeId.getNodeValue();
					if (XpathUtils.isStable(uid))
					{
						Locator locator = new Locator().kind(kind).id(id).uid(uid);
						if (tryLocator(locator, node) == 1)
						{
							return locator;
						}
					}
				}
			}
			return null;
		}

		//region attrs
		private Locator locatorByAttrs()
		{
			List<Pair> list = allAttributes(node);
			List<List<Pair>> cases = IntStream.range(1, 1 << list.size())
					.boxed()
					.sorted(Comparator.comparingInt(Integer::bitCount))
					.limit(MAX_TRIES)
					.map(e -> XpathUtils.shuffle(e, list))
					.collect(Collectors.toList());

			for (List<Pair> caze : cases)
			{
				Locator locator = new Locator().kind(kind).id(id);
				caze.forEach(p -> locator.set(p.kind, p.value));

				if (tryLocator(locator, node) == 1)
				{
					return locator;
				}
			}

			return null;
		}

		private List<Pair> allAttributes(Node node)
		{
			List<Pair> list = new ArrayList<>();
			addAttr(list, node, LocatorFieldKind.UID);
			addAttr(list, node, LocatorFieldKind.CLAZZ);
			addAttr(list, node, LocatorFieldKind.NAME);
			addAttr(list, node, LocatorFieldKind.TITLE);
			addAttr(list, node, LocatorFieldKind.ACTION);
			addAttr(list, node, LocatorFieldKind.TOOLTIP);
			addAttr(list, node, LocatorFieldKind.TEXT);
			return list;
		}

		private void addAttr(List<Pair> list, Node node, LocatorFieldKind kind)
		{
			if (!node.hasAttributes())
			{
				return;
			}
			if (kind == LocatorFieldKind.TEXT)
			{
				String textContent = node.getTextContent();
				if (XpathUtils.isStable(textContent))
				{
					list.add(new Pair(kind, textContent));
				}
			}
			String attrName = this.pluginInfo.attributeName(kind);
			if (attrName == null)
			{
				return;
			}
			Node attrNode = node.getAttributes().getNamedItem(attrName);
			if (attrNode == null)
			{
				return;
			}
			String value = attrNode.getNodeValue();
			if (XpathUtils.isStable(value))
			{
				list.add(new Pair(kind, value));
			}
		}

		private static class Pair
		{
			public Pair(LocatorFieldKind kind, String value)
			{
				this.kind = kind;
				this.value = value;
			}

			public LocatorFieldKind kind;
			public String value;
		}
		//endregion

		//region xpath
		private Locator locatorByExtendAttrs()
		{
			Locator locator = new Locator().kind(this.kind).id(this.id).xpath("./" + this.node.getNodeName());
			if (tryLocator(locator, this.node) == 1)
			{
				return locator;
			}

			List<StringPair> list = IntStream.range(0, node.getAttributes().getLength())
					.mapToObj(this.node.getAttributes()::item)
					.filter(attr -> XpathUtils.isStable(attr.getNodeName()) && XpathUtils.isStable(attr.getNodeValue()))
					.map(attr -> new StringPair(attr.getNodeName(), attr.getNodeValue()))
					.collect(Collectors.toList());

			List<List<StringPair>> pairList = IntStream.range(1, 1 << list.size())
					.boxed()
					.sorted(Comparator.comparingInt(Integer::bitCount))
					.limit(MAX_TRIES)
					.map(i -> XpathUtils.shuffle(i, list))
					.collect(Collectors.toList());

			for (List<StringPair> pair : pairList)
			{
				locator = new Locator().kind(this.kind).id(this.id).xpath(createXpath(pair));
				if (tryLocator(locator, node) == 1)
				{
					return locator;
				}
			}
			return null;
		}

		private String createXpath(List<StringPair> list)
		{
			return list.stream()
					.map(pair -> "contains("+pair.key + ","+pair.value+")")
					.collect(Collectors.joining(" and ", ".//"+this.node.getNodeName()+"[", "]"));
		}

		private static class StringPair
		{
			String value;
			String key;

			public StringPair(String value, String key)
			{
				this.value = value;
				this.key = key;
			}
		}

		private Locator locatorByXpath(Node node)
		{
			String ownerPath = ".";

			List<String> parameters = XpathUtils.getAllNodeAttribute(node);
			String relativePath = XpathUtils.fullXpath(ownerPath, this.owner, node, false, parameters, false);
			Locator locator = new Locator().kind(kind).id(id).xpath(relativePath);

			if (tryLocator(locator, node) == 1)
			{
				return locator;
			}
			return null;
		}

		private Locator locatorByRelativeXpath()
		{
			String ownerPath = ".";
			String xpath = XpathViewer.fullXpath(ownerPath, this.owner, node, false, null, true);
			String[] parts = xpath.split("/");

			Node parent = node;
			for (int level = 0; level < parts.length; level++)
			{
				Locator relativeLocator = locatorByXpath(parent);
				if (relativeLocator != null)
				{
					String finalPath = XpathViewer.fullXpath(relativeLocator.getXpath(), parent, node, false, null, false);

					Locator finalLocator = new Locator().kind(kind).id(id).xpath(finalPath);
					if (tryLocator(finalLocator, node) == 1)
					{
						return finalLocator;
					}
				}

				parent = parent.getParentNode();
			}

			Locator locator = new Locator().kind(kind).id(id).xpath(xpath);
			if (tryLocator(locator, node) == 1)
			{
				return locator;
			}

			return null;
		}
		//endregion

		private int tryLocator(Locator locator, Node node)
		{
			if (locator == null)
			{
				return 0;
			}
			return this.findFunction.applyAsInt(locator, node);
		}
		//endregion

	}
}
