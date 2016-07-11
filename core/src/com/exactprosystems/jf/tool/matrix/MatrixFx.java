////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.guidic.controls.Table;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemAttribute;
import com.exactprosystems.jf.documents.matrix.parser.items.NameSpace;
import com.exactprosystems.jf.documents.matrix.parser.items.TempItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.tool.ApplicationConnector;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.main.Main;

import javafx.scene.control.ButtonType;
import javafx.util.Pair;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.Reader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MatrixFx extends Matrix
{
	public enum PlaceToInsert
	{
		After, Before, Child
	}

	private static final String DELIMITER = ",";

	public static final String Dialog = "Matrix";
	public static final String DIALOG_BREAKPOINT = "BreakPointMatrix";
	public static final String DIALOG_DEFAULTS = "DefaultsAppAndClient";

	public MatrixFx(Matrix matrix, DocumentFactory factory) throws Exception
	{
		super(matrix, factory);
		getRoot().init(this);
		init(factory);
	}

	public MatrixFx(String matrixName, DocumentFactory factory, IMatrixListener matrixListener) throws Exception
	{
		super(matrixName, factory, matrixListener);
		init(factory);
	}

	//==============================================================================================================================
	// AbstractDocument
	//==============================================================================================================================
	@Override
	public void display() throws Exception
	{
		super.display();

		initController();

		this.controller.displayTitle(getName());

		displayGuiDictionaries();
		displayClientDictionaries();

		this.controller.displayTab(this.getRoot());
		restoreSettings(getFactory().getSettings());
	}

	@Override
	public void create() throws Exception
	{
		super.create();
	}

	public void createLibrary() throws Exception
	{
		getRoot().get(0).remove();
		NameSpace nameSpace = new NameSpace();
		getRoot().insert(0, nameSpace);
	}

	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
	}

	@Override
	public void save(String fileName) throws Exception
	{
		super.save(fileName);

		getFactory().getConfiguration().matrixChanged(getName(), this);

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
	public void close(Settings settings) throws Exception
	{
		super.close(settings);
		this.runner.close();
		storeSettings(settings);
		if (this.context != null)
		{
			this.context.close();
			this.context = null;
		}
		if (this.getDefaultApplicationConnection() != null)
		{
			stopDefaultApplication();
		}

		this.controller.close();
	}

	@Override
	protected void afterRedoUndo()
	{
		enumerate();
	}

	//==============================================================================================================================
	// methods from Matrix
	//==============================================================================================================================
	@Override
	public void setDefaultApp(String id)
	{
		super.setDefaultApp(id);
		this.defaultAppId = id;
	}

	@Override
	public void setDefaultClient(String id)
	{
		super.setDefaultClient(id);
		this.defaultClientId = id;
	}

	@Override
	public void start(Context context, AbstractEvaluator evaluator, ReportBuilder report)
	{
		super.start(context, evaluator, report);
	}

	@Override
	public void insert(MatrixItem item, int index, MatrixItem what)
	{
		if (item == null)
		{
			super.insert(item, index, what);
			return;
		}
		
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
		if (!(what instanceof TempItem))
		{
			addCommand(undo, redo);
			super.changed(true);
		}
		else
		{
			redo.execute();
		}
	}

	public void replace(MatrixItem tempItem, String newItemName)
	{
		MatrixItem parent = tempItem.getParent();
		int index = parent.index(tempItem);
		MatrixItem newItem = null;
		try
		{
			if (Tokens.containsIgnoreCase(newItemName))
			{
				if (newItemName.equalsIgnoreCase(Tokens.RawTable.get()))
				{
					newItem = Parser.createItem(Tokens.RawTable.get(), Table.class.getSimpleName());
				}
				else if (newItemName.equalsIgnoreCase(Tokens.RawMessage.get()))
				{
					newItem = Parser.createItem(Tokens.RawMessage.get(), "none");
				}
				else
				{
					newItem = Parser.createItem(newItemName, null);
				}
			}
			else
			{
				newItem = Parser.createItem(Tokens.Action.get(), newItemName);
			}
			newItem.init(this);
			newItem.createId();
			insert(tempItem.getParent(), index, newItem);
			//			tempItem.getParent().insert(index, newItem);
		}
		catch (Exception e)
		{
			//			DialogsHelper.showError(e.getMessage());
		}
		finally
		{
			this.controller.remove(tempItem);
			tempItem.remove();
			enumerate();
			//			if (newItem != null)
			//			{
			//				this.controller.display(newItem);
			//				this.controller.setCurrent(newItem);
			//			}
		}

		super.changed(true);
	}

	@Override
	public void remove(MatrixItem item)
	{
		if (item != null)
		{
			MatrixItem parent = item.getParent();
			int index = super.getIndex(item);
			Command undo = () -> {
				super.insert(parent, index, item);
				this.controller.display(item);
			};
			Command redo = () -> {
				super.remove(item);
				this.controller.remove(item);
			};
			addCommand(undo, redo);
			super.changed(true);
		}
	}

	@Override
	public AppConnection getDefaultApplicationConnection()
	{
		return this.applicationConnector == null ? null : this.applicationConnector.getAppConnection();
	}

	private static class Temp
	{
		private MatrixItem parent;
		private MatrixItem item;
		int index;

		public Temp(MatrixItem parent, MatrixItem item, int index)
		{
			this.parent = parent;
			this.item = item;
			this.index = index;
		}

		public MatrixItem getParent()
		{
			return parent;
		}

		public MatrixItem getItem()
		{
			return item;
		}

		public int getIndex()
		{
			return index;
		}

	}

	public void remove(List<MatrixItem> items)
	{
		if (items != null && !items.isEmpty())
		{
			if (items.stream().map(MatrixItem::getParent).distinct().count() != 1)
			{
				DialogsHelper.showInfo("Only neighbors can be removed");
				return;
			}
			List<Temp> collect = items.stream().map(item -> new Temp(item.getParent(), item, super.getIndex(item))).collect(Collectors.toList());
			Command undo = () ->
			{
				collect.sort((t1,t2) -> Integer.compare(t1.getIndex(), t2.getIndex()));
				collect.forEach(temp -> {
					super.insert(temp.getParent(), temp.getIndex(), temp.getItem());
					this.controller.display(temp.getItem());
				});
				enumerate();
			};
			Command redo = () ->
			{
				items.forEach(item -> {
					super.remove(item);
					this.controller.remove(item);
				});
				enumerate();
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
			findAndCallParameters(number, par -> par.remove(index + 1), -1);
		};
		Command redo = () ->
		{
			findAndCallParameters(number, par -> par.insert(index + 1, "", "", TypeMandatory.Extra), index + 1);
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
					par.remove(index + 1);
				}
			}, -1);
		};
		Command redo = () ->
		{
			findAndCallParameters(number, par ->
			{
				for (int i = 0; i < size; i++)
				{
					Pair<ReadableValue, TypeMandatory> pair = list.get(i);
					par.insert(index + 1 + i, pair.getKey().getValue(), "", pair.getValue());
				}
			}, -1);
		};
		addCommand(undo, redo);
	}


	public void parameterRemove(MatrixItem item, int index) throws CloneNotSupportedException
	{
		int number = item.getNumber();
		Parameter last = item.getParameters().getByIndex(index).clone();
		Command undo = () ->
		{
			findAndCallParameters(number, par -> par.insert(index, last.getName(), last.getExpression(), last.getType()), -1);
		};
		Command redo = () ->
		{
			findAndCallParameters(number, par -> par.remove(index), -1);
		};
		addCommand(undo, redo);
	}

	public void parameterMoveLeft(MatrixItem item, int index)
	{
		int number = item.getNumber();
		int size = item.getParameters().size();
		Command undo = () ->
		{
			findAndCallParameters(number, par -> par.moveRight((index - 1 + size) % size), -1);
		};
		Command redo = () ->
		{
			findAndCallParameters(number, par -> par.moveLeft(index), -1);
		};
		addCommand(undo, redo);
	}

	public void parameterMoveRight(MatrixItem item, int index)
	{
		int number = item.getNumber();
		int size = item.getParameters().size();
		Command undo = () ->
		{
			findAndCallParameters(number, par -> par.moveLeft((index + 1 + size) % size), -1);
		};
		Command redo = () ->
		{
			findAndCallParameters(number, par -> par.moveRight(index), -1);
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
			findAndCallParameters(number, par -> par.getByIndex(index).setName(last), -1);
		};
		Command redo = () ->
		{
			findAndCallParameters(number, par -> par.getByIndex(index).setName(name), -1);
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
			findAndCallParameters(number, par -> par.getByIndex(index).setExpression(last), -1);
		};
		Command redo = () ->
		{
			findAndCallParameters(number, par -> par.getByIndex(index).setExpression(value), -1);
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
				findAndCallParameters(number, par -> par.setValue(last), -1);
			};
			Command redo = () ->
			{
				findAndCallParameters(number, par -> par.setValue(parameters), -1);
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
		IntStream.range(0, size).forEach(i -> lastStates.add(root.get(i).isOff()));

		Command undo = () ->
		{
			IntStream.range(0, size).forEach(i -> root.get(i).setOff(lastStates.get(i)));
		};
		Command redo = () ->
		{
			IntStream.range(0, size).forEach(i -> root.get(i).setOff(flag));
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
		Parser parser = new Parser();
		String string = parser.itemsToString(list.toArray(new MatrixItem[0]));
		Common.saveToClipboard(string);
	}

	public void paste(MatrixItem where) throws Exception
	{
		String string = Common.getFromClipboard();
		Parser parser = new Parser();
		MatrixItem[] items = parser.stringToItems(string);

		if (where != null && items != null)
		{
			MatrixItem parent = where.getParent();
			int index = parent.index(where);
			for (MatrixItem item : items)
			{
				item.init(this);
				insert(parent, index++, item);
			}
		}
		enumerate();
		this.controller.refresh();
		super.changed(true);
	}

	public void paste(PlaceToInsert place, MatrixItem where) throws Exception
	{
		long t1 = System.currentTimeMillis();
		String string = Common.getFromClipboard();
//		System.out.println("Get from clipboard : " + (System.currentTimeMillis() - t1));
		Parser parser = new Parser();
		MatrixItem[] items = parser.stringToItems(string);

		if (where != null && items != null)
		{
			insert(place, where, items);
		}
	}

	public void insertNew(MatrixItem item, PlaceToInsert place, String kind, String value) throws Exception
	{
		MatrixItem newItem = Parser.createItem(kind, value);
		newItem.init(this);
		newItem.createId();
		insert(place, item, new MatrixItem[]{newItem});
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

	public void breakPoint(List<MatrixItem> items)
	{
		checkAndCall(items, item -> item.setBreakPoint(!item.isBreakPoint()));
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

	public void startMatrix() throws Exception
	{
		this.controller.coloring();
		getFactory().getConfiguration().getRunnerListener().subscribe(this.runner);
		this.runner.start();
	}

	public void stopMatrix() throws Exception
	{
		this.runner.stop();
		this.controller.coloring();
	}

	public void pauseMatrix() throws Exception
	{
		this.runner.pause();
	}

	public void stepMatrix() throws Exception
	{
		this.runner.step();
	}

	public void showResult() throws Exception
	{
		if (this.runner.getReportName() != null)
		{
			File file = new File(this.runner.getReportName());
			this.controller.showResult(file, this.getName());
		}
	}

	public void showWatch()
	{
		this.controller.showWatcher(this, this.context);
	}

	public void startDefaultApplication(String idAppEntry) throws Exception
	{
		if (idAppEntry.equals(EMPTY_STRING))
		{
			DialogsHelper.showInfo("Select not empty application id");
			return;
		}
		this.applicationConnector.setIdAppEntry(idAppEntry);
		this.applicationConnector.startApplication();
	}

	public void connectDefaultApplication(String idAppEntry) throws Exception
	{
		if (idAppEntry.equals(EMPTY_STRING))
		{
			DialogsHelper.showInfo("Select not empty application id");
			return;
		}
		this.applicationConnector.setIdAppEntry(idAppEntry);
		this.applicationConnector.connectApplication();
	}

	public void stopDefaultApplication() throws Exception
	{
		this.applicationConnector.stopApplication();
	}

	//==============================================================================================================================
	private void init(DocumentFactory factory) throws Exception
	{
		this.console = new TabConsole(System.out);
		this.context = factory.createContext();
		
		this.context.setOut(this.console);
		this.runner = new MatrixRunner(this.context, this, this.startDate, null);
		this.runner.setStartTime(this.startDate);
		this.applicationConnector = new ApplicationConnector(factory);

		super.saved();
	}

	private void initController() throws Exception
	{
		if (!this.isControllerInit)
		{
			getFactory().getConfiguration().register(this);

			this.controller = Common.loadController(MatrixFx.class.getResource("MatrixFx.fxml"));
			this.controller.init(this, this.context, this.console);
			setListener(this.controller);
			this.isControllerInit = true;
			this.applicationConnector.setApplicationListener(this.controller::displayApplicationStatus);

		}
	}

	private void displayGuiDictionaries() throws Exception
	{
		ArrayList<String> result = new ArrayList<>();
		result.add(EMPTY_STRING);
		result.addAll(this.context.getConfiguration().getApplicationPool().appNames().stream().collect(Collectors.toList()));
		this.controller.displayAppList(result);
	}

	private void displayClientDictionaries() throws Exception
	{
		ArrayList<String> result = new ArrayList<>();
		result.add(EMPTY_STRING);
		result.addAll(this.context.getConfiguration().getClientPool().clientNames().stream().collect(Collectors.toList()));
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

	private void insert(PlaceToInsert place, MatrixItem where, MatrixItem[] items) throws Exception
	{
		MatrixItemAttribute annotation = where.getClass().getAnnotation(MatrixItemAttribute.class);
		if (annotation != null && !annotation.hasChildren() && place == PlaceToInsert.Child)
		{
			place = PlaceToInsert.After;
		}
		MatrixItem parent = where.getParent();
		int index = parent.index(where);
		for (int i = 0; i < items.length; i++)
		{
			MatrixItem item = items[i];
			item.init(this);
			switch (place)
			{
				case Before:
					insert(parent, index + i, item);
					break;

				case After:
					insert(parent, index + 1 + i, item);
					break;

				case Child:
					insert(where, i, item);
					break;
			}
		}
		enumerate();
		this.controller.refresh();
		super.changed(true);
	}

	private void storeSettings(Settings settings) throws Exception
	{
		ArrayList<Integer> breakPoints = new ArrayList<>();
		this.getRoot().bypass(item -> {
			if (item.isBreakPoint())
			{
				breakPoints.add(item.getNumber());
			}
		});
		String absolutePathMatrix = new File(this.getName()).getAbsolutePath();
		if (breakPoints.isEmpty())
		{
			//if matrix was present and don't have breakpoint - remove it;
			settings.remove(Main.MAIN_NS, DIALOG_BREAKPOINT, absolutePathMatrix);
		}
		else
		{
			settings.setValue(Main.MAIN_NS, DIALOG_BREAKPOINT, absolutePathMatrix, breakPoints.stream().map(Object::toString).collect(Collectors.joining(DELIMITER)));
		}

		if (Str.areEqual(this.defaultAppId, EMPTY_STRING) && Str.areEqual(this.defaultClientId, EMPTY_STRING))
		{
			settings.remove(Main.MAIN_NS, DIALOG_DEFAULTS, absolutePathMatrix);
		}
		else
		{
			settings.setValue(Main.MAIN_NS, DIALOG_DEFAULTS, absolutePathMatrix, this.defaultAppId + DELIMITER + this.defaultClientId);
		}
		settings.saveIfNeeded();
	}

	private void restoreSettings(Settings settings)
	{
		Settings.SettingsValue breakPoints = settings.getValue(Main.MAIN_NS, DIALOG_BREAKPOINT, new File(this.getName()).getAbsolutePath());
		Optional.ofNullable(breakPoints).ifPresent(setting -> {
			List<Integer> list = Arrays.stream(setting.getValue().split(DELIMITER)).mapToInt(Integer::valueOf).mapToObj(i -> i).collect(Collectors.toList());

			this.getRoot().bypass(item -> {
				if (list.contains(item.getNumber()))
				{
					item.setBreakPoint(true);
				}
			});
		});

		Settings.SettingsValue defaults = settings.getValue(Main.MAIN_NS, DIALOG_DEFAULTS, new File(this.getName()).getAbsolutePath());
		if (Objects.isNull(defaults))
		{
			this.controller.setDefaultApp(this.defaultAppId);
			this.controller.setDefaultClient(this.defaultClientId);
		}
		else
		{
			String[] split = defaults.getValue().split(DELIMITER);
			if (split.length == 2)
			{
				this.defaultAppId = split[0];
				this.defaultClientId = split[1];
			}
			this.controller.setDefaultApp(this.defaultAppId);
			this.controller.setDefaultClient(this.defaultClientId);
		}
		super.setDefaultApp(this.defaultAppId);
		super.setDefaultClient(this.defaultClientId);

	}

	private void checkAndCall(List<MatrixItem> items, MatrixItemApplier applier)
	{
		if (items != null && !items.isEmpty() && applier != null)
		{
			items.forEach(item -> {
				try
				{
					applier.call(item);
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
				}
			});
			enumerate();
			this.controller.refresh();
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
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void findAndCallParameters(int itemNumber, ParameterApplier applier, int selectIndex)
	{
		MatrixItem item = find(it -> it.getNumber() == itemNumber);
		if (item != null && applier != null)
		{
			try
			{
				applier.call(item.getParameters());
				this.controller.refreshParameters(item, selectIndex);
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
	private Context 				context;
	private MatrixRunner 			runner;
	private Date 					startDate = new Date();
	private TabConsole 				console;
	private ApplicationConnector applicationConnector;
	private String defaultAppId = EMPTY_STRING;
	private String defaultClientId = EMPTY_STRING;

	private static final Logger	logger	= Logger.getLogger(MatrixFx.class);
}
