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
package com.exactprosystems.jf.tool.newconfig.nodes;

import com.exactprosystems.jf.tool.CssVariables;

import javafx.scene.image.Image;

import java.io.File;
import java.util.Optional;

public class FolderTreeNode extends FileTreeNode
{
	public FolderTreeNode(File file)
	{
		super(file);
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.of(new Image(super.isExpanded() ? CssVariables.Icons.FOLDER_ICON_OPENED : CssVariables.Icons.FOLDER_ICON));
	}
}
