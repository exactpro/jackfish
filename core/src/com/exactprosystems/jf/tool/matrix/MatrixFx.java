/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.matrix;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.Sys;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemExecutingState;
import com.exactprosystems.jf.documents.matrix.parser.items.TempItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.scene.control.ButtonType;
import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MatrixFx extends Matrix
{
	public static final String DELIMITER = ",";

	public static final String Dialog            = "Matrix";
	public static final String DIALOG_BREAKPOINT = "BreakPointMatrix";
	public static final String DIALOG_DEFAULTS   = "DefaultsAppAndClient";

	public MatrixFx(String matrixName, DocumentFactory factory, IMatrixListener matrixListener, boolean isLibrary)
	{
		super(matrixName, factory, matrixListener, isLibrary);
		super.saved();
	}

	//==============================================================================================================================
	// AbstractDocument
	//==============================================================================================================================
	@Override
	public void display() throws Exception
	{
		super.display();
		this.getRoot().fire();
		getNameProperty().fire();

		restoreSettings(getFactory().getSettings());
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
			ButtonType decision;
			while (true)
			{
				decision = DialogsHelper.showSaveFileDialog(getNameProperty().get());
				if (decision == ButtonType.YES && !getNameProperty().isNullOrEmpty())
				{
					save(getNameProperty().get());
					break;
				}
				if (decision == ButtonType.YES && getNameProperty().isNullOrEmpty())
				{
					File file = DialogsHelper.showSaveAsDialog(this);
					if (file != null)
					{
						save(file.getPath());
						break;
					}
				}
				if (decision == ButtonType.NO)
				{
					return true;
				}
				if (decision == ButtonType.CANCEL)
				{
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public void close() throws Exception
	{
		storeSettings(getFactory().getSettings());
		super.close();
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
	public void insert(MatrixItem item, int index, MatrixItem what)
	{
		if (item == null)
		{
			super.insert(item, index, what);
			return;
		}

		Command undo = () -> super.remove(what);
		Command redo = () -> super.insert(item, index, what);
		if (!(what instanceof TempItem))
		{
			addCommand(undo, redo);
		}
		else
		{
			redo.execute();
		}
	}

	@Override
	public void remove(MatrixItem item)
	{
		if (item != null)
		{
			MatrixItem parent = item.getParent();
			int index = super.getIndex(item);
			Command undo = () -> super.insert(parent, index, item);
			Command redo = () -> super.remove(item);
			addCommand(undo, redo);
		}
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
				DialogsHelper.showInfo(R.MATRIX_FX_NEIGHBORS_REMOVE.get());
				return;
			}
			List<Temp> collect = items.stream().map(item -> new Temp(item.getParent(), item, super.getIndex(item))).collect(Collectors.toList());
			Command undo = () -> {
				collect.sort(Comparator.comparingInt(Temp::getIndex));
				collect.forEach(temp -> super.insert(temp.getParent(), temp.getIndex(), temp.getItem()));
				enumerate();
			};
			Command redo = () -> {
				items.forEach(super::remove);
				enumerate();
			};
			addCommand(undo, redo);
		}
	}

	public MutableValue<Long> timerProperty()
	{
		return this.timer;
	}

	public void displayTimer(long ms, boolean needShow)
	{
		if (needShow)
		{
			this.timer.accept(ms);
		}
		else
		{
			this.timer.accept(-1L);
		}
	}

	//==============================================================================================================================
	public void parameterInsert(MatrixItem item, int index)
	{
		if (!AbstractAction.additionFieldsAllow(item))
		{
			return;
		}
		int number = item.getNumber();
		Command undo = () -> {
			findAndCallParameters(number, par -> par.remove(index + 1), -1);
		};
		Command redo = () -> {
			findAndCallParameters(number, par -> par.insert(index + 1, "", "", TypeMandatory.Extra), index + 1);
		};
		addCommand(undo, redo);
	}

	public void parameterInsert(MatrixItem item, int index, List<Pair<ReadableValue, TypeMandatory>> list)
	{
		int number = item.getNumber();
		int size = list.size();

		Command undo = () -> {
			findAndCallParameters(number, par -> {
				for (int i = 0; i < size; i++)
				{
					par.remove(index + 1);
				}
			}, -1);
		};
		Command redo = () -> {
			findAndCallParameters(number, par -> {
				for (int i = 0; i < size; i++)
				{
					Pair<ReadableValue, TypeMandatory> pair = list.get(i);
					par.insert(index + 1 + i, pair.getKey().getValue(), "", pair.getValue());
				}
			}, -1);
		};
		addCommand(undo, redo);
	}


	public void parameterRemove(MatrixItem item, int index)
	{
		int number = item.getNumber();
		Parameter last = new Parameter(item.getParameters().getByIndex(index));
		Command undo = () -> {
			findAndCallParameters(number, par -> par.insert(index, last.getName(), last.getExpression(), last.getType()), -1);
		};
		Command redo = () -> {
			findAndCallParameters(number, par -> par.remove(index), -1);
		};
		addCommand(undo, redo);
	}

	public void parameterMoveLeft(MatrixItem item, int index)
	{
		int number = item.getNumber();
		int size = item.getParameters().size();
		Command undo = () -> {
			findAndCallParameters(number, par -> par.moveRight((index - 1 + size) % size), -1);
		};
		Command redo = () -> {
			findAndCallParameters(number, par -> par.moveLeft(index), -1);
		};
		addCommand(undo, redo);
	}

	public void parameterMoveRight(MatrixItem item, int index)
	{
		int number = item.getNumber();
		int size = item.getParameters().size();
		Command undo = () -> {
			findAndCallParameters(number, par -> par.moveLeft((index + 1 + size) % size), -1);
		};
		Command redo = () -> {
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
		Command undo = () -> {
			findAndCallParameters(number, par -> par.getByIndex(index).setName(last), -1);
		};
		Command redo = () -> {
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
		Command undo = () -> {
			findAndCallParameters(number, par -> par.getByIndex(index).setExpression(last), -1);
		};
		Command redo = () -> {
			findAndCallParameters(number, par -> par.getByIndex(index).setExpression(value), -1);
		};
		addCommand(undo, redo);
	}

	public void setupCall(MatrixItem item, String reference, Parameters parameters)
	{
		int number = item.getNumber();
		Parameters last = new Parameters(item.getParameters());
		Command undo = () -> findAndCallParameters(number, par -> par.setValue(last), -1);
		Command redo = () -> findAndCallParameters(number, par -> par.setValue(new Parameters(parameters)), -1);
		addCommand(undo, redo);
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

		Command undo = () -> {
			IntStream.range(0, size).forEach(i -> root.get(i).setOff(lastStates.get(i)));
		};
		Command redo = () -> {
			IntStream.range(0, size).forEach(i -> root.get(i).setOff(flag));
		};
		addCommand(undo, redo);
	}

	public void copy(List<MatrixItem> list) throws Exception
	{
		if (list == null || list.stream().map(MatrixItem::getParent).distinct().count() != 1)
		{
			DialogsHelper.showInfo(R.MATRIX_FX_NEIGHBORS_COPY.get());
			return;
		}
		Parser parser = new Parser();
		String string = parser.itemsToString(list.toArray(new MatrixItem[0]));
		Sys.copyToClipboard(string);
	}

	public MatrixItem[] paste(MatrixItem where) throws Exception
	{
		String string = Sys.getFromClipboard();
		Parser parser = new Parser();
		MatrixItem[] items = parser.stringToItems(string);

		if (where != null && items != null)
		{
			insert(where, items);
		}
		return items;
	}

	public MatrixItem[] insertNew(MatrixItem item, String kind, String value) throws Exception
	{
		MatrixItem newItem = Parser.createItem(kind, value);
		newItem.init(this, this);
		newItem.createId();
		MatrixItem[] items = new MatrixItem[]{newItem};
		insert(item, items);
		return items;
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

	public void setParameter(Object parameter)
	{
		this.parameter = parameter;
	}

	public void startMatrix() throws Exception
	{
		if (getEngine() != null)
		{
			if (!getEngine().isRunning())
			{
				this.getEngine().getContext().getOut().println((String) null);
				this.getEngine().getContext().getOut().println(R.MATRIX_FX_START_AT.get() + " " + (this.startDate == null ? new Date() : this.startDate));
			}

			getEngine().start(this.startDate, this.parameter);
		}
	}

	public void stop()
	{
		if (getEngine() != null)
		{
			getEngine().stop();
			super.matrixListener.matrixFinished(this, 0, 0);
			this.getEngine().getContext().getOut().println(R.MATRIX_FX_MATRIX_STOPPED.get());
			this.timer.accept(-1L);
		}
	}

	public void pauseMatrix()
	{
		if (getEngine() != null)
		{
			getEngine().pause();
		}
	}

	public void pausedMatrix(Matrix matrix)
	{
		if (matrix == this && getEngine() != null)
		{
			getEngine().pause();
		}
	}

	public void stepMatrix()
	{
		if (getEngine() != null)
		{
			getEngine().step();
		}
	}

	public void showResult()
	{
		if (getEngine() != null && getEngine().getReportName() != null)
		{
			File file = new File(getEngine().getReportName());
			DialogsHelper.displayReport(file, getNameProperty().get(), this.factory);
		}
	}

	void clearExecutingState()
	{
		this.getRoot().bypass(item -> item.changeExecutingState(MatrixItemExecutingState.None));
	}

	//==============================================================================================================================
	private void insert(MatrixItem where, MatrixItem[] items) throws Exception
	{
		MatrixItem parent = where.getParent();
		int index = parent.index(where);
		for (int i = 0; i < items.length; i++)
		{
			MatrixItem item = items[i];
			item.init(this, this);
			insert(parent, index + i, item);
		}
		enumerate();
		super.getChangedProperty().accept(true);
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
		String absolutePathMatrix = new File(getNameProperty().get()).getAbsolutePath();
		if (breakPoints.isEmpty())
		{
			settings.remove(Settings.MAIN_NS, DIALOG_BREAKPOINT, absolutePathMatrix);
		}
		else
		{
			settings.setValue(Settings.MAIN_NS, DIALOG_BREAKPOINT, absolutePathMatrix, breakPoints.stream().map(Object::toString).collect(Collectors.joining(DELIMITER)));
		}

		if (Str.areEqual(this.defaultAppId, EMPTY_STRING) && Str.areEqual(this.defaultClientId, EMPTY_STRING))
		{
			settings.remove(Settings.MAIN_NS, DIALOG_DEFAULTS, absolutePathMatrix);
		}
		else
		{
			settings.setValue(Settings.MAIN_NS, DIALOG_DEFAULTS, absolutePathMatrix, this.defaultAppId + DELIMITER + this.defaultClientId);
		}
		settings.saveIfNeeded();
	}

	private void restoreSettings(Settings settings)
	{
		Settings.SettingsValue breakPoints = settings.getValue(Settings.MAIN_NS, DIALOG_BREAKPOINT, new File(getNameProperty().get()).getAbsolutePath());
		Optional.ofNullable(breakPoints).ifPresent(setting -> {
			List<Integer> list = Arrays.stream(setting.getValue().split(DELIMITER)).mapToInt(Integer::valueOf).boxed().collect(Collectors.toList());

			this.getRoot().bypass(item -> {
				if (list.contains(item.getNumber()))
				{
					item.setBreakPoint(true);
				}
			});
		});
	}

	private void checkAndCall(List<MatrixItem> items, Consumer<MatrixItem> applier)
	{
		if (items != null && !items.isEmpty() && applier != null)
		{
			items.forEach(item -> {
				try
				{
					applier.accept(item);
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
				}
			});
			enumerate();
		}
	}

	private void findAndCallParameters(int itemNumber, Consumer<Parameters> applier, int selectIndex)
	{
		MatrixItem item = find(it -> it.getNumber() == itemNumber);
		if (item != null && applier != null)
		{
			try
			{
				applier.accept(item.getParameters());
				item.parametersFire(selectIndex);
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}

	private MutableValue<Long> timer = new MutableValue<>(0L);

	private String defaultAppId;
	private String defaultClientId;

	private Object parameter = null;
	private Date startDate = null;

	private static final Logger logger = Logger.getLogger(MatrixFx.class);
}
