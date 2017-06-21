package com.exactprosystems.jf.tool.wizard.related;

import com.exactprosystems.jf.tool.Common;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;

import java.util.Optional;

public class XpathCell extends TreeCell<XpathItem>
{
	public XpathCell()
	{
		ContextMenu menu = new ContextMenu();
		menu.setAutoHide(true);

		MenuItem copyText = new MenuItem("Copy node");
		copyText.setOnAction(event -> Optional.ofNullable(getTreeItem().getValue()).ifPresent(value -> Common.copyText(value.getText())));
		menu.getItems().add(copyText);

		this.setContextMenu(menu);
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
