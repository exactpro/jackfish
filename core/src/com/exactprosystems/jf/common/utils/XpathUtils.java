/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.common.utils;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.guidic.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

	/**
	 * Apply passed offsets for all descendants for node, if descendant has node type is {@link Node#ELEMENT_NODE}<br>
	 * Applying will for rectangle, which saved in the user data with name {@link IRemoteApplication#rectangleName}
	 * @param node root node for get descendants
	 * @param xOffset x offset for rectangle
	 * @param yOffset y offset for rectangle
	 */
	public static void applyOffset(Node node, int xOffset, int yOffset)
	{
		boolean isDocument = node.getNodeType() == Node.DOCUMENT_NODE;
		IntStream.range(0, node.getChildNodes().getLength())
				.mapToObj(node.getChildNodes()::item)
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

	/**
	 * Collect all rectangles from descendants, which has node type is {@link Node#ELEMENT_NODE}
	 * @param node root node for collecting rectangle.
	 * @return list of all rectangles
	 */
	public static List<Rectangle> collectAllRectangles(Node node)
	{
		List<Rectangle> collect = new ArrayList<>();
		boolean isDocument = node.getNodeType() == Node.DOCUMENT_NODE;

		IntStream.range(0, node.getChildNodes().getLength())
				.mapToObj(node.getChildNodes()::item)
				.filter(item -> item.getNodeType() == Node.ELEMENT_NODE)
				.map(XpathUtils::collectAllRectangles)
				.forEach(collect::addAll);

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

	/**
	 * Find nodes by passed xpath expression
	 * @param node owner for finding nodes
	 * @param xpathStr xpath expression
	 * @return if expression is null or invalid, will return null. Otherwise will return List of founded nodes
	 */
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
		catch (XPathExpressionException ignored)
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

	/**
	 * Return the first nodes from founded via xpath expression
	 * @param node owner for finding nodes
	 * @param xpathStr xpath expression
	 * @return null if no one nodes found. Otherwise will return a first node from founded
	 */
	public static Node getFirst(Node node, String xpathStr)
	{
		List<Node> nodes = evaluate(node, xpathStr);
		return nodes == null || nodes.isEmpty() ? null : nodes.get(0);
	}

	/**
	 * Find general parent for 2 passed nodes
	 * @return general node for 2 passed nodes
	 */
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

	/**
	 * Return list of ancestors for a passed node.<br>
	 * Example : <code>Document -> ... -> node.getParent().getParent() -> node.getParent()</code>
	 * @return list of ancestors for a passed node. This list starts from Document node to parent of a passed node
	 */
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

	/**
	 * Return list of all descendants from a passed node
	 * @param node node, for will finding all descendants
	 * @return list of all descendants for a passed node. List contains the passed node
	 */
	public static List<Node> descedants(Node node)
	{
		List<Node> list = new ArrayList<>();
		collectDescendants(node, list);
		return list;
	}

	private static void collectDescendants(Node node, List<Node> nodes)
	{
		nodes.add(node);
		NodeList childNodes = node.getChildNodes();
		IntStream.range(0, childNodes.getLength())
				.mapToObj(childNodes::item)
				.forEach(child -> collectDescendants(child, nodes));
	}

	/**
	 * Return string, contains all attributes for a passed node.<br>
	 * This string should be used for a xpath expression
	 * @param useText if this parameter is true, text from the node will present on result string
	 * @param parameters list of parameters, which need be into result string
	 * @param predicate for filtering parameter.
	 * @return string, contains all parameters, which matches by passed predicate.
	 */
	public static String getParameters(Node node, boolean useText, List<String> parameters, Predicate<String> predicate)
	{
		String res = "";
		NamedNodeMap attr = node.getAttributes();
		if (attr != null)
		{
			res = parameters.stream()
					.filter(p -> attr.getNamedItem(p) != null)
					.filter(p -> predicate == null || XpathUtils.isStable(attr.getNamedItem(p).getNodeValue(), predicate))
					.map(p -> "contains(@" + p + ",\"" + attr.getNamedItem(p).getNodeValue() + "\")")
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

	/**
	 * Return text from the node. A return string contains all text from children nodes,which have node type {@link Node#TEXT_NODE}
	 * @return return null, if node is null. Otherwise will return string, contains all texts from the node
	 */
	public static String text(Node node)
	{
		if (node == null)
		{
			return null;
		}

		StringBuilder sb = new StringBuilder();
		IntStream.range(0, node.getChildNodes().getLength())
				.mapToObj(i -> node.getChildNodes().item(i))
				.filter(item -> item.getNodeType() == Node.TEXT_NODE)
				.map(Node::getNodeValue)
				.filter(Objects::nonNull)
				.map(value -> value.trim().replace('\n', ' '))
				.forEach(sb::append);
		return sb.toString();
	}

	/**
	 * Convert the passed node to a string.<br>
	 * Example : <code> &lt;tagName parameter1="value1" parameter2="value2"&gt;nodeValue&lt;/tagName&gt; </code>
	 * @param node which will converting to string representation
	 * @return string representation for the passed node. If node has no attributes ( {@link Node#hasAttributes()} return false) will return null;
	 */
	public static String findText(Node node)
	{
		if (node == null)
		{
			return null;
		}

		if (node.hasAttributes())
		{
			NamedNodeMap attributes = node.getAttributes();
			String result = IntStream.range(0, attributes.getLength())
					.mapToObj(attributes::item)
					.map(attr -> attr.getNodeName() + " " + attr.getNodeValue())
					.collect(Collectors.joining(" "));
			return (result + " " + node.getNodeValue()).trim().replaceAll("\n", " ");
		}
		return null;
	}

	/**
	 * Return index of a node on a list of parent children. Found only children, which has the same tag name as node tag name
	 * @param node node, for which need found index
	 * @return index of node on a list of parent children
	 */
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

	/**
	 * @return true, if the node has siblings, which has the same tag name as node tag name. Otherwise return false.
	 */
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

	/**
	 * @return list of all node attributes (only attribute name). If node has no attributes, will return {@link Collections#emptyList()}
	 */
	public static List<String> getAllNodeAttribute(Node node)
	{
		if (!node.hasAttributes())
		{
			return Collections.emptyList();
		}
		NamedNodeMap attributes = node.getAttributes();
		return IntStream.range(0, attributes.getLength())
				.mapToObj(attributes::item)
				.map(Node::getNodeName)
				.collect(Collectors.toList());
	}

	/**
	 * @return true, if a passed identifier stable. Return false otherwise.
	 */
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

	/**
	 * Shuffle the passed list via passed bit mask
	 * @return new shuffled list
	 */
	public static <T> List<T> shuffle(int mask, List<T> source)
	{
		List<T> res = new ArrayList<>();
		int oneBit;
		while((oneBit = Integer.lowestOneBit(mask)) != 0)
		{
			res.add(source.get(Integer.numberOfTrailingZeros(mask)));
			mask ^= oneBit;
		}
		return res;
	}

	/**
	 * Apply a consumer for all descendants of the node, if descendants has node type is {@link Node#ELEMENT_NODE}
	 * @param node starting node
	 * @param func function, which will apply for all matches descendants
	 */
	public static void passTree(Node node, Consumer<Node> func)
	{
		func.accept(node);
		IntStream.range(0, node.getChildNodes().getLength())
				.mapToObj(node.getChildNodes()::item)
				.filter(item -> item.getNodeType() == Node.ELEMENT_NODE)
				.forEach(item -> passTree(item, func));
	}

	/**
	 * @param node from which will get attributes
	 * @return if node has no attributes, will return {@link Collections#emptyList()}. <br>
	 * Otherwise will return list of all attributes
	 */
	public static List<Attr> extractAttributes(Node node)
	{
		if (!node.hasAttributes())
		{
			return Collections.emptyList();
		}
		return IntStream.range(0, node.getAttributes().getLength())
				.mapToObj(node.getAttributes()::item)
				.map(attr -> new Attr(attr.getNodeName(), attr.getNodeValue()))
				.collect(Collectors.toList());
	}

	/**
	 * Static class builder for found one locator via passed parameters.
	 */
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

		//region public methods
		public static FindLocator start(ToIntBiFunction<Locator,Node> findFunction, String id, ControlKind kind, Node node, PluginInfo pluginInfo)
		{
			return new FindLocator(findFunction, id, kind, node, pluginInfo);
		}

		/**
		 * Include find locator via Id
		 */
		public FindLocator findById()
		{
			this.findById = true;
			this.pluginInfo = pluginInfo;
			return this;
		}

		/**
		 * Include find locator via all attributes from node
		 */
		public FindLocator findByAttrs()
		{
			this.findByAttrs = true;
			return this;
		}

		/**
		 * Include find locator via xpath expression
		 * @param owner
		 * @return
		 */
		public FindLocator findByXpath(Node owner)
		{
			this.owner = owner;
			this.findByXpath = true;
			return this;
		}

		/**
		 * @return locator.
		 *
		 * @see Locator
		 */
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
				locator = this.locatorByAttrs();
				if (locator != null)
				{
					return locator;
				}
			}
			if (this.findByXpath)
			{
				locator = this.locatorByExtendAttrs(this.node);
				if (locator != null)
				{
					return locator;
				}
				locator = this.locatorByXpath(this.node);
				if (locator != null)
				{
					return locator;
				}
				String tempXpath = null;
				Locator tempLocator = this.locatorByRelativeXpathWithSibling(this.node);
				if (tempLocator != null)
				{
					tempXpath = tempLocator.getXpath();
				}
				locator = this.locatorByRelativeXpath();
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
		//endregion

		//region private methods

		/**
		 * Return "weight" attribute for passed xpath expression.<br>
		 * The weight evaluate via count of backslash and count of brackets
		 * @return xpath weight
		 */
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

		/**
		 * Try to find locator via it Id.
		 *
		 * @see PluginInfo
		 * @see Locator
		 * @see LocatorFieldKind#UID
		 *
		 * @return return locator, if :
		 * <ul>
		 *  <li>1. Id is presented</li>
		 *  <li>2. Id is stable</li>
		 *  <li>3. Locator with it Id found the only one</li>
		 * </ul>
		 * If one of these parameters are false, will return null. Otherwise will return locator
		 */
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
						Locator locator = new Locator().kind(this.kind).id(this.id).uid(uid);
						if (this.tryLocator(locator, this.node) == 1)
						{
							return locator;
						}
					}
				}
			}
			return null;
		}

		//region attrs

		/**
		 * Try to find locator via known parameters
		 * @see LocatorFieldKind
		 * @return locator, if locator via parameters found the only one
		 */
		private Locator locatorByAttrs()
		{
			List<Pair> list = this.allAttributes(this.node);
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

				if (this.tryLocator(locator, node) == 1)
				{
					return locator;
				}
			}

			return null;
		}

		/**
		 * @return Return list of all known parameters ( if parameter is present and stable)
		 */
		private List<Pair> allAttributes(Node node)
		{
			List<Pair> list = new ArrayList<>();
			this.addAttr(list, node, LocatorFieldKind.UID);
			this.addAttr(list, node, LocatorFieldKind.NAME);
			this.addAttr(list, node, LocatorFieldKind.TITLE);
			this.addAttr(list, node, LocatorFieldKind.ACTION);
			this.addAttr(list, node, LocatorFieldKind.TOOLTIP);
			this.addAttr(list, node, LocatorFieldKind.TEXT);
			this.addAllClasses(list, node);
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
				String textContent = text(node);
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
			Pair(LocatorFieldKind kind, String value)
			{
				this.kind = kind;
				this.value = value;
			}

			LocatorFieldKind kind;
			String value;
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

			//add text only from node
			String textContent = text(node);
			if (isStable(textContent, this.pluginInfo::isStable))
			{
				list.addAll(StringPair.textPairs(textContent));
			}

			//add text for all descendants
			descedants(node)
					.stream()
					.filter(item -> item.getNodeType() == Node.TEXT_NODE)
					.map(Node::getNodeValue)
					.filter(text -> isStable(text, this.pluginInfo::isStable))
					.distinct()
					.map(StringPair::textPairs)
					.forEach(list::addAll);

			//shuffle all pair
			List<List<StringPair>> pairList = IntStream.range(1, 1 << list.size())
					.boxed()
					.sorted(Comparator.comparingInt(Integer::bitCount))
					.limit(MAX_TRIES)
					.map(i -> XpathUtils.shuffle(i, list))
					.collect(Collectors.toList());

			//create xpath based on pairs
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
			//collect all pairs
			List<String> collect = list.stream()
					.map(StringPair::list)
					.flatMap(Arrays::stream)
					.collect(Collectors.toList());

			//shuffle all pairs
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

			public static List<StringPair> textPairs(String value)
			{
				return Arrays.asList(
						//collect all text from node
						new StringPair(".", value),
						//collect text only from node
						new StringPair("text()", value)
				);
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

			if (this.tryLocator(locator, node) == 1)
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
				Locator locator = this.locatorByExtendAttrs(parent);
				if (locator == null)
				{
					locator = this.locatorByXpath(parent);
				}
				if (locator != null)
				{
					String finalPath = XpathUtils.fullXpath(locator.getXpath(), parent, node, false, null, true, this.pluginInfo::isStable);

					Locator finalLocator = new Locator().kind(kind).id(id).xpath(finalPath);
					if (this.tryLocator(finalLocator, node) == 1)
					{
						return finalLocator;
					}
				}

				parent = parent.getParentNode();
			}

			Locator locator = new Locator().kind(kind).id(id).xpath(xpath);
			if (this.tryLocator(locator, node) == 1)
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

			this.checkLocatorWithRelation(siblings, node, locators);

			List<Node> list = new ArrayList<>();
			this.collectChild(node.getParentNode(), list, 0);
			this.checkLocatorWithRelation(list, node, locators);
			list.clear();

			int deep = 0;
			Node parent = node.getParentNode();
			while (deep++ < MAX_CHILD_DEEP && parent != null)
			{
				parent = parent.getParentNode();
			}

			this.collectChild(parent, list, MAX_CHILD_DEEP - deep);
			this.checkLocatorWithRelation(list, node, locators);

			return locators.stream()
					.min(Comparator.comparingLong(l -> xpathWeight(l.getXpath())))
					.orElse(null);
		}

		private void checkLocatorWithRelation(List<Node> nodes, Node node, List<Locator> locators)
		{
			for (Node childItem : nodes)
			{
				if (!childItem.hasAttributes())
				{
					continue;
				}
				Locator locator = this.locatorByExtendAttrs(childItem);
				if (locator == null)
				{
					locator = this.locatorByXpath(childItem);
				}
				if (locator != null)
				{
					String finalPath = XpathUtils.fullXpath(locator.getXpath(), childItem, node, false, null, true, this.pluginInfo::isStable);

					Locator finalLocator = new Locator().kind(kind).id(id).xpath(finalPath);
					if (this.tryLocator(finalLocator, node) == 1)
					{
						locators.add(finalLocator);
					}
				}
			}
		}

		private void collectChild(Node node, List<Node> list, int level)
		{
			if (level > MAX_CHILD_DEEP)
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
					.peek(childNode -> this.collectChild(childNode, list, level + 1))
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
