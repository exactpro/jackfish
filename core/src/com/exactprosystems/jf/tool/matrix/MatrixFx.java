////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.Parameter;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.Parser;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.common.parser.items.MatrixItemAttribute;
import com.exactprosystems.jf.common.parser.items.TypeMandatory;
import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.common.parser.listeners.RunnerListener;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;

import javafx.scene.control.ButtonType;
import javafx.util.Pair;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MatrixFx extends Matrix
{
	public enum PlaceToInsert
	{
		After, Before, Child
	}

	public static final String	Dialog				= "Matrix";

	public MatrixFx(Matrix matrix, Configuration config, IMatrixListener matrixListener, RunnerListener runnerListener) throws Exception
	{
		super(matrix);
		getRoot().init(this);
		init(config, matrixListener, runnerListener);
		initController();
	}

	public MatrixFx(String matrixName, Configuration config, IMatrixListener matrixListener, RunnerListener runnerListener) throws Exception
	{
		super(matrixName, matrixListener);
		init(config, matrixListener, runnerListener);
	}

	//==============================================================================================================================
	// AbstractDocument
	//==============================================================================================================================
	@Override
	public void display() throws Exception
	{
		super.display();

		this.controller.displayTitle(getName());
		
		displayGuiDictionaries();
		displayClientDictionaries();

		this.controller.displayTab(this.getRoot());
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

		this.config.matrixChanged(getName(), this);
		
		this.controller.saved(getName());
		this.controller.displayTitle(getName());
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
	public void close() throws Exception
	{
		super.close();

		this.config.unregister(this);

		if (this.runner != null)
		{
			this.runner.close();
			this.runner = null;
		}
		
		if (this.context != null)
		{
			this.context.close();
			this.context = null;
		}
		
		this.controller.close();
	}
	
	//==============================================================================================================================
	// methods from Matrix
	//==============================================================================================================================
	@Override
	public void start(Context context, AbstractEvaluator evaluator, ReportBuilder report)
	{
		super.start(context, evaluator, report);
	}

	@Override
	public void insert(MatrixItem item, int index, MatrixItem what)
	{
		int lastIndex = Math.min(item.count() - 1, index);
		int number = lastIndex < 0 ? item.getNumber() : item.get(lastIndex).getNumber();
		Command undo = () ->
		{
			super.remove(what);
			this.controller.remove(what);
//			this.controller.setCurrent(find(i -> i.getNumber() == number));
		};
		Command redo = () ->
		{
			super.insert(item, index, what);
			this.controller.display(what);
//			this.controller.setCurrent(what);
		};
		addCommand(undo, redo);
		super.changed(true);
	}
	
	@Override
	public void remove(MatrixItem item)
	{
		if (item != null)
		{
			MatrixItem parent = item.getParent();
			int index = super.getIndex(item);
			int number = item.getNumber() - 1;
			Command undo = () -> {
				super.insert(parent, index, item);
				this.controller.display(item);
				//			this.controller.setCurrent(item);
			};
			Command redo = () -> {
				super.remove(item);
				this.controller.remove(item);
				//			this.controller.setCurrent(find(i -> i.getNumber() == number));
			};
			addCommand(undo, redo);
			super.changed(true);
		}
	}

	//==============================================================================================================================
	public void parameterInsert(MatrixItem item, int index)
	{
		int number = item.getNumber();
		Command undo = () ->
		{
			findAndCallParameters(number, par -> par.remove(index));
		};
		Command redo = () ->
		{
			findAndCallParameters(number, par -> par.insert(index, "", "", TypeMandatory.Extra));
		};
		addCommand(undo, redo);
	}

	public void parameterInsert(MatrixItem item, int index, List<Pair<ReadableValue, TypeMandatory>> list)
	{
		int number = item.getNumber();
		int size = list.size();
		
		Command undo = () ->
		{
			findAndCallParameters(number, par -> 
			{
				for (int i = 0; i < size; i++)
				{
					par.remove(index);
				}
			});
		};
		Command redo = () ->
		{
			findAndCallParameters(number, par -> 
			{
				for (int i = 0; i < size; i++)
				{
					Pair<ReadableValue, TypeMandatory> pair = list.get(i);
					par.insert(index + i, pair.getKey().getValue(), "", pair.getValue());
				}
			});
		};
		addCommand(undo, redo);
	}
	
	
	public void parameterRemove(MatrixItem item, int index) throws CloneNotSupportedException
	{
		int number = item.getNumber();
		Parameter last = item.getParameters().getByIndex(index).clone();
		Command undo = () ->
		{
			findAndCallParameters(number, par -> par.insert(index, last.getName(), last.getExpression(), last.getType()));
		};
		Command redo = () ->
		{
			findAndCallParameters(number, par -> par.remove(index));
		};
		addCommand(undo, redo);
	}

	public void parameterMoveLeft(MatrixItem item, int index)
	{
		int number = item.getNumber();
		int size = item.getParameters().size();
		Command undo = () ->
		{
			findAndCallParameters(number, par -> par.moveRight((index - 1 + size) % size));
		};
		Command redo = () ->
		{
			findAndCallParameters(number, par -> par.moveLeft(index));
		};
		addCommand(undo, redo);
	}

	public void parameterMoveRight(MatrixItem item, int index)
	{
		int number = item.getNumber();
		int size = item.getParameters().size();
		Command undo = () ->
		{
			findAndCallParameters(number, par -> par.moveLeft((index + 1 + size) % size));
		};
		Command redo = () ->
		{
			findAndCallParameters(number, par -> par.moveRight(index));
		};
		addCommand(undo, redo);
	}

	public void parameterSetName(MatrixItem item, int index, String name)
	{
		int number = item.getNumber();
		String last = item.getParameters().getByIndex(index).getName();
		if (Str.areEqual(last, name))
		{
			return;
		}
		Command undo = () ->
		{
			findAndCallParameters(number, par -> par.getByIndex(index).setName(last));
		};
		Command redo = () ->
		{
			findAndCallParameters(number, par -> par.getByIndex(index).setName(name));
		};
		addCommand(undo, redo);
	}
	
	public void parameterSetValue(MatrixItem item, int index, String value)
	{
		int number = item.getNumber();
		String last = item.getParameters().getByIndex(index).getExpression();
		if (Str.areEqual(last, value))
		{
			return;
		}
		Command undo = () ->
		{
			findAndCallParameters(number, par -> par.getByIndex(index).setExpression(last));
		};
		Command redo = () ->
		{
			findAndCallParameters(number, par -> par.getByIndex(index).setExpression(value));
		};
		addCommand(undo, redo);
	}
	
	public void setupCall(MatrixItem item, String reference, Parameters parameters)
	{
		try
		{
			int number = item.getNumber();
			Parameters last = item.getParameters().clone();
			Command undo = () ->
			{
				findAndCallParameters(number, par -> par.setValue(last));
			};
			Command redo = () ->
			{
				findAndCallParameters(number, par -> par.setValue(parameters));
			};
			addCommand(undo, redo);
		} catch (CloneNotSupportedException e)
		{ }
	}

	
	public void markFirstLevel(boolean flag) throws Exception
	{
		MatrixItem root = getRoot();
		if (root == null)
		{
			return;
		}

		int size = root.count();
		List<Boolean> lastStates = new ArrayList<>(size);
		for (int i = 0; i < size; i++)
		{
			lastStates.add(root.get(i).isOff());
		}

		Command undo = () ->
		{
			for (int i = 0; i < size; i++)
			{
				root.get(i).setOff(lastStates.get(i));
			}
		};
		Command redo = () ->
		{
			for (int i = 0; i < size; i++)
			{
				root.get(i).setOff(flag);
			}
		};
		addCommand(undo, redo);
	}
	
	public void setOff(int number, boolean flag)
	{
		Command undo = () ->
		{
			findAndCall(number, item -> item.setOff(!flag));
		};
		Command redo = () ->
		{
			findAndCall(number, item -> item.setOff(flag));
		};
		addCommand(undo, redo);
	}

	
	public void copy(List<MatrixItem> list) throws Exception
	{
		copyList.clear();
		
		if (list != null && !list.isEmpty())
		{
			MatrixItem parent = list.get(0).getParent();

			copyList.addAll(list.stream().filter(item -> item.getParent() == parent).collect(Collectors.toList()));
		}
	}
	
	public void paste(MatrixItem where) throws Exception
	{
		if (where != null)
		{
			MatrixItem parent = where.getParent();
			int index = parent.index(where);
			for (MatrixItem item : copyList)
			{
				insert(parent, index++, item.clone());
			}
			enumerate();
			this.controller.refresh();
			super.changed(true);
		}
	}
	
	public void insertNew(MatrixItem item, PlaceToInsert place, String kind, String value) throws Exception
	{
		MatrixItem newItem = Parser.createItem(kind, value);
		MatrixItem parent = item.getParent();
		int index = parent.index(item);
		newItem.init(this);
		newItem.createId();
		
		MatrixItemAttribute annotation = item.getClass().getAnnotation(MatrixItemAttribute.class);
		if (annotation != null && !annotation.hasChildren() && place == PlaceToInsert.Child)
		{
			place = PlaceToInsert.After;
		}
		switch (place)
		{
			case Before:
				insert(parent, index, newItem);
				break;
				
			case After:
				insert(parent, index + 1, newItem);
				break;
				
			case Child:
				insert(item, 0, newItem);
				break;
		}
		
		enumerate();
		this.controller.refresh();
		super.changed(true);
	}

	public void move(MatrixItem from, MatrixItem to) throws Exception
	{
		int index = to.getParent().index(to);
		insert(to.getParent(), index == -1 ? 0 : index, from.clone());
		remove(from);
		enumerate();
		this.controller.refresh();
		super.changed(true);
	}

	public void setCurrent(MatrixItem item)
	{
		this.controller.setCurrent(item);
	}
	
	public void breakPoint(MatrixItem item)
	{
		checkAndCall(item,  matrixItem ->		
		{
			matrixItem.setBreakPoint(!matrixItem.isBreakPoint());
			this.controller.refresh();
		});

	}

	public MatrixItem find(Predicate<MatrixItem> strategy)
	{
		return find(getRoot(), strategy);
	}
	
	public MatrixItem find(MatrixItem parent, Predicate<MatrixItem> strategy)
	{
		if (strategy.test(parent))
		{
			return parent;
		}
		
		for (int i = 0; i < parent.count(); i++)
		{
			MatrixItem item = find(parent.get(i), strategy);
			if (item != null)
			{
				return item;
			}
		}
		return null;
	}
	
	public void setStartTime(Date date)
	{
		this.startDate = date;
	}

	public void changeDefaultApp(String app) throws Exception
	{
		this.context.setDefaultApp(app);
	}

	public void changeDefaultClient(String client) throws Exception
	{
		this.context.setDefaultClient(client);
	}

	public void startMatrix() throws Exception
	{
		this.controller.coloring();
		this.runnerListener.subscribe(this.runner);
		this.runner.start();
	}

	public void stopMatrix()
	{
		if (this.runner != null)
		{
			this.runner.stop();
			this.controller.coloring();
		}
	}

	public void pauseMatrix()
	{
		if (this.runner != null)
		{
			this.runner.pause();
		}
	}

	public void stepMatrix()
	{
		if (this.runner != null)
		{
			this.runner.step();
		}
	}

	public void showResult() throws Exception
	{
		if (this.runner != null && this.runner.getReportName() != null)
		{
			File file = new File(this.runner.getReportName());
			this.controller.showResult(file, this.getName());
		}
	}
		

	public void showWatch()
	{
		this.controller.showWatcher(this, context);
	}

	public void setColor()
	{
		// TODO what for?
	}

	//==============================================================================================================================
	private void init(Configuration config, IMatrixListener matrixListener, RunnerListener runnerListener) throws Exception
	{
		this.config = config;
		this.runnerListener = runnerListener;
		this.console = new TabConsole(System.out);
		this.context = new Context(matrixListener, runnerListener, this.console, this.config);
		this.runner = new MatrixRunner(this.context, this, this.startDate, null);
		this.runner.setStartTime(this.startDate);

		super.saved();
	}

	private void initController() throws Exception
	{
		if (!this.isControllerInit)
		{
			this.config.register(this);

			this.controller = Common.loadController(MatrixFx.class.getResource("MatrixFx.fxml"));
			this.controller.init(this, this.context, this.console);
			setListener(this.controller);
			this.isControllerInit = true;
		}
	}

	private void displayGuiDictionaries() throws Exception
	{
		ArrayList<String> result = new ArrayList<>();
		result.add(null);
		this.context.getApplications().appNames();
		
		for (String app : this.context.getApplications().appNames())
		{
			result.add(app);
		}
		this.controller.displayAppList(result);
	}

	private void displayClientDictionaries() throws Exception
	{
		ArrayList<String> result = new ArrayList<>();
		result.add(null);
		for (String client : this.context.getClients().clientNames())
		{
			result.add(client);
		}
		this.controller.displayClientList(result);
	}

	@FunctionalInterface
	public static interface MatrixItemApplier
	{
		void call(MatrixItem item) throws Exception;
	}

	@FunctionalInterface
	public static interface ParameterApplier
	{
		void call(Parameters parameters) throws Exception;
	}
	
	
	private void checkAndCall(MatrixItem item, MatrixItemApplier applier)
	{
		if (item != null && applier != null)
		{
			try
			{
				applier.call(item);
				enumerate();
				this.controller.refresh();
//				this.controller.setCurrent(item);
			}
			catch (Exception e)
			{ 
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	private void findAndCall(int itemNumber, MatrixItemApplier applier)
	{
		MatrixItem item = find(it -> it.getNumber() == itemNumber);
		if (item != null && applier != null)
		{
			try
			{
				applier.call(item);
				enumerate();
				this.controller.refresh();
//				this.controller.setCurrent(item);
			}
			catch (Exception e)
			{ 
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void findAndCallParameters(int itemNumber, ParameterApplier applier)
	{
		MatrixItem item = find(it -> it.getNumber() == itemNumber);
		if (item != null && applier != null)
		{
			try
			{
				applier.call(item.getParameters());
				this.controller.refreshParameters(item);
//				this.controller.setCurrent(item);
			}
			catch (Exception e)
			{ 
				logger.error(e.getMessage(), e);
			}
		}
	}

	private boolean isControllerInit = false;
	private MatrixFxController 		controller;
	private Configuration 			config;
	private Context 				context;
	private MatrixRunner 			runner;
	private Date 					startDate = new Date();
	private TabConsole 				console;
	private RunnerListener 			runnerListener;

	private static List<MatrixItem>	copyList = new ArrayList<MatrixItem>();

	private static final Logger	logger	= Logger.getLogger(MatrixFx.class);
}
