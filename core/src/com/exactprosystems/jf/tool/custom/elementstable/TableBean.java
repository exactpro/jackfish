package com.exactprosystems.jf.tool.custom.elementstable;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;

public class TableBean
{
	private String      id;
	private ControlKind controlKind;
	private boolean     xpath;
	private boolean     isNew;
	private int         count;
	private TableBean option = this;
	private AbstractControl abstractControl;

	public TableBean()
	{
	}

	public TableBean(AbstractControl abstractControl)
	{
		this.abstractControl = abstractControl;
		this.id = this.abstractControl.getID();
		this.controlKind = this.abstractControl.getBindedClass();
		this.xpath = !Str.IsNullOrEmpty(this.abstractControl.getXpath());
		this.isNew = false;
		this.count = 0;
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
		return controlKind;
	}

	public void setControlKind(ControlKind controlKind)
	{
		this.controlKind = controlKind;
	}

	public boolean isXpath()
	{
		return xpath;
	}

	public void setXpath(boolean xpath)
	{
		this.xpath = xpath;
	}

	public boolean isNew()
	{
		return isNew;
	}

	public void setNew(boolean aNew)
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

	public TableBean getOption()
	{
		return option;
	}

	public void setOption(TableBean option)
	{
		this.option = option;
	}
}
