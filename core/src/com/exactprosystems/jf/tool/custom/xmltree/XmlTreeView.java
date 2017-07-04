package com.exactprosystems.jf.tool.custom.xmltree;

import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.dictionary.dialog.ElementWizardBean;
import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;
import com.exactprosystems.jf.tool.wizard.related.XmlItem;
import com.exactprosystems.jf.tool.wizard.related.XmlTreeItem;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventTarget;
import javafx.scene.control.*;
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

	final TreeTableView<XmlTreeItem> treeTableView;

	private int currentIndex = -1;

	private BorderPane waitingNode;

	private Consumer<Void> updateCounters;

	private Consumer<List<Rectangle>> removeConsumer;
	private List<Consumer<XmlTreeItem>> selectionConsumers = new ArrayList<>();

	private Map<Rectangle, TreeItem<XmlTreeItem>> map = new HashMap<>();

	private Map<MarkerStyle, Boolean> stateMap;

	public XmlTreeView(Consumer<Void> updateCounters, Consumer<Void> refreshTable)
	{
		super();

		this.stateMap = Stream.of(MarkerStyle.values()).collect(Collectors.toMap(v -> v, v -> true));
		this.updateCounters = updateCounters;
		this.treeTableView = new TreeTableView<>();
		this.treeTableView.setSkin(new MyCustomSkin(this, this.treeTableView));
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

	public void selectItem(Rectangle rectangle)
	{
		TreeItem<XmlTreeItem> treeItem = this.map.get(rectangle);
		if (treeItem != null)
		{
			this.treeTableView.getSelectionModel().select(treeItem);
			this.scrollToElement(treeItem);
		}
	}

	public void selectAndScroll(XmlTreeItem treeItem)
	{
		this.treeTableView.getSelectionModel().clearSelection();
		
//		this.treeTableView.getSelectionModel().select(treeItem);
//		scrollToElement(treeItem);
	}

	public List<XmlTreeItem> findItem(String what, boolean matchCase, boolean wholeWord)
	{
		ArrayList<XmlTreeItem> res = new ArrayList<>();
//		TreeItem<XmlTreeItem> root = this.treeTableView.getRoot();
//		addItems(res, root, what, matchCase, wholeWord);
		return res;
	}

	public void selectNextMark()
	{
		List<TreeItem<XmlTreeItem>> markedRows = getMarkedTreeItems();
		if (markedRows.isEmpty())
		{
			return;
		}
		this.currentIndex = Math.min(this.currentIndex + 1, markedRows.size() - 1);
		TreeItem<XmlTreeItem> treeItem = markedRows.get(this.currentIndex);
		scrollToElement(treeItem);
		this.treeTableView.getSelectionModel().select(treeItem);
	}

	public void selectPrevMark()
	{
		List<TreeItem<XmlTreeItem>> markedRows = getMarkedTreeItems();
		if (markedRows.isEmpty())
		{
			return;
		}
        this.currentIndex = Math.max(this.currentIndex - 1, 0);
		TreeItem<XmlTreeItem> treeItem = markedRows.get(this.currentIndex);
		scrollToElement(treeItem);
		this.treeTableView.getSelectionModel().select(treeItem);
	}

	public void setMarkersVisible(MarkerStyle state, boolean visible)
	{
		this.stateMap.replace(state, visible);
		this.refresh();
	}

    public void select(org.w3c.dom.Node node)
    {
    }

    public void deselect()
    {
    }
    
    public void setMarker(org.w3c.dom.Node node, MarkerStyle state)
    {
    }
	
    public void removeMarkers(MarkerStyle state)
    {
    }
	
	
	
	
	
	public void refresh()
	{
		Platform.runLater(() -> 
		{
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
		}
		refresh();
	}

	public void removeBean(ElementWizardBean bean)
	{
		List<TreeItem<XmlTreeItem>> list = new ArrayList<>();
		byPass(this.treeTableView.getRoot(), list, xpathTreeItem -> xpathTreeItem != null && xpathTreeItem.contains(bean));
		Optional.ofNullable(this.removeConsumer).ifPresent(c -> c.accept(
				list.stream()
					.map(TreeItem::getValue)
					.filter(Objects::nonNull)
					.map(XmlItem::getRectangle)
					.collect(Collectors.toList())
		));
		list.forEach(e -> e.getValue().clearRelation(bean));
		refresh();
	}

	public void removeConsumer(Consumer<List<Rectangle>> consumer)
	{
		this.removeConsumer = consumer;
	}

	//endregion

	//region private methods
    private TreeItem<XmlTreeItem> findByNode(org.w3c.dom.Node node)
    {
        TreeItem<XmlTreeItem> root = this.treeTableView.getRoot();
        List<TreeItem<XmlTreeItem>> list = new ArrayList<>();
        byPass(root, list, xpathTreeItem -> xpathTreeItem != null && xpathTreeItem.getNode() != null && xpathTreeItem.getNode().equals(node));
        return list.get(0);
    }
    
    private void clearRelation(ElementWizardBean bean)
    {
        List<TreeItem<XmlTreeItem>> list = new ArrayList<>();
        byPass(this.treeTableView.getRoot(), list, x -> x != null && x.contains(bean));
        list.forEach(item -> item.getValue().clearRelation(bean));
    }

	private List<TreeItem<XmlTreeItem>> getMarkedTreeItems()
	{
        List<TreeItem<XmlTreeItem>> list = new ArrayList<>();
        byPass(this.treeTableView.getRoot(), list);

        return list
				.stream()
				.filter(treeItem -> treeItem.getValue().isMarkVisible())
				.collect(Collectors.toList());
	}

	private void addWaitingPane()
	{
		this.waitingNode = new BorderPane();
		this.waitingNode.setCenter(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS));
		this.waitingNode.setBottom(new Text("Waiting for document..."));
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
            treeItem.setValue(new XmlTreeItem(node));
            Rectangle rec = (Rectangle) node.getUserData(IRemoteApplication.rectangleName);
            this.map.put(rec, treeItem);
            parent.getChildren().add(treeItem);
        }
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

//	private void passTree(Rectangle keyRectangle, Set<Rectangle> set, TreeItem<XmlTreeItem> item)
//	{
//		XmlTreeItem xpath = item.getValue();
//		if (xpath != null)
//		{
//			Rectangle rec = xpath.getRectangle();
//
//			if (rec != null && rec.intersects(keyRectangle))
//			{
//				set.add(rec);
//			}
//		}
//		item.getChildren().forEach(child -> passTree(keyRectangle, set, child));
//	}

//	private void addItems(List<XmlTreeItem> list, XmlTreeItem current, String what, boolean matchCase, boolean wholeWord)
//	{
//		Optional.ofNullable(current).ifPresent(value -> {
//			if (matches(value.getText(), what, matchCase, wholeWord))
//			{
//				list.add(current);
//			}
//		});
//		Optional.ofNullable(current.getChildren()).ifPresent(childer -> childer.forEach(item -> addItems(list, item, what, matchCase, wholeWord)));
//	}

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
}