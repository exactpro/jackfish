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

package com.exactprosystems.jf.tool.wizard.related.refactor;

import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

import java.util.List;

public class RefactorAddParameter extends Refactor
{
	private String message;
	private List<WizardCommand> command;

	public RefactorAddParameter(MatrixItem item, Parameter parameter, int index)
	{
		this.message = String.format("Add parameter %s %s to item %s in a matrix %s"
				, parameter
				, index == -1 ? "to the end" : "by index " + index
				, item
				, Common.getRelativePath(item.getMatrix().getNameProperty().get())
		);
		this.command = CommandBuilder.start()
				.addParameter(item, parameter, index)
				.build();
	}

	@Override
	public List<WizardCommand> getCommands()
	{
		return this.command;
	}

	@Override
	public String toString()
	{
		return this.message;
	}
}
