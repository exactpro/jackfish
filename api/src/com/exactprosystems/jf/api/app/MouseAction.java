////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.io.Serializable;

public enum MouseAction  implements Serializable
{
	Move(0),
	LeftClick(1),
	RightClick(2),
	LeftDoubleClick(3),
	RightDoubleClick(4),
	DragNDrop(5),
	Press(6),
	Drop(7),
	Focus(8),
	Enter(9),
	Activated(10);
	
	private MouseAction(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	private int id;

	private static final long	serialVersionUID	= -3586047076930611493L;
}
