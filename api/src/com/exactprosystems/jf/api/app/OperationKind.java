////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.conditions.ColorCondition;
import com.exactprosystems.jf.api.error.app.ElementNotEnabled;
import com.exactprosystems.jf.api.error.app.ElementNotFoundException;
import com.exactprosystems.jf.api.error.app.OperationNotAllowedException;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public enum OperationKind
{
	FOREACH("foreach")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return part.i == Integer.MAX_VALUE ? ".foreach(%1$s)" : ".foreach(%1$s, %2$d)";
		}
		
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			int max = part.i;
			
			for (int c = 0; c < holder.size() && c < max; c++)
			{
				holder.setIndex(c);
				part.operation.operate(executor, holder.get(LocatorKind.Element), holder.getValue());
			}
			return true;
		}
	},
	
	REPEAT("repeat")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".repeat(%2$d, %1$s)";
		}

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
		protected String formulaTemplate(Part part)
		{
			return ".count()";
		}
		
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setInt(holder.size());
			return true;
		}
	},
	
	USE("use")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".use(%2$d)";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			holder.setIndex(part.i);
			return true;
		}
	},

	USE_LOCATOR("use(loc)")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			if (part.locatorKind != LocatorKind.Element)
			{
				return "";
			}
			
			return part.locatorId == null ? ".use(%15$s)" : ".use('%13$s')";
		}

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
		protected boolean checkEnabled()
		{
			return true;
		}

		@Override
		protected String formulaTemplate(Part part)
		{
			return ".setValue(%5$f)";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return executor.setValue(holder.getValue(), part.d);
		}
	},

	GET_LIST("getList")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".getList()";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setList(executor.getList(holder.getList()));
			return true;
		}
	},

	GET_RECTANGLE("getRectangle")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".getRectangle()";
		}

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
		protected boolean checkEnabled()
		{
			return true;
		}

		@Override
		protected String formulaTemplate(Part part)
		{
			return ".push()";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return executor.push(holder.getValue());
		}
	},
	
	PRESS("press")
	{
		@Override
		protected boolean checkEnabled()
		{
			return true;
		}

		@Override
		protected String formulaTemplate(Part part)
		{
			return ".press(%12$s)";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
		    return executor.press(holder.getValue(), part.key);
		}
	},
	
	KEY_UP("keyUp")
	{
		@Override
		protected boolean checkEnabled()
		{
			return true;
		}

		@Override
		protected String formulaTemplate(Part part)
		{
			return ".keyUp(%12$s)";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
		    return executor.upAndDown(holder.getValue(), part.key, false);
		}
	},
	
	KEY_DOWN("keyDown")
	{
		@Override
		protected boolean checkEnabled()
		{
			return true;
		}

		@Override
		protected String formulaTemplate(Part part)
		{
			return ".keyDown(%12$s)";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
		    return executor.upAndDown(holder.getValue(), part.key, true);
		}
	},
	
    IS_ENABLED("isEnabled")
    {
        @Override
        protected String formulaTemplate(Part part)
        {
            return ".isEnabled()";
        }

        @Override
        public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
        {
            boolean res = executor.elementIsEnabled(holder.getValue());
            result.setBool(res);
            return true;
        }
    },
    
    IS_VISIBLE("isVisible")
    {
        @Override
        protected String formulaTemplate(Part part)
        {
            return ".isVisible()";
        }

        @Override
        public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
        {
            boolean res = executor.elementIsVisible(holder.getValue());
            result.setBool(res);
            return true;
        }
    },
    
	CHECK_LIST("checkList")  
    {
        @Override
        protected String formulaTemplate(Part part)
        {
            return ".checkList(%17$s, %6$b)";
        }

        @Override
        protected String errorMessage(Part part)
        {
            return null;
        }

        @Override
        public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
        {
            List<String> list = executor.getList(holder.getValue());
            StringBuilder sb = new StringBuilder();
            boolean res = compareTwoLists(part.list, list, part.b, sb);
            if (!res)
            {
                result.setError(this.toFormula(part) + " mismatched. " + sb.toString(), part.locator);
            }
            result.setOk(res);
            return res;
        }

        private boolean compareTwoLists(List<String> expected, List<String> actual, boolean ignoreOrder, StringBuilder sb)
        {
            if (expected == actual)
            {
                return true;
            }
            boolean res = true;
            List<String> expectedCopy = expected == null ? new ArrayList<>() : new ArrayList<String>(expected); 
            List<String> actualCopy   = actual   == null ? new ArrayList<>() : new ArrayList<String>(actual);
            if (ignoreOrder)
            {
                actualCopy.removeAll(expected);
                expectedCopy.removeAll(actual);
                if (!actualCopy.isEmpty())
                {
                    res = false;
                    sb.append(" extra items in actual: ").append(Arrays.toString(actualCopy.toArray()));
                }
                if (!expectedCopy.isEmpty())
                {
                    res = false;
                    sb.append(" extra items in expected: ").append(Arrays.toString(expectedCopy.toArray()));
                }
            }
            else
            {
                List<String> diff = new ArrayList<>();
                for (int i = 0; i < Math.max(expectedCopy.size(), actualCopy.size()); i++)
                {
                    String expStr = i < expectedCopy.size() ? expectedCopy.get(i) : "";
                    String actStr = i < actualCopy.size()   ? actualCopy.get(i)   : "";
                    if (!Objects.equals(expStr, actStr))
                    {
                        diff.add(expStr + " : " + actStr);
                        res = false;
                    }
                }
                if (!res)
                {
                    sb.append("mismatched: " + Arrays.toString(diff.toArray()));
                }
                
            }
            return res;
        }
    },
	
    CHECK_COLOR_XY("checkColor")
    {
        @Override
        protected String formulaTemplate(Part part)
        {
            return ".checkColor(%3$d, %4$d, %10$s)";
        }

        @Override
        protected String errorMessage(Part part)
        {
            return null;
        }
        
        @Override
        public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
        {
            Color actual = executor.getColorXY(holder.getValue(), part.x, part.y);
            Map<String, Object> map = new HashMap<>();
            map.put("color", actual);
            boolean res = part.colorCondition.isMatched(map);
            if (!res)
            {
                result.setError("Expected color: " + ((ColorCondition)part.colorCondition).getColor() + " actual color: " + actual, part.locator);
            }
            result.setOk(res);
            return res;
        }
    },
    

    CHECK("check")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".check('%8$s', %6$b)";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setText(String.valueOf(checkText(executor.getValue(holder.getValue()), part.text, false, part.b)));
			return true;
		}
	},

	CHECK_XY("check(x,y)")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".check('%8$s', %3$d, %4$d, %6$b)";
		}

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
		protected String formulaTemplate(Part part)
		{
			return ".checkRegexp('%8$s', %6$b)";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setText(String.valueOf(checkText(executor.getValue(holder.getValue()), part.text, true, part.b)));
			return true;
		}
	},

	CHECK_REGEXP_XY("checkRegexp(x,y)")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".checkRegexp('%8$s', %3$d, %4$d, %6$b)";
		}

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
		protected String formulaTemplate(Part part)
		{
			return ".checkAttr('%7$s', '%8$s')";
		}

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
		protected String formulaTemplate(Part part)
		{
			return ".checkAttrRegexp('%7$s', '%8$s', %6$b)";
		}

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
		protected String formulaTemplate(Part part)
		{
			return ".move()";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return executor.mouse(holder.getValue(), part.x, part.y, MouseAction.Move);
		}
	},
	
	MOVE_XY("move(x,y)")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".move(%3$d, %4$d)";
		}

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
		protected String formulaTemplate(Part part)
		{
			return ".click()";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			MouseAction mouse = part.mouse == null ? MouseAction.LeftClick : part.mouse;
			return executor.mouse(holder.getValue(), part.x, part.y, mouse);
		}
	},
	
	CLICK_XY("click(x,y)")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".click(%3$d, %4$d, %11$s)";
		}

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
		protected boolean checkEnabled()
		{
			return true;
		}

		@Override
		protected String formulaTemplate(Part part)
		{
			return ".text('%8$s', %6$b)";
		}


		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
		    return executor.text(holder.getValue(), "" + part.text, part.b);
		}
	},
	
	TEXT_XY("text(x,y)")
	{
		@Override
		protected boolean checkEnabled()
		{
			return true;
		}

		@Override
		protected String formulaTemplate(Part part)
		{
			return ".text('%8$s', %3$d, %4$d)";
		}

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
		protected boolean checkEnabled()
		{
			return true;
		}

		@Override
		protected String formulaTemplate(Part part)
		{
			return ".toogle(%6$b)";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
		    return executor.toggle(holder.getValue(), part.b);
		}
	},
	
	SELECT_BY_INDEX("select(int)")
	{
		@Override
		protected boolean checkEnabled()
		{
			return true;
		}

		@Override
		protected String formulaTemplate(Part part)
		{
			return ".select(%2$d)";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
		    return executor.selectByIndex(holder.getValue(), part.i);
		}
	},
	
	SELECT("select(str)")
	{
		@Override
		protected boolean checkEnabled()
		{
			return true;
		}

		@Override
		protected String formulaTemplate(Part part)
		{
			return ".select('%8$s')";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
		    return executor.select(holder.getValue(), part.text);
		}
	},
	
	EXPAND("expand")
	{
		@Override
		protected boolean checkEnabled()
		{
			return true;
		}

		@Override
		protected String formulaTemplate(Part part)
		{
			return ".expand('%8$s')";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
		    return executor.expand(holder.getValue(), part.text, true);
		}
	},
	
	COLLAPSE("collapse")
	{
		@Override
		protected boolean checkEnabled()
		{
			return true;
		}

		@Override
		protected String formulaTemplate(Part part)
		{
			return ".collaps('%8$s')";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			return executor.expand(holder.getValue(), part.text, false);
		}
	},

	SCRIPT("script")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".script('%8$s')";
		}

		@Override
		protected <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			String scriptResult = executor.script(holder.getValue(), part.text);
			result.setText(scriptResult);
			return true;
		}
	},

	DELAY("delay")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".delay(%2$d)";
		}

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
		protected String formulaTemplate(Part part)
		{
			return part.locatorId == null ? ".wait(%15$s, %2$d, %16$b)" : ".wait('%13$s', %2$d, %16$b)";
		}
		
	    protected String errorMessage(Part part)
	    {
	        return this.toFormula(part) + " Timeout";
	    }

		@Override
		protected boolean needToFind()
		{
			return false;
		}
		
		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			AtomicLong atomicLong = new AtomicLong();
			boolean ok = executor.wait(part.locator, part.i , part.toAppear, atomicLong);
			result.setText(String.valueOf(atomicLong.get()));
			return ok;
		}
	},

	GET("get")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".get()";
		}

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
		protected String formulaTemplate(Part part)
		{
			return ".getAttr('%7$s')";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			String str = executor.getAttr(holder.getValue(), part.str);
			result.setText(str);
			return true;
		}
	},

	GET_VALUE("getValue")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".getValue()";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setText(executor.getValue(holder.getValue()));
			return true;
		}
	},

    GET_COLOR_XY("getColor")
    {
        @Override
        protected String formulaTemplate(Part part)
        {
            return ".getColor(%3$d, %4$d)";
        }

        @Override
        public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
        {
            Color actual = executor.getColorXY(holder.getValue(), part.x, part.y);
            result.setColor(actual);
            return true;
        }
    },

	GET_VALUE_XY("getValue(x,y)")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".getValue(%3$d, %4$d)";
		}

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

	DRAG_N_DROP("dragNDrop")
	{
		@Override
		protected <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			T value = holder.getValue();

			Locator whereDrop = holder.get(LocatorKind.Dropped);
			Locator whereDropOwner = holder.get(LocatorKind.DroppedOwner);

			T whereDropValue = whereDrop != null ? executor.find(whereDropOwner, whereDrop) : null;

			return executor.dragNdrop(value, part.x, part.y, whereDropValue, part.x2, part.y2, part.b);
		}
	},

	GET_TABLE("getTable")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".getTable()";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setArray(executor.getTable(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), holder.get(LocatorKind.Element).useNumericHeader(), OperationKind.columns(holder.get(LocatorKind.Element))));
			return true;
		}
	},

	GET_ROW("getRow")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".getRow(%9$s, %10$s)";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setMap(executor.getRow(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), 
					holder.get(LocatorKind.Element).useNumericHeader(), OperationKind.columns(holder.get(LocatorKind.Element)), part.valueCondition, part.colorCondition));
			return true;
		}
	},

	GET_ROW_BY_INDEX("getRowByIndex")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".getRowByIndex(%2$d)";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setMap(executor.getRowByIndex(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), 
					holder.get(LocatorKind.Element).useNumericHeader(), OperationKind.columns(holder.get(LocatorKind.Element)), part.i));
			return true;
		}
	},

	GET_ROW_INDEXES("getRowIndexes")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".getRowIndexes(%9$s, %10$s)";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setList(executor.getRowIndexes(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), 
					holder.get(LocatorKind.Element).useNumericHeader(), OperationKind.columns(holder.get(LocatorKind.Element)), part.valueCondition, part.colorCondition));
			return true;
		}
	},

	GET_ROW_WITH_COLOR("getRowWithColor")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".getRowWithColor(%2$d)";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setColorMap(executor.getRowWithColor(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), 
					holder.get(LocatorKind.Element).useNumericHeader(), OperationKind.columns(holder.get(LocatorKind.Element)), part.i));
			return true;
		}
	},
	GET_TABLE_SIZE("getTableSize")
	{
		@Override
		protected String formulaTemplate(Part part)
		{
			return ".getTableSize()";
		}

		@Override
		public <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
		{
			result.setInt(executor.getTableSize(holder.getValue(), holder.get(LocatorKind.Rows), holder.get(LocatorKind.Header), 
					holder.get(LocatorKind.Element).useNumericHeader()));

			return true;
		}
	}, 
	;
	
	
	
	

	OperationKind(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return this.name;
	}

	public String toFormula(Part part)
	{
		return String.format(formulaTemplate(part),
								part.operation,
								part.i,
								part.x,
								part.y,
								part.d,
								part.b,
								part.str, 
								part.text,
								part.valueCondition,
								part.colorCondition,
								part.mouse,
								part.key,
								part.locatorId,
								part.locatorKind,
								part.locator,
								part.toAppear,
								part.list
							);
	}
	
	public <T> boolean operate(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception
	{
		// check permissions for this part
		Locator locator = holder.get(LocatorKind.Element);
		if (locator.isDummy() && part.locator == null)
		{
			throw new OperationNotAllowedException("Can't use operate for dummy locator and dummy control");
		}
		if (!locator.getControlKind().isAllowed(part.kind))
		{
            throw new OperationNotAllowedException("Operation " + part.kind + " is not allowed for " + locator.getControlKind());
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

		Visibility visibility = locator.getVisibility();
		if ((visibility != Visibility.Enable) && checkEnabled() && !executor.elementIsEnabled(holder.getValue()))
		{
			throw new ElementNotEnabled("Element " + locator.getId() + " is not enabled in operation: " + this);
		}

		boolean res = operateDerived(part, executor, holder, result);
		if (!res)
		{
		    String errorMessage = errorMessage(part); 
		    if (errorMessage != null)
		    {
		        result.setError(errorMessage(part), part.locator);
		    }
		}
		return res;
	}


	protected String formulaTemplate(Part part) { return ""; } 

	protected boolean checkEnabled()
	{
		return false;
	}

	protected boolean needToFind()
	{
		return true;
	}
	
    protected String errorMessage(Part part)
    {
        return this.toFormula(part) + " can't be performed";
    }

    protected abstract <T> boolean operateDerived(Part part, OperationExecutor<T> executor, Holder<T> holder, OperationResult result) throws Exception;

	private static boolean checkText(String componentText, String what, boolean isRegexp, boolean needException) throws Exception
	{
		boolean result;
		if (componentText == null)
		{
			result = what == null && !isRegexp;
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
			//TODO throw one of JFRemoteException
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

	private static String[] columns(Locator locator)
	{
		String columns = locator.getColumns();
		if (Str.IsNullOrEmpty(columns))
		{
			return null;
		}
		return columns.split("\\|");
	}

	private String	name;
}
