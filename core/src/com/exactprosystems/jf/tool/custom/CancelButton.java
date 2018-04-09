/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
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
