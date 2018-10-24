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
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

import java.util.Arrays;
import java.util.List;

public class RefactorRemoveParameters extends Refactor
{
	private String              message;
	private List<WizardCommand> commands;

	public RefactorRemoveParameters(MatrixItem item, int[] parameterIndexes)
	{
		this.message = String.format("Remove parameters with indexes %s from %s [%s] from a matrix %s"
				, Arrays.toString(parameterIndexes)
				, item
				, item.getNumber()
				, Common.getRelativePath(item.getMatrix().getNameProperty().get())
		);
		commands = CommandBuilder.start()
				.removeParameters(item, parameterIndexes)
				.build();
	}

	@Override
	public String toString()
	{
		return this.message;
	}

	@Override
	public List<WizardCommand> getCommands()
	{
		return commands;
	}
}
