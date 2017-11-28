////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.guidic;

import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.ISection;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import org.apache.log4j.Logger;

import javax.xml.bind.annotation.*;
import java.util.*;
import java.util.function.BiFunction;

@XmlRootElement(name = "window")
@XmlAccessorType(XmlAccessType.NONE)
public class Window implements IWindow, Mutable
{
	@XmlAttribute
	private String name;

	@XmlElement(name = "self")
	private Section self;

	@XmlElement(name = "onOpen")
	private Section onOpen;

	@XmlElement(name = "run")
	private Section run;

	@XmlElement(name = "onClose")
	private Section onClose;

	@XmlElement(name = "close")
	private Section close;
	
	private Map<SectionKind, Section> allSections;
	
	public Window()
	{
		this.self 		= new Section();
		this.onOpen 	= new Section();
		this.run 		= new Section();
		this.onClose 	= new Section();
		this.close		= new Section();

		this.allSections = new LinkedHashMap<>();
	}

	public Window(String name)
	{
		this();
		
		this.name = name;
	}

	public static Window createCopy(Window window) throws Exception
	{
		if (window == null)
		{
			return null;
		}
		
		Window newWindow = new Window();
		newWindow.name = window.getName();
		newWindow.self = Section.createCopy(window.self);
		newWindow.onOpen = Section.createCopy(window.onOpen);
		newWindow.run = Section.createCopy(window.run);
		newWindow.onClose = Section.createCopy(window.onClose);
		newWindow.close = Section.createCopy(window.close);
		newWindow.allSections = new LinkedHashMap<>();
		newWindow.correctAll();
		newWindow.changed = false;
		return newWindow;
	}
	
	public void correctAll()
	{
		correctOne(SectionKind.Self, 		this.self);
		correctOne(SectionKind.OnOpen, 		this.onOpen);
		correctOne(SectionKind.Run, 		this.run);
		correctOne(SectionKind.OnClose, this.onClose);
		correctOne(SectionKind.Close,		this.close);
	}
	
	private void correctOne(SectionKind kind, Section section)
	{
		this.allSections.put(kind, section);
		section.setSection(this, kind);
	}

	@Override
	public String toString()
	{
		return this.name;
	}

    //------------------------------------------------------------------------------------------------------------------
    // interface Mutable
    //------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean isChanged()
	{
		if (this.changed)
		{
			return true;
		}
		for (Section section : this.allSections.values())
		{
			if (section.isChanged())
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void saved()
	{
		this.changed = false;
		for(Section control : this.allSections.values())
		{
			control.saved();
		}
	}
	
    //------------------------------------------------------------------------------------------------------------------
    // interface IWindow
    //------------------------------------------------------------------------------------------------------------------
	@Override
	public void setName(String name)
	{
		this.changed = true;
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
    public ISection getSection(SectionKind section) 
    {
    	return this.allSections.get(section);
    }

	@Override
	public boolean hasReferences(IControl control)
	{
		if (control == null || control.getID() == null || control.getID().isEmpty())
		{
			return false;
		}
		
		for (Section section : this.allSections.values())
		{
			if (section.hasReferences(control))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void removeControl(IControl control)
	{
		for (Section section : this.allSections.values())
		{
			if (section.removeControl(control))
			{
				break;
			}
		}
	}

	@Override
	public void addControl(SectionKind kind, IControl control)
	{
		Section section = this.allSections.get(kind);
		if (section == null)
		{
			section = new Section();
			this.allSections.put(kind, section);
		}
		section.addControl(control);
		section.setSection(this, kind);
	}

	@Override
    public Collection<IControl> getControls(SectionKind kind)
    {
        List<IControl> result = new ArrayList<>();
		if (kind == null)
		{
	        for(ISection section : this.allSections.values())
	        {
	        	result.addAll(section.getControls());
	        }
		}
		else
		{
			Section section = this.allSections.get(kind); 
			if (section != null)
			{
	        	result.addAll(section.getControls());
			}
		}
        return result;
    }
    
	@Override
	public IControl getFirstControl(SectionKind kind)
	{
		if (kind == null)
		{
			kind = SectionKind.Run;
		}
		Section section = this.allSections.get(kind); 
		if (section != null)
		{
			return section.getFirstControl();
		}
		return null;
	}

	@Override
	public IControl getControlForName(SectionKind kind, String name)
	{
		if (kind == null)
		{
			for (ISection section : this.allSections.values())
			{
				IControl control = section.getControlById(name);
				if (control != null)
				{
					return control;
				}
			}
		}
		else
		{
			Section section = this.allSections.get(kind); 
			if (section != null)
			{
				return section.getControlById(name);
			}
		}
		
		return null;
	}
	
	@Override
	public void checkParams(Collection<String> params) throws Exception
	{
		List<String> errors = new ArrayList<>();
		for (String fieldName : params)
		{
			boolean found = false;
			//TODO think about it
			if (fieldName.isEmpty() || fieldName.contains("dummy_") || fieldName.contains("Dummy_"))
			{
				continue;
			}
			for (IControl control : this.run.getControls())
			{
				String controlFieldName = control.getID(); 
				
				if (controlFieldName != null && controlFieldName.equals(fieldName))
				{
					found = true;
					break;
				}
			}
			if (!found)
			{
				errors.add(fieldName);
			}
		}
		
		if (errors.size() >  0)
		{
			throw new Exception("The following fields are missing in the dictionary: " + Arrays.toString(errors.toArray()));
		}
	}

	@Override
	public boolean containsControl(String controlName)
	{
		for (ISection section : this.allSections.values())
		{
			IControl control = section.getControlById(controlName);
			if (control != null)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public IControl getSelfControl()
	{
		if (this.self != null)
		{
			Iterator<IControl> iterator = this.self.getControls().iterator();
			if (iterator.hasNext())
			{
				return iterator.next();
			}
		}
		return null;
	}
	
	@Override
	public IControl getOwnerControl(IControl control)
	{
		if (control != null && control.getOwnerID() != null && !control.getOwnerID().isEmpty())
		{
			return getControlForName(null, control.getOwnerID());
		}
		
		return null;
	}

	@Override
	public IControl getReferenceControl(IControl control)
	{
		if (control != null && control.getRefID() != null && !control.getRefID().isEmpty())
		{
			return getControlForName(null, control.getRefID());
		}

		return null;
	}

	@Override
	public IControl getRowsControl(IControl control)
	{
		if (control != null && control.getRowsId() != null && !control.getRowsId().isEmpty())
		{
			return getControlForName(null, control.getRowsId());
		}

		return null;
	}

	@Override
	public IControl getHeaderControl(IControl control)
	{
		if (control != null && control.getHeaderId() != null && !control.getHeaderId().isEmpty())
		{
			return getControlForName(null, control.getHeaderId());
		}

		return null;
	}

    @Override
    public List<IControl> allMatched(BiFunction<ISection, IControl, Boolean> predicate)
    {
        List<IControl> res = new ArrayList<>();
        this.allSections.values().forEach(s -> s.getControls().forEach(c -> 
        { 
            if (predicate.apply(s, c))
            { 
                res.add(c); 
            } 
        } ));
        return res;
    }


	//------------------------------------------------------------------------------------------------------------------
	public void evaluateAll(AbstractEvaluator evaluator)
	{
		for (Section section : this.allSections.values())
		{
			section.evaluateAll(evaluator);
		}
	}

	
	protected static final Logger logger = Logger.getLogger(Window.class);
	private boolean changed = false;

	private boolean compareSections(Section s1, Section s2)
	{
		return s1.getControls().removeAll(s2.getControls());
	}

}
