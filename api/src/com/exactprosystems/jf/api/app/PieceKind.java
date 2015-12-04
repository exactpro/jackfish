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
	
	LEFT("left")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
			if (others == null)
			{
				result.error("Others elements is empty");
				return;
			}
			
			Rectangle selfArea = executor.getRectangle(self.get(0));
			Rectangle otherArea = executor.getRectangle(others.get(0));
			long selfRight = selfArea.x + selfArea.width;
			long otherLeft = otherArea.x;
			long value = otherLeft - selfRight;
			
			boolean res = piece.range.func(value, piece.a, piece.b);
			if (!res)
			{
				result.error("" + value + " is not " + piece.range.toString(piece.a, piece.b));
			}
		}
	},
	
	RIGHT("right")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
		}
	},

	TOP("top")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
		}
	},

	BOTTOM("bottom")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception 
		{
		}
	},

	RALIGN("ralign")
	{
		@Override
		protected <T> void performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others, CheckingLayoutResult result) throws Exception
		{
		}
	}
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
	
	private String name;

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

}
