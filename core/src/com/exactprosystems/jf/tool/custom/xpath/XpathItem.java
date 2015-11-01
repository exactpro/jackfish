package com.exactprosystems.jf.tool.custom.xpath;

import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.w3c.dom.Node;

public class XpathItem
{
	private HBox box;
	private Node node;

	public XpathItem(HBox box, Node node)
	{
		this.box = box;
		this.node = node;
	}

	public HBox getBox()
	{
		return box;
	}

	public Node getNode()
	{
		return node;
	}

	public String getText()
	{
		StringBuilder builder = new StringBuilder();
		this.box.getChildren().stream().filter(node -> node instanceof Text).forEach(text -> builder.append(((Text) text).getText()));
		return builder.toString();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		XpathItem xpathItem = (XpathItem) o;

		if (box != null ? !box.equals(xpathItem.box) : xpathItem.box != null)
			return false;
		return !(node != null ? !node.equals(xpathItem.node) : xpathItem.node != null);

	}

	@Override
	public int hashCode()
	{
		int result = box != null ? box.hashCode() : 0;
		result = 31 * result + (node != null ? node.hashCode() : 0);
		return result;
	}
}
