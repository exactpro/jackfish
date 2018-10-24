/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.documents.guidic;

import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.guidic.controls.*;
import com.exactprosystems.jf.documents.matrix.parser.items.MutableArrayList;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


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

	@XmlTransient
	private Window.SectionKind sectionKind;

	@XmlTransient
	private IWindow window;

	public Section()
	{
		this.controls = new MutableArrayList<>();
	}

	/**
	 * Create section copy based on passed section
	 */
	public static Section createCopy(Section section) throws Exception
	{
		Section newSection = new Section();
		newSection.setSection(section.window, section.getSectionKind());
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

	//region interface ISection
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
            String refId  = ctrl.getRefID();
			String rowsId  = ctrl.getRowsId();
			String headerId = ctrl.getHeaderId();

			if (     ownerId != null && ownerId.equals(control.getID())
			        || refId != null && refId.equals(control.getID())
			        || headerId != null && headerId.equals(control.getID())
			        || rowsId != null && rowsId.equals(control.getID()))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void addControl(IControl control)
	{
		this.addControl(this.controls.size(), control);
	}

	@Override
	public void addControl(int index, IControl control)
	{
		if (control != null && control instanceof AbstractControl)
		{
			control.setSection(this);
			if(this.sectionKind == IWindow.SectionKind.Self && this.controls.isEmpty())
			{
				try
				{
					if(Str.IsNullOrEmpty(((AbstractControl) control).getID()))
					{
						((AbstractControl) control).set(AbstractControl.idName, "self");
					}
				}
				catch (Exception ignored)
				{}
			}
			this.controls.add(index, (AbstractControl)control);
		}
	}

	@Override
	public void setSection(IWindow window, Window.SectionKind sectionKind)
	{
	    this.window = window;
		this.sectionKind = sectionKind;
		this.controls.forEach(control -> control.setSection(this));
	}

	@Override
	public Collection<IControl> getControls()
	{
		return new ArrayList<>(this.controls);
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
	public IControl getControlById(String name)
	{
		if (name == null)
		{
			return null;
		}

		return this.controls.stream()
				.filter(control -> name.equals(control.getID()))
				.findFirst()
				.orElse(null);
	}

	@Override
	public IControl getControlByIdAndValue(String name, Object obj) 
	{
		IControl result = this.getControlById(name);
		if (result != null && result.getAddition() != null && result.getAddition() == Addition.SwitchByValue)
		{
			result = this.getControlById(name + obj);
		}
		return result;
	}

	@Override
	public List<String>  getControlsNames()
	{
		return this.controls.stream()
				.map(IControl::getID)
				.filter(id -> !Str.IsNullOrEmpty(id))
				.collect(Collectors.toList());
	}

	@Override
	public Window.SectionKind getSectionKind()
	{
		return this.sectionKind;
	}

	@Override
	public IWindow getWindow()
	{
		return this.window;
	}
	//endregion

    //region interface Mutable
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
    //endregion

	/**
	 * Evaluate all controls (only expression fields) from the section
	 * @param evaluator evaluator for evaluating
	 *
	 * @see AbstractControl#evaluate(AbstractEvaluator)
	 */
	public void evaluateAll(AbstractEvaluator evaluator)
	{
		this.controls.forEach(c -> c.evaluate(evaluator));
	}

	/**
	 * Remove all controls from the section
	 */
	public void clearSection()
	{
		this.controls.clear();
	}

	/**
	 * @return index of the passed control
	 */
	public int indexOf(AbstractControl control)
	{
		return this.controls.indexOf(control);
	}

	/**
	 * @return AbstractControl by index from the section
	 */
	public AbstractControl getByIndex(int index)
	{
		return this.controls.get(index);
	}

	public void setControl(int position, AbstractControl control)
	{
		control.setSection(this);
		this.controls.set(position, control);
	}

	/**
	 * Remove the passed control from the section
	 * @return true, if removing was successful. Otherwise return false
	 */
	public boolean removeControl(IControl control)
	{
		return this.controls.remove(control);
	}

	/**
	 * Replace the old control to the new control
	 * @param control the old control, which will replacing
	 * @param newControl the new control, which will inserting instead the old control
	 * @return index of inserting the new control
	 *
	 * @see IControl
	 */
	public int replaceControl(IControl control, IControl newControl)
	{
		Iterator<AbstractControl> iter = this.controls.iterator();
		int index = 0;
		while(iter.hasNext())
		{
			AbstractControl next = iter.next();
			if (next.equals(control))
			{
				iter.remove();
				this.addControl(index, newControl);
				return index;
			}
			index++;
		}
		return -1;
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + " size=" + this.controls.size();
	}
}
