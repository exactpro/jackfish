////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;

public enum PieceKind
{
	VISIBLE("visible")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			result.set(self.get(0) != null); // TODO 
		}
	},

	COUNT("count")
	{
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
	},
	
	CONTAINS("contains")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			if (others == null)
			{
				result.error("Others elements is empty");
				return;
			}
			
			Rectangle s = executor.getRectangle(self.get(0));
			Rectangle o = executor.getRectangle(others.get(0));
			
			if (	s.x > o.x || (s.x + s.width) < (o.x + o.width)
				|| 	s.y > o.y || (s.y + s.height) < (o.y + o.height) )
			{
				result.error("does not contain " + piece.locator);
			}
		}
	},

	LEFT("left")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return o.x - (s.x + s.width);
				}
			});
		}
	},
	
	RIGHT("right")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return s.x - (o.x + o.width);
				}
			});
		}
	},

	TOP("top")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return o.y - (s.y + s.height);
				}
			});
		}
	},

	BOTTOM("bottom")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return s.y - (o.y + o.height);
				}
			});
		}
	},

	INSIDE_LEFT("inLeft")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return (o.x + o.width) - (s.x + s.width);
				}
			});
		}
	},
	
	INSIDE_RIGHT("inRight")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return (s.x + s.width) - (o.x + o.width);
				}
			});
		}
	},

	INSIDE_TOP("inTop")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return (o.y + o.height) - (s.y + s.height);
				}
			});
		}
	},

	INSIDE_BOTTOM("inBottom")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return (s.y + s.height) - (o.y + o.height);
				}
			});
		}
	},

	ON_LEFT("onLeft")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return o.x - s.x;
				}
			});
		}
	},
	
	ON_RIGHT("onRight")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return s.x - o.x;
				}
			});
		}
	},

	ON_TOP("onTop")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return o.y - s.y;
				}
			});
		}
	},

	ON_BOTTOM("onBottom")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return s.y - o.y;
				}
			});
		}
	},

	LEFT_ALIGNED("lAlign")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return o.x - s.x;
				}
			});
		}
	},
	
	RIGHT_ALIGNED("rAlign")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return s.x - o.x;
				}
			});
		}
	},
	
	TOP_ALIGNED("tAlign")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return o.y - s.y;
				}
			});
		}
	},

	BOTTOM_ALIGNED("bAlign")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return s.y - o.y;
				}
			});
		}
	},
	
	HORIZONTAL_CENTERED("hCenter")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return (o.x + o.width / 2) - (s.x + s.width / 2);
				}
			});
		}
	},

	VERTICAL_CENTERED("vCenter")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
			check(piece, executor, self, others, result, new Measure() 
			{
				@Override
				public long calc(Rectangle s, Rectangle o)
				{
					return (o.y + o.height / 2) - (s.y + s.height / 2);
				}
			});
		}
	},

	;
	
	private PieceKind(String name)
	{
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}
	
	public <T> void perform(Piece piece, OperationExecutor<T> executor, List<T> self, CheckingLayoutResult result)  throws Exception
	{
		if (selfNeedOne() && self.size() != 1 )
		{
			result.error("Count of element should be 1 instead " + self.size());
			return;
		}
		 
		List<T> others = Collections.emptyList();
		if (piece.locator != null)
		{
			others = executor.findAll(piece.owner, piece.locator);
		}
		if (otherNeedOne() && others.size() != 1 ) 
		{
			result.error("Count of relative elements should be 1 instead " + others.size());
			return;
		}
		
		performDerived(piece, executor, self, others, result);
	}

	protected boolean selfNeedOne()
	{
		return true;
	}

	protected boolean otherNeedOne()
	{
		return true;
	}

	protected abstract <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result)  throws Exception;

	private static interface Measure 
	{
		long calc(Rectangle s, Rectangle o);
	}
	
	private static <T> void check(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result, Measure func) throws Exception 
	{
		if (others == null)
		{
			result.error("Others elements is empty");
			return;
		}
		
		Rectangle selfArea = executor.getRectangle(self.get(0));
		Rectangle otherArea = executor.getRectangle(others.get(0));
		
		long value = func.calc(selfArea, otherArea);
		
		boolean res = piece.range.func(value, piece.a, piece.b);
		if (!res)
		{
			result.error("" + value + " is not " + piece.range.toString("" + piece.a, "" + piece.b));
		}
	}

	
	private String name;
}
