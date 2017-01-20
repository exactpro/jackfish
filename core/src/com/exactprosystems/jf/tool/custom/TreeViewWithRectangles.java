package com.exactprosystems.jf.tool.custom;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.xpath.XpathCell;
import com.exactprosystems.jf.tool.custom.xpath.XpathItem;
import com.exactprosystems.jf.tool.custom.xpath.XpathViewer;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.awt.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class TreeViewWithRectangles
{
	private final AnchorPane anchorPane;
	private final TreeView<XpathItem> treeView;

	private Node waitingNode;

	private Consumer<XpathItem> consumer;

	public TreeViewWithRectangles()
	{
		this.anchorPane = new AnchorPane();
		this.treeView = new TreeView<>();

		AnchorPane.setTopAnchor(this.treeView, 0.0);
		AnchorPane.setLeftAnchor(this.treeView, 0.0);
		AnchorPane.setRightAnchor(this.treeView, 0.0);
		AnchorPane.setBottomAnchor(this.treeView, 0.0);

		this.anchorPane.getChildren().add(this.treeView);

		addWaitingPane();

		this.treeView.setCellFactory(p -> new XpathCell());
		this.treeView.getStyleClass().add(CssVariables.XPATH_TREE_VIEW);
		this.treeView.setShowRoot(false);

		this.treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
				Optional.ofNullable(consumer).ifPresent(c -> c.accept(newValue == null ? null : newValue.getValue()))
		);
	}

	//region public methods
	public void replaceWaitingPane(Node node)
	{
		AnchorPane.setTopAnchor(node, 50.0);
		AnchorPane.setLeftAnchor(node, 50.0);

		this.anchorPane.getChildren().remove(this.waitingNode);
		this.waitingNode = node;
		this.anchorPane.getChildren().add(this.waitingNode);
	}

	public Node getContent()
	{
		return this.anchorPane;
	}

	public void displayDocument(Document document)
	{
		this.anchorPane.getChildren().remove(this.waitingNode);

		this.treeView.setRoot(new TreeItem<>());
		this.displayTree(document, this.treeView.getRoot());
		expand(this.treeView.getRoot());
	}

	public Map<Rectangle, Set<Rectangle>> buildMap(int width, int height, Dimension cellSize)
	{
		Map<Rectangle, Set<Rectangle>> map = new HashMap<>();

		int x = 0;
		while (x < width)
		{
			int y = 0;
			while (y < height)
			{
				Rectangle key = new Rectangle(new Point(x, y), cellSize);
				Set<Rectangle> set = new HashSet<>();
				passTree(key, set, this.treeView.getRoot());
				if (set.size() > 0)
				{
					map.put(key, set);
				}

				y += cellSize.height;
			}
			x += cellSize.width;
		}
		return map;
	}

	private void passTree(Rectangle keyRectangle, Set<Rectangle> set, TreeItem<XpathItem> item)
	{
		XpathItem xpath = item.getValue();
		if (xpath != null)
		{
			Rectangle rec = xpath.getRectangle();

			if (rec != null && rec.intersects(keyRectangle))
			{
				set.add(rec);
			}
		}
		item.getChildren().forEach(child -> passTree(keyRectangle, set, child));
	}

	public void setTreeViewConsumer(Consumer<XpathItem> consumer)
	{
		this.consumer = consumer;
	}
	//endregion

	//region private methods
	private void addWaitingPane()
	{
		this.waitingNode = new BorderPane();
		((BorderPane)this.waitingNode).setCenter(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS));
		((BorderPane)this.waitingNode).setBottom(new Text("Waiting for document..."));
		AnchorPane.setLeftAnchor(this.waitingNode, 50.0);
		AnchorPane.setTopAnchor(this.waitingNode, 50.0);

		this.anchorPane.getChildren().add(this.waitingNode);
	}

	private void expand(TreeItem<XpathItem> item)
	{
		item.setExpanded(true);
		item.getChildren().forEach(this::expand);
	}

	private void displayTree(org.w3c.dom.Node node, TreeItem<XpathItem> parent)
	{
		boolean isDocument = node.getNodeType() == org.w3c.dom.Node.DOCUMENT_NODE;

		TreeItem<XpathItem> treeItem = isDocument ? parent : new TreeItem<>();
		IntStream.range(0, node.getChildNodes().getLength())
				.mapToObj(node.getChildNodes()::item)
				.filter(item -> item.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				.forEach(item -> displayTree(item, treeItem));
		if (!isDocument)
		{
			treeItem.setValue(new XpathItem(stringNode(node, XpathViewer.text(node)), node));
			parent.getChildren().add(treeItem);
		}
	}

	private HBox stringNode(org.w3c.dom.Node node, String text)
	{
		HBox box = new HBox();

		box.getChildren().add(createText("<" + node.getNodeName() + " ", CssVariables.XPATH_NODE, true));
		NamedNodeMap attributes = node.getAttributes();
		Optional.ofNullable(attributes).ifPresent(atrs -> {
			int length = atrs.getLength();
			IntStream.range(0, length)
					.mapToObj(atrs::item)
					.forEach(item -> box.getChildren().addAll(
							createText(item.getNodeName(), CssVariables.XPATH_ATTRIBUTE_NAME, false)
							, createText("=", CssVariables.XPATH_TEXT, false)
							, createText("\"" + item.getNodeValue() + "\" ", CssVariables.XPATH_ATTRIBUTE_VALUE, true)));
		});
		if (Str.IsNullOrEmpty(text))
		{
			box.getChildren().add(createText("/>", CssVariables.XPATH_NODE, true));
		}
		else
		{
			box.getChildren().addAll(createText(">", CssVariables.XPATH_NODE, true), createText(text, CssVariables.XPATH_TEXT, true), createText("</" + node.getNodeName() + ">", CssVariables.XPATH_NODE, true));
		}
		return box;
	}

	private Text createText(String text, String cssClass, boolean useContextMenu)
	{
		Text t = new Text(text);
		if (useContextMenu && !text.isEmpty())
		{
			t.setOnContextMenuRequested(event -> {

				MenuItem item = new MenuItem("Copy " + text);
				item.setOnAction(e -> Common.copyText(text));
				if (t.getParent().getParent() instanceof XpathCell)
				{
					XpathCell parent = (XpathCell) t.getParent().getParent();
					SeparatorMenuItem separator = new SeparatorMenuItem();
					ContextMenu treeMenu = parent.getContextMenu();
					treeMenu.getItems().add(0, item);
					treeMenu.getItems().add(1, separator);
					treeMenu.setOnHidden(e -> treeMenu.getItems().removeAll(item, separator));
				}
				else
				{
					ContextMenu menu = new ContextMenu();
					menu.setAutoHide(true);
					menu.getItems().add(item);
					menu.show(t, MouseInfo.getPointerInfo().getLocation().getX(), MouseInfo.getPointerInfo().getLocation().getY());
				}
			});
		}
		t.getStyleClass().add(cssClass);
		return t;
	}
	//endregion
}
