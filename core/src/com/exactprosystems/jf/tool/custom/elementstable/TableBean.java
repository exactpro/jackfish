////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.elementstable;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import org.w3c.dom.Node;

public class TableBean
{
	private String      id;
	private boolean     isNew;
	private int         count;
	private TableBean option = this;
	private AbstractControl abstractControl;
	private Node node;
	private String style;

	public TableBean(AbstractControl abstractControl)
	{
		this.abstractControl = abstractControl;
		this.id = this.abstractControl.getID();
		this.isNew = false;
		this.count = 0;
	}

	public Node getNode()
	{
		return node;
	}

	public void setNode(Node node)
	{
		this.node = node;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public ControlKind getControlKind()
	{
		return this.abstractControl.getBindedClass();
	}

	public void setControlKind(ControlKind controlKind)
	{
		//TODO

	}

	public boolean getXpath()
	{
		return !Str.IsNullOrEmpty(this.abstractControl.getXpath());
	}

	public boolean getIsNew()
	{
		return isNew;
	}

	public void setIsNew(boolean aNew)
	{
		isNew = aNew;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public AbstractControl getAbstractControl()
	{
		return abstractControl;
	}

	public void setAbstractControl(AbstractControl abstractControl)
	{
		this.abstractControl = abstractControl;
	}

	public String getStyle()
	{
		return style;
	}

	public void setStyle(String style)
	{
		this.style = style;
	}

	public TableBean getOption()
	{
		return option;
	}

	public void setOption(TableBean option)
	{
		this.option = option;
	}
}
