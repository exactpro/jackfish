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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class XpathViewer
{
	private double currentZoom = 1;
	private double initialWidth = 0;
	private double initialHeight = 0;
	private Optional<Rectangle> currentRectangle = Optional.empty();
	private Optional<Map.Entry<Rectangle, String>> inspectEntry = Optional.empty();

	private int xOffset = 0;
	private Document						document;
	private XpathViewerContentController	controller;
	private Node							currentNode;
	private Locator							owner;
	private IRemoteApplication				service;

	private String							relativeXpath;
	private int yOffset = 0;

	private Map<Rectangle, String> mapRectangles = new HashMap<>();
	private Map<Rectangle, String> mapCurrentRectangles = new HashMap<>();

	public XpathViewer(Locator owner, Document document, IRemoteApplication service)
	{
		this.document = document;
		this.owner = owner;
		this.service = service;
		this.bypassDocument(this.document, (r, s) -> this.mapRectangles.put(r, s));
	}

	public String show(String initial, String title, String themePath, boolean fullScreen)
	{
		this.controller = Common.loadController(XpathViewer.class.getResource("XpathViewerContent.fxml"));
		this.controller.init(this, initial);
		this.controller.displayTree(this.document);
		if (this.service != null)
		{
			Task<BufferedImage> task = new Task<BufferedImage>()
			{
				@Override
				protected BufferedImage call() throws Exception
				{
					if (owner != null)
					{
						Rectangle rectangle = service.getRectangle(null, owner);
						xOffset = rectangle.x;
						yOffset = rectangle.y;
					}
					BufferedImage image = service.getImage(null, owner).getImage();
					initialWidth = image.getWidth();
					initialHeight = image.getHeight();
					return image;
				}
			};
			task.setOnSucceeded(event -> Common.tryCatch(() -> this.controller.displayImage(((BufferedImage) event.getSource().getValue())), "Error on display image"));
			task.setOnFailed(event -> Common.tryCatch(() -> this.controller.displayImage(null), "Error on display image"));
			new Thread(task).start();
		}
		else
		{
			Common.tryCatch(() -> this.controller.displayImage(null), "Error on display image");
		}
		String result = this.controller.show(title, themePath, fullScreen);
		return result == null ? initial : result;
	}

	public void setRelativeXpath(String xpath)
	{
		this.relativeXpath = xpath;
	}

	public void applyXpath(String xpath)
	{
		this.controller.deselectItems();
		this.controller.displayResults(evaluate(xpath));
	}

	public void updateNode(Node node)
	{
		this.currentNode = node;
		
		ArrayList<String> params = new ArrayList<>();
		NamedNodeMap attributes = node.getAttributes();
		Optional.ofNullable(attributes).ifPresent(attr-> IntStream.range(0, attr.getLength()).mapToObj(attr::item).map(Node::getNodeName).forEach(params::add));
		this.controller.displayParams(params);
	}

	public void createXpaths(boolean useText, List<String> parameters)
	{
		Node relativeNode = null;
		if (this.relativeXpath != null)
		{
			List<Node> nodes = evaluate(this.relativeXpath);
			relativeNode = nodes == null || nodes.isEmpty() ? null : nodes.get(0);
		}
		
		String xpath1 = fullXpath(relativeNode, currentNode, false, 	null, 		true);
		String xpath2 = fullXpath(relativeNode, currentNode, useText, 	parameters, true);
		String xpath3 = fullXpath(relativeNode, currentNode, false, 	null, 		false);
		String xpath4 = fullXpath(relativeNode,	currentNode, useText, 	parameters, false);
		
		this.controller.displayXpaths(xpath1, xpath2, xpath3, xpath4);
		this.controller.displayCounters(evaluate(xpath1), evaluate(xpath2), evaluate(xpath3), evaluate(xpath4));

		Rectangle rectangle = (Rectangle) this.currentNode.getUserData(IRemoteApplication.rectangleName);
		this.currentRectangle = Optional.ofNullable(rectangle);
		this.displayRectangle();
	}
	
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
				.filter(value -> value != null)
				.map(value -> value.trim().replace('\n', ' '))
				.forEach(sb::append);
		return sb.toString();
	}

	public void changeScale(double scale)
	{
		this.currentZoom = scale;
		resizeImage();
	}

	public void startInspect()
	{
		this.mapCurrentRectangles.clear();
		this.mapCurrentRectangles.putAll(this.mapRectangles.entrySet()
				.parallelStream()
				.collect(Collectors.toMap(key -> new Rectangle(
						(int) ((key.getKey().x - xOffset) * currentZoom),
						(int) ((key.getKey().y - xOffset) * currentZoom),
						(int) (key.getKey().width * currentZoom),
						(int) (key.getKey().height * currentZoom)), Map.Entry::getValue))
		);
	}

	public void moveOnImage(double x, double y)
	{
		Map.Entry<Rectangle, String> oldEntry = this.inspectEntry.orElse(new AbstractMap.SimpleEntry<>(new Rectangle(-1, -1, -1, -1), ""));
		Rectangle oldRectangle = oldEntry.getKey();
		this.inspectEntry = this.mapCurrentRectangles.entrySet()
				.parallelStream()
				.filter(entry -> {
					Rectangle r = entry.getKey();
					return r.x < x && (r.x + r.width) > x &&
							r.y < y && (r.y + r.height) > y;
				})
				.sorted((e1,e2) -> Double.compare(e1.getKey().width * e1.getKey().height, e2.getKey().width * e2.getKey().height))
				.findFirst();
		this.inspectEntry.filter(entry -> !Objects.equals(oldRectangle, entry.getKey())).ifPresent(entry -> this.controller.displayInspectRectangle(entry.getKey(), entry.getValue()));
	}

	public void clickOnImage()
	{
		this.inspectEntry.ifPresent(entry -> this.controller.selectItem(entry.getKey()));
		this.controller.stopInspect();
	}

	// ============================================================
	// private methods
	// ============================================================
	private String getStringNode(Node node)
	{
		StringBuilder builder = new StringBuilder();
		String id = "";
		String className = "";
		builder.append(node.getNodeName()).append(" ");
		NamedNodeMap attrs = node.getAttributes();
		if (attrs != null)
		{
			for (int i = 0; i < attrs.getLength(); i++)
			{
				Node attr = attrs.item(i);
				if (attr != null && attr.getNodeName().equals("id"))
				{
					id = attr.getNodeValue();
				}
				if (attr != null && attr.getNodeName().equals("class"))
				{
					className = attr.getNodeValue();
				}
			}
		}
		if (!id.isEmpty())
		{
			builder.append("#").append(id).append(" ");
		}
		if (!className.isEmpty())
		{
			Arrays.stream(className.split("\\s")).forEach(cn -> builder.append(".").append(cn).append(" "));
		}
		return builder.toString();
	}

	private void bypassDocument(Node node, BiConsumer<Rectangle, String> biConsumer)
	{
		Object userData = node.getUserData(IRemoteApplication.rectangleName);
		Optional.ofNullable(userData)
				.filter(data -> data instanceof Rectangle)
				.map(rect -> (Rectangle) rect)
				.ifPresent(rec -> biConsumer.accept(rec, getStringNode(node)));

		IntStream.range(0, node.getChildNodes().getLength())
				.mapToObj(node.getChildNodes()::item)
				.filter(item -> item.getNodeType() == Node.ELEMENT_NODE)
				.forEach(childNode -> bypassDocument(childNode, biConsumer));
	}

	private void resizeImage()
	{
		this.controller.resizeImage(this.currentZoom * this.initialWidth, this.currentZoom * this.initialHeight);
		displayRectangle();
	}

	private void displayRectangle()
	{
		this.currentRectangle.ifPresent(rect -> {
			Rectangle newRectangle = new Rectangle(
					(int) ((rect.x - xOffset) * currentZoom),
					(int) ((rect.y - yOffset) * currentZoom),
					(int) ((rect.width) * currentZoom),
					(int) ((rect.height) * currentZoom)
			);
			this.controller.displayRectangle(newRectangle);
		});
	}

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
			return IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item).collect(Collectors.toList());
		}
		catch (XPathExpressionException e)
		{
		}
		
		return null;
	}

	private String fullXpath(Node relative, Node node, boolean useText, List<String> parameters, boolean fromRoot)
	{
		if (node == null)
		{
			return "//*";
		}
		
		if (relative == null)
		{
			if (!fromRoot)
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
			while(current != null && !current.equals(common))
			{
				current = current.getParentNode();
				backPath += "/..";
			}
			return this.relativeXpath + backPath + xpath(common, node, useText, parameters);
		}
	}
	
	private String xpath(Node parent, Node node, boolean useText, List<String> parameters)
	{
		if (node instanceof Document)
		{
			return "";
		}
		if (node == null || node.equals(parent))
		{
			return "";
		}
		return xpath(parent, node.getParentNode(), false, null) + "/" + node.getNodeName() 
				+ 	(parameters != null && !parameters.isEmpty() || useText
						? "[" + getParameters(node, useText, parameters) + "]"
						: (hasSiblings(node) 
							? "[" + getIndexNode(node) + "]"
							: ""
						)
					);
	}
	
	private Node commonAncestor(Node node1, Node node2)
	{
		if (node1 == null || node2 == null)
		{
			return null;
		}
		Iterator<Node> iterator1 = ancestors(node1).iterator(); 
		Iterator<Node> iterator2 = ancestors(node2).iterator(); 
		Node res = null;
		while(iterator1.hasNext() && iterator2.hasNext())
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
	
	private List<Node> ancestors(Node node)
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

	private String getParameters(Node node, boolean useText, List<String> parameters)
	{
		String res = "";
		NamedNodeMap attr = node.getAttributes();
		if (attr != null)
		{
			res = parameters.stream()
					.filter(p -> attr.getNamedItem(p) != null)
					.map(p -> "@" + attr.getNamedItem(p))
					.collect(Collectors.joining(" and "));
		}
		if (useText)
		{
			res = res + (res.isEmpty() ? "" : " and ") + "contains(text(), \"" + text(node) + "\")";  
		}
		return res;
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
			if (item.equals(node))
			{
				return result;
			}
		}
		return result;
	}

	private boolean hasSiblings(Node node)
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