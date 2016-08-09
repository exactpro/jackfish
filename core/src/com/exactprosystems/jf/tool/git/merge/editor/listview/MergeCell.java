////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.merge.editor.listview;

public class MergeCell
{
	public enum MergeType
	{
		MOVE_TO_LEFT,
		MOVE_TO_RIGHT,
		WITHOUT_MOVE
	}

	private final String text;
	private MergeType mergeType;

	public MergeCell(String text, MergeType mergeType)
	{
		this.text = text;
		this.mergeType = mergeType;
	}

	public String getText()
	{
		return text;
	}

	public MergeType getMergeType()
	{
		return mergeType;
	}

	public void updateMergeType(MergeType mergeType)
	{
		this.mergeType = mergeType;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MergeCell mergeCell = (MergeCell) o;

		return text.equals(mergeCell.text);

	}

	@Override
	public int hashCode()
	{
		return text.hashCode();
	}
}
