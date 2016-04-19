////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.newconfig;

import com.exactprosystems.jf.tool.Common;

import java.util.Optional;

public class TablePair
{
	private String key;
	private String value;
	private String tooltipSeparator = null;
	private boolean isRemovable = false;
	private boolean isPath = false;
	private boolean isEditable = true;
	private Common.Function fnc;

	public TablePair(String key, String value)
	{
		this.key = key;
		this.value = value;
	}

	public String getKey()
	{
		return key;
	}

	public String getValue()
	{
		return value;
	}

	public boolean isRemovable()
	{
		return isRemovable;
	}

	public void canRemove(boolean removable)
	{
		isRemovable = removable;
	}

	public boolean isPath()
	{
		return isPath;
	}

	public void remove()
	{
		Optional.ofNullable(this.fnc).ifPresent(f -> {
			try
			{
				f.call();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		});
	}

	public boolean isEditable()
	{
		return isEditable;
	}

	public String getTooltipSeparator()
	{
		return tooltipSeparator;
	}

	public static class TablePairBuilder
	{
		private boolean isRemovable = false;
		private boolean isPath = false;
		private String key;
		private String value;
		private String tooltipSeparator;
		private Common.Function fnc;
		private boolean isEditable = true;

		public static TablePairBuilder create()
		{
			return new TablePairBuilder();
		}

		public TablePairBuilder canRemove(boolean editable)
		{
			this.isRemovable = editable;
			return this;
		}

		public TablePairBuilder isPath(boolean path)
		{
			this.isPath = path;
			return this;
		}

		public TablePairBuilder key(String key)
		{
			this.key = key;
			return this;
		}

		public TablePairBuilder value(String value)
		{
			this.value = value;
			return this;
		}

		public TablePairBuilder removeEvent(Common.Function fnc)
		{
			this.fnc = fnc;
			return this;
		}

		public TablePairBuilder edit(boolean flag)
		{
			this.isEditable = flag;
			return this;
		}

		public TablePairBuilder tooltipSeparator(String s)
		{
			this.tooltipSeparator = s;
			return this;
		}

		public TablePair build()
		{
			TablePair pair = new TablePair(this.key, this.value);
			pair.isRemovable = this.isRemovable;
			pair.isPath = this.isPath;
			pair.fnc = this.fnc;
			pair.isEditable = this.isEditable;
			pair.tooltipSeparator = this.tooltipSeparator;
			return pair;
		}
	}
}
