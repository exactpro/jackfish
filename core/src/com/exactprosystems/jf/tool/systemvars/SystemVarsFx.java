////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.systemvars;

import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.evaluator.SystemVars;
import com.exactprosystems.jf.common.parser.Parameter;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;

import javafx.scene.control.ButtonType;
import javafx.util.Pair;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SystemVarsFx extends SystemVars
{
	private SystemVarsFxController controller;
	private boolean isControllerInit = false;
	private AbstractEvaluator evaluator;

	public SystemVarsFx(String fileName, Configuration config) throws Exception
	{
		super(fileName, config);
		
		this.evaluator = config.createEvaluator();
	}

	//==============================================================================================================================
	// AbstractDocument
	//==============================================================================================================================
	@Override
	protected void afterRedoUndo() 
	{
		super.afterRedoUndo();
		this.getParameters().evaluateAll(this.evaluator);
		this.controller.displayNewParameters(getParameterList());
	}
	
	@Override
	public void display() throws Exception
	{
		super.display();
		
		this.controller.displayTitle(Common.getSimpleTitle(getName()));
		this.controller.displayNewParameters(evaluateData());
	}

	@Override
	public void create() throws Exception
	{
		super.create();
		initController();
	}
	
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
		initController();
	}
	
	@Override
	public void save(String fileName) throws Exception
	{
		super.save(fileName);
		this.controller.saved(getName());
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
			ButtonType desision = DialogsHelper.showSaveFileDialog(this.getName());
			if (desision == ButtonType.YES)
			{
				save(getName());
			}
			if (desision == ButtonType.CANCEL)
			{
				return false;
			}
		}
		
		return true;
	}


	@Override
	public void close(Settings settings) throws Exception
	{
		super.close(settings);
		this.controller.close();
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
		super.changed(true);
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
		super.changed(true);
	}

	public void addNewVariable() throws Exception
	{
		Command undo = () -> 
		{ 
			this.getParameters().remove(this.getParameters().size() - 1); 
		};
		Command redo = () -> 
		{ 
			this.getParameters().add("name", "'expression'"); 
		};
		addCommand(undo, redo);
		super.changed(true);
	}

	public void removeParameters(List<Parameter> parameters)
	{
		List<Pair<Integer, Parameter>>  indexes = parameters.stream()
			.map(par -> new Pair<Integer, Parameter>(getParameters().getIndex(par), par))
			.sorted((pair1, pair2) -> pair1.getKey() - pair2.getKey()).collect(Collectors.toList());
		
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
		super.changed(true);
	}

	//----------------------------------------------------------------------------------------------
	private void initController()
	{
		if (!this.isControllerInit)
		{
			this.controller = Common.loadController(SystemVarsFx.class.getResource("SystemVarsFx.fxml"));
			this.controller.init(this);
			getConfiguration().register(this);
			this.isControllerInit = true;
		}
	}
	
	private ArrayList<Parameter> evaluateData() throws Exception
	{
		ArrayList<Parameter> res = new ArrayList<>();
		List<Parameter> variables = this.getParameterList();
		variables.forEach(p -> 
		{
			p.evaluate(this.evaluator);
			res.add(p);
		});
		return res;
	}
	
	private Parameter getParameterByIndex(int index)
	{
		return getParameters().getByIndex(index);
	}
}
