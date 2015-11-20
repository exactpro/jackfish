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
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			for (int c = 0; c < part.i; c++)
			{
				part.operation.operate(executor, locators.get(LocatorKind.Element), component.value);
			}
			return true;
		}
	},
	
	COUNT("count")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			result.setText(String.valueOf(list != null ? list.size() : 0));
			return true;
		}
	},
	
	USE("use")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			if (part.i >= list.size() || part.i < 0)
			{
				throw new Exception("Wrong index in 'use(" + part.i + ")' cause size is " +  list.size() + " for locator " + locators.get(LocatorKind.Element));
			}
			component.value = list.get(part.i);
			return true;
		}
	},

	USE_LOCATOR("use(loc)")
	{
		@Override
		protected boolean needToFind()
		{
			return false;
		}
		
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			component.value = null;
			list.clear();
			locators.put(part.locatorKind, part.locator);
			return true;
		}
	},
	
	SET("set")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			return executor.setValue(component.value, part.d);
		}
	},

	GET_VALUE("getValue")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			result.setText(executor.getValue(component.value));
			return true;
		}
	},

	PUSH("push")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			return executor.push(component.value);
		}
	},
	
	PRESS("press")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			return executor.press(component.value, part.key);
		}
	},
	
	KEY_UP("keyUp")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			return executor.upAndDown(component.value, part.key, false);
		}
	},
	
	KEY_DOWN("keyDown")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			return executor.upAndDown(component.value, part.key, true);
		}
	},
	
	CHECK("check")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			result.setText(String.valueOf(checkText(executor.getValue(component.value), part.text, false, part.b)));
			return true;
		}
	},

	CHECK_XY("check(x,y)")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			String str = isTable(locators, executor) 
					? executor.getValueTableCell(component.value, part.x, part.y) 
					: executor.getValue(component.value);
			result.setText(String.valueOf(checkText(str, part.text, false, part.b)));
			return true;
		}
	},
	
	CHECK_REGEXP("checkRegexp")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			result.setText(String.valueOf(checkText(executor.getValue(component.value), part.text, true, part.b)));
			return true;
		}
	},

	CHECK_REGEXP_XY("checkRegexp(x,y)")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			String str = isTable(locators, executor) ? executor.getValueTableCell(component.value, part.x, part.y) : executor.getValue(component.value);
			result.setText(String.valueOf(checkText(str, part.text, true, part.b)));
			return true;
		}
	},
	
	CHECK_ATTR("checkAttr")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			boolean res = checkText(executor.getAttr(component.value, part.str), part.text, false, part.b);
			result.setText(String.valueOf(res));
			return true;
		}
	},
	
	CHECK_ATTR_REGEXP("checkAttrRegexp")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			boolean res = checkText(executor.getAttr(component.value, part.str), part.text, true, part.b);
			result.setText(String.valueOf(res));
			return true;
		}
	},

	MOVE("move")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			return executor.mouse(component.value, part.x, part.y, MouseAction.Move);
		}
	},
	
	MOVE_XY("move(x,y)")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			return isTable(locators, executor) 
					? executor.mouseTable(component.value, part.x, part.y, MouseAction.Move) 
					: executor.mouse(component.value, part.x, part.y, MouseAction.Move);
		}
	},

	CLICK("click")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			MouseAction mouse = part.mouse == null ? MouseAction.LeftClick : part.mouse;
			return executor.mouse(component.value, part.x, part.y, mouse);
		}
	},
	
	CLICK_XY("click(x,y)")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			MouseAction mouse = part.mouse == null ? MouseAction.LeftClick : part.mouse;
			return isTable(locators, executor) 
					? executor.mouseTable(component.value, part.x, part.y, mouse) 
					: executor.mouse(component.value, part.x, part.y, mouse);
		}
	},
	
	TEXT("text")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			return executor.text(component.value, "" + part.text, part.b);
		}
	},
	
	TEXT_XY("text(x,y)")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			return isTable(locators, executor) 
					? executor.textTableCell(component.value, part.x, part.y, "" + part.text) 
					: executor.text(component.value, "" + part.text, part.b);
		}
	},
	
	TOGGLE("toggle")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			return executor.toggle(component.value, part.b);
		}
	},
	
	SELECT("select")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
            return executor.select(component.value, part.text);
		}
	},
	
	EXPAND("expand")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			return executor.fold(component.value, part.text, false);
		}
	},
	
	COLLAPSE("collapse")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			return executor.fold(component.value, part.text, true);
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
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
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
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			AtomicLong atomicLong = new AtomicLong();
			boolean ok = executor.wait(locators.get(LocatorKind.Element), part.i , part.b, atomicLong);
			result.setText(String.valueOf(atomicLong.get()));
			return ok;
		}
	},

	GET("get")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			String str = executor.get(component.value);
			result.setText(str);
			return true;
		}
	},
	
	GET_ATTR("getAttr")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			String str = executor.getAttr(component.value, part.str);
			result.setText(str);
			return true;
		}
	},

	GET_VALUE_XY("getValue(x,y)")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			String str = isTable(locators, executor) 
					? executor.getValueTableCell(component.value, part.x, part.y) 
					: executor.getValue(component.value);
			result.setText(str);
			return true;
		}
	},
	
	GET_TABLE("getTable")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			result.setArray(executor.getTable(component.value, locators.get(LocatorKind.Rows), locators.get(LocatorKind.Header), 
					locators.get(LocatorKind.Element).useNumericHeader()));
			return true;
		}
	},

	GET_ROW("getRow")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			result.setMap(executor.getRow(component.value, locators.get(LocatorKind.Rows), locators.get(LocatorKind.Header), 
					locators.get(LocatorKind.Element).useNumericHeader(), part.valueCondition, part.colorCondition));
			return true;
		}
	},

	GET_ROW_BY_INDEX("getRowByIndex")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			result.setMap(executor.getRowByIndex(component.value, locators.get(LocatorKind.Rows), locators.get(LocatorKind.Header), 
					locators.get(LocatorKind.Element).useNumericHeader(), part.i));
			return true;
		}
	},

	GET_ROW_INDEXES("getRowIndexes")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			result.setList(executor.getRowIndexes(component.value, locators.get(LocatorKind.Rows), locators.get(LocatorKind.Header), 
					locators.get(LocatorKind.Element).useNumericHeader(), part.valueCondition, part.colorCondition));
			return true;
		}
	},

	GET_ROW_WITH_COLOR("getRowWithoutColor")
	{
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception
		{
			result.setColorMap(executor.getRowWithColor(component.value, locators.get(LocatorKind.Rows), locators.get(LocatorKind.Header), 
					locators.get(LocatorKind.Element).useNumericHeader(), part.i));
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

	
	public <T> boolean operate(Part part, OperationExecutor<T> executor, List<T> elementList, Holder<T> elementHolder, LocatorsHolder locators,
			OperationResult result) throws Exception
	{
		// check permissions for this part
		Locator locator = locators.get(LocatorKind.Element);
		if (!locator.getControlKind().isAllowed(part.kind))
		{
			throw new Exception("Operation " + part.kind + " is not allowed for " + locator.getControlKind());
		}

		// find it, if it needs
		if (needToFind())
		{
			elementList.clear();
			Locator owner = locators.get(LocatorKind.Owner);

			if (locator.getControlKind() == ControlKind.Table && executor.tableIsContainer())
			{
				if (elementHolder.value == null)
				{
					elementHolder.value = executor.find(owner, locator);
				}
				if (part.x >= 0 && part.y >= 0)
				{
					elementHolder.value = executor.lookAtTable(elementHolder.value, locators.get(LocatorKind.Rows), locators.get(LocatorKind.Header), part.x, part.y);
					part.x = Integer.MIN_VALUE;
					part.y = Integer.MIN_VALUE;
				}
			}
			else
			{
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
						elementList.addAll(list);
					}
					elementHolder.value = elementList == null || elementList.size() == 0 ? null : elementList.get(0);
				}
				else
				{
					if (isReal(locator))
					{
						elementHolder.value = executor.find(owner, locator);
					}
				}
			}
			
			
			if (elementHolder.value == null)
			{
				throw new Exception("Component is not found for locator = " + locator);
			}
		}
		

		return operateDerived(part, executor, locators, elementList, elementHolder, result);
	}

	

	protected boolean needToFind()
	{
		return true;
	}
	
	protected abstract <T> boolean operateDerived(Part part, OperationExecutor<T> executor, LocatorsHolder locators, List<T> list, Holder<T> component, OperationResult result) throws Exception;

	
	
	
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

	public static <T> boolean isTable(LocatorsHolder locators, OperationExecutor<T> executor)
	{
		Locator locator = locators.get(LocatorKind.Element);
		if (locator == null)
		{
			return false;
		}
		return locator.getControlKind() == ControlKind.Table && !executor.tableIsContainer();
	}
	
	private static boolean isReal(Locator locator)
	{
		return locator != null && locator.getControlKind() != null && !locator.getControlKind().isVirtual();
	}

	
	private String	name;
}
