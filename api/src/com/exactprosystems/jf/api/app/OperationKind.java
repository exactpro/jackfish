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
		protected boolean needToFind()
		{
			return false;
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			for (int c = 0; c < part.i; c++)
			{
				part.operation.operate(executor, holder.get(LocatorKind.Element), null);
			}
			return true;
		}
	},
	
	COUNT("count")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setText(String.valueOf(holder.size()));
			return true;
		}
	},
	
	USE("use")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			holder.setIndex(part.i);
			return true;
		}
	},

	USE_LOCATOR("use")
	{
		@Override
		protected boolean needToFind()
		{
			return false;
		}
		
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			holder.put(part.locatorKind, part.locator);
			return true;
		}
	},
	
	SET("setValue")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return executor.setValue(holder.getValue(), part.d);
		}
	},

	GET_VALUE("getValue")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setText(executor.getValue(holder.getValue()));
			return true;
		}
	},

	GET_RECTANGLE("getRectangle")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setRectangle(executor.getRectangle(holder.getValue()));
			return true;
		}
	},

	PUSH("push")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return executor.push(holder.getValue());
		}
	},
	
	PRESS("press")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return executor.press(holder.getValue(), part.key);
		}
	},
	
	KEY_UP("keyUp")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return executor.upAndDown(holder.getValue(), part.key, false);
		}
	},
	
	KEY_DOWN("keyDown")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return executor.upAndDown(holder.getValue(), part.key, true);
		}
	},
	
	CHECK("check")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setText(String.valueOf(checkText(executor.getValue(holder.getValue()), part.text, false, part.b)));
			return true;
		}
	},

	CHECK_XY("check")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			String str = isTable(holder, executor) 
					? executor.getValueTableCell(holder.getValue(), part.x, part.y) 
					: executor.getValue(executor.lookAtTable(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), part.x, part.y));
			result.setText(String.valueOf(checkText(str, part.text, false, part.b)));
			return true;
		}
	},
	
	CHECK_REGEXP("checkRegexp")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setText(String.valueOf(checkText(executor.getValue(holder.getValue()), part.text, true, part.b)));
			return true;
		}
	},

	CHECK_REGEXP_XY("checkRegexp")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			String str = isTable(holder, executor)
					? executor.getValueTableCell(holder.getValue(), part.x, part.y)
					: executor.getValue(executor.lookAtTable(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), part.x, part.y));
			result.setText(String.valueOf(checkText(str, part.text, true, part.b)));
			return true;
		}
	},
	
	CHECK_ATTR("checkAttr")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			boolean res = checkText(executor.getAttr(holder.getValue(), part.str), part.text, false, part.b);
			result.setText(String.valueOf(res));
			return true;
		}
	},
	
	CHECK_ATTR_REGEXP("checkAttrRegexp")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			boolean res = checkText(executor.getAttr(holder.getValue(), part.str), part.text, true, part.b);
			result.setText(String.valueOf(res));
			return true;
		}
	},

	MOVE("move")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return executor.mouse(holder.getValue(), part.x, part.y, MouseAction.Move);
		}
	},
	
	MOVE_XY("move")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			if (isTable(holder, executor))
			{
				return executor.mouseTable(holder.getValue(), part.x, part.y, MouseAction.Move);
			}
			else
			{
				Locator locator = holder.get(LocatorKind.Element);
				if (locator != null && locator.getControlKind() == ControlKind.Table)
				{
					return executor.mouse(executor.lookAtTable(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), part.x, part.y), Integer.MIN_VALUE, Integer.MIN_VALUE, MouseAction.Move);
				}
				else
				{
					return executor.mouse(holder.getValue(), part.x, part.y, MouseAction.Move);
				}
			}
		}
	},

	CLICK("click")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			MouseAction mouse = part.mouse == null ? MouseAction.LeftClick : part.mouse;
			return executor.mouse(holder.getValue(), part.x, part.y, mouse);
		}
	},
	
	CLICK_XY("click")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			MouseAction mouse = part.mouse == null ? MouseAction.LeftClick : part.mouse;
			if (isTable(holder, executor))
			{
				return executor.mouseTable(holder.getValue(), part.x, part.y, mouse);
			}
			else
			{
				Locator locator = holder.get(LocatorKind.Element);
				if (locator != null && locator.getControlKind() == ControlKind.Table)
				{
					return executor.mouse(executor.lookAtTable(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), part.x, part.y), Integer.MIN_VALUE, Integer.MIN_VALUE, mouse);
				}
				else
				{
					return executor.mouse(holder.getValue(), part.x, part.y, mouse);
				}
			}
		}
	},
	
	TEXT("text")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return executor.text(holder.getValue(), "" + part.text, part.b);
		}
	},
	
	TEXT_XY("text")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return isTable(holder, executor) 
					? executor.textTableCell(holder.getValue(), part.x, part.y, "" + part.text) 
					: executor.text(executor.lookAtTable(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), part.x, part.y), "" + part.text, part.b);
		}
	},
	
	TOGGLE("toggle")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return executor.toggle(holder.getValue(), part.b);
		}
	},
	
	SELECT("select")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
            return executor.select(holder.getValue(), part.text);
		}
	},
	
	EXPAND("expand")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return executor.fold(holder.getValue(), part.text, false);
		}
	},
	
	COLLAPSE("collapse")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return executor.fold(holder.getValue(), part.text, true);
		}
	},
	
	DELAY("delay")
	{
		@Override
		protected boolean needToFind()
		{
			return false;
		}
		
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			Thread.sleep(part.i);
			return true;
		}
	},
	
	WAIT("wait")
	{
		@Override
		protected boolean needToFind()
		{
			return false;
		}
		
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			AtomicLong atomicLong = new AtomicLong();
			boolean ok = executor.wait(holder.get(LocatorKind.Element), part.i , part.b, atomicLong);
			result.setText(String.valueOf(atomicLong.get()));
			return ok;
		}
	},

	GET("get")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			String str = executor.get(holder.getValue());
			result.setText(str);
			return true;
		}
	},
	
	GET_ATTR("getAttr")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			String str = executor.getAttr(holder.getValue(), part.str);
			result.setText(str);
			return true;
		}
	},

	GET_VALUE_XY("getValue")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			String str = isTable(holder, executor) 
					? executor.getValueTableCell(holder.getValue(), part.x, part.y) 
					: executor.getValue(executor.lookAtTable(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), part.x, part.y));
			result.setText(str);
			return true;
		}
	},
	
	GET_TABLE("getTable")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setArray(executor.getTable(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), 
					holder.get(LocatorKind.Element).useNumericHeader()));
			return true;
		}
	},

	GET_ROW("getRow")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setMap(executor.getRow(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), 
					holder.get(LocatorKind.Element).useNumericHeader(), part.valueCondition, part.colorCondition));
			return true;
		}
	},

	GET_ROW_BY_INDEX("getRowByIndex")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setMap(executor.getRowByIndex(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), 
					holder.get(LocatorKind.Element).useNumericHeader(), part.i));
			return true;
		}
	},

	GET_ROW_INDEXES("getRowIndexes")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setList(executor.getRowIndexes(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), 
					holder.get(LocatorKind.Element).useNumericHeader(), part.valueCondition, part.colorCondition));
			return true;
		}
	},

	GET_ROW_WITH_COLOR("getRowWithoutColor")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setColorMap(executor.getRowWithColor(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), 
					holder.get(LocatorKind.Element).useNumericHeader(), part.i));
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

	public String getName()
	{
		switch (this)
		{
			case USE_LOCATOR:
				return "use(loc)";
			case SET:
				return "set";
			case CHECK_XY:
				return "check(x,y)";
			case CHECK_REGEXP_XY:
				return "checkRegexp(x,y)";
			case MOVE_XY:
				return "move(x,y)";
			case CLICK_XY:
				return "click(x,y)";
			case TEXT_XY:
				return "text(x,y)";
			case GET_VALUE_XY:
				return "getValue(x,y)";
		}
		return this.name;
	}

	
	public <T> boolean operate(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
	{
		// check permissions for this part
		Locator locator = holder.get(LocatorKind.Element);
		if (!locator.getControlKind().isAllowed(part.kind))
		{
			throw new Exception("Operation " + part.kind + " is not allowed for " + locator.getControlKind());
		}

		// find it, if it needs
		if (needToFind() && holder.isEmpty())
		{
			Locator owner = holder.get(LocatorKind.Owner);

			if (locator.getAddition() == Addition.Many)
			{
				T dialog = null;
				if (isReal(owner))
				{
					dialog = executor.find(null, owner);
				}
				
				if (isReal(locator))
				{
					List<T> list = executor.findAll(locator.getControlKind(), dialog, locator);
					holder.setValues(list);
				}
			}
			else
			{
				if (isReal(locator))
				{
					holder.setValue(executor.find(owner, locator));
				}

				if (holder.isEmpty())
				{
					throw new ElementNotFoundException(locator);
				}
			}
			
		}

		return operateDerived(part, executor, holder, result);
	}

	

	protected boolean needToFind()
	{
		return true;
	}
	
	protected abstract <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception;

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
			throw new RemoteException(isRegexp 
					? String.format("actual value '%s' does not match regexp %s", componentText, what) 
					: String.format("actual value '%s' not equals expected value '%s'", componentText, what));
		}
		return result;
	}

	public static <T> boolean isTable(Holder<T> holder, OperationExecutor<T> executor)
	{
		Locator locator = holder.get(LocatorKind.Element);
		return locator != null && locator.getControlKind() == ControlKind.Table && !executor.tableIsContainer();
	}
	
	private static boolean isReal(Locator locator)
	{
		return locator != null && locator.getControlKind() != null && !locator.getControlKind().isVirtual();
	}

	private String	name;
}
