////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.functions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;

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
	
	@Override
	public String toString()
	{
		return Xml.class.getSimpleName() + this.node.toString();
	}
	
	public void setText(String text)
	{
		this.node.setTextContent(text);
	}

	public String getText()
	{
		String res = this.node.getTextContent();
		return res == null ? "" : res;
	}
	
	public void setAttributes(Map<String, Object> attr)
	{
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
		table.addValues(buff);
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

	public List<Xml> getChildren()
	{
		List<Xml> res = new ArrayList<Xml>();
		NodeList nodes = this.node.getChildNodes();
		
		
		if (nodes != null)
		{
			for (int i = 0; i < nodes.getLength(); i++)
			{
				Node node = nodes.item(i);
				res.add(new Xml(node));
			}
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
		
		if (res != null)
		{
            return new Xml(res.appendChild(foundNode));
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
