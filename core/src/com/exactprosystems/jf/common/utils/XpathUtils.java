////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.utils;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.exactprosystems.jf.api.app.IRemoteApplication;

public class XpathUtils
{
    private XpathUtils()
    {
    }

    
    public static List<Rectangle> collectAllRectangles(Node node, int xOffset, int yOffset)
    {
        List<Rectangle> collect = new ArrayList<>();
        boolean isDocument = node.getNodeType() == Node.DOCUMENT_NODE;
        IntStream.range(0, node.getChildNodes().getLength()).mapToObj(node.getChildNodes()::item)
                .filter(item -> item.getNodeType() == Node.ELEMENT_NODE)
                .forEach(item -> collect.addAll(collectAllRectangles(item, xOffset, yOffset)));
        if (!isDocument)
        {
            Rectangle rec = (Rectangle) node.getUserData(IRemoteApplication.rectangleName);
            if (rec != null)
            {
                rec.x -= xOffset;
                rec.y -= yOffset;

            }
            collect.add(rec);
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
}
