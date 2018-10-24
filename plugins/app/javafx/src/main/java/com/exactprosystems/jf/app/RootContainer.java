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
