package com.exactprosystems.jf.tool.custom.xpath;

import javafx.scene.layout.HBox;
import org.w3c.dom.Node;

public class XpathTreeItem extends XpathItem
{
	boolean isIcon;

	public XpathTreeItem(HBox box, Node node)
	{
		super(box, node);
	}

	public boolean isIcon()
	{
		return isIcon;
	}

	public void setIcon(boolean icon)
	{
		isIcon = icon;
	}
}
