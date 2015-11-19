package com.exactprosystems.jf.tool.custom.xpath;

import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.tool.Common;

import javafx.concurrent.Task;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XpathViewer
{
	private Document						document;
	private XpathViewerContentController	controller;
	private Node							currentNode;
	private Locator							owner;
	private IRemoteApplication				service;
	private String							relativeXpath;

	public XpathViewer(Locator owner, Document document, IRemoteApplication service)
	{
		this.document = document;
		this.owner = owner;
		this.service = service;
	}

	public String show(String initial, String title, String themePath, boolean fullScreen)
	{
		this.controller = Common.loadController(XpathViewer.class.getResource("XpathViewerContent.fxml"));
		this.controller.init(this, initial);
		this.controller.displayTree(this.document);
		String result = this.controller.show(title, themePath, fullScreen);
		return result == null ? initial : result;
	}

	public void setRelativeXpath(String xpath)
	{
		this.relativeXpath = xpath;
	}

	public void applyXpath(String xpath)
	{
		this.controller.displayResults(evaluate(xpath));
	}

	public void updateNode(Node node)
	{
		this.currentNode = node;
		
		ArrayList<String> params = new ArrayList<>();
		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null)
		{
			int length = attributes.getLength();
			for (int i = 0; i < length; i++)
			{
				Node item = attributes.item(i);
				params.add(item.getNodeName());
			}
		}
		
		this.controller.displayParams(params);
	}

	public void createXpaths(List<String> parameters)
	{
		Node relativeNode = null;
		if (this.relativeXpath != null)
		{
			List<Node> nodes = evaluate(this.relativeXpath);
			relativeNode = nodes == null || nodes.isEmpty() ? null : nodes.get(0);
		}
		
		String xpath1 = createXpathAbsolute(relativeNode, this.currentNode);
		String xpath2 = createXpathWithParameters(relativeNode, this.currentNode, parameters);
		String xpath3 = createXpathWithoutParameters(relativeNode, this.currentNode);

		this.controller.displayXpaths(xpath1, xpath2, xpath3);
		this.controller.displayCounters(evaluate(xpath1), evaluate(xpath2), evaluate(xpath3));
		
		if (this.service != null)
		{
			new Thread(new Task<Void>()
			{
				@Override
				protected Void call() throws Exception
				{
					Common.tryCatch(() -> service.highlight(owner, xpath1), "Error on highlight element");
					return null;
				}
			}).start();
		}
	}

	// ============================================================
	// private methods
	// ============================================================
	private List<Node> evaluate(String xpathStr)
	{
		if (xpathStr == null)
		{
			return null;
		}
		XPath xpath = XPathFactory.newInstance().newXPath();
		try
		{
			XPathExpression compile = xpath.compile(xpathStr);
			NodeList nodeList = (NodeList) compile.evaluate(this.document.getDocumentElement(), XPathConstants.NODESET);
			List<Node> nodes = new ArrayList<>();
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				nodes.add(nodeList.item(i));
			}
			return nodes;
		}
		catch (XPathExpressionException e)
		{
		}
		
		return null;
	}
	
	private boolean haveSameChild(Node node)
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
				res++;
				if (res > 1)
				{
					return true;
				}
			}
		}
		return res != 1;
	}

	private int getIndexNode(Node node)
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
			if (item == node)
			{
				return result;
			}
		}
		return result;
	}

	private String getNodePath(Node node, Node parent)
	{
		if (node instanceof Document || (parent != null && node == parent))
		{
			return null;
		}
		String s = node.getNodeName();
		if (haveSameChild(node))
		{
			int index = getIndexNode(node);
			s = s + "[" + index + "]";
		}
		return s + "/";
	}

	private String getAbsoluteXpath(Node currentNode, Node generalParent)
	{
		StringBuilder b = new StringBuilder(currentNode.getNodeName());
		if (haveSameChild(currentNode))
		{
			int index = getIndexNode(currentNode);
			b.append("[").append(index).append("]");
		}
		String s;
		Node parent = currentNode.getParentNode();
		while ((s = getNodePath(parent, generalParent)) != null)
		{
			b.insert(0, s);
			parent = parent.getParentNode();
		}
		return b.insert(0, "//").toString();
	}

	private ArrayList<Node> getParents(Node node)
	{
		ArrayList<Node> parents = new ArrayList<>();
		Node parentNode = node.getParentNode();
		while (!(parentNode instanceof Document))
		{
			parents.add(parentNode);
			parentNode = parentNode.getParentNode();
		}
		Collections.reverse(parents);
		return parents;
	}

	private Node getGeneralParent(Node relativeNode, Node currentNode, AtomicInteger count)
	{
		ArrayList<Node> nodeParents = getParents(currentNode);
		ArrayList<Node> relativeParents = getParents(relativeNode);
		int nodeParentsSize = nodeParents.size();
		int relativeParentsSize = relativeParents.size();
		int size = nodeParentsSize > relativeParentsSize ? nodeParentsSize : relativeParentsSize;
		Node generalParent = null;
		for (int i = 0; i < size; i++)
		{
			if ((i == relativeParentsSize || i == nodeParentsSize) || (nodeParents.get(i) != relativeParents.get(i)))
			{
				generalParent = nodeParents.get(i - 1);
				count.set(relativeParentsSize - i + 1);
				break;
			}
		}
		return generalParent;
	}

	private String getXpath(Node relativeNode, Node currentNode, AtomicInteger count, String xpathCurrentNode)
	{
		String xpath;
		StringBuilder builder = new StringBuilder(this.relativeXpath);
		Stream.iterate(0, i -> ++i).limit(count.get()).forEach(i1 -> builder.append("/.."));
		if (relativeNode != currentNode)
		{
			builder.append(xpathCurrentNode);
		}
		xpath = builder.toString();
		return xpath;
	}

	private String getXpathWithParameters(Node currentNode, List<String> parameters)
	{
		StringBuilder b = new StringBuilder("//");
		b.append(currentNode.getNodeName());
		if (currentNode.getAttributes() != null)
		{
			String collect = parameters.stream().map(p -> "@" + currentNode.getAttributes().getNamedItem(p)).collect(Collectors.joining(" and "));
			b.append("[").append(collect).append("]");
		}

		return b.toString();
	}

	private String createXpathAbsolute(Node relativeNode, Node currentNode)
	{
		String xpath = null;
		if (relativeNode == null)
		{
			xpath = getAbsoluteXpath(currentNode, null);
		}
		else
		{
			AtomicInteger count = new AtomicInteger(0);
			Node generalParent = getGeneralParent(relativeNode, currentNode, count);
			String xpathCurrentNode = getAbsoluteXpath(currentNode, generalParent);
			xpath = getXpath(relativeNode, currentNode, count, xpathCurrentNode);
		}
		return xpath;
	}

	private String createXpathWithParameters(Node relativeNode, Node currentNode, final List<String> parameters)
	{
		String xpath = null;
		if (relativeNode != null)
		{
			AtomicInteger count = new AtomicInteger(0);
			getGeneralParent(relativeNode, currentNode, count);
			String xpathCurrentNode = getXpathWithParameters(currentNode, parameters);
			xpath = getXpath(relativeNode, currentNode, count, xpathCurrentNode);
		}
		else
		{
			xpath = getXpathWithParameters(currentNode, parameters);
		}
		return xpath;
	}

	private String createXpathWithoutParameters(Node relativeNode, Node currentNode)
	{
		String xpath = "//" + currentNode.getNodeName();
		if (relativeNode != null)
		{
			AtomicInteger count = new AtomicInteger(0);
			getGeneralParent(relativeNode, currentNode, count);
			xpath = getXpath(relativeNode, currentNode, count, xpath);
		}

		return xpath;
	}
}