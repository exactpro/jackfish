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

package com.exactprosystems.jf.tool.custom.skin;

import com.sun.javafx.scene.control.skin.TreeTableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;

public class CustomTreeTableViewSkin<T> extends TreeTableViewSkin<T>
{
	public CustomTreeTableViewSkin(TreeTableView<T> treeTableView)
	{
		super(treeTableView);
	}

	public void scrollTo(int index)
	{
		if (!this.isIndexVisible(index))
		{
			this.show(index);
		}
	}

	@Override
	public void resizeColumnToFitContent(TreeTableColumn<T, ?> tc, int maxRows)
	{
		super.resizeColumnToFitContent(tc, maxRows);
	}

	public ScrollBar getVSB()
	{
		return (ScrollBar) super.queryAccessibleAttribute(AccessibleAttribute.VERTICAL_SCROLLBAR);
	}

	@Override
	protected VirtualFlow<TreeTableRow<T>> createVirtualFlow()
	{
		return new CustomVirtualFlow<>();
	}

	//region private methods
	private void show(int index)
	{
		super.flow.show(index);
	}

	private boolean isIndexVisible(int index)
	{
		return super.flow.getFirstVisibleCell() != null &&
				super.flow.getLastVisibleCell() != null &&
				super.flow.getFirstVisibleCell().getIndex() <= index - 1 &&
				super.flow.getLastVisibleCell().getIndex() >= index + 1;
	}
	//endregion
}
