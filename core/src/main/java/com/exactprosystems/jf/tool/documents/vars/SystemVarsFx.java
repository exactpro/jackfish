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

package com.exactprosystems.jf.tool.documents.vars;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.vars.SystemVars;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.scene.control.ButtonType;
import javafx.util.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SystemVarsFx extends SystemVars
{
	public SystemVarsFx(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);
	}

	//==============================================================================================================================
	// Document
	//==============================================================================================================================
	@Override
	public void display() throws Exception
	{
		super.display();

		getParameters().fire();
	}

	@Override
	public void save(String fileName) throws Exception
	{
		super.save(fileName);
	}

	@Override
	public boolean canClose() throws Exception
	{
		if (!super.canClose())
		{
			return false;
		}
		
		if (isChanged())
		{
			ButtonType desision = DialogsHelper.showSaveFileDialog(getNameProperty().get());
			if (desision == ButtonType.YES)
			{
				save(getNameProperty().get());
			}
			if (desision == ButtonType.CANCEL)
			{
				return false;
			}
		}
		
		return true;
	}


	//----------------------------------------------------------------------------------------------
	public void updateNameRow(int index, String newValue)
	{
		String lastName = getParameterByIndex(index).getName();
		Command undo = () -> 
		{
			getParameterByIndex(index).setName(lastName);
		};
		Command redo = () -> 
		{
			getParameterByIndex(index).setName(newValue);
		};
		addCommand(undo, redo);
	}

	public void updateExpressionRow(int index, String newValue)
	{
		String lastExpression = getParameterByIndex(index).getExpression();
		Command undo = () -> 
		{ 
			getParameterByIndex(index).setExpression(lastExpression); 
		};
		Command redo = () -> 
		{ 
			getParameterByIndex(index).setExpression(newValue); 
		};
		addCommand(undo, redo);
	}

	void updateDescriptionRow(int index, String newValue)
	{
		String lastDescription = getParameterByIndex(index).getDescription();
		Command undo = () ->
		{
			getParameterByIndex(index).setDescription(lastDescription);
		};
		Command redo = () ->
		{
			getParameterByIndex(index).setDescription(newValue);
		};
		addCommand(undo, redo);
	}

	public void addNewVariable() throws Exception
	{
		Command undo = () -> 
		{ 
			this.getParameters().remove(this.getParameters().size() - 1); 
		};
		Command redo = () -> 
		{ 
			this.getParameters().add(R.SYSTEM_VARS_FX_NAME.get(), R.SYSTEM_VARS_FX_EXPRESSION.get());
		};
		addCommand(undo, redo);
	}

	public void removeParameters(List<Parameter> parameters)
	{
		List<Pair<Integer, Parameter>>  indexes = parameters.stream()
			.map(par -> new Pair<>(getParameters().getIndex(par), par))
			.sorted(Comparator.comparingInt(Pair::getKey)).collect(Collectors.toList());
		
		Command undo = () -> 
		{
			indexes.forEach(pair -> getParameters()
					.insert(pair.getKey(), pair.getValue().getName(), pair.getValue().getExpression(), pair.getValue().getType()));
		};
		Command redo = () -> 
		{
			for (int i = indexes.size() - 1; i >= 0; i--)
			{
				getParameters().remove(indexes.get(i).getKey().intValue());
			}
		};
		addCommand(undo, redo);
	}

    //==============================================================================================================================
    // AbstractDocument
    //==============================================================================================================================
    @Override
    protected void afterRedoUndo()
    {
        super.afterRedoUndo();
    }
	
	private Parameter getParameterByIndex(int index)
	{
		return getParameters().getByIndex(index);
	}
}
