package com.exactprosystems.jf.tool.custom.xpath;

import com.exactprosystems.jf.api.app.IRemoteApplication;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.w3c.dom.Node;

import java.awt.*;
import java.util.stream.Collectors;

public class XpathItem
{
	private HBox box;
	private Node node;
	private Rectangle rectangle;

	public XpathItem(HBox box, Node node)
	{
		this.box = box;
		this.box.setAlignment(Pos.CENTER_LEFT);
		this.node = node;
		this.rectangle = (Rectangle)node.getUserData(IRemoteApplication.rectangleName);
	}

	public HBox getBox()
	{
		return this.box;
	}

	public Node getNode()
	{
		return this.node;
	}
	
	public Rectangle getRectangle()
	{
		return this.rectangle;
	}

	public String getText()
	{
		return this.box.getChildren().stream()
				.filter(node -> node instanceof Text)
				.map(text -> ((Text) text).getText())
				.collect(Collectors.joining());
//		StringBuilder builder = new StringBuilder();
//		this.box.getChildren().stream().filter(node -> node instanceof Text).forEach(text -> builder.append(((Text) text).getText()));
//		return builder.toString();
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
