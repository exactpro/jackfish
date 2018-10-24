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

package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemState;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class IconCell extends TreeTableCell<MatrixItem, MatrixItemState>
{
	@Override
	public void updateItem(MatrixItemState state, boolean empty)
	{
		super.updateItem(state, empty);
		setGraphic(null);
		if (state != null)
		{
			boolean isTracing = ((MatrixTreeView) getTreeTableRow().getTreeTableView()).isTracing();
			TreeItem<MatrixItem> treeItem = getTreeTableRow().getTreeItem();
			switch (state)
			{
				case None:			setGraphic(null);return;
				case BreakPoint:	setGraphic(new ImageView(new Image(CssVariables.Icons.BREAK_POINT_ICON))); return;
				case Executing:
					if (isTracing)
					{
						setGraphic(new ImageView(new Image(CssVariables.Icons.EXECUTING_ITEM_ICON)));
					}
					return;

				case ExecutingWithBreakPoint:
					setGraphic(new ImageView(new Image(isTracing ? CssVariables.Icons.EXECUTING_BREAK_POINT_ICON : CssVariables.Icons.BREAK_POINT_ICON)));
					return;

				case ExecutingParent:
					if (isTracing && treeItem != null && !treeItem.isExpanded())
					{
						setGraphic(new ImageView(new Image(CssVariables.Icons.EXECUTING_ITEM_ICON)));
					}
			}
		}
	}
}