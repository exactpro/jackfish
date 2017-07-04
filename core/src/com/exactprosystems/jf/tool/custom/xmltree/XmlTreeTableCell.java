package com.exactprosystems.jf.tool.custom.xmltree;

import java.util.Optional;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.wizard.related.XmlTreeItem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableCell;

public class XmlTreeTableCell extends TreeTableCell<XmlTreeItem, XmlTreeItem>
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