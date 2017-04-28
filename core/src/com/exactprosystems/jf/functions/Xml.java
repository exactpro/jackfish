////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.functions;

import com.exactprosystems.jf.api.common.Str;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Xml  
{
	public Xml(String fileName) throws Exception
	{
		try (InputStream  fileReader = new FileInputStream(fileName))
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
		passWholeDOM(this.node, hash);

		return hash.get();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this.node.equals(obj);
	}

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
					.filter(node -> node instanceof Attr)
					.map(node -> (Attr)node)
					.collect(Collectors.toMap(Attr::getName, Attr::getValue));

			Map<String, String> anotherMap = IntStream.range(0, anotherAttrs.getLength())
					.mapToObj(anotherAttrs::item)
					.filter(node -> node instanceof Attr)
					.map(node -> (Attr)node)
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

	@Override
	public String toString()
	{
		return Xml.class.getSimpleName() + this.node.toString();
	}
	
	public void setText(String text) throws Exception
	{
		if (this.node.getNodeType() == Node.DOCUMENT_NODE)
		{
			throw new Exception("Cant set text to document");
		}
		this.node.setTextContent(text);
	}

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
	
	public void setAttributes(Map<String, Object> attr) throws Exception
	{
		if (!(this.node instanceof Element))
		{
			throw new Exception("Attributes may sets only into Element");
		}
		for (Entry<String, Object> entry : attr.entrySet())
		{
			((Element)this.node).setAttribute(entry.getKey(), String.valueOf(entry.getValue()));
		}
	}
	
	public String getAttribute(String name) throws Exception
	{
		if (this.node instanceof Element)
		{
			return ((Element)this.node).getAttribute(name);
		}
		return null;
	}

	
	public void report(ReportBuilder report, String beforeTestcase, String title) throws Exception
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		DOMSource source = new DOMSource(this.node);
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(output);
 		transformer.transform( source, result);		
 		
		ReportTable table = report.addExplicitTable(title, beforeTestcase, false, 0, new int[] {}, new String[] {});
		String buff = new String(output.toByteArray(), StandardCharsets.UTF_8);
		table.addValues(HTMLhelper.htmlescape(buff));
	}

	public boolean save(String fileName) throws Exception
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		
		DOMSource source = new DOMSource(this.node);
		StreamResult result = new StreamResult(new File(fileName));
 		transformer.transform( source, result);		
		
		return true;
	}

	public boolean hasChildren()
	{
		NodeList nodes = this.node.getChildNodes();
		//check that element is not a text
		return nodes != null && IntStream.range(0, nodes.getLength())
				.mapToObj(nodes::item)
				//check that element is not a text
				.filter(node -> node.getNodeType() != Node.TEXT_NODE)
				.count() != 0;

	}

	public List<Xml> getChildren()
	{
		List<Xml> res = new ArrayList<>();
		NodeList nodes = this.node.getChildNodes();
		if (nodes != null)
		{
			IntStream.range(0, nodes.getLength())
					.mapToObj(nodes::item)
					//check that element is not a text
					.filter(node -> node.getNodeType() != Node.TEXT_NODE)
					.map(Xml::new)
					.forEach(res::add);
		}
			
		return res;
	}

	public Xml getChild(String name)
	{
		NodeList nodes = this.node.getChildNodes();
		if (nodes != null)
		{
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				if (node.getNodeName().equals(name))
				{
					return new Xml(node);
				}
			}
		}
			
		return null;
	}

	public Xml createListByXpath(String nodeName, String xpath) throws Exception
	{
		Document doc = getDocument();
		if (doc == null)
		{
			return null;
		}
		Element res = doc.createElement(nodeName);

		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodes = (NodeList) xPath.evaluate(xpath, this.node, XPathConstants.NODESET);

		for (int i = 0; i < nodes.getLength(); ++i) 
		{
		    res.appendChild(nodes.item(i));
		}
		
		return new Xml(res);
	}
	
	public Xml createOneByXpath(String nodeName, String xpath) throws Exception
	{

        Document doc = getDocument();
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

	public void removeByXpath(String xpath) throws Exception
	{
		XPath xPath = XPathFactory.newInstance().newXPath();
		NodeList nodes = (NodeList)xPath.evaluate(xpath, this.node, XPathConstants.NODESET);
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

	public void addNode(Xml copiedXML)
	{
		Node clonednode = copiedXML.node;
		Node newnode = this.getDocument().importNode(clonednode,true);
		if (this.node instanceof Document)
		{
			getDocument().getDocumentElement().appendChild(newnode);

		}
		else
		{
			this.node.appendChild(newnode);
		}
	}

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

		for (Entry<String, Object> entry : attr.entrySet())
		{
			child.setAttribute(entry.getKey(), String.valueOf(entry.getValue()));
		}
	}

	public String getNodeName()
	{
		return this.node.getNodeName();
	}
	
	public Document getDocument()
	{
		return (this.node instanceof Document) ? (Document)this.node : this.node.getOwnerDocument();  
	}

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
		for (int i = 0; i < children.getLength(); i++)
		{
			Node child = children.item(i);
			passWholeDOM(child, hash);
		}
	}

	protected Node node;
}
