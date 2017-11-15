////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.xmltree;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.utils.XpathUtils;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.wizard.related.XmlItem;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.w3c.dom.NamedNodeMap;

import java.awt.MouseInfo;
import java.util.Optional;
import java.util.stream.IntStream;

public class XmlTreeTableCell extends TreeTableCell<XmlItem, XmlItem>
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
	protected void updateItem(XmlItem item, boolean empty)
	{
		super.updateItem(item, empty);
		if (item != null && !empty)
		{
			org.w3c.dom.Node node = item.getNode();
			HBox box = stringNode(node, XpathUtils.text(node));
			box.setAlignment(Pos.CENTER_LEFT);
			if (item.isHighlight())
			{
				box.getStyleClass().addAll(CssVariables.XPATH_FIND_TREE_ITEM);
			}
			setGraphic(box);
		}
		else
		{
			setGraphic(null);
		}
	}

	private static HBox stringNode(org.w3c.dom.Node node, String text)
	{
		HBox box = new HBox();

		box.getChildren().add(createText("<" + node.getNodeName() + " ", CssVariables.XPATH_NODE));
		NamedNodeMap attributes = node.getAttributes();
		Optional.ofNullable(attributes)
				.ifPresent(atrs -> IntStream.range(0, atrs.getLength())
						.mapToObj(atrs::item)
						.forEach(item ->
								box.getChildren().addAll(
										createText(item.getNodeName(), CssVariables.XPATH_ATTRIBUTE_NAME),
										createText("=", CssVariables.XPATH_TEXT),
										createText("\"" + item.getNodeValue() + "\" ", CssVariables.XPATH_ATTRIBUTE_VALUE))
						)
				);
		if (Str.IsNullOrEmpty(text))
		{
			box.getChildren().add(createText("/>", CssVariables.XPATH_NODE));
		}
		else
		{
			box.getChildren().addAll(
					createText(">", CssVariables.XPATH_NODE),
					createText(text, CssVariables.XPATH_TEXT),
					createText("</" + node.getNodeName() + ">", CssVariables.XPATH_NODE)
			);
		}
		return box;
	}

	private static Text createText(String text, String cssClass)
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
}