////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.guidic;

import com.exactprosystems.jf.api.app.Addition;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.ISection;
import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.guidic.controls.*;
import com.exactprosystems.jf.documents.matrix.parser.items.MutableArrayList;

import javax.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


@XmlRootElement(name = "section")
@XmlAccessorType(XmlAccessType.NONE)
public class Section implements ISection, Mutable
{
	@XmlElements({
		@XmlElement(name="any", 			type=Any.class),
		@XmlElement(name="button", 			type=Button.class),
		@XmlElement(name="checkBox", 		type=CheckBox.class),
		@XmlElement(name="comboBox", 		type=ComboBox.class),
		@XmlElement(name="dialog", 			type=Dialog.class),
		@XmlElement(name="frame", 			type=Frame.class),
		@XmlElement(name="label", 			type=Label.class),
		@XmlElement(name="listView",		type=ListView.class),
		@XmlElement(name="menu", 			type=Menu.class),
		@XmlElement(name="menuItem",		type=MenuItem.class),
		@XmlElement(name="panel", 			type=Panel.class),
		@XmlElement(name="radioButton", 	type=RadioButton.class),
		@XmlElement(name="row", 			type=Row.class),
		@XmlElement(name="table", 			type=Table.class),
		@XmlElement(name="tabPanel",		type=TabPanel.class),
		@XmlElement(name="textBox", 		type=TextBox.class),
		@XmlElement(name="toggleButton",	type=ToggleButton.class),
		@XmlElement(name="toolTip", 		type=ToolTip.class),
		@XmlElement(name="tree", 			type=Tree.class),
		@XmlElement(name="treeItem", 		type=TreeItem.class),
		@XmlElement(name="wait", 			type=Wait.class),
		@XmlElement(name="image",			type=Image.class),
		@XmlElement(name="splitter",		type=Splitter.class),
		@XmlElement(name="spinner",			type=Spinner.class),
		@XmlElement(name="progressBar",		type=ProgressBar.class),
		@XmlElement(name="scrollBar",		type=ScrollBar.class),
		@XmlElement(name="slider",			type=Slider.class),
	})
	private MutableArrayList<AbstractControl> controls;
	
	public Section()
	{
		this.controls = new MutableArrayList<>();
	}

	public static Section createCopy(Section section) throws Exception
	{
		Section newSection = new Section();
		newSection.setSection(section.getSectionKind());
		newSection.controls = new MutableArrayList<>();
		for (AbstractControl control : section.controls)
		{
			AbstractControl copy = AbstractControl.createCopy(control);
			if (copy != null)
			{
				copy.setSection(newSection);
				newSection.controls.add(copy);
			}
		}
		return newSection;
	}

	// TODO need to add throwing exceptions when we can't find elements
	//------------------------------------------------------------------------------------------------------------------
    // interface ISection
    //------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean hasReferences(IControl control)
	{
		for (AbstractControl ctrl : this.controls)
		{
			if (control == ctrl)
			{
				continue;
			}
			
			String ownerId = ctrl.getOwnerID();
			String rowsId  = ctrl.getRowsId();
			
			if (ownerId != null && ownerId.equals(control.getID()) || rowsId != null && rowsId.equals(control.getID()))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void addControl(IControl control) throws Exception
	{
		addControl(this.controls.size(), control);
	}

	@Override
	public void addControl(int index, IControl control) throws Exception
	{
		if (control != null && control instanceof AbstractControl)
		{
			control.setSection(this);
			this.controls.add(index, (AbstractControl)control);
		}
	}

	@Override
	public void setSection(Window.SectionKind sectionKind)
	{
		this.sectionKind = sectionKind;
		for (AbstractControl control : this.controls)
		{
			control.setSection(this);
		}
	}

	@Override
	public Collection<IControl> getControls()
	{
		return new ArrayList<IControl>(this.controls);
	}

	@Override
	public IControl getFirstControl()
	{
		if (this.controls.isEmpty())
		{
			return null;
		}
		return this.controls.get(0);
	}

	@Override
	public IControl getControlById(String name) throws Exception
	{
		if (name == null)
		{
			return null;
		}
		
		for (IControl control : this.controls)
		{
			if (name.equals(control.getID()))
			{
				return control;
			}
		}
		
		return null;
	}

	@Override
	public IControl getControlByIdAndValue(String name, Object obj) throws Exception
	{
		IControl result = getControlById(name);
		if (result.getAddition() != null && result.getAddition() == Addition.SwitchByValue)
		{
			result = getControlById(name+obj);
		}
		return result;
	}

	@Override
	public List<String>  getControlsNames()
	{
		List<String> ret = new ArrayList<String>();
		
		for (IControl control : this.controls)
		{
			String name = control.getID();
			if (name != null && !name.isEmpty())
			{
				ret.add(control.getID());
			}
		}
		
		return ret;
	}

	@Override
	public Window.SectionKind getSectionKind()
	{
		return this.sectionKind;
	}
	
	
    //------------------------------------------------------------------------------------------------------------------
    // interface Mutable
    //------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean isChanged()
	{
		return this.controls.isChanged();
	}
	
	@Override
	public void saved()
	{
		this.controls.saved();
	}
    //------------------------------------------------------------------------------------------------------------------
	
	public void evaluateAll(AbstractEvaluator evaluator)
	{
		for (AbstractControl control : this.controls)
		{
			control.evaluate(evaluator);
		}
	}
	
	
	public int indexOf(AbstractControl control)
	{
		return this.controls.indexOf(control);
	}
	
	public void setControl(int position, AbstractControl control)
	{
		control.setSection(this);
		this.controls.set(position, control);
	}
	
	public boolean removeControl(IControl control)
	{
		return this.controls.remove(control);
	}
	
	public void replaceControl(IControl control, IControl newControl) throws Exception
	{
		Iterator<AbstractControl> iter = this.controls.iterator();
		int index = 0;
		while(iter.hasNext())
		{
			AbstractControl next = iter.next();
			if (next.equals(control))
			{
				iter.remove();
				addControl(index, newControl);
				return;
			}
			index++;
		}
	}

	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " size=" + this.controls.size();
	}

	private Window.SectionKind sectionKind;



}
