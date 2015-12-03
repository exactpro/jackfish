////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.awt.Rectangle;
import java.util.List;

public enum PieceKind
{
	VISIBLE("visible")
	{
		@Override
		protected <T> boolean performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others) throws Exception 
		{
			return self.get(0) != null; // TODO 
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
		protected <T> boolean performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others) throws Exception 
		{
			return piece.range.func(self.size(), piece.a, piece.b);
		}
	},
	
	LEFT("left")
	{
		@Override
		protected <T> boolean performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others) throws Exception 
		{
			if (others == null)
			{
				return false;
			}
			
			Rectangle selfArea = executor.getRectangle(self.get(0));
			Rectangle otherArea = executor.getRectangle(others.get(0));
			long selfRight = selfArea.x + selfArea.width;
			long otherLeft = otherArea.x;
			
			return piece.range.func(selfRight - otherLeft, piece.a, piece.b);
		}
	},
	
	RIGHT("right")
	{
		@Override
		protected <T> boolean performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others) throws Exception 
		{
			return false;
		}
	},

	TOP("top")
	{
		@Override
		protected <T> boolean performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others) throws Exception 
		{
			return false;
		}
	},

	BOTTOM("bottom")
	{
		@Override
		protected <T> boolean performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others) throws Exception 
		{
			return false;
		}
	},

	RALIGN("ralign")
	{
		@Override
		protected <T> boolean performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others) throws Exception
		{
			return false;
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

	public <T> boolean perform(Piece piece, OperationExecutor<T> executor, List<T> self)  throws Exception
	{
		if (selfNeedOne() && self.size() != 1 )
		{
			return false;
		}
		
		List<T> others = executor.findAll(piece.owner, piece.locator);
		if (otherNeedOne() && others.size() != 1 ) 
		{
			return false;
		}
		
		return performDerived(piece, executor, self, others);
	}

	protected boolean selfNeedOne()
	{
		return true;
	}

	protected boolean otherNeedOne()
	{
		return true;
	}

	protected abstract <T> boolean performDerived(Piece piece, OperationExecutor<T> executor, List<T> self, List<T> others)  throws Exception;

}
