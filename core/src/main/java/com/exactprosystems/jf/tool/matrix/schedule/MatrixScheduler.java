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

package com.exactprosystems.jf.tool.matrix.schedule;

import com.exactprosystems.jf.api.common.MatrixState;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import javafx.stage.Window;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Objects;

public class MatrixScheduler implements DocumentFactory.MatrixStateChanged
{
	private ScheduleController controller;
	private DocumentFactory factory;

	public MatrixScheduler(DocumentFactory factory) throws Exception
	{
		this.controller = Common.loadController(MatrixScheduler.class.getResource("Schedule.fxml"));
		this.controller.init(this);
		this.factory = factory;
		this.factory.setMatrixChangeListener(this);
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
		collect.forEach(matrix -> Common.tryCatch(() -> matrix.start(null, null), R.MATRIX_SCHEDULER_ERROR_START_MATRIX.get()));
	}

	void stopSelected(List<Matrix> collect)
	{
		collect.forEach(matrix -> Common.tryCatch(matrix::stop, R.MATRIX_SCHEDULER_ERROR_START_MATRIX.get()));
	}

	void destroySelected(List<Matrix> collect)
	{
		collect.forEach(runner -> Common.tryCatch(runner::close, R.MATRIX_SCHEDULER_ERROR_START_MATRIX.get()));
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
					DialogsHelper.showError(String.format(R.MATRIX_SCHEDULER_OPER_MATRIX_ERROR.get(), "" + matrix));
				}
			}
		}, R.MATRIX_SCHEDULER_ERROR_START_MATRIX.get()));
	}

	void loadSeveral()
	{
		List<File> files = DialogsHelper.showMultipleDialog(R.MATRIX_SCHEDULER_CHOOSE_MATRICES.get(), R.COMMON_JF_FILTER.get(), "*.jf");
		if (files != null)
		{
			files.stream()
					.filter(Objects::nonNull)
					.forEach(file -> ((Matrix) this.factory.createDocument(DocumentKind.MATRIX, Common.getRelativePath(file.getAbsolutePath()))).getStateProperty().fire());
		}
	}

	void showReport(Matrix matrix){
		Common.tryCatch(((MatrixFx) matrix)::showResult, R.MATRIX_SCHEDULER_ERROR_OPEN_REPORT.get());
	}
}
