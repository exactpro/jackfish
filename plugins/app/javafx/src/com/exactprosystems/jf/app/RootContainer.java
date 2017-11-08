package com.exactprosystems.jf.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RootContainer extends Parent
{
	private List<Node> targets = new ArrayList<>();

	public void addTarget(Node target)
	{
		this.targets.add(target);
	}

	@Override
	protected ObservableList<Node> getChildren()
	{
		return FXCollections.observableArrayList(this.targets);
	}

	@Override
	public ObservableList<Node> getChildrenUnmodifiable()
	{
		return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(this.targets));
	}

	@Override
	public String toString()
	{
		return String.format("RootContainer, targets size : %s , targets : [%s]", this.targets.size(), this.targets.stream().map(MatcherFx::targetToString).collect(Collectors.joining(",")));
	}
}
