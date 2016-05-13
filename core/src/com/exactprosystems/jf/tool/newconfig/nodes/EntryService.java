////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig.nodes;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Node;

import java.util.List;

class EntryService extends Service<List<Node>>
{
	private final EntryServiceInterface task;

	public EntryService(EntryServiceInterface task)
	{
		this.task = task;
	}

	@Override
	protected Task<List<Node>> createTask()
	{
		return new Task<List<Node>>()
		{
			@Override
			protected List<Node> call() throws Exception
			{
				return task.apply();
			}
		};
	}

	interface EntryServiceInterface
	{
		List<Node> apply() throws Exception;
	}
}
