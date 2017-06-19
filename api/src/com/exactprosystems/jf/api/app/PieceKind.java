////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.common.Str;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public enum PieceKind implements Measure
{
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
		public Arrow arrow()
		{
			return null;
		}

		@Override
		protected boolean othersNeed()
		{
			return false;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s(%2$s)"; 
 		}

		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			String text = null;
			if(!self.isEmpty() && self.get(0) != null)
			{
				text = executor.get(self.get(0));
			}
			
			result.set(Str.areEqual(text, piece.name)); 
		}
	},

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
		public Arrow arrow()
		{
			return null;
		}

		@Override
		protected boolean othersNeed()
		{
			return false;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s(%5$s)";
		}

		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			result.set(!self.isEmpty() && self.get(0) != null); 	// FIXME
		}
	},

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
		public Arrow arrow()
		{
			return null;
		}

		@Override
		protected boolean othersNeed()
		{
			return false;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s(%5$s)";
		}

		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			result.set(!self.isEmpty() && self.get(0) != null);  // FIXME
		} 
	},

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
		public Arrow arrow()
		{
			return null;
		}

		@Override
		protected boolean othersNeed()
		{
			return false;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s(%2$s, %4$s)";
		}

		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			String text = null;
			if(!self.isEmpty() && self.get(0) != null)
			{
				text = executor.getAttr(self.get(0), piece.name);
			}
			
			result.set(Str.areEqual(text, piece.text)); 
		}
	},



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
		public Arrow arrow()
		{
			return null;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s()";
		}

		@Override
		protected boolean selfNeedOne()
		{
			return false;
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
				result.error(piece, "Element in not visible");
				return;
			}
			result.set(true);
		}
	},

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
		public Arrow arrow()
		{
			return null;
		}

		@Override
		protected String formulaTemplate()
		{
			return ".%1$s()";
		}

		@Override
		protected boolean selfNeedOne()
		{
			return false;
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
				result.error(piece, "Element in visible");
				return;
			}
			result.set(true);
		}
	},

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
		public Arrow arrow()
		{
			return null;
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
			result.set(piece.range.func(self.size(), piece.a, piece.b));
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
		public Arrow arrow()
		{
			return Arrow.WIDTH;
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
		public Arrow arrow()
		{
			return Arrow.HEIGHT;
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
		public Arrow arrow()
		{
			return null;
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
				result.error(piece, "Referenced elements absent");
				return;
			}

			Rectangle s = executor.getRectangle(self.get(0), true);
			Rectangle o = executor.getRectangle(others.get(0), true);

			if (	s.x > o.x || (s.x + s.width) < (o.x + o.width)
				|| 	s.y > o.y || (s.y + s.height) < (o.y + o.height) )
			{
				result.error(piece, "Does not contain " + piece.locator);
			}
		}
	},

	/**
	 * If self rectangle on left on other rectangle, distance will be positive.
	 * We need calculate distance between left line self control and right line other control
	 */
	LEFT("left")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public Arrow arrow()
		{
			return Arrow.LEFT_RIGHT;
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return s.x - (o.x + o.width);
		}
	},

	/**
	 * If self rectangle on right on other rectangle, distance will be positive.
	 * We need calculate distance between right line self control and left line other control
	 */
	RIGHT("right")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public Arrow arrow()
		{
			return Arrow.RIGHT_LEFT;
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return o.x - (s.x + s.width);
		}
	},

	/**
	 * If self rectangle under other rectangle, distance will be positive.
	 * We need calculate distance between top line self control and bottom line other control
	 */
	TOP("top")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public Arrow arrow()
		{
			return Arrow.TOP_BOTTOM;
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return s.y - (o.y + o.height);
		}
	},

	/**
	 * If self rectangle above other rectangle, distance will be positive.
	 * We need calculate distance between bottom line self control and top line other control
	 */
	BOTTOM("bottom")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public Arrow arrow()
		{
			return Arrow.BOTTOM_TOP;
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return o.y - (s.y + s.height);
		}
	},

	/**
	 * If whole self rectangle inside other rectangle, distance will be positive
	 * We need to calculate distance between left line's both rectangles.
	 */
	INSIDE_LEFT("inLeft")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public Arrow arrow()
		{
			return Arrow.LEFT_LEFT;
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return s.x - o.x;
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
		public Arrow arrow()
		{
			return Arrow.RIGHT_RIGHT;
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return (o.x + o.width) - (s.x + s.width);
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
		public Arrow arrow()
		{
			return Arrow.TOP_TOP;
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return s.y - o.y;
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
		public Arrow arrow()
		{
			return Arrow.BOTTOM_BOTTOM;
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return (o.y + o.height) - (s.y + s.height);
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
		public Arrow arrow()
		{
			return Arrow.LEFT_LEFT;
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
	ON_RIGHT("onRight")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public Arrow arrow()
		{
			return Arrow.RIGHT_RIGHT;
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
	ON_TOP("onTop")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public Arrow arrow()
		{
			return Arrow.TOP_TOP;
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
	ON_BOTTOM("onBottom")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, this);
		}

		@Override
		public Arrow arrow()
		{
			return Arrow.BOTTOM_BOTTOM;
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return -INSIDE_BOTTOM.distance(s, o);
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
		public Arrow arrow()
		{
			return Arrow.LEFT_LEFT;
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
		public Arrow arrow()
		{
			return Arrow.RIGHT_RIGHT;
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
		public Arrow arrow()
		{
			return Arrow.TOP_TOP;
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
		public Arrow arrow()
		{
			return Arrow.BOTTOM_BOTTOM;
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
		public Arrow arrow()
		{
			return Arrow.H_CENTERS;
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return (s.x + s.width / 2) - (o.x + o.width / 2);
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
		public Arrow arrow()
		{
			return Arrow.V_CENTERS;
		}

		@Override
		public int distance(Rectangle s, Rectangle o)
		{
			return (s.y + s.height / 2) - (o.y + o.height / 2);
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


	public boolean useName()
	{
		return true;
	}

	public boolean useRange()
	{
		return true;
	}

	public <T> void perform(Piece piece, OperationExecutor<T> executor, List<T> self, Locator locator, CheckingLayoutResult result)  throws Exception
	{
		if (selfNeedOne() && self.size() != 1 )
		{
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
			result.error(piece, "Count of relative elements " + piece.locator.getId() + " should be 1, but was found " + others.size());
			return;
		}
		if (othersNeed() && others.size() == 0 )
		{
			result.error(piece, "Relative element '" + piece.locator.getId() + "' does not found");
			return;
		}

		performDerived(piece, executor, self, others, result);
	}

	public String toFormula(String controlId, Range range, String first, String second, String text, String color)
	{
		return String.format(formulaTemplate(), this.name, controlId, range == null ? "" : range.toString(first, second), text, color);
	}

	public int distance(Rectangle s, Rectangle o)
	{
		return 0;
	}

	public abstract Arrow arrow();

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
			result.error(piece, "Others elements is empty");
			return;
		}

		Rectangle selfArea = executor.getRectangle(self.get(0), true);
		Rectangle otherArea = (others.size() > 0) ? executor.getRectangle(others.get(0), true) : null;

		long value = func.distance(selfArea, otherArea);

		boolean res = piece.range.func(value, piece.a, piece.b);
		if (!res)
		{
			result.error(piece, "Expected = " + value + " actual = " + piece.range.toString("" + piece.a, "" + piece.b));
		}
	}


	private String 	name;
}
