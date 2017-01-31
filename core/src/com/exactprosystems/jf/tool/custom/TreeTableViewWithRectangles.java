package com.exactprosystems.jf.tool.custom;

import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.xpath.XpathCell;
import com.exactprosystems.jf.tool.custom.xpath.XpathItem;
import com.exactprosystems.jf.tool.custom.xpath.XpathViewer;
import com.sun.javafx.scene.control.skin.TreeTableViewSkin;
import javafx.beans.property.SimpleObjectProperty;
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

public class TreeTableViewWithRectangles
{
	private final AnchorPane anchorPane;
	private final TreeTableView<XpathItem> treeTableView;

	private Node waitingNode;

	private Consumer<XpathItem> consumer;

	private Map<Rectangle, TreeItem<XpathItem>> map = new HashMap<>();

	public TreeTableViewWithRectangles()
	{
		this.anchorPane = new AnchorPane();
		this.treeTableView = new TreeTableView<>();
		this.treeTableView.setSkin(new MyCustomSkin(this.treeTableView));
		this.treeTableView.getStyleClass().add(CssVariables.EMPTY_HEADER_COLUMN);

		AnchorPane.setTopAnchor(this.treeTableView, 0.0);
		AnchorPane.setLeftAnchor(this.treeTableView, 0.0);
		AnchorPane.setRightAnchor(this.treeTableView, 0.0);
		AnchorPane.setBottomAnchor(this.treeTableView, 0.0);

		this.anchorPane.getChildren().add(this.treeTableView);

		TreeTableColumn<XpathItem, String> c0 = new TreeTableColumn<>();
		int value = 30;
		c0.setPrefWidth(value);
		c0.setMaxWidth(value);
		c0.setMinWidth(value);
		TreeTableColumn<XpathItem, XpathItem> c1 = new TreeTableColumn<>();
		c1.setCellFactory(p -> new XpathTreeTableCell());
		c1.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getValue()));

		this.treeTableView.getColumns().addAll(c0, c1);
		this.treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
//		c1.prefWidthProperty().bind(this.treeTableView.widthProperty().subtract(value + 2));
		this.treeTableView.setTreeColumn(c1);


		addWaitingPane();

		//		this.treeTableView.setCellFactory(p -> new XpathCell());
		this.treeTableView.getStyleClass().add(CssVariables.XPATH_TREE_VIEW);
		this.treeTableView.setShowRoot(false);

		this.treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> Optional.ofNullable(consumer).ifPresent(c -> c.accept(newValue == null ? null : newValue.getValue())));
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

	public void displayDocument(Document document, int xOffset, int yOffset)
	{
		this.anchorPane.getChildren().remove(this.waitingNode);

		this.treeTableView.setRoot(new TreeItem<>());
		this.displayTree(document, this.treeTableView.getRoot(), xOffset, yOffset);
		expand(this.treeTableView.getRoot());
		((MyCustomSkin) this.treeTableView.getSkin()).resizeColumnToFitContent(this.treeTableView.getColumns().get(1), -1);
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
				passTree(key, set, this.treeTableView.getRoot());
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

	public void setTreeViewConsumer(Consumer<XpathItem> consumer)
	{
		this.consumer = consumer;
	}

	public void selectItem(Rectangle rectangle)
	{
		TreeItem<XpathItem> treeItem = this.map.get(rectangle);
		if (treeItem != null)
		{
			this.treeTableView.getSelectionModel().select(treeItem);
			this.scrollToElement(treeItem);
		}
	}
	//endregion

	//region private methods
	private void addWaitingPane()
	{
		this.waitingNode = new BorderPane();
		((BorderPane) this.waitingNode).setCenter(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS));
		((BorderPane) this.waitingNode).setBottom(new Text("Waiting for document..."));
		AnchorPane.setLeftAnchor(this.waitingNode, 50.0);
		AnchorPane.setTopAnchor(this.waitingNode, 50.0);

		this.anchorPane.getChildren().add(this.waitingNode);
	}

	private void expand(TreeItem<XpathItem> item)
	{
		item.setExpanded(true);
		item.getChildren().forEach(this::expand);
	}

	private void displayTree(org.w3c.dom.Node node, TreeItem<XpathItem> parent, int xOffset, int yOffset)
	{
		boolean isDocument = node.getNodeType() == org.w3c.dom.Node.DOCUMENT_NODE;

		TreeItem<XpathItem> treeItem = isDocument ? parent : new TreeItem<>();
		IntStream.range(0, node.getChildNodes().getLength()).mapToObj(node.getChildNodes()::item).filter(item -> item.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE).forEach(item -> displayTree(item, treeItem, xOffset, yOffset));
		if (!isDocument)
		{
			treeItem.setValue(new XpathItem(stringNode(node, XpathViewer.text(node)), node));
			Rectangle rec = (Rectangle) node.getUserData(IRemoteApplication.rectangleName);
			if (rec != null)
			{
				rec.x -= xOffset;
				rec.y -= yOffset;

			}
			this.map.put(rec, treeItem);
			parent.getChildren().add(treeItem);
		}
	}

	private HBox stringNode(org.w3c.dom.Node node, String text)
	{
		HBox box = new HBox();

		box.getChildren().add(createText("<" + node.getNodeName() + " ", CssVariables.XPATH_NODE, true));
		NamedNodeMap attributes = node.getAttributes();
		Optional.ofNullable(attributes).ifPresent(atrs ->
		{
			int length = atrs.getLength();
			IntStream.range(0, length).mapToObj(atrs::item).forEach(item -> box.getChildren().addAll(createText(item.getNodeName(), CssVariables.XPATH_ATTRIBUTE_NAME, false), createText("=", CssVariables.XPATH_TEXT, false), createText("\"" + item.getNodeValue() + "\" ", CssVariables.XPATH_ATTRIBUTE_VALUE, true)));
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
			t.setOnContextMenuRequested(event ->
			{

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

	private void scrollToElement(TreeItem<XpathItem> xpathItemTreeItem)
	{
		MyCustomSkin skin = (MyCustomSkin) treeTableView.getSkin();
		int row = treeTableView.getRow(xpathItemTreeItem);
		if (!skin.isIndexVisible(row))
		{
			skin.show(row);
		}
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
	//endregion

	private class MyCustomSkin extends TreeTableViewSkin<XpathItem>
	{
		public MyCustomSkin(TreeTableView<XpathItem> treeTableView)
		{
			super(treeTableView);
		}

		public void show(int index)
		{
			flow.show(index);
		}

		public boolean isIndexVisible(int index)
		{
			return flow.getFirstVisibleCell() != null &&
					flow.getLastVisibleCell() != null &&
					flow.getFirstVisibleCell().getIndex() <= index - 1 &&
					flow.getLastVisibleCell().getIndex() >= index + 1;
		}

		@Override
		public void resizeColumnToFitContent(TreeTableColumn<XpathItem, ?> tc, int maxRows)
		{
			super.resizeColumnToFitContent(tc, maxRows);
		}
	}

	private class XpathTreeTableCell extends TreeTableCell<XpathItem, XpathItem>
	{
		public XpathTreeTableCell()
		{
		}

		@Override
		protected void updateItem(XpathItem item, boolean empty)
		{
			super.updateItem(item, empty);
			if (item != null)
			{
				setGraphic(item.getBox());
			}
			else
			{
				setGraphic(null);
			}
		}
	}
}