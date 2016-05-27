////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix.schedule;

import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.listeners.MatrixListener;
import com.exactprosystems.jf.documents.matrix.parser.listeners.RunnerListener;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;

import javafx.stage.Window;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RunnerScheduler implements RunnerListener
{
	private static final Logger logger = Logger.getLogger(RunnerScheduler.class);
	private ScheduleController controller;
	private ConcurrentHashMap<MatrixRunner, Boolean> map;
	private Configuration configuration;

	public RunnerScheduler() throws Exception
	{
		this.map = new ConcurrentHashMap<>();
		this.controller = Common.loadController(RunnerScheduler.class.getResource("Schedule.fxml"));
		this.controller.init(this);
	}

	public void show(Window window)
	{
		if (!this.controller.isShowing())
		{
			this.controller.show(window);
		}
	}

	@Override
	public void subscribe(MatrixRunner runner)
	{
		if (this.map.containsKey(runner))
		{
			return;
		}
		Boolean sub = this.map.put(runner, Boolean.TRUE);
		sub = sub == null ? true : sub;
		logger.trace(String.format("MatrixRunner %s subscribe %s", runner.toString(), (sub ? "" : "un") + "successful"));
		this.controller.displayRunner(runner);
	}

	@Override
	public void unsubscribe(MatrixRunner runner)
	{
		Boolean remove = this.map.remove(runner);
		remove = remove == null ? true : remove;
		logger.trace(String.format("MatrixRunner %s subscribe %s", runner.toString(), (remove ? "" : "un") + "successful"));
		this.controller.removeRunner(runner);
	}

	@Override
	public void stateChange(MatrixRunner matrixRunner, MatrixRunner.State state, int done, int total)
	{
		this.controller.displayState(matrixRunner, state, done, total);
	}

	@Override
	public void setConfiguration(Configuration configuration)
	{
		this.configuration = configuration;
	}

	public void startSelected(List<MatrixRunner> collect)
	{
		long count = collect.stream().filter(MatrixRunner::isRunning).count();
		if (count == 0)
		{
			this.map.keySet().stream().filter(collect::contains).forEach(runner -> Common.tryCatch(runner::start, "Error on start runner"));
		}
	}

	public void stopSelected(List<MatrixRunner> collect)
	{
		this.map.keySet().stream().filter(collect::contains).forEach(runner -> Common.tryCatch(runner::stop, "Error on start runner"));
	}

	public void loadSeveral()
	{
		List<File> files = DialogsHelper.showMultipleDialog("Choose matrices", "jf files (*.jf)", "*.jf");
		Optional.ofNullable(files)
			.ifPresent(list -> list.stream()
			.filter(Objects::nonNull)
			.forEach(file -> Common.tryCatch(() ->
			{
				Context context = this.configuration.createContext(new MatrixListener(), System.out);
				MatrixRunner runner = new MatrixRunner(context, file, null, null);
				this.map.put(runner, Boolean.TRUE);
			}, "Error on create new runner")));
	}

	public void showSelected(List<MatrixRunner> collect)
	{
		this.map.keySet().stream().filter(collect::contains).forEach(runner -> Common.tryCatch(() ->
		{
			runner.process((matrix, context, report, startTime) ->
			{
				CustomTab tab = Common.checkDocument(matrix);
				if (tab != null)
				{
					matrix = (MatrixFx) tab.getDocument();
				}
				else
				{
					try
					{
						unsubscribe(runner);
						matrix = new MatrixFx(matrix, context.getConfiguration(), context.getMatrixListener());
						matrix.display();
					}
					catch (Exception e)
					{
						DialogsHelper.showError("Couldn't open the matrix " + matrix);
						return false;
					}
				}
				
				return true;
			});
			
		}, "Error on start runner"));
	}

	public void destroySelected(List<MatrixRunner> collect)
	{
		this.map.keySet().stream().filter(collect::contains).forEach(runner -> Common.tryCatch(runner::close, "Error on start runner"));
	}
}
