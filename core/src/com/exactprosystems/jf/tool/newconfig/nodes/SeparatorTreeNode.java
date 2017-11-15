////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;

import java.util.Optional;

public class SeparatorTreeNode extends TreeNode
{
	@Override
	public Node getView()
	{
		return new Separator();
	}

	@Override
	public Optional<Image> icon()
	{
		return Optional.empty();
	}
}
