package com.exactprosystems.jf.tool.custom.xmltree;

import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.dictionary.dialog.ElementWizardBean;
import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;
import com.exactprosystems.jf.tool.wizard.related.XpathItem;
import com.exactprosystems.jf.tool.wizard.related.XmlTreeItem;
import com.sun.javafx.scene.control.skin.TreeTableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class XmlTreeView extends AnchorPane
{
	public static final double TRANSPARENT_RECT = 0.25;

	private final TreeTableView<XmlTreeItem> treeTableView;

	private int currentIndex = -1;

	private Node waitingNode;

	private Consumer<Void> updateCounters;
	private Consumer<Void> refreshTable;

	private Consumer<List<Rectangle>> removeConsumer;
	private List<Consumer<XmlTreeItem>> selectionConsumers = new ArrayList<>();
	private Consumer<List<Rectangle>> markedRowsConsumer;

	private Map<Rectangle, TreeItem<XmlTreeItem>> map = new HashMap<>();

	private Map<MarkerStyle, Boolean> stateMap;

	public XmlTreeView(Consumer<Void> updateCounters, Consumer<Void> refreshTable)
	{
		super();

		this.stateMap = Stream.of(MarkerStyle.values()).collect(Collectors.toMap(v -> v, v -> true));
		this.updateCounters = updateCounters;
		this.refreshTable = refreshTable;
		this.treeTableView = new TreeTableView<>();
		this.treeTableView.setSkin(new MyCustomSkin(this.treeTableView));
		this.treeTableView.getStyleClass().add(CssVariables.EMPTY_HEADER_COLUMN);

		AnchorPane.setTopAnchor(this.treeTableView, 0.0);
		AnchorPane.setLeftAnchor(this.treeTableView, 0.0);
		AnchorPane.setRightAnchor(this.treeTableView, 0.0);
		AnchorPane.setBottomAnchor(this.treeTableView, 0.0);

		this.getChildren().add(this.treeTableView);

		TreeTableColumn<XmlTreeItem, XmlTreeItem> c0 = new TreeTableColumn<>();
		int value = 30;
		c0.setPrefWidth(value);
		c0.setMaxWidth(value);
		c0.setMinWidth(value);
		c0.setCellFactory(p -> new XmlIconCell());
		c0.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getValue()));
        this.treeTableView.getColumns().add(c0);

		TreeTableColumn<XmlTreeItem, XmlTreeItem> c1 = new TreeTableColumn<>();
		c1.setCellFactory(p -> new XmlTreeTableCell());
		c1.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getValue()));
        c1.setPrefWidth(5000.0);
        this.treeTableView.getColumns().add(c1);

        this.treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
		this.treeTableView.setTreeColumn(c1);

		addWaitingPane();

		this.treeTableView.getStyleClass().add(CssVariables.XPATH_TREE_VIEW);
		this.treeTableView.setShowRoot(false);
		this.treeTableView.setOnMouseClicked(e -> {
			EventTarget target = e.getTarget();
			if (target instanceof XmlIconCell)
			{
				XmlIconCell iconCell = (XmlIconCell) target;
				TreeItem<XmlTreeItem> treeItem = iconCell.getTreeTableRow().getTreeItem();
				if (treeItem != null)
				{
					XmlTreeItem xpathTreeItem = treeItem.getValue();
					xpathTreeItem.changeState();
					this.refreshTable.accept(null);
					this.displayMarkedRows();
					this.updateCounters.accept(null);
					refresh();
				}
			}

		});

		this.treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
				this.selectionConsumers.stream()
						.filter(Objects::nonNull)
						.forEach(c -> c.accept(newValue == null ? null : newValue.getValue())));
	}

	
	//region public methods
	public void setMarkersVisible(boolean value)
	{
		this.treeTableView.getColumns().get(0).setVisible(value);
	}

	public void displayDocument(Document document)
	{
		this.getChildren().remove(this.waitingNode);

		this.treeTableView.setRoot(new TreeItem<>());
		displayTree(document, this.treeTableView.getRoot());
		expand(this.treeTableView.getRoot());
		((MyCustomSkin) this.treeTableView.getSkin()).resizeColumnToFitContent(this.treeTableView.getColumns().get(1), -1);
	}

	public void addSelectionConsumer(Consumer<XmlTreeItem> consumer)
	{
		this.selectionConsumers.add(consumer);
	}

	public void setDisplayMarkedRowsConsumer(Consumer<List<Rectangle>> markedRowsConsumer)
	{
		this.markedRowsConsumer = markedRowsConsumer;
	}

	public void selectItem(Rectangle rectangle)
	{
		TreeItem<XmlTreeItem> treeItem = this.map.get(rectangle);
		if (treeItem != null)
		{
			this.treeTableView.getSelectionModel().select(treeItem);
			this.scrollToElement(treeItem);
		}
	}

	public void selectAndScroll(TreeItem<XmlTreeItem> XpathTreeItem)
	{
		this.treeTableView.getSelectionModel().clearSelection();
		this.treeTableView.getSelectionModel().select(XpathTreeItem);
		scrollToElement(XpathTreeItem);
	}

	public List<TreeItem<XmlTreeItem>> findItem(String what, boolean matchCase, boolean wholeWord)
	{
		ArrayList<TreeItem<XmlTreeItem>> res = new ArrayList<>();
		TreeItem<XmlTreeItem> root = this.treeTableView.getRoot();
		addItems(res, root, what, matchCase, wholeWord);
		return res;
	}

	public void nextMark()
	{
		List<TreeItem<XmlTreeItem>> markedRows = getTreeItems();
		if (markedRows.isEmpty())
		{
			return;
		}
		this.currentIndex = Math.min(this.currentIndex + 1, markedRows.size() - 1);
		TreeItem<XmlTreeItem> treeItem = markedRows.get(this.currentIndex);
		scrollToElement(treeItem);
		this.treeTableView.getSelectionModel().select(treeItem);
	}

	public void prevMark()
	{
		List<TreeItem<XmlTreeItem>> markedRows = getTreeItems();
		if (markedRows.isEmpty())
		{
			return;
		}
        this.currentIndex = Math.max(this.currentIndex - 1, 0);
		TreeItem<XmlTreeItem> treeItem = markedRows.get(this.currentIndex);
		scrollToElement(treeItem);
		this.treeTableView.getSelectionModel().select(treeItem);
	}

	public void setState(MarkerStyle state, boolean newValue)
	{
		this.stateMap.replace(state, newValue);
		this.displayMarkedRows();
		this.refresh();
	}

	public TreeItem<XmlTreeItem> findByNode(org.w3c.dom.Node node)
	{
		TreeItem<XmlTreeItem> root = this.treeTableView.getRoot();
		List<TreeItem<XmlTreeItem>> list = new ArrayList<>();
		byPass(root, list, xpathTreeItem -> xpathTreeItem != null && xpathTreeItem.getNode() != null && xpathTreeItem.getNode().equals(node));
		return list.get(0);
	}

	public List<TreeItem<XmlTreeItem>> findByNodes(List<org.w3c.dom.Node> nodes)
	{
		TreeItem<XmlTreeItem> root = this.treeTableView.getRoot();
		List<TreeItem<XmlTreeItem>> list = new ArrayList<>();
		byPass(root, list, xpathTreeItem -> xpathTreeItem != null
				&& xpathTreeItem.getNode() != null
				&& nodes.stream().anyMatch(node -> xpathTreeItem.getNode() == node)
		);
		return list;
	}

	public void refresh()
	{
		Platform.runLater(() -> {
			this.treeTableView.getColumns().get(0).setVisible(false);
			this.treeTableView.getColumns().get(0).setVisible(true);
		});
	}

	public void clearAndAddRelation(ElementWizardBean bean)
	{
		TreeItem<XmlTreeItem> selectedItem = this.treeTableView.getSelectionModel().getSelectedItem();
		if (selectedItem == null)
		{
			this.treeTableView.getSelectionModel().selectFirst();
		}
		if (selectedItem != null)
		{
			boolean prevStateIsSet = selectedItem.getValue().getStyle() != null;
			clearRelation(bean);
//			this.controller.changeStateCount(-1, prevStateIsSet ? selectedItem.getValue().getState() : TreeItemState.UPDATE);
			selectedItem.getValue().addRelation(bean, MarkerStyle.UPDATE);
//			this.controller.changeStateCount(1, selectedItem.getValue().getState());
			this.updateCounters.accept(null);
			this.refreshTable.accept(null);
		}
		this.displayMarkedRows();
		refresh();
	}

	private void clearRelation(ElementWizardBean bean)
	{
		List<TreeItem<XmlTreeItem>> list = new ArrayList<>();
		byPass(this.treeTableView.getRoot(), list, x -> x != null && x.contains(bean));
		list.forEach(item -> item.getValue().clearRelation(bean));
	}

	public void removeBean(ElementWizardBean bean)
	{
		List<TreeItem<XmlTreeItem>> list = new ArrayList<>();
		byPass(this.treeTableView.getRoot(), list, xpathTreeItem -> xpathTreeItem != null && xpathTreeItem.contains(bean));
		Optional.ofNullable(this.removeConsumer).ifPresent(c -> c.accept(
				list.stream()
					.map(TreeItem::getValue)
					.filter(Objects::nonNull)
					.map(XpathItem::getRectangle)
					.collect(Collectors.toList())
		));
		list.forEach(e -> e.getValue().clearRelation(bean));
		refresh();
	}

	public void removeConsumer(Consumer<List<Rectangle>> consumer)
	{
		this.removeConsumer = consumer;
	}

	public void forEach(Consumer<XmlTreeItem> consumer)
	{
		byPass(this.treeTableView.getRoot(), new ArrayList<>(), xpathTreeItem -> {
			consumer.accept(xpathTreeItem);
			return false;
		});
	}
	//endregion

	//region private methods
    private List<TreeItem<XmlTreeItem>> getMarkedRows()
    {
        List<TreeItem<XmlTreeItem>> list = new ArrayList<>();
        byPass(this.treeTableView.getRoot(), list);
        return list;
    }
	
	private List<TreeItem<XmlTreeItem>> getTreeItems()
	{
		return getMarkedRows()
				.stream()
				.filter(treeItem -> treeItem.getValue().isMarkVisible())
				.collect(Collectors.toList());
	}

	private void addWaitingPane()
	{
		this.waitingNode = new BorderPane();
		((BorderPane) this.waitingNode).setCenter(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS));
		((BorderPane) this.waitingNode).setBottom(new Text("Waiting for document..."));
		AnchorPane.setLeftAnchor(this.waitingNode, 50.0);
		AnchorPane.setTopAnchor(this.waitingNode, 50.0);

		this.getChildren().add(this.waitingNode);
	}

	private void expand(TreeItem<XmlTreeItem> item)
	{
		item.setExpanded(true);
		item.getChildren().forEach(this::expand);
	}

    private void displayTree(org.w3c.dom.Node node, TreeItem<XmlTreeItem> parent)
    {
        boolean isDocument = node.getNodeType() == org.w3c.dom.Node.DOCUMENT_NODE;

        TreeItem<XmlTreeItem> treeItem = isDocument ? parent : new TreeItem<>();
        IntStream.range(0, node.getChildNodes().getLength()).mapToObj(node.getChildNodes()::item)
                .filter(item -> item.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
                .forEach(item -> displayTree(item, treeItem));
        if (!isDocument)
        {
            treeItem.setValue(new XmlTreeItem(stringNode(node, XpathUtils.text(node)), node));
            Rectangle rec = (Rectangle) node.getUserData(IRemoteApplication.rectangleName);
            this.map.put(rec, treeItem);
            parent.getChildren().add(treeItem);
        }
    }

    
    private HBox stringNode(org.w3c.dom.Node node, String text)
	{
		HBox box = new HBox();

		box.getChildren().add(createText("<" + node.getNodeName() + " ", CssVariables.XPATH_NODE));
		NamedNodeMap attributes = node.getAttributes();
		Optional.ofNullable(attributes).ifPresent(atrs ->
		{
			IntStream.range(0, atrs.getLength())
					.mapToObj(atrs::item)
					.forEach(item -> box.getChildren().addAll(
							createText(item.getNodeName(), CssVariables.XPATH_ATTRIBUTE_NAME)
							, createText("=", CssVariables.XPATH_TEXT)
							, createText("\"" + item.getNodeValue() + "\" ", CssVariables.XPATH_ATTRIBUTE_VALUE)
					));
		});
		if (Str.IsNullOrEmpty(text))
		{
			box.getChildren().add(createText("/>", CssVariables.XPATH_NODE));
		}
		else
		{
			box.getChildren().addAll(
					createText(">", CssVariables.XPATH_NODE)
					, createText(text, CssVariables.XPATH_TEXT)
					, createText("</" + node.getNodeName() + ">", CssVariables.XPATH_NODE)
			);
		}
		return box;
	}

	private Text createText(String text, String cssClass)
	{
		Text t = new Text(text);
		t.setOnContextMenuRequested(event -> {

			MenuItem item = new MenuItem("Copy " + text);
			item.setOnAction(e -> Common.copyText(text));
			if (t.getParent().getParent() instanceof XmlTreeTableCell)
			{
				XmlTreeTableCell parent = (XmlTreeTableCell) t.getParent().getParent();
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
		t.getStyleClass().add(cssClass);
		return t;
	}

	private void scrollToElement(TreeItem<XmlTreeItem> xpathItemTreeItem)
	{
		TreeItem<XmlTreeItem> parent = xpathItemTreeItem.getParent();
		while (parent != null)
		{
			parent.setExpanded(true);
			parent = parent.getParent();
		}
		MyCustomSkin skin = (MyCustomSkin) treeTableView.getSkin();
		int row = treeTableView.getRow(xpathItemTreeItem);
		if (!skin.isIndexVisible(row))
		{
			skin.show(row);
		}
	}

	private void passTree(Rectangle keyRectangle, Set<Rectangle> set, TreeItem<XmlTreeItem> item)
	{
		XmlTreeItem xpath = item.getValue();
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

	private void addItems(List<TreeItem<XmlTreeItem>> list, TreeItem<XmlTreeItem> current, String what, boolean matchCase, boolean wholeWord)
	{
		Optional.ofNullable(current.getValue()).ifPresent(value -> {
			if (matches(value.getText(), what, matchCase, wholeWord))
			{
				list.add(current);
			}
		});
		Optional.ofNullable(current.getChildren()).ifPresent(childer -> childer.forEach(item -> addItems(list, item, what, matchCase, wholeWord)));
	}

	private boolean matches(String text, String what, boolean matchCase, boolean wholeWord)
	{
		return Arrays.stream(what.split("\\s")).filter(s -> !SearchHelper.matches(text, s, matchCase, wholeWord)).count() == 0;
	}

	private void byPass(TreeItem<XmlTreeItem> treeItem, List<TreeItem<XmlTreeItem>> list)
	{
		XmlTreeItem value = treeItem.getValue();
		if (value != null && value.getStyle() != null)
		{
			list.add(treeItem);
		}
		treeItem.getChildren().forEach(child -> byPass(child, list));
	}

	private void byPass(TreeItem<XmlTreeItem> treeItem, List<TreeItem<XmlTreeItem>> list, Predicate<XmlTreeItem> predicate)
	{
		if (treeItem == null)
		{
			return;
		}
		XmlTreeItem value = treeItem.getValue();
		if (predicate.test(value))
		{
			list.add(treeItem);
		}
		treeItem.getChildren().forEach(child -> byPass(child, list, predicate));
	}

	private void displayMarkedRows()
	{
		if(this.markedRowsConsumer != null)
		{
		}
	}


	private class MyCustomSkin extends TreeTableViewSkin<XmlTreeItem>
	{
		public MyCustomSkin(TreeTableView<XmlTreeItem> treeTableView)
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
		public void resizeColumnToFitContent(TreeTableColumn<XmlTreeItem, ?> tc, int maxRows)
		{
			super.resizeColumnToFitContent(tc, maxRows);
			TreeTableColumn<XmlTreeItem, ?> column = treeTableView.getColumns().get(1);
			double width = column.getWidth();
			column.setPrefWidth(width);
			column.setMaxWidth(width);
			column.setMinWidth(width);
		}
	}

	private class XmlTreeTableCell extends TreeTableCell<XmlTreeItem, XmlTreeItem>
	{
		public XmlTreeTableCell()
		{
			ContextMenu menu = new ContextMenu();
			menu.setAutoHide(true);

			MenuItem copyText = new MenuItem("Copy node");
			copyText.setOnAction(event -> Optional.ofNullable(this.getTreeTableRow().getTreeItem().getValue()).ifPresent(value -> Common.copyText(value.getText())));
			menu.getItems().add(copyText);

			this.setContextMenu(menu);
		}

		@Override
		protected void updateItem(XmlTreeItem item, boolean empty)
		{
			super.updateItem(item, empty);
			if (item != null && !empty)
			{
				setGraphic(item.getBox());
			}
			else
			{
				setGraphic(null);
			}
		}
	}

	private class XmlIconCell extends TreeTableCell<XmlTreeItem, XmlTreeItem>
	{
		private ImageView imageView = new ImageView();

		@Override
		protected void updateItem(XmlTreeItem item, boolean empty)
		{
			super.updateItem(item, empty);
			setTooltip(null);
			if (item != null && !empty)
			{
				MarkerStyle icon = item.getStyle();
				List<XmlTreeItem.BeanWithMark> list = item.getList();
				if (!list.isEmpty())
				{
					String tooltip = list.stream()
							.filter(beanWithMark -> beanWithMark.getBean() != null)
							.map(bean -> bean.getBean().getId() + " ["+bean.getBean().getControlKind().name() + "]")
							.collect(Collectors.joining("\n"));
					this.setTooltip(new Tooltip(tooltip));
				}
				this.imageView.setImage(icon == null ? null : new Image(icon.getIconPath()));
				this.imageView.setOpacity(item.isMarkVisible() ? 1.0 : 0.4);
				setGraphic(this.imageView);
			}
			else
			{
				setGraphic(null);
			}
		}
	}
}