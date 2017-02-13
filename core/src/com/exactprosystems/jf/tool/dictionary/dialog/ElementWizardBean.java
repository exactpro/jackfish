package com.exactprosystems.jf.tool.dictionary.dialog;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import javafx.scene.paint.Color;

public class ElementWizardBean
{
	private int number;
	private String id;
	private ControlKind controlKind;
	private boolean xpath;
	private boolean isNew;
	private int count;
	private ElementWizardBean option = this;

	private String styleClass;

	private AbstractControl abstractControl;

	public ElementWizardBean()
	{
	}

	public ElementWizardBean(AbstractControl abstractControl, int number, String id, ControlKind controlKind, boolean xpath, boolean isNew, int count)
	{
		this.abstractControl = abstractControl;
		this.number = number;
		this.id = id;
		this.controlKind = controlKind;
		this.xpath = xpath;
		this.isNew = isNew;
		this.count = count;
	}

	public int getNumber()
	{
		return number;
	}

	public void setNumber(int number)
	{
		this.number = number;
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

	public ElementWizardBean getOption()
	{
		return option;
	}

	public void setOption(ElementWizardBean option)
	{
		this.option = option;
	}

	public AbstractControl getAbstractControl()
	{
		return abstractControl;
	}

	public void setAbstractControl(AbstractControl abstractControl)
	{
		this.abstractControl = abstractControl;
	}

	public String getStyleClass()
	{
		return styleClass;
	}

	public void setStyleClass(String styleClass)
	{
		this.styleClass = styleClass;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ElementWizardBean that = (ElementWizardBean) o;

		return abstractControl != null ? abstractControl.equals(that.abstractControl) : that.abstractControl == null;
	}

	@Override
	public int hashCode()
	{
		return abstractControl != null ? abstractControl.hashCode() : 0;
	}
}
