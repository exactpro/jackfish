package com.exactprosystems.jf.tool.dictionary.dialog;

import com.exactprosystems.jf.api.app.ControlKind;

public class ElementWizardBean
{
	int number;
	String id;
	ControlKind controlKind;
	boolean isXpath;
	boolean isNew;
	int count;

	public ElementWizardBean()
	{
	}

	public ElementWizardBean(int number, String id, ControlKind controlKind, boolean isXpath, boolean isNew, int count)
	{
		this.number = number;
		this.id = id;
		this.controlKind = controlKind;
		this.isXpath = isXpath;
		this.isNew = isNew;
		this.count = count;
	}

	public int getNumber()
	{
		return number;
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
		return isXpath;
	}

	public void setXpath(boolean xpath)
	{
		isXpath = xpath;
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
}
