/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CheckingLayoutResult implements Serializable
{
	private static final long	serialVersionUID	= 3562209509406788900L;

	private boolean ok = true;
	private List<String> errors = new ArrayList<>();
	private List<CheckingLayoutResultBean> beans = new ArrayList<>();

	public boolean isOk()
	{
		return this.ok;
	}
	
	public List<String> getErrors()
	{
		return this.errors;
	}

	public List<CheckingLayoutResultBean> getNewErrors()
	{
		return this.beans;
	}

	public void error(Piece piece, String string)
	{
		this.ok = false;
		this.errors.add("Processing part is [" + piece + "] " + string);
	}

	public void newError(Piece piece, String actual, String expected)
	{
		this.ok = false;
		this.beans.add(new CheckingLayoutResultBean(piece.name, piece.toString(), actual, expected));
	}

	public void set(boolean b)
	{
		if (!b)
		{
			this.ok = false;
		}
	}
}
