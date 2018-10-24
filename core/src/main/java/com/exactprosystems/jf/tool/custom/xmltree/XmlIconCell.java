/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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