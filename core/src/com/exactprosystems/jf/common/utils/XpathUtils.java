////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.utils;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.guidic.Attr;
import org.w3c.dom.*;

import javax.xml.xpath.*;
import java.awt.Rectangle;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public static String fullXpath(String relativeXpath, Node relative, Node node, boolean useText, List<String> parameters, boolean longPath, Predicate<String> predicate)
    {
    	if (node == null)
    	{
    		return "//*";
    	}
    
    	if (relative == null)
    	{
    		if (!longPath)
    		{
    			return "/" + XpathUtils.xpath(node.getParentNode(), node, useText, parameters, predicate);
    		}
    		return XpathUtils.xpath(null, node, useText, parameters, predicate);
    	}
    	else
    	{
    		Node common = XpathUtils.commonAncestor(relative, node);
    		Node current = relative;
    		StringBuilder backPath = new StringBuilder();
    		while (current != null && !current.equals(common))
    		{
    			current = current.getParentNode();
    			backPath.append("/..");
    		}

    		if (!longPath)
    		{
				String prefix = relativeXpath + backPath;
				if (prefix.length() < 3)
				{
					prefix += "/";
				}
				return prefix + XpathUtils.xpath(node.getParentNode(), node, useText, parameters, predicate);
			}
    		return relativeXpath + backPath + XpathUtils.xpath(common, node, useText, parameters, predicate);
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

    public static String xpath(Node parent, Node node, boolean useText, List<String> parameters, Predicate<String> predicate)
    {
    	if (node instanceof Document)
    	{
    		return "";
    	}
    	if (node == null || node.equals(parent))
    	{
    		return "";
    	}
    	return xpath(parent, node.getParentNode(), false, null, predicate) + "/" + node.getNodeName() + (parameters != null && !parameters.isEmpty() || useText ?
    			"[" + XpathUtils.getParameters(node, useText, parameters, predicate) + "]" : (XpathUtils.hasSiblings(node) ? "[" + XpathUtils.getIndexNode(node) + "]" : ""));
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
    	List<Node> res = new ArrayList<>();
    	Node current = node;
    	while (current != null)
    	{
    		res.add(0, current);
    		current = current.getParentNode();
    	}
    	return res;
    }

    public static String getParameters(Node node, boolean useText, List<String> parameters, Predicate<String> predicate)
    {
    	String res = "";
    	NamedNodeMap attr = node.getAttributes();
    	if (attr != null)
    	{
    		res = parameters.stream()
					.filter(p -> attr.getNamedItem(p) != null)
					.filter(p -> predicate == null || XpathUtils.isStable(attr.getNamedItem(p).getNodeValue(), predicate))
					.map(p -> "contains(@" + p + ",\"" + attr.getNamedItem(p).getNodeValue()+"\")")
					.collect(Collectors.joining(" and "));
    	}
    	if (useText)
    	{
			String text = text(node);
			if (isStable(text, predicate))
			{
				res = res + (res.isEmpty() ? "" : " and ") + "contains(text(), \"" + text + "\")";
			}
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

	public static String findText(Node node)
	{
		if (node == null)
		{
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(node.getNodeName()).append(" ");
		for(int i = 0; i < node.getAttributes().getLength(); i++)
		{
			Node attr = node.getAttributes().item(i);
			sb.append(attr.getNodeName()).append(" ").append(attr.getNodeValue()).append(" ");
		}
		sb.append(node.getNodeValue());

		return sb.toString().trim().replaceAll("\n", " ");
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

	public static boolean isStable(String identifier, Predicate<String> predicate)
	{
		if (Str.IsNullOrEmpty(identifier))
		{
			return false;
		}
		if (identifier.split(" ").length > 3)
		{
			return false;
		}
		if (!identifier.matches("^[a-zA-Z\\s]+$"))
		{
			return false;
		}
		if (identifier.contains("null"))
		{
			return false;
		}
		return predicate == null || predicate.test(identifier);
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
		private static final int MAX_TRIES = 128;
		private static final int MAX_CHILD_DEEP = 4;

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
				locator = locatorByExtendAttrs(this.node);
				if (locator != null)
				{
					return locator;
				}
				locator = locatorByXpath(this.node);
				if (locator != null)
				{
					return locator;
				}
				String tempXpath = null;
				Locator tempLocator = locatorByRelativeXpathWithSibling(this.node);
				if (tempLocator != null)
				{
					tempXpath = tempLocator.getXpath();
				}
				locator = locatorByRelativeXpath();
				if (locator != null)
				{
					if (tempXpath != null && xpathWeight(tempXpath) < xpathWeight(locator.getXpath()))
					{
						return tempLocator;
					}
					return locator;
				}
			}
			return null;
		}

		private static long xpathWeight(String xpath)
		{
			long slashCount = Arrays.stream(xpath.split("/")).filter(s -> !s.isEmpty()).count();
			Pattern pattern = Pattern.compile("(\\[\\d+\\])");
			Matcher matcher = pattern.matcher(xpath);
			int i = 0;
			while (matcher.find())
			{
				i++;
			}
			return slashCount + 1000 * i;
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
					if (XpathUtils.isStable(uid, this.pluginInfo::isStable))
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
				for (Pair p : caze)
				{
					if (p.kind == LocatorFieldKind.CLAZZ)
					{
						String clazz = (String) locator.get(LocatorFieldKind.CLAZZ);
						String del = " ";
						if (Str.IsNullOrEmpty(clazz))
						{
							clazz = del = "";
						}
						locator.set(LocatorFieldKind.CLAZZ, clazz + del + p.value);
					}
					else
					{
						locator.set(p.kind, p.value);
					}
				}

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
			addAttr(list, node, LocatorFieldKind.NAME);
			addAttr(list, node, LocatorFieldKind.TITLE);
			addAttr(list, node, LocatorFieldKind.ACTION);
			addAttr(list, node, LocatorFieldKind.TOOLTIP);
			addAttr(list, node, LocatorFieldKind.TEXT);
			addAllClasses(list, node);
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
				String textContent = text(node);// node.hasChildNodes() && node.getFirstChild() instanceof Text ? node.getFirstChild().getTextContent() : node.getTextContent();
				if (XpathUtils.isStable(textContent,this.pluginInfo::isStable))
				{
					list.add(new Pair(kind, textContent.trim()));
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
			if (XpathUtils.isStable(value,this.pluginInfo::isStable))
			{
				list.add(new Pair(kind, value));
			}
		}

		private void addAllClasses(List<Pair> list, Node node)
		{
			if (!node.hasAttributes())
			{
				return;
			}
			String attrName = this.pluginInfo.attributeName(LocatorFieldKind.CLAZZ);
			if (Str.IsNullOrEmpty(attrName))
			{
				return;
			}
			Node attrNode = node.getAttributes().getNamedItem(attrName);
			if (attrNode == null)
			{
				return;
			}
			String value = attrNode.getNodeValue();
			Arrays.stream(value.split("\\s+"))
					.filter(clazz -> XpathUtils.isStable(clazz, this.pluginInfo::isStable))
					.map(clazz -> new Pair(LocatorFieldKind.CLAZZ, clazz))
					.forEach(list::add);
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
		private Locator locatorByExtendAttrs(Node node)
		{
			Locator locator = new Locator().kind(this.kind).id(this.id).xpath("./" + node.getNodeName());
			if (tryLocator(locator, node) == 1)
			{
				return locator;
			}
			locator = new Locator().kind(this.kind).id(this.id).xpath(".//" + node.getNodeName());
			if (tryLocator(locator, node) == 1)
			{
				return locator;
			}

			//create all pairs
			List<StringPair> list = IntStream.range(0, node.hasAttributes() ? node.getAttributes().getLength() : 0)
					.mapToObj(node.getAttributes()::item)
					.filter(attr -> XpathUtils.isStable(attr.getNodeName(), this.pluginInfo::isStable) && XpathUtils.isStable(attr.getNodeValue(), this.pluginInfo::isStable))
					.map(attr -> new StringPair("@" + attr.getNodeName(), attr.getNodeValue()))
					.collect(Collectors.toList());

			String textContent = text(node);//.getTextContent();

			if (isStable(textContent, this.pluginInfo::isStable))
			{
				//collect all text from node
				list.add(new StringPair(".", textContent));
				//collect text only from node
				list.add(new StringPair("text()", textContent));
			}

			//shuffle all pair
			List<List<StringPair>> pairList = IntStream.range(1, 1 << list.size())
					.boxed()
					.sorted(Comparator.comparingInt(Integer::bitCount))
					.limit(MAX_TRIES)
					.map(i -> XpathUtils.shuffle(i, list))
					.collect(Collectors.toList());

			//create xpath on pairs
			List<String> xpaths = pairList.stream()
					.map(l -> this.createXpaths(l, node))
					.flatMap(List::stream)
					.distinct()
					.sorted(Comparator.comparingInt(String::length).thenComparing(String::compareTo))
					.collect(Collectors.toList());

			for (String xpath : xpaths)
			{
				locator = new Locator().kind(this.kind).id(this.id).xpath(xpath);
				if (tryLocator(locator, node) == 1)
				{
					return locator;
				}
			}

			return null;
		}

		private List<String> createXpaths(List<StringPair> list, Node node)
		{
			List<String> collect = list.stream()
					.map(StringPair::list)
					.flatMap(Arrays::stream)
					.collect(Collectors.toList());

			List<List<String>> xpathCollect = IntStream.range(1, 1 << collect.size())
					.boxed()
					.sorted(Comparator.comparingInt(Integer::bitCount))
					.limit(MAX_TRIES)
					.map(i -> XpathUtils.shuffle(i, collect))
					.collect(Collectors.toList());

			final String nodeName = node.getNodeName();
			return xpathCollect.stream()
					.map(c -> c.stream().collect(Collectors.joining(" and ")))
					.filter(s -> !(s.matches(".*?contains\\((@\\w+|\\.),\"([\\w\\s]+)\"\\).+?(\\1)=\"(\\2)\".*?") || s.matches(".*?(@\\w+|\\.)=\"([\\w\\s]+)\".+?contains\\((\\1),\"(\\2)\"\\).*?")))
					.map(s -> ".//" + nodeName + "[" + s + "]")
					.collect(Collectors.toList());
		}

		private static class StringPair
		{
			String value;
			String key;

			public StringPair(String key, String value)
			{
				this.value = value;
				this.key = key;
			}

			public String[] list()
			{
				return new String[]{
						String.format("contains(%s,\"%s\")", this.key,this.value.trim())
						, String.format("%s=\"%s\"", this.key,this.value.trim())
				};
			}

			@Override
			public String toString()
			{
				return this.key + " : " + this.value;
			}
		}

		private Locator locatorByXpath(Node node)
		{
			String ownerPath = "./";

			List<String> parameters = XpathUtils.getAllNodeAttribute(node);
			String relativePath = XpathUtils.fullXpath(ownerPath, this.owner, node, false, parameters, false, this.pluginInfo::isStable);
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
			String xpath = XpathUtils.fullXpath(ownerPath, this.owner, node, false, null, true, this.pluginInfo::isStable);
			String[] parts = xpath.split("/");

			Node parent = node;
			for (int level = 0; level < parts.length; level++)
			{
				Locator locator = locatorByExtendAttrs(parent);
				if (locator == null)
				{
					locator = locatorByXpath(parent);
				}
				if (locator != null)
				{
					String finalPath = XpathUtils.fullXpath(locator.getXpath(), parent, node, false, null, true, this.pluginInfo::isStable);

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

		private Locator locatorByRelativeXpathWithSibling(Node node)
		{
			//check siblings
			NodeList childNodes = node.getParentNode().getChildNodes();
			List<Node> siblings = IntStream.range(0, childNodes.getLength())
					.mapToObj(childNodes::item)
					.collect(Collectors.toList());
			List<Locator> locators = new ArrayList<>();

			checkLocatorWithRelation(siblings, node, locators);
			Optional<Locator> max = locators.stream().max(Comparator.comparingLong(l -> xpathWeight(l.getXpath())));
			if (max.isPresent())
			{
				locators.clear();
				return max.get();
			}

			List<Node> list = new ArrayList<>();

			collectChild(node.getParentNode(), list, 0);
			checkLocatorWithRelation(list, node, locators);

			list.clear();

			int deep = 0;
			Node parent = node.getParentNode();
			while (deep++ < MAX_CHILD_DEEP && parent != null)
			{
				parent = parent.getParentNode();
			}

			collectChild(parent, list, MAX_CHILD_DEEP - deep);
			checkLocatorWithRelation(list, node, locators);
			return locators.stream().max(Comparator.comparingLong(l -> xpathWeight(l.getXpath()))).orElse(null);
		}

		private void checkLocatorWithRelation(List<Node> nodes, Node node, List<Locator> locators)
		{
			for (Node childItem : nodes)
			{
				if (!childItem.hasAttributes())
				{
					continue;
				}
				Locator locator = locatorByExtendAttrs(childItem);
				if (locator == null)
				{
					locator = locatorByXpath(childItem);
				}
				if (locator != null)
				{
					String finalPath = XpathUtils.fullXpath(locator.getXpath(), childItem, node, false, null, true, this.pluginInfo::isStable);

					Locator finalLocator = new Locator().kind(kind).id(id).xpath(finalPath);
					if (tryLocator(finalLocator, node) == 1)
					{
						locators.add(finalLocator);
					}
				}
			}
		}

		private void collectChild(Node node, List<Node> list, int deep)
		{
			if (deep > MAX_CHILD_DEEP)
			{
				return;
			}
			if (node == null || !node.hasChildNodes())
			{
				return;
			}
			NodeList child = node.getChildNodes();
			IntStream.range(0, child.getLength())
					.mapToObj(child::item)
					.peek(childNode -> collectChild(childNode, list, deep + 1))
					.forEach(list::add);

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
