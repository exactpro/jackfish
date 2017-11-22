////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RootContainer extends Parent
{
	private List<Window> windows = new ArrayList<>();

	public void addWindow(Window window)
	{
		this.windows.add(window);
	}

	@Override
	protected ObservableList<Node> getChildren()
	{
		return FXCollections.observableArrayList(this.windows.stream().map(w -> w.getScene().getRoot()).collect(Collectors.toList()));
	}

	@Override
	public ObservableList<Node> getChildrenUnmodifiable()
	{
		return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(this.windows.stream().map(w -> w.getScene().getRoot()).collect(Collectors.toList())));
	}

	public List<Window> getWindows()
	{
		return windows;
	}

	@Override
	public String toString()
	{
		return String.format("RootContainer, targets size : %s , targets : [%s]", this.windows.size(), this.windows.stream().map(MatcherFx::targetToString).collect(Collectors.joining(",")));
	}
}
