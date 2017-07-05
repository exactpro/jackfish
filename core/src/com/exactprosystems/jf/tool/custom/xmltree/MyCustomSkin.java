package com.exactprosystems.jf.tool.custom.xmltree;

import com.exactprosystems.jf.tool.wizard.related.XmlTreeItem;
import com.sun.javafx.scene.control.skin.TreeTableViewSkin;

import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

class MyCustomSkin extends TreeTableViewSkin<XmlTreeItem>
{
	/**
     * 
     */
    private final XmlTreeView xmlTreeView;

    public MyCustomSkin(XmlTreeView xmlTreeView, TreeTableView<XmlTreeItem> treeTableView)
	{
		super(treeTableView);
        this.xmlTreeView = xmlTreeView;
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
		
//		TreeTableColumn<XmlTreeItem, ?> column = this.xmlTreeView.treeTableView.getColumns().get(1);
//		double width = column.getWidth();
//		column.setPrefWidth(width);
//		column.setMaxWidth(width);
//		column.setMinWidth(width);
	}
}