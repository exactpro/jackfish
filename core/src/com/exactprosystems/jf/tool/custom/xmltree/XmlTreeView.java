////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.xmltree;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.skin.CustomTreeTableViewSkin;
import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;
import com.exactprosystems.jf.tool.wizard.related.XmlItem;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventTarget;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.awt.Rectangle;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class XmlTreeView extends AnchorPane
{
	private TreeTableView<XmlItem> treeTableView;
	private int currentIndex = -1;
	private BorderPane                waitingNode;
	private OnSelectionChangeListener onSelectionChanged;
	private OnMarkerChangeListener    onMarkerChanged;
	private Map<MarkerStyle, Boolean> stateMap;

	public XmlTreeView()
	{
		super();

		this.stateMap = Stream.of(MarkerStyle.values()).collect(Collectors.toMap(Function.identity(), v -> true));
		this.treeTableView = new TreeTableView<>();
		this.treeTableView.setSkin(new CustomTreeTableViewSkin<>(this.treeTableView));
		this.treeTableView.getStyleClass().add(CssVariables.EMPTY_HEADER_COLUMN);

		AnchorPane.setTopAnchor(this.treeTableView, 0.0);
		AnchorPane.setLeftAnchor(this.treeTableView, 0.0);
		AnchorPane.setRightAnchor(this.treeTableView, 0.0);
		AnchorPane.setBottomAnchor(this.treeTableView, 0.0);

		this.getChildren().add(this.treeTableView);

		TreeTableColumn<XmlItem, XmlItem> c0 = new TreeTableColumn<>();
		int value = 30;
		c0.setPrefWidth(value);
		c0.setMaxWidth(value);
		c0.setMinWidth(value);
		c0.setCellFactory(p -> new XmlIconCell());
		c0.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue().getValue()));
		this.treeTableView.getColumns().add(c0);

		TreeTableColumn<XmlItem, XmlItem> c1 = new TreeTableColumn<>();
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
				TreeItem<XmlItem> treeItem = iconCell.getTreeTableRow().getTreeItem();
				Optional.ofNullable(treeItem).ifPresent(this::changeMarkerState);
			}
		});

		this.treeTableView.setOnKeyPressed(e ->
		{
			if (e.getCode() == KeyCode.SPACE)
			{
				TreeItem<XmlItem> selectedItem = this.treeTableView.getSelectionModel().getSelectedItem();
				Optional.ofNullable(selectedItem).ifPresent(this::changeMarkerState);
			}
		});

		this.treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (this.onSelectionChanged != null)
			{
				// not selected
				XmlItem oldItem = oldValue == null ? null : oldValue.getValue();
				MarkerStyle oldMarker = selectionStyle(oldItem);

				// selected
				XmlItem newItem = newValue == null ? null : newValue.getValue();
				MarkerStyle newMarker = selectionStyle(newItem);

				this.onSelectionChanged.changed(oldItem, oldMarker, newItem, newMarker);
			}
		});

		this.treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			if (newValue != null)
			{
				scrollTo(newValue);
			}
		});

		TreeTableColumn<XmlItem, ?> column = this.treeTableView.getColumns().get(1);
		double width = column.getWidth();
		column.setPrefWidth(width);
		column.setMaxWidth(width);
		column.setMinWidth(width);
	}

	//region set listeners
	public void setOnSelectionChanged(OnSelectionChangeListener listener)
	{
		this.onSelectionChanged = listener;
	}

	public void setOnMarkerChanged(OnMarkerChangeListener listener)
	{
		this.onMarkerChanged = listener;
	}
	//endregion

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
		expandTree(this.treeTableView.getRoot());
		((CustomTreeTableViewSkin) this.treeTableView.getSkin()).resizeColumnToFitContent(this.treeTableView.getColumns().get(1), -1);
	}

	public void select(org.w3c.dom.Node node)
	{
		TreeItem<XmlItem> treeItem = findFirst(i -> i != null && i.getNode().equals(node));
		if (treeItem != null)
		{
			this.treeTableView.getSelectionModel().select(treeItem);
			this.expandAndScroll(treeItem);
		}
	}

	public void deselect()
	{
		this.treeTableView.getSelectionModel().clearSelection();
	}

	public void setMarker(org.w3c.dom.Node node, MarkerStyle style)
	{
		TreeItem<XmlItem> treeItem = findFirst(i -> i != null && i.getNode().equals(node));
		if (treeItem != null)
		{
			Optional.ofNullable(this.onMarkerChanged).ifPresent(c -> c.changed(treeItem.getValue(), treeItem.getValue().getStyle(), style, false));
			treeItem.getValue().setStyle(style);
			this.refresh();
		}
	}

	public void removeMarkers(MarkerStyle state)
	{
		passTree(this.treeTableView.getRoot(), i -> i.setStyle(null));
	}

	public void selectItem(Rectangle rectangle)
	{
		TreeItem<XmlItem> treeItem = findFirst(i -> i != null && i.getRectangle() != null && i.getRectangle().equals(rectangle));
		if (treeItem != null)
		{
			this.treeTableView.getSelectionModel().select(treeItem);
			this.expandAndScroll(treeItem);
		}
	}

	public List<org.w3c.dom.Node> findItem(String what, boolean matchCase, boolean wholeWord)
	{
		return findAll(i -> i != null && matches(i.findText(), what, matchCase, wholeWord))
				.stream()
				.map(i -> i.getValue().getNode())
				.collect(Collectors.toList());
	}

	public void selectNextMark()
	{
		List<TreeItem<XmlItem>> markedRows = getMarkedTreeItems();
		if (markedRows.isEmpty())
		{
			return;
		}
		this.currentIndex = Math.min(this.currentIndex + 1, markedRows.size() - 1);
		TreeItem<XmlItem> treeItem = markedRows.get(this.currentIndex);
		expandAndScroll(treeItem);
		this.treeTableView.getSelectionModel().select(treeItem);
	}

	public void selectPrevMark()
	{
		List<TreeItem<XmlItem>> markedRows = getMarkedTreeItems();
		if (markedRows.isEmpty())
		{
			return;
		}
		this.currentIndex = Math.max(this.currentIndex - 1, 0);
		TreeItem<XmlItem> treeItem = markedRows.get(this.currentIndex);
		expandAndScroll(treeItem);
		this.treeTableView.getSelectionModel().select(treeItem);
	}

	public void setMarkersVisible(MarkerStyle state, boolean visible)
	{
		findAll(treeItem -> {
			if (treeItem != null && treeItem.getStyle() == state)
			{
				treeItem.setVisible(visible);
			}
			return true;
		});
		this.stateMap.replace(state, visible);
		this.refresh();
	}

	public void refresh()
	{
		Common.runLater(() -> {
			this.treeTableView.getColumns().get(1).setVisible(false);
			this.treeTableView.getColumns().get(1).setVisible(true);
		});
	}

	public void highlightNodes(List<Node> nodes)
	{
		List<TreeItem<XmlItem>> list = findAll(xmlItem -> {
			if (xmlItem == null)
			{
				return false;
			}
			xmlItem.highlight(false);
			return nodes.stream().anyMatch(node -> xmlItem.getNode() == node);
		});

		list.stream().filter(Objects::nonNull).map(TreeItem::getValue).filter(Objects::nonNull).forEach(item -> item.highlight(true));
		if (!list.isEmpty())
		{
			TreeItem<XmlItem> firstFound = list.get(0);
			this.treeTableView.getSelectionModel().clearSelection();
			this.treeTableView.getSelectionModel().select(firstFound);
			this.expandAndScroll(firstFound);
		}
		this.refresh();
	}

	public int getMarkedRowCount()
	{
		return getMarkedTreeItems().size();
	}

	public List<Pair<Node, MarkerStyle>> getMarkedItems()
	{
		List<TreeItem<XmlItem>> list = getMarkedTreeItems();
		return list.stream()
				.filter(Objects::nonNull)
				.map(TreeItem::getValue)
				.filter(Objects::nonNull)
				.map(xmlItem -> new Pair<>(xmlItem.getNode(), xmlItem.getStyle()))
				.collect(Collectors.toList());
	}
	//endregion

	//region private methods
	private void changeMarkerState(TreeItem<XmlItem> treeItem)
	{
		XmlItem item = treeItem.getValue();
		MarkerStyle oldValue = item.getStyle();
		MarkerStyle newValue = item.changeStyle();
		item.setVisible(newValue == null ? true : this.stateMap.get(newValue));
		if (this.onMarkerChanged != null)
		{
			TreeItem<XmlItem> selectedItem = this.treeTableView.getSelectionModel().selectedItemProperty().get();
			boolean selected = selectedItem != null && selectedItem.getValue() == item;
			oldValue = oldValue == null && selected ? MarkerStyle.SELECT : oldValue;
			newValue = newValue == null && selected ? MarkerStyle.SELECT : newValue;
			this.onMarkerChanged.changed(item, oldValue, newValue, selected);
		}
		refresh();
	}

	private MarkerStyle selectionStyle(XmlItem item)
	{
		if (item == null)
		{
			return null;
		}
		MarkerStyle style = item.getStyle();
		if (style == null)
		{
			style = MarkerStyle.SELECT;
		}
		return style;
	}

	private List<TreeItem<XmlItem>> getMarkedTreeItems()
	{
		return findAll(treeItem -> treeItem != null && treeItem.getStyle() != null);
	}

	private void addWaitingPane()
	{
		this.waitingNode = new BorderPane();
		this.waitingNode.setCenter(new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS));
		this.waitingNode.setBottom(new Text(R.XML_TREE_VIEW_WAITING.get()));
		AnchorPane.setLeftAnchor(this.waitingNode, 50.0);
		AnchorPane.setTopAnchor(this.waitingNode, 50.0);

		this.getChildren().add(this.waitingNode);
	}

	private void expandTree(TreeItem<XmlItem> item)
	{
		item.setExpanded(true);
		item.getChildren().forEach(this::expandTree);
	}

	private void displayTree(org.w3c.dom.Node node, TreeItem<XmlItem> parent)
	{
		boolean isDocument = node.getNodeType() == org.w3c.dom.Node.DOCUMENT_NODE;

		TreeItem<XmlItem> treeItem = isDocument ? parent : new TreeItem<>();
		IntStream.range(0, node.getChildNodes().getLength()).mapToObj(node.getChildNodes()::item)
				.filter(item -> item.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
				.forEach(item -> displayTree(item, treeItem));
		if (!isDocument)
		{
			treeItem.setValue(new XmlItem(node));
			parent.getChildren().add(treeItem);
		}
	}

	private void expandAndScroll(TreeItem<XmlItem> xpathItemTreeItem)
	{
		TreeItem<XmlItem> parent = xpathItemTreeItem.getParent();
		while (parent != null)
		{
			parent.setExpanded(true);
			parent = parent.getParent();
		}
		scrollTo(xpathItemTreeItem);
	}

	private void scrollTo(TreeItem<XmlItem> xpathItemTreeItem)
	{
		CustomTreeTableViewSkin skin = (CustomTreeTableViewSkin) this.treeTableView.getSkin();
		int row = this.treeTableView.getRow(xpathItemTreeItem);
		skin.scrollTo(row);
	}

	private boolean matches(String text, String what, boolean matchCase, boolean wholeWord)
	{
		return Arrays.stream(what.split("\\s")).filter(s -> !SearchHelper.matches(text, s, matchCase, wholeWord)).count() == 0;
	}

	private TreeItem<XmlItem> findFirst(Predicate<XmlItem> predicate)
	{
		List<TreeItem<XmlItem>> list = findAll(predicate);
		return list.isEmpty() ? null : list.get(0);
	}

	private List<TreeItem<XmlItem>> findAll(Predicate<XmlItem> predicate)
	{
		return findAll(this.treeTableView.getRoot(), predicate);
	}

	private List<TreeItem<XmlItem>> findAll(TreeItem<XmlItem> treeItem, Predicate<XmlItem> predicate)
	{
		List<TreeItem<XmlItem>> list = new ArrayList<>();
		XmlItem value = treeItem.getValue();
		if (predicate.test(value))
		{
			list.add(treeItem);
		}
		treeItem.getChildren().forEach(child -> list.addAll(findAll(child, predicate)));
		return list;
	}

	private void passTree(TreeItem<XmlItem> treeItem, Consumer<XmlItem> consumer)
	{
		XmlItem value = treeItem.getValue();
		consumer.accept(value);
		treeItem.getChildren().stream()
				.filter(Objects::nonNull)
				.forEach(child -> passTree(child, consumer));
	}

	//endregion
}