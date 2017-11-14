package com.exactprosystems.jf.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RootContainer extends Parent
{
	private List<Stage> stages = new ArrayList<>();

	public void addStage(Stage stage)
	{
		this.stages.add(stage);
	}

	@Override
	protected ObservableList<Node> getChildren()
	{
		return FXCollections.observableArrayList(this.stages.stream().map(s -> s.getScene().getRoot()).collect(Collectors.toList()));
	}

	@Override
	public ObservableList<Node> getChildrenUnmodifiable()
	{
		return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(this.stages.stream().map(s -> s.getScene().getRoot()).collect(Collectors.toList())));
	}

	public List<Stage> getStages()
	{
		return this.stages;
	}

	@Override
	public String toString()
	{
		return String.format("RootContainer, targets size : %s , targets : [%s]", this.stages.size(), this.stages.stream().map(MatcherFx::targetToString).collect(Collectors.joining(",")));
	}
}
