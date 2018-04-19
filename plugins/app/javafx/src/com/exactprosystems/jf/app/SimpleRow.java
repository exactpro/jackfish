/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.util.ArrayList;
import java.util.List;

class SimpleRow extends Parent
{
	List<Node> children = new ArrayList<>();

	SimpleRow()
	{

	}

	void add(Node node)
	{
		this.children.add(node);
	}

	@Override
	public ObservableList<Node> getChildrenUnmodifiable()
	{
		return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(this.children));
	}

	@Override
	protected ObservableList<Node> getChildren()
	{
		return FXCollections.observableArrayList(this.children);
	}
}
