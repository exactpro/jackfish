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

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.common.Str;

import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * Example<br>
 * <br>
 * $self<br>
 * DoSpec.left(10, 'other')<br>
 * <br>
 * This means, that self element on left on other element on 10px<br>
 */
public enum PieceKind implements Measure
{
	/**
	 * check text of self
	 */
	TEXT("text")
	{
		@Override
		public boolean useName()
		{
			return true;
		}

		@Override
		public boolean useRange()
		{
			return false;
		}

		@Override
		protected boolean othersNeed()
		{
			return false;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s(%4$s)";
 		}

		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			String text = null;
			if(!self.isEmpty() && self.get(0) != null)
			{
				text = executor.get(self.get(0));
			}

			boolean isEquals = Str.areEqual(text, piece.text);
			if (!isEquals)
			{
				result.newError(piece, piece.text, text);
				result.error(piece, "Actual = " + piece.text + " Expected = " + text);
			}
		}
	},

	/**
	 * check color of self
	 */
	COLOR("color")
	{
		@Override
		public boolean useName()
		{
			return false;
		}

		@Override
		public boolean useRange()
		{
			return false;
		}

		@Override
		protected boolean othersNeed()
		{
			return false;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s(%6$s)";
		}

		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			Color color = null;
			if (!self.isEmpty() && self.get(0) != null)
			{
				color = executor.getColor(self.get(0), true);
			}
			if (!piece.color.equals(color))
			{
				result.newError(piece, piece.color.toString(), color == null ? "null" : color.toString());
				result.error(piece, "Actual = " + piece.color + " Expected = " + color);
			}
		}
	},

	/**
	 * check background color of self element
	 */
	BACK_COLOR("backColor")
	{
		@Override
		public boolean useName()
		{
			return false;
		}

		@Override
		public boolean useRange()
		{
			return false;
		}

		@Override
		protected boolean othersNeed()
		{
			return false;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s(%6$s)";
		}

		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			Color color = null;
			if (!self.isEmpty() && self.get(0) != null)
			{
				color = executor.getColor(self.get(0), false);
			}
			if (!piece.color.equals(color))
			{
				result.newError(piece, piece.color.toString(), color == null ? "null" : color.toString());
				result.error(piece, "Actual = " + piece.color + " Expected = " + color);
			}
		} 
	},

	/**
	 * check attribute with name
	 */
	ATTR("attr")
	{
		@Override
		public boolean useName()
		{
			return false;
		}

		@Override
		public boolean useRange()
		{
			return false;
		}

		@Override
		protected boolean othersNeed()
		{
			return false;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s(%4$s, %5$s)";
		}

		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			String text = null;
			if(!self.isEmpty() && self.get(0) != null)
			{
				text = executor.getAttr(self.get(0), piece.text);
			}

			boolean res = Str.areEqual(text, piece.text2);
			if (!res)
			{
				result.newError(piece, text, piece.text2);
			}
			result.set(res);
		}
	},


	/**
	 * check that element is visible
	 */
	VISIBLE("visible")
	{
		@Override
		public boolean useName()
		{
			return false;
		}

		@Override
		public boolean useRange()
		{
			return false;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s()";
		}

		@Override
		protected boolean othersNeed()
		{
			return false;
		}

		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			boolean res = !self.isEmpty() && self.get(0) != null;
			if (!res)
			{
				result.newError(piece, "Element in not visible", "Element is visible");
				result.error(piece, "Element in not visible");
				return;
			}
			result.set(true);
		}
	},

	/**
	 * check that element is invisible
	 */
	INVISIBLE("invisible")
	{
		@Override
		public boolean useName()
		{
			return false;
		}

		@Override
		public boolean useRange()
		{
			return false;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s()";
		}

		@Override
		protected boolean othersNeed()
		{
			return false;
		}

		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			boolean res = self.isEmpty() || self.get(0) == null;
			if (!res)
			{
				result.newError(piece, "Element is visible", "Element in not visible");
				result.error(piece, "Element in visible");
				return;
			}
			result.set(true);
		}
	},

	/**
	 * check count of elements
	 */
	COUNT("count")
	{
		@Override
		public boolean useName()
		{
			return false;
		}

		@Override
		public boolean useRange()
		{
			return true;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s(%3$s)";
		}

		@Override
		protected boolean selfNeedOne()
		{
			return false;
		}

		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			boolean func = piece.range.func(self.size(), piece.a, piece.b);
			if (!func)
			{
				result.newError(piece, "" + self.size(), piece.range.toString("" + piece.a, "" + piece.b));
				result.error(piece, "Actual = " + self.size() + " Expected = " + piece.range.toString("" + piece.a, "" + piece.b));
			}
		}

		@Override
		protected boolean othersNeed()
		{
			return false;
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return 1;
		}
	},

	/**
	 * distance - current width on self rectangle. Always positive
	 */
	WIDTH("width")
	{
		@Override
		public boolean useName()
		{
			return false;
		}

		@Override
		public boolean useRange()
		{
			return true;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s(%3$s)";
		}

		@Override
		protected boolean othersNeed()
		{
			return false;
		}

		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return s.width;
		}
	},

	/**
	 * distance - current height of self rectangle. Always positive
	 */
	HEIGHT("height")
	{
		@Override
		public boolean useName()
		{
			return false;
		}

		@Override
		public boolean useRange()
		{
			return true;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s(%3$s)";
		}

		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		protected boolean othersNeed()
		{
			return false;
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return s.height;
		}
	},

	CONTAINS("contains")
	{
		@Override
		public boolean useName()
		{
			return true;
		}

		@Override
		public boolean useRange()
		{
			return false;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s('%2$s')";
		}

		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			if (others == null)
			{
				result.newError(piece, "Referenced elements absent", "References elements are not null");
				result.error(piece, "Referenced elements absent");
				return;
			}

			Rectangle s = executor.getRectangle(self.get(0));
			Rectangle o = executor.getRectangle(others.get(0));

			if (	s.x > o.x || (s.x + s.width) < (o.x + o.width)
				|| 	s.y > o.y || (s.y + s.height) < (o.y + o.height) )
			{
				result.newError(piece, "Does not contain " + piece.locator, "Contains");
				result.error(piece, "Does not contain " + piece.locator);
			}
		}
	},

	/**
	 * If self rectangle on left on other rectangle, distance will be positive.<br>
	 * We need calculate distance between left line self control and right line other control<br>
	 * <br>
	 * +------+   +------+<br>
	 * | Self |---|Other |<br>
	 * +------+   +------+<br>
	 * <br>
	 * positive distance<br>
	 */
	LEFT("left")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return o.x - (s.x + s.width);
		}
	},

	/**
	 * If self rectangle on right on other rectangle, distance will be positive.<br>
	 * We need calculate distance between right line self control and left line other control<br>
	 *
	 * +------+   +------+<br>
	 * |Other |---| Self |<br>
	 * +------+   +------+<br>
	 * <br>
	 * positive distance<br>
	 *
	 */
	RIGHT("right")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return s.x - (o.x + o.width);
		}
	},

	/**
	 * If self rectangle above other rectangle, distance will be positive.<br>
	 * We need calculate distance between top line self control and bottom line other control<br>
	 *
	 * +------+ <br>
	 * | Self | <br>
	 * +------+ <br>
	 *    |     <br>
	 * +------+ <br>
	 * | Other |<br>
	 * +------+ <br>
	 *
	 *  positive distance
	 */
	TOP("top")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return o.y - (s.y + s.height);
		}
	},

	/**
	 * If self rectangle below other rectangle, distance will be positive.<br></br>
	 * We need calculate distance between bottom line self control and top line other control<br></br>
	 *
	 * +------+ <br>
	 * |Other | <br>
	 * +------+ <br>
	 *    |     <br>
	 * +------+ <br>
	 * | Self |<br>
	 * +------+ <br>
	 *
	 *  positive distance
	 *
	 */
	BOTTOM("bottom")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return s.y - (o.y + o.height);
		}
	},

	/**
	 * If whole self rectangle inside other rectangle, distance will be positive<br>
	 * We need to calculate distance between left line's both rectangles.<br>
	 */
	INSIDE_LEFT("inLeft")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return s.x - o.x;
		}

		@Override
		public boolean checkValid(Rectangle s, Rectangle o)
		{
			return o.x <= s.x
					&& (o.x + o.width) >= (s.x + s.width)
					&& o.y <= s.y
					&& (o.y + o.height) >= (s.y + s.height);
		}
	},

	/**
	 * If whole self rectangle inside other rectangle, distance will be positive
	 * We need to calculate distance between right line's both rectangles.
	 */
	INSIDE_RIGHT("inRight")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return (o.x + o.width) - (s.x + s.width);
		}

		@Override
		public boolean checkValid(Rectangle s, Rectangle o)
		{
			return o.x <= s.x
					&& (o.x + o.width) >= (s.x + s.width)
					&& o.y <= s.y
					&& (o.y + o.height) >= (s.y + s.height);
		}
	},

	/**
	 * If whole self rectangle inside other rectangle, distance will be positive
	 * We need to calculate distance between top line's both rectangles.
	 */
	INSIDE_TOP("inTop")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return s.y - o.y;
		}

		@Override
		public boolean checkValid(Rectangle s, Rectangle o)
		{
			return o.x <= s.x
					&& (o.x + o.width) >= (s.x + s.width)
					&& o.y <= s.y
					&& (o.y + o.height) >= (s.y + s.height);
		}
	},

	/**
	 * If whole self rectangle inside other rectangle, distance will be positive
	 * We need to calculate distance between bottom line's both rectangles.
	 */
	INSIDE_BOTTOM("inBottom")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return (o.y + o.height) - (s.y + s.height);
		}

		@Override
		public boolean checkValid(Rectangle s, Rectangle o)
		{
			return o.x <= s.x
					&& (o.x + o.width) >= (s.x + s.width)
					&& o.y <= s.y
					&& (o.y + o.height) >= (s.y + s.height);
		}
	},

	/**
	 * If left line of self rectangle on the left of the left line other rectangle, distance will be positive;
	 * We need calculate distance between left line's both rectangles.
	 */
	ON_LEFT("onLeft")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return -INSIDE_LEFT.distance(s, o);
		}

		@Override
		public boolean checkValid(Rectangle s, Rectangle o)
		{
			return s.intersects(o) && !o.contains(s) && s.x <= o.x;
		}
	},

	/**
	 * If right line of self rectangle on the right of the right line other rectangle, distance will be positive;
	 * We need calculate distance between right line's both rectangles.
	 */
	ON_RIGHT("onRight")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return -INSIDE_RIGHT.distance(s, o);
		}

		@Override
		public boolean checkValid(Rectangle s, Rectangle o)
		{
			return s.intersects(o) && !o.contains(s) && (o.x + o.width) <= (s.x + s.width);
		}
	},

	/**
	 * If top line of self rectangle above top line other rectangle, distance will be positive;
	 * We need calculate distance between top line's both rectangles.
	 */
	ON_TOP("onTop")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return -INSIDE_TOP.distance(s, o);
		}

		@Override
		public boolean checkValid(Rectangle s, Rectangle o)
		{
			return s.intersects(o) && !o.contains(s) && s.y < o.y;
		}
	},

	/**
	 * If bottom line of self rectangle below bottom line other rectangle, distance will be positive;
	 * We need calculate distance between bottom line's both rectangles.
	 */
	ON_BOTTOM("onBottom")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return -INSIDE_BOTTOM.distance(s, o);
		}

		@Override
		public boolean checkValid(Rectangle s, Rectangle o)
		{
			return s.intersects(o) && !o.contains(s) && o.y + o.height <= s.y + s.height;
		}
	},

	/**
	 * If left line of self rectangle on the left of the left line other rectangle, distance will be positive;
	 * We need calculate distance between left line's both rectangles.
	 */
	LEFT_ALIGNED("lAlign")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return -INSIDE_LEFT.distance(s, o);
		}
	},

	/**
	 * If right line of self rectangle on the right of the right line other rectangle, distance will be positive;
	 * We need calculate distance between right line's both rectangles.
	 */
	RIGHT_ALIGNED("rAlign")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return -INSIDE_RIGHT.distance(s, o);
		}
	},

	/**
	 * If top line of self rectangle above top line other rectangle, distance will be positive;
	 * We need calculate distance between top line's both rectangles.
	 */
	TOP_ALIGNED("tAlign")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return -INSIDE_TOP.distance(s, o);
		}
	},

	/**
	 * If bottom line of self rectangle below bottom line other rectangle, distance will be positive;
	 * We need calculate distance between bottom line's both rectangles.
	 */
	BOTTOM_ALIGNED("bAlign")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return -INSIDE_BOTTOM.distance(s, o);
		}
	},

	/**
	 *  If vertical center of self rectangle on the right of vertical center of other rectangle, distance will be positive.
	 *  We need calculate distance between vertical line's both rectangle's
	 */
	HORIZONTAL_CENTERED("hCenter")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return (s.y + s.height / 2) - (o.y + o.height / 2);
		}
	},

	/**
	 *  If horizontal center of self rectangle below of horizontal center of other rectangle, distance will be positive.
	 *  We need calculate distance between horizontal line's both rectangle's
	 */
	VERTICAL_CENTERED("vCenter")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return (s.x + s.width / 2) - (o.x + o.width / 2);
		}
	}
	;

	PieceKind(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return this.name;
	}


	//TODO delete
	public boolean useName()
	{
		return true;
	}

	//TODO delete
	public boolean useRange()
	{
		return true;
	}

	public <T> void perform(Piece piece, OperationExecutor<T> executor, List<T> self, Locator locator, CheckingLayoutResult result)  throws Exception
	{
		if (selfNeedOne() && self.size() != 1 )
		{
			result.newError(piece, "Element '" + locator.getId() + "' does not found", "");
			result.error(piece, "Element '" + locator.getId() + "' does not found");
			return;
		}

		// TODO check the logic with otherNeedOne()
		List<T> others = Collections.emptyList();
		if (piece.locator != null)
		{
			others = executor.findAll(piece.owner, piece.locator);
		}
		if (otherNeedOne() && others.size() != 1 )
		{
			result.newError(piece, "Need 1", "Found : " + others.size());
			result.error(piece, "Count of relative elements " + piece.locator.getId() + " should be 1, but was found " + others.size());
			return;
		}
		if (othersNeed() && others.size() == 0 )
		{
			result.newError(piece, "Relative element '" + piece.locator.getId() + "' does not found", "");
			result.error(piece, "Relative element '" + piece.locator.getId() + "' does not found");
			return;
		}

		performDerived(piece, executor, self, others, result);
	}

	public String toFormula(String controlId, Range range, String first, String second, String text, String text2, String color)
	{
		return String.format(formulaTemplate(), this.name, controlId, range == null ? "" : range.toString(first, second), text, text2, color);
	}

	public int distance(Rectangle s, Rectangle o)
	{
		return 0;
	}

	public static PieceKind findByName(String name)
	{
		for (PieceKind kind : values())
		{
			if (kind.name.equals(name))
			{
				return kind;
			}
		}
		return null;
	}

	public boolean checkValid(Rectangle s, Rectangle o)
	{
		return true;
	}

	protected boolean selfNeedOne()
	{
		return true;
	}

	protected boolean otherNeedOne()
	{
		return false;
	}

	protected boolean othersNeed()
	{
		return true;
	}

	protected String formulaTemplate()
	{
		return ".%1$s('%2$s',%3$s)";
	}

	protected abstract <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result)  throws Exception;



	private static <T> void check(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result, Measure func) throws Exception
	{
		if (others == null)
		{
			result.newError(piece, "Others elements are empty", "Other elements are not empty");
			result.error(piece, "Others elements is empty");
			return;
		}

		Rectangle selfArea = executor.getRectangle(self.get(0));
		Rectangle otherArea = (others.size() > 0) ? executor.getRectangle(others.get(0)) : null;

		long value = func.distance(selfArea, otherArea);

		boolean res = piece.range.func(value, piece.a, piece.b);
		if (!res)
		{
			result.newError(piece, "" + value, piece.range.toString("" + piece.a, "" + piece.b));
			result.error(piece, "Actual = " + value + " Expected = " + piece.range.toString("" + piece.a, "" + piece.b));
		}
	}


	private String 	name;
}
