////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public enum OperationKind
{
	REPEAT("repeat")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			for (int c = 0; c < part.i; c++)
			{
				part.operation.operate(executor, locator, component);
			}
			return true;
		}
	},
	
	COUNT("count")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			result.setText(String.valueOf(list != null ? list.size() : 0));
			return true;
		}
	},
	
	USE("use")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			if (list == null)
			{
				throw new Exception("Can't do 'use(" + part.i + ")' for locator " + locator);
			}
			if (part.i >= list.size() || part.i < 0)
			{
				throw new Exception("Wrong index in 'use(" + part.i + ")' cause size is " +  list.size() + " for locator " + locator);
			}
			
			component = list.get(part.i);
			holder.value = component;
			return true;
		}
	},

	USE_LOCATOR("use(loc)")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component, Holder<T> holder, OperationResult result) throws Exception
		{
			// TODO check it
			checkComponent(component, locator);
			holder.value = component;
			return true;
		}
	},
	
	SET("set")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			return executor.setValue(component, part.d);
		}
	},

	GET_VALUE("getValue")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component, Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			result.setText(executor.getValue(component));
			return true;
		}
	},

	PUSH("push")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			return executor.push(component);
		}
	},
	
	PRESS("press")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			return executor.press(component, part.key);
		}
	},
	
	KEY_UP("keyUp")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			return executor.upAndDown(component, part.key, false);
		}
	},
	
	KEY_DOWN("keyDown")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			return executor.upAndDown(component, part.key, true);
		}
	},
	
	CHECK("check")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			result.setText(String.valueOf(checkText(executor.getValue(component), part.text, false, part.b)));
			return true;
		}
	},

	CHECK_XY("check(x,y)")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component, Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			String str = isTable(locator, executor) ? executor.getValueTableCell(component, part.x, part.y) : executor.getValue(component);
			result.setText(String.valueOf(checkText(str, part.text, false, part.b)));
			return true;
		}
	},
	
	CHECK_REGEXP("checkRegexp")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			result.setText(String.valueOf(checkText(executor.getValue(component), part.text, true, part.b)));
			return true;
		}
	},

	CHECK_REGEXP_XY("checkRegexp(x,y)")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component, Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			String str = isTable(locator, executor) ? executor.getValueTableCell(component, part.x, part.y) : executor.getValue(component);
			result.setText(String.valueOf(checkText(str, part.text, true, part.b)));
			return true;
		}
	},
	
	MOVE("move")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			return executor.mouse(component, part.x, part.y, MouseAction.Move);
		}
	},
	
	MOVE_XY("move(x,y)")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			return isTable(locator, executor) ? executor.mouseTable(component, part.x, part.y, MouseAction.Move) : executor.mouse(component, part.x, part.y, MouseAction.Move);
		}
	},

	CLICK("click")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			MouseAction mouse = part.mouse == null ? MouseAction.LeftClick : part.mouse;
			return executor.mouse(component, part.x, part.y, mouse);
		}
	},
	
	CLICK_XY("click(x,y)")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			MouseAction mouse = part.mouse == null ? MouseAction.LeftClick : part.mouse;
			return isTable(locator, executor) ? executor.mouseTable(component, part.x, part.y, mouse) : executor.mouse(component, part.x, part.y, mouse);
		}
	},
	
	TEXT("text")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			return executor.text(component, "" + part.text, part.b);
		}
	},
	
	TEXT_XY("text(x,y)")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			return isTable(locator, executor) ? executor.textTableCell(component, part.x, part.y, "" + part.text) : executor.text(component, "" + part.text, part.b);
		}
	},
	
	TOGGLE("toggle")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			return executor.toggle(component, part.b);
		}
	},
	
	MARK("mark")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
            return executor.mark(component);
		}
	},
	
	SELECT("select")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
            return executor.select(component, part.text);
		}
	},
	
	EXPAND("expand")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			return executor.fold(component, part.text, false);
		}
	},
	
	COLLAPSE("collapse")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			return executor.fold(component, part.text, true);
		}
	},
	
	DELAY("delay")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			Thread.sleep(part.i);
			return true;
		}
	},
	
	WAIT("wait")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			AtomicLong atomicLong = new AtomicLong();
			boolean ok = executor.wait(locator, part.i , part.b, atomicLong);
			result.setText(String.valueOf(atomicLong.get()));
			return ok;
		}
	},
	
	GET("get")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			String str = executor.get(component);
			result.setText(str);
			return true;
		}
	},
	
	GET_VALUE_XY("getValue(x,y)")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			String str = isTable(locator, executor) ? executor.getValueTableCell(component, part.x, part.y) : executor.getValue(component);
			result.setText(str);
			return true;
		}
	},
	
	GET_TABLE("getTable")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			result.setArray(executor.getTable(component, additional, header, locator.useNumericHeader()));
			return true;
		}
	},

	GET_ROW("getRow")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			result.setMap(executor.getRow(component, additional, header, locator.useNumericHeader(), part.valueCondition, part.colorCondition));
			return true;
		}
	},

	GET_ROW_BY_INDEX("getRowByIndex")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			result.setMap(executor.getRowByIndex(component, additional, header, locator.useNumericHeader(), part.i));
			return true;
		}
	},

	GET_ROW_INDEXES("getRowIndexes")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			result.setList(executor.getRowIndexes(component, additional, header, locator.useNumericHeader(), part.valueCondition, part.colorCondition));
			return true;
		}
	},

	GET_ROW_WITH_COLOR("getRowWithoutColor")
	{
		@Override
		public <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
				Holder<T> holder, OperationResult result) throws Exception
		{
			checkComponent(component, locator);
			result.setColorMap(executor.getRowWithColor(component, additional, header, locator.useNumericHeader(), part.i));
			return true;
		}
	};

	OperationKind(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return this.name;
	}

	public abstract <T> boolean operate(Part part, OperationExecutor<T> executor, Locator locator, Locator additional, Locator header, List<T> list, T component,
			Holder<T> holder, OperationResult result) throws Exception;

	
	
	private static <T> void checkComponent(T component, Locator locator) throws Exception
	{
		if (component == null)
		{
			throw new Exception("Component is not found for locator = " + locator);
		}
	}
	
	private static boolean checkText(String componentText, String what, boolean isRegexp, boolean needException) throws Exception
	{
		boolean result;
		if (componentText == null)
		{
			result = false;
		}
		else
		{
			if (isRegexp)
			{
				result = componentText.matches(what);
			}
			else
			{
				result = componentText.equals(what);
			}
		}
		if (needException && !result)
		{
			throw new RemoteException(isRegexp ? String.format("actual value '%s' does not match regexp %s", componentText, what) : String.format("actual value '%s' not equals expected value '%s'", componentText, what));
		}
		return result;
	}

	public static <T> boolean isTable(Locator locator, OperationExecutor<T> executor)
	{
		return locator.getControlKind() == ControlKind.Table && !executor.tableIsContainer();
	}
	
	
	private String	name;
}
