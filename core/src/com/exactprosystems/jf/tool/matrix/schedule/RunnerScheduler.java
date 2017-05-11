////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix.schedule;

import com.exactprosystems.jf.api.common.IMatrixRunner;
import com.exactprosystems.jf.api.common.MatrixState;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.FxDocumentFactory;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.listeners.RunnerListener;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.stage.Window;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class RunnerScheduler implements RunnerListener
{
	private static final Logger logger = Logger.getLogger(RunnerScheduler.class);
	private ScheduleController controller;
	private ConcurrentHashMap<IMatrixRunner, Boolean> map;
	private DocumentFactory factory;

	public RunnerScheduler(FxDocumentFactory fxDocumentFactory) throws Exception
	{
		this.map = new ConcurrentHashMap<>();
		this.controller = Common.loadController(RunnerScheduler.class.getResource("Schedule.fxml"));
		this.controller.init(this);
		this.factory = fxDocumentFactory;
	}

	public void show(Window window)
	{
		if (!this.controller.isShowing())
		{
			this.controller.show(window);
		}
	}

	//region Interface RunnerListener
	@Override
	public void subscribe(IMatrixRunner runner)
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
	public void unsubscribe(IMatrixRunner runner)
	{
		Boolean remove = this.map.remove(runner);
		remove = remove == null ? true : remove;
		logger.trace(String.format("MatrixRunner %s subscribe %s", runner.toString(), (remove ? "" : "un") + "successful"));
		this.controller.removeRunner(runner);
	}

	@Override
	public void stateChange(IMatrixRunner matrixRunner, MatrixState state, int done, int total)
	{
		this.controller.displayState(matrixRunner, state, done, total);
	}
	//endregion

	void startSelected(List<IMatrixRunner> collect)
	{
		this.map.keySet().stream()
				.filter(collect::contains)
				.forEach(runner -> Common.tryCatch(runner::start, "Error on start runner"));
	}

	void stopSelected(List<IMatrixRunner> collect)
	{
		this.map.keySet().stream()
				.filter(collect::contains)
				.forEach(runner -> Common.tryCatch(runner::stop, "Error on start runner"));
	}

	void destroySelected(List<IMatrixRunner> collect)
	{
		this.map.keySet().stream().filter(collect::contains).forEach(runner -> Common.tryCatch(((MatrixRunner)runner)::close, "Error on start runner"));
	}

	void showSelected(List<IMatrixRunner> collect)
	{
		this.map.keySet().stream()
				.filter(collect::contains)
				.forEach(runner -> Common.tryCatch(() ->
		{
			((MatrixRunner)runner).process((matrix, context, report, startTime) ->
			{
				CustomTab tab = Common.checkDocument(matrix);
				if (tab == null)
				{
					try
					{
						unsubscribe(runner);
						matrix.load(new FileReader(runner.getMatrixName()));
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

	void loadSeveral()
	{
		List<File> files = DialogsHelper.showMultipleDialog("Choose matrices", "jf files (*.jf)", "*.jf");
		if (files != null)
		{
			files.stream()
				.filter(Objects::nonNull)
				.forEach(file -> Common.tryCatch(() ->
				{
					try(Reader reader = CommonHelper.readerFromFile(file))
					{
						Context context = this.factory.createContext();
						MatrixRunner runner = context.createRunner(file.getPath(), reader, null, null);
						//	                this.map.put(runner, Boolean.TRUE);
						this.subscribe(runner);
		    	    }
				}, "Error on create new runner"));
		}
	}
}
