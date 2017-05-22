package com.exactprosystems.jf.tool.custom.xpath;

import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.JFRemoteException;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.ServiceLambdaBean;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class XpathViewer
{
	private Document                     document;
	private XpathViewerContentController controller;
	private Node                         currentNode;

	private String relativeXpath;

	private Supplier<Document> documentSupplier;
	private ServiceLambdaBean  serviceLambdaBean;
	private Locator            ownerLocator;


	private static ExecutorService executor = Executors.newFixedThreadPool(1);
	private Service<Document>       documentService;
	private Service<ImageAndOffset> imageService;
	private int xOffset = 0;
	private int yOffset = 0;

	@Deprecated
	public XpathViewer(Locator owner, Document document, IRemoteApplication service)
	{
		this.document = document;
		this.ownerLocator = owner;
	}

	public XpathViewer(Locator ownerLocator, Supplier<Document> documentSupplier, ServiceLambdaBean serviceLambdaBean) throws Exception
	{
		this.ownerLocator = ownerLocator;
		this.serviceLambdaBean = serviceLambdaBean;
		this.documentSupplier = documentSupplier;

		this.controller = Common.loadController(XpathViewer.class.getResource("XpathViewerContent.fxml"));
	}


	public String show(String initial, String title, List<String> themePaths, boolean fullScreen)
	{
		this.controller.init(this, initial);
		String result = this.controller.show(title, themePaths, fullScreen);
		return result == null ? initial : result;
	}

	//region xpath methods
	void setRelativeXpath(String xpath)
	{
		this.relativeXpath = xpath;
	}

	void applyXpath(String xpath)
	{
		Node rootNode = this.document;
		if (this.ownerLocator != null)
		{
			rootNode = getFirst(this.document, "/*");
		}

		this.controller.deselectItems();
		this.controller.displayResults(evaluate(rootNode, xpath));
	}

	void updateNode(Node node)
	{
		this.currentNode = node;

		ArrayList<String> params = new ArrayList<>();
		NamedNodeMap attributes = node.getAttributes();
		Optional.ofNullable(attributes).ifPresent(attr -> IntStream.range(0, attr.getLength()).mapToObj(attr::item).map(Node::getNodeName).forEach(params::add));
		this.controller.displayParams(params);
	}

	void createXpaths(boolean useText, List<String> parameters)
	{
		String relativePath = null;
		Node relativeNode = null;
		Node rootNode = this.document;

		if (this.ownerLocator != null)
		{
			relativePath = ".";
			relativeNode = getFirst(this.document, "/*");
			rootNode = relativeNode;
		}

		if (!Str.IsNullOrEmpty(this.relativeXpath))
		{
			relativePath = this.relativeXpath;
			relativeNode = getFirst(this.document, this.relativeXpath);
		}

		String xpath1 = fullXpath(relativePath, relativeNode, currentNode, false, null, true);
		String xpath2 = fullXpath(relativePath, relativeNode, currentNode, useText, parameters, true);
		String xpath3 = fullXpath(relativePath, relativeNode, currentNode, false, null, false);
		String xpath4 = fullXpath(relativePath, relativeNode, currentNode, useText, parameters, false);

		this.controller.displayXpaths(xpath1, xpath2, xpath3, xpath4);
		this.controller.displayCounters(evaluate(rootNode, xpath1), evaluate(rootNode, xpath2), evaluate(rootNode, xpath3), evaluate(rootNode, xpath4));

		if (this.currentNode != null)
		{
			Rectangle rectangle = (Rectangle) this.currentNode.getUserData(IRemoteApplication.rectangleName);
			this.controller.displayRectangle(rectangle);
		}
	}
	//endregion

	void displayImageAndTree()
	{
		if (documentService != null)
		{
			this.documentService.cancel();
		}
		if (imageService != null)
		{
			this.imageService.cancel();
		}
		this.documentService = new Service<Document>()
		{
			@Override
			protected Task<Document> createTask()
			{
				return new Task<Document>()
				{
					@Override
					protected Document call() throws Exception
					{
						return documentSupplier.get();
						//						byte[] treeBytes = service.getTreeBytes(ownerLocator);
						//						return Converter.convertByteArrayToXmlDocument(treeBytes);
					}
				};
			}
		};

		this.imageService = new Service<ImageAndOffset>()
		{
			@Override
			protected Task<ImageAndOffset> createTask()
			{
				return new Task<ImageAndOffset>()
				{
					@Override
					protected ImageAndOffset call() throws Exception
					{
						int offsetX, offsetY;
						Rectangle rectangle = serviceLambdaBean.rectangleSupplier().get();
						offsetX = rectangle.x;
						offsetY = rectangle.y;
						BufferedImage image = serviceLambdaBean.bufferedImageSupplier().get();
						return new ImageAndOffset(image, offsetX, offsetY);
					}
				};
			}
		};
		this.documentService.setExecutor(executor);
		this.imageService.setExecutor(executor);

		this.documentService.setOnSucceeded(event ->
		{
			this.document = (Document) event.getSource().getValue();
			this.currentNode = XpathViewer.getFirst(this.document, "/*");
			this.controller.displayDocument(this.document, xOffset, yOffset);
		});
		this.imageService.setOnSucceeded(event ->
		{
			ImageAndOffset imageAndOffset = (ImageAndOffset) event.getSource().getValue();
			xOffset = imageAndOffset.offsetX;
			yOffset = imageAndOffset.offsetY;
			this.controller.displayImage(imageAndOffset.image);
		});

		this.imageService.setOnFailed(event ->
		{
			Throwable exception = event.getSource().getException();
			String message = exception.getMessage();
			if (exception.getCause() instanceof JFRemoteException)
			{
				message = ((JFRemoteException) exception.getCause()).getErrorKind().toString();
			}
			this.controller.displayImageFailing(message);
		});

		this.documentService.setOnFailed(event ->
		{
			Throwable exception = event.getSource().getException();
			String message = exception.getMessage();
			if (exception.getCause() instanceof JFRemoteException)
			{
				message = ((JFRemoteException) exception.getCause()).getErrorKind().toString();
			}
			this.controller.displayDocumentFailing(message);
		});
		this.imageService.start();
		this.documentService.start();
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
				return "/" + xpath(node.getParentNode(), node, useText, parameters);
			}
			return xpath(null, node, useText, parameters);
		}
		else
		{
			Node common = commonAncestor(relative, node);
			Node current = relative;
			String backPath = "";
			while (current != null && !current.equals(common))
			{
				current = current.getParentNode();
				backPath += "/..";
			}

			if (!longPath)
			{
				return relativeXpath + backPath + "/" + xpath(node.getParentNode(), node, useText, parameters);
			}
			return relativeXpath + backPath + xpath(common, node, useText, parameters);
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

	public static Node getFirst(Node node, String xpathStr)
	{
		List<Node> nodes = evaluate(node, xpathStr);
		return nodes == null || nodes.isEmpty() ? null : nodes.get(0);
	}

	//region private methods
	private static String xpath(Node parent, Node node, boolean useText, List<String> parameters)
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
				"[" + getParameters(node, useText, parameters) + "]" : (hasSiblings(node) ? "[" + getIndexNode(node) + "]" : ""));
	}

	private static Node commonAncestor(Node node1, Node node2)
	{
		if (node1 == null || node2 == null)
		{
			return null;
		}
		Iterator<Node> iterator1 = ancestors(node1).iterator();
		Iterator<Node> iterator2 = ancestors(node2).iterator();
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

	private static List<Node> ancestors(Node node)
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

	private static String getParameters(Node node, boolean useText, List<String> parameters)
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

	private static int getIndexNode(Node node)
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

	private static boolean hasSiblings(Node node)
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
	//endregion

}