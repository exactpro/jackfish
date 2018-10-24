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
package com.exactprosystems.jf.tool.custom;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;

import java.util.function.Supplier;

public class CancelButton<T> extends Button
{
	private Task<T> task;
	private Service<T> service;
	boolean isRunning = false;

	public CancelButton(Supplier<T> function)
	{
		super();
		this.task = new Task<T>()
		{
			@Override
			protected T call() throws Exception
			{
				return function.get();
			}
		};

		this.service = new Service<T>()
		{
			@Override
			protected Task<T> createTask()
			{
				return task;
			}
		};

		this.setOnAction(event -> {
			if (this.isRunning)
			{
				stop();
			}
			else
			{
				start();
			}
		});
		this.onActionProperty().addListener((observable, oldValue, newValue) -> {
			this.setOnAction(oldValue);
		});
	}

	private void start()
	{
		this.setGraphic(createIndicator());
		service.restart();
		isRunning = true;
	}

	private void stop()
	{
		this.setGraphic(null);
		service.cancel();
		isRunning = false;
	}

	private ProgressIndicator createIndicator()
	{
		ProgressIndicator indicator = new ProgressIndicator();
		indicator.setPrefHeight(this.getHeight());
		indicator.setMinHeight(this.getHeight());
		indicator.setMaxHeight(this.getHeight());
		return indicator;
	}
}
