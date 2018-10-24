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

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;

import java.util.List;

public class RefactorSetField  extends Refactor
{
	private List<WizardCommand> command;
	private String message;

	public RefactorSetField(Matrix matrix, Tokens token, String value, List<Integer> itemIds)
	{
        int size = itemIds.size();
        this.message = String.format(R.REFACTOR_SET_FIELD_MESSAGE.get(), token, value, Common.getRelativePath(matrix.getNameProperty().get()), size);
	    CommandBuilder builder = CommandBuilder.start();
	    itemIds.forEach(c -> builder.findAndHandleMatrixItem(matrix, c, i -> i.set(token, value)));
		this.command = builder.build();
	}
	
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