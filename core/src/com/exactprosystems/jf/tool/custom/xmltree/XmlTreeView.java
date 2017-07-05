////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.xmltree;

import com.exactprosystems.jf.documents.matrix.parser.SearchHelper;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;
import com.exactprosystems.jf.tool.wizard.related.XmlTreeItem;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventTarget;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import org.w3c.dom.Document;
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

    private TreeTableView<XmlTreeItem> treeTableView;
    private int                        currentIndex     = -1;
    private BorderPane                 waitingNode;
    private OnSelectionChangeListener  onSelectionChanged;
    private OnMarkerChangeListener     onMarkerChanged;
    private Map<MarkerStyle, Boolean>  stateMap;

	public XmlTreeView()
	{
		super();

		this.stateMap = Stream.of(MarkerStyle.values()).collect(Collectors.toMap(v -> v, v -> true));
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
		this.treeTableView.setOnMouseClicked(e -> 
		{
			EventTarget target = e.getTarget();
			if (target instanceof XmlIconCell)
			{
				XmlIconCell iconCell = (XmlIconCell) target;
				TreeItem<XmlTreeItem> treeItem = iconCell.getTreeTableRow().getTreeItem();
				if (treeItem != null)
				{
					XmlTreeItem xmlTreeItem = treeItem.getValue();
					MarkerStyle oldValue = xmlTreeItem.getStyle();
					MarkerStyle newValue = xmlTreeItem.changeState();
					if (this.onMarkerChanged != null)
					{
					    this.onMarkerChanged.changed(xmlTreeItem.getNode(), oldValue, newValue);
					}
					refresh();
				}
			}

		});

		this.treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
		    if (this.onSelectionChanged != null)
		    {
		        org.w3c.dom.Node oldNode = oldValue == null ? null : oldValue.getValue().getNode(); 
                org.w3c.dom.Node newNode = newValue == null ? null : newValue.getValue().getNode(); 
		        this.onSelectionChanged.changed(oldNode, newNode);
		    }
		});
	
        TreeTableColumn<XmlTreeItem, ?> column = this.treeTableView.getColumns().get(1);
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
		((MyCustomSkin) this.treeTableView.getSkin()).resizeColumnToFitContent(this.treeTableView.getColumns().get(1), -1);
	}


    public void select(org.w3c.dom.Node node)
    {
        TreeItem<XmlTreeItem> treeItem = findFirst(i -> i != null && i.getNode().equals(node));
        if (treeItem != null)
        {
            this.treeTableView.getSelectionModel().select(treeItem);
            this.scrollToElement(treeItem);
        }
    }

    public void deselect()
    {
        this.treeTableView.getSelectionModel().clearSelection();
    }
    
    public void setMarker(org.w3c.dom.Node node, MarkerStyle style)
    {
        TreeItem<XmlTreeItem> treeItem = findFirst(i -> i != null && i.getNode().equals(node));
        if (treeItem != null)
        {
            treeItem.getValue().addRelation(null, style);
            treeItem.getValue().setMarkIsVisible(true);
        }
    }
    
    public void removeMarkers(MarkerStyle state)
    {
        passTree(this.treeTableView.getRoot(), i -> i.clearRelation(null));
    }
	
	public void selectItem(Rectangle rectangle)
	{
	    TreeItem<XmlTreeItem> treeItem  = findFirst(i -> i != null && i.getRectangle() != null && i.getRectangle().equals(rectangle));
        if (treeItem != null)
        {
            this.treeTableView.getSelectionModel().select(treeItem);
            this.scrollToElement(treeItem);
        }
	}

	public List<org.w3c.dom.Node> findItem(String what, boolean matchCase, boolean wholeWord)
	{
        List<org.w3c.dom.Node> list = findAll(i -> i != null &&  matches(i.getText(), what, matchCase, wholeWord))
                .stream().map(i -> i.getValue().getNode()).collect(Collectors.toList());
	    return list;
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

	public void refresh()
	{
		Platform.runLater(() -> 
		{
			this.treeTableView.getColumns().get(0).setVisible(false);
			this.treeTableView.getColumns().get(0).setVisible(true);
		});
	}
	//endregion

	//region private methods
	private List<TreeItem<XmlTreeItem>> getMarkedTreeItems()
	{
        return findAll(treeItem -> treeItem.isMarkVisible());
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

	private void expandTree(TreeItem<XmlTreeItem> item)
	{
		item.setExpanded(true);
		item.getChildren().forEach(this::expandTree);
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

	private boolean matches(String text, String what, boolean matchCase, boolean wholeWord)
	{
		return Arrays.stream(what.split("\\s")).filter(s -> !SearchHelper.matches(text, s, matchCase, wholeWord)).count() == 0;
	}

    private TreeItem<XmlTreeItem>  findFirst(Predicate<XmlTreeItem> predicate)
    {
        List<TreeItem<XmlTreeItem>> list = findAll(predicate);
        if (!list.isEmpty())
        {
            return list.get(0);
        }
        return null;
    }

    private List<TreeItem<XmlTreeItem>>  findAll(Predicate<XmlTreeItem> predicate)
    {
        return findAll(this.treeTableView.getRoot(), predicate);
    }

    private List<TreeItem<XmlTreeItem>>  findAll(TreeItem<XmlTreeItem> treeItem, Predicate<XmlTreeItem> predicate)
    {
        List<TreeItem<XmlTreeItem>> list = new ArrayList<>();
        XmlTreeItem value = treeItem.getValue();
        if (predicate.test(value))
        {
            list.add(treeItem);
        }
        treeItem.getChildren().forEach(child -> list.addAll(findAll(child, predicate)));
        return list;
    }
    
    private void  passTree(TreeItem<XmlTreeItem> treeItem, Consumer<XmlTreeItem> consumer)
    {
        XmlTreeItem value = treeItem.getValue();
        consumer.accept(value);
        treeItem.getChildren().stream().filter(Objects::nonNull).forEach(child -> passTree(child, consumer));
    }
}