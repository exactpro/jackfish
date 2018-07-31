/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.functions;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.report.HTMLhelper;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Xml
{
	/**
	 * The root node for the class
	 */
	protected Node node;

	public Xml(String fileName) throws Exception
	{
		try (InputStream fileReader = new FileInputStream(fileName))
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			this.node = docBuilder.parse(fileReader);
		}
	}

	public Xml(Reader reader) throws Exception
	{
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		this.node = docBuilder.parse(new InputSource(reader));
	}

	public Xml(Node node)
	{
		this.node = node;
	}

	@Override
	public int hashCode()
	{
		AtomicInteger hash = new AtomicInteger(0);
		this.passWholeDOM(this.node, hash);
		return hash.get();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || this.getClass() != o.getClass())
		{
			return false;
		}
		Xml xml = (Xml) o;
		return this.node.isEqualNode(xml.node);
	}

	@Override
	public String toString()
	{
		return Xml.class.getSimpleName() + this.node.toString();
	}

	//region public API

	/**
	 * Compare the root node with another root node.
	 * If parameters ignoreNodesOrder is true, the order of nodes will ignored
	 */
	public boolean compareTo(Xml another, Boolean ignoreNodesOrder)
	{
		//region tag name

		if (!Str.areEqual(this.node.getNodeName(), another.node.getNodeName()))
		{
			return false;
		}

		//endregion

		//region check text

		if (!Str.areEqual(this.getText(), another.getText()))
		{
			return false;
		}

		//endregion

		//region check parameters
		if (this.node instanceof Element && another.node instanceof Element)
		{
			NamedNodeMap thisAttrs = this.node.getAttributes();
			NamedNodeMap anotherAttrs = another.node.getAttributes();

			if (thisAttrs.getLength() != anotherAttrs.getLength())
			{
				return false;
			}
			Map<String, String> thisMap = IntStream.range(0, thisAttrs.getLength())
					.mapToObj(thisAttrs::item)
					.filter(item -> item instanceof Attr)
					.map(attr -> (Attr)attr)
					.collect(Collectors.toMap(Attr::getName, Attr::getValue));

			Map<String, String> anotherMap = IntStream.range(0, anotherAttrs.getLength())
					.mapToObj(anotherAttrs::item)
					.filter(item -> item instanceof Attr)
					.map(attr -> (Attr)attr)
					.collect(Collectors.toMap(Attr::getName, Attr::getValue));

			if (!thisMap.equals(anotherMap))
			{
				return false;
			}
		}


		//endregion

		//region check children
		List<Xml> thisChildren = this.getChildren();
		List<Xml> anotherChildren = another.getChildren();
		if (thisChildren.size() != anotherChildren.size())
		{
			return false;
		}

		if (!ignoreNodesOrder)
		{

			for (int i = 0; i < thisChildren.size(); i++)
			{
				Xml thisChild = thisChildren.get(i);
				Xml anotherChild = anotherChildren.get(i);

				if (!thisChild.compareTo(anotherChild, ignoreNodesOrder))
				{
					return false;
				}
			}
		}
		else
		{
			boolean[] thisArray = new boolean[thisChildren.size()];
			boolean[] anotherArray = new boolean[thisChildren.size()];

			int thisCounter = 0;
			Iterator<Xml> thisIterator = thisChildren.iterator();
			while (thisIterator.hasNext())
			{
				Xml thisXml = thisIterator.next();
				int anotherCounter = 0;
				Iterator<Xml> anotherIterator = anotherChildren.iterator();
				while (anotherIterator.hasNext())
				{
					if (anotherArray[anotherCounter])
					{
						anotherIterator.next();
						anotherCounter++;
						continue;
					}

					Xml anotherXml = anotherIterator.next();

					if (thisXml.compareTo(anotherXml, true))
					{
						thisArray[thisCounter] = true;
						anotherArray[anotherCounter] = true;
					}

					anotherCounter++;
				}
				thisCounter++;
			}

			for (boolean b : thisArray)
			{
				if (!b)
				{
					return false;
				}
			}

			for (boolean b : anotherArray)
			{
				if (!b)
				{
					return false;
				}
			}
		}

		//endregion

		return true;
	}

	/**
	 * Set the passed text to the root node.
	 * @throws Exception if this node has type {@link Node#DOCUMENT_NODE} (it mean, that the root node - is document, not element)
	 */
	public void setText(String text) throws Exception
	{
		if (this.node.getNodeType() == Node.DOCUMENT_NODE)
		{
			throw new Exception(R.XML_CANT_SET_TEXT_EXCEPTION.get());
		}
		this.node.setTextContent(text);
	}

	/**
	 * @return the text from the node.
	 * The text will contains a text from all children, which has type {@link Node#TEXT_NODE}
	 */
	public String getText()
	{
		//from http://stackoverflow.com/questions/12191414/node-gettextcontent-is-there-a-way-to-get-text-content-of-the-current-node-no
		NodeList list = this.node.getChildNodes();
		return IntStream.range(0, list.getLength())
				.mapToObj(list::item)
				.filter(child -> child.getNodeType() == Node.TEXT_NODE)
				.map(Node::getTextContent)
				.collect(Collectors.joining(""));
	}

	/**
	 * Set the passed attribute to the root node
	 * @throws Exception if the root node is not instance of {@link Element}
	 */
	public void setAttributes(Map<String, Object> attr) throws Exception
	{
		if (!(this.node instanceof Element))
		{
			throw new Exception(R.XML_SET_ATTR_EXCEPTION.get());
		}
		for (Entry<String, Object> entry : attr.entrySet())
		{
			((Element) this.node).setAttribute(entry.getKey(), String.valueOf(entry.getValue()));
		}
	}

	/**
	 * @return attribute value by the passed name from the root node.
	 * If the root node is not instance of {@link Element}, null will returned
	 */
	public String getAttribute(String name)
	{
		if (this.node instanceof Element)
		{
			return ((Element) this.node).getAttribute(name);
		}
		return null;
	}

	/**
	 * @return the map, contains all attribute from the root node.
	 * If the root node is not instance of {@link Element}, {@link Collections#emptyMap()} will returned
	 */
	public Map<String, String> getAllAttributes()
	{
		if (this.node instanceof Element)
		{
			NamedNodeMap attributes = this.node.getAttributes();
			return IntStream.range(0, attributes.getLength())
					.boxed()
					.collect(Collectors.toMap(i -> attributes.item(i).getNodeName(), i -> attributes.item(i).getNodeValue()));
		}
		return Collections.emptyMap();
	}

	/**
	 * Report the root node ( and all ancestors) via passed {@link ReportBuilder} instance.
	 */
	public void report(ReportBuilder report, String beforeTestcase, String title) throws Exception
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		DOMSource source = new DOMSource(this.node);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(output);
		transformer.transform(source, result);

		ReportTable table = report.addExplicitTable(title, beforeTestcase, false, true, new int[]{});
		String buff = new String(output.toByteArray(), StandardCharsets.UTF_8);
		table.addValues(HTMLhelper.htmlescape(buff));
	}

	/**
	 * Save the root node into file with passed fileName
	 */
	public boolean save(String fileName) throws Exception
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		DOMSource source = new DOMSource(this.node);
		StreamResult result = new StreamResult(new File(fileName));
		transformer.transform(source, result);

		return true;
	}

	/**
	 * @return true, if the root node contains any children, except child, which has type {@link Node#TEXT_NODE}
	 */
	public boolean hasChildren()
	{
		NodeList nodes = this.node.getChildNodes();
		return nodes != null
				&& IntStream.range(0, nodes.getLength())
				.mapToObj(nodes::item)
				//check that element is not a text
				.anyMatch(item -> item.getNodeType() != Node.TEXT_NODE);

	}

	/**
	 * @return the list, which contains all children from the root node, except a child, which has type {@link Node#TEXT_NODE}
	 * If the root node doesn't contains any not text children, {@link Collections#emptyList()} will returned
	 */
	public List<Xml> getChildren()
	{
		return Optional.ofNullable(this.node.getChildNodes())
				.map(nodes -> IntStream.range(0, nodes.getLength())
						.mapToObj(nodes::item)
						//check that element is not a text
						.filter(item -> item.getNodeType() != Node.TEXT_NODE)
						.map(Xml::new)
						.collect(Collectors.toList()))
				.orElse(Collections.emptyList());
	}

	/**
	 * @return a child, which has {@link Node#getNodeName()} is the same that passed name or else @{code null}, if a child is not exists
	 */
	public Xml getChild(String name)
	{
		return Optional.ofNullable(this.node.getChildNodes())
				.map(nodes -> IntStream.range(0, nodes.getLength())
						.mapToObj(nodes::item)
						.filter(item -> item.getNodeName().equals(name))
						.findFirst()
						.map(Xml::new)
						.orElse(null))
				.orElse(null);
	}

	/**
	 * @return a new Xml object, which has root node with tag @{code nodeName} and all elements, which was found via passed xpath expression
	 */
	public Xml createListByXpath(String nodeName, String xpath) throws Exception
	{
		Document doc = this.getDocument();
		if (doc == null)
		{
			return null;
		}
		Element res = doc.createElement(nodeName);

		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodes = (NodeList) xPath.evaluate(xpath, this.node, XPathConstants.NODESET);

		IntStream.range(0, nodes.getLength())
				.mapToObj(nodes::item)
				.forEach(res::appendChild);

		return new Xml(res);
	}

	/**
	 * @return a new Xml object, which has root node with tag @{code nodeName} and first of elements, which was found via passed xpath expression
	 */
	public Xml createOneByXpath(String nodeName, String xpath) throws Exception
	{
		Document doc = this.getDocument();
		if (doc == null)
		{
			return null;
		}
		Element res = doc.createElement(nodeName);

		XPath xPath = XPathFactory.newInstance().newXPath();
		Node foundNode = (Node) xPath.evaluate(xpath, this.node, XPathConstants.NODE);

		if (foundNode != null)
		{
			res.appendChild(foundNode);
			return new Xml(res);
		}

		return null;
	}

	/**
	 * @return a new Xml object, which has root node is first of elements, which was found via passed xpath expression
	 */
	public Xml findOneByXpath(String xpath) throws Exception
	{
		XPath xPath = XPathFactory.newInstance().newXPath();
		Node res = (Node) xPath.evaluate(xpath, this.node, XPathConstants.NODE);

		if (res != null)
		{
			return new Xml(res);
		}

		return null;
	}

	public List<Xml> findNodesByXpath(String xpath) throws Exception
	{
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList res = (NodeList) xPath.evaluate(xpath, this.node, XPathConstants.NODESET);
		return Optional.ofNullable(res)
				.map(result -> IntStream.range(0, result.getLength())
						.mapToObj(i -> new Xml(result.item(i)))
						.collect(Collectors.toList()))
				.orElse(Collections.emptyList());
	}

	/**
	 * Remove a node, which will found by the passed xpath from the root node
	 */
	public void removeByXpath(String xpath) throws Exception
	{
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodes = (NodeList) xPath.evaluate(xpath, this.node, XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); ++i)
		{
			Node child = nodes.item(i);
			Node parent = child.getParentNode();
			if (parent != null)
			{
				parent.removeChild(child);
			}
		}
	}

	/**
	 * Add the passed xml object to the children of the root node.
	 * If root node instance of {@link Document}, the passed node will added to the {@link Document#getDocumentElement()}
	 */
	public void addNode(Xml copiedXML)
	{
		Node clonedNode = copiedXML.node;
		Node newNode = this.getDocument().importNode(clonedNode, true);
		if (this.node instanceof Document)
		{
			this.getDocument().getDocumentElement().appendChild(newNode);
		}
		else
		{
			this.node.appendChild(newNode);
		}
	}

	/**
	 * Create a new node with the passed nodeName, content and parameters and append to the root node
	 */
	public void addNode(String nodeName, String content, Map<String, Object> attr)
	{
		Document doc = getDocument();
		if (doc == null)
		{
			return;
		}

		Element child = doc.createElement(nodeName);
		child.setTextContent(content);

		this.node.appendChild(child);

		attr.forEach((key, value) -> child.setAttribute(key, String.valueOf(value)));
	}

	public String getNodeName()
	{
		return this.node.getNodeName();
	}

	public Document getDocument()
	{
		return (this.node instanceof Document) ? (Document) this.node : this.node.getOwnerDocument();
	}
	//endregion

	//region private methods
	private void passWholeDOM(Node node, AtomicInteger hash)
	{
		int current = hash.get();
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++)
		{
			Attr attr = (Attr) attributes.item(i);
			current ^= attr.getName().hashCode();
			current ^= attr.getValue().hashCode();
		}
		String text = node.getNodeValue();
		if (text != null)
		{
			current ^= text.hashCode();
		}
		hash.set(current);
		NodeList children = node.getChildNodes();
		IntStream.range(0, children.getLength())
				.mapToObj(children::item)
				.forEach(child -> this.passWholeDOM(child, hash));
	}
	//endregion
}
