////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix.schedule;

import com.exactprosystems.jf.api.common.MatrixState;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.stage.Window;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Objects;

public class RunnerScheduler implements DocumentFactory.MatrixStateChanged
{
	private ScheduleController controller;
	private DocumentFactory factory;

	public RunnerScheduler(DocumentFactory factory) throws Exception
	{
		this.controller = Common.loadController(RunnerScheduler.class.getResource("Schedule.fxml"));
		this.controller.init(this);
		this.factory = factory;
		this.factory.setMatrixChangeLlistener(this);
	}

	public void show(Window window)
	{
		if (!this.controller.isShowing())
		{
			this.controller.show(window);
		}
	}

	@Override
	public void changed(Matrix matrix, MatrixState oldState, MatrixState newState)
	{
		int done = matrix.countResult(Result.Passed);
		int total = matrix.currentItem();
		this.controller.displayState(matrix, newState, done, total);
	}

	void startSelected(List<Matrix> collect)
	{
		collect.forEach(matrix -> Common.tryCatch(() -> matrix.start(null, null), "Error on start matrix"));
	}

	void stopSelected(List<Matrix> collect)
	{
		collect.forEach(matrix -> Common.tryCatch(matrix::stop, "Error on start matrix"));
	}

	void destroySelected(List<Matrix> collect)
	{
		collect.forEach(runner -> Common.tryCatch(runner::close, "Error on start runner"));
	}

	void showSelected(List<Matrix> collect)
	{
		collect.forEach(matrix -> Common.tryCatch(() ->
		{
			CustomTab tab = Common.checkDocument(matrix);
			if (tab == null)
			{
				try
				{
					matrix.load(new FileReader(matrix.getNameProperty().get()));
					matrix.display();
				}
				catch (Exception e)
				{
					DialogsHelper.showError("Couldn't open the matrix " + matrix);
				}
			}
		}, "Error on start matrix"));
	}

	void loadSeveral()
	{
		List<File> files = DialogsHelper.showMultipleDialog("Choose matrices", "jf files (*.jf)", "*.jf");
		if (files != null)
		{
			files.stream()
					.filter(Objects::nonNull)
					.forEach(file -> ((Matrix) this.factory.createDocument(DocumentKind.MATRIX, Common.getRelativePath(file.getAbsolutePath()))).getStateProperty().fire());
		}
	}
}
