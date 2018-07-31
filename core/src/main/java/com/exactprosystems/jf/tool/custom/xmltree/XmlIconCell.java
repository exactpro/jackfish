/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.custom.xmltree;

import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;
import com.exactprosystems.jf.tool.wizard.related.XmlItem;

import javafx.scene.control.TreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class XmlIconCell extends TreeTableCell<XmlItem, XmlItem>
{
	private ImageView imageView = new ImageView();

	@Override
	protected void updateItem(XmlItem item, boolean empty)
	{
		super.updateItem(item, empty);
		if (item != null && !empty)
		{
			MarkerStyle icon = item.getStyle();
			this.imageView.setImage(icon == null ? null : new Image(icon.getIconPath()));
			this.imageView.setOpacity(item.isVisible() ? 1.0 : 0.4);
			setGraphic(this.imageView);
		}
		else
		{
			setGraphic(null);
		}
	}
}