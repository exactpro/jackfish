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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import org.apache.log4j.Logger;

import javax.xml.bind.annotation.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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

	protected static final Logger logger = Logger.getLogger(Window.class);
	private Map<SectionKind, Section> allSections;
	private boolean changed = false;

	public Window()
	{
		this.self = new Section();
		this.onOpen = new Section();
		this.run = new Section();
		this.onClose = new Section();
		this.close = new Section();

		this.allSections = new EnumMap<>(SectionKind.class);
	}

	public Window(String name)
	{
		this();
		
		this.name = name;
	}

	/**
	 * Create the copy based on passed window
	 * @param window the window, which used for create copy
	 * @return the new window, based on the passed window. If the passed window is {@code null} will returned {@code null}
	 * @throws Exception something went wrong
	 */
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
		newWindow.allSections = new EnumMap<>(SectionKind.class);
		newWindow.correctAll();
		newWindow.changed = false;
		return newWindow;
	}
	
	public void correctAll()
	{
		this.correctOne(SectionKind.Self, this.self);
		this.correctOne(SectionKind.OnOpen, this.onOpen);
		this.correctOne(SectionKind.Run, this.run);
		this.correctOne(SectionKind.OnClose, this.onClose);
		this.correctOne(SectionKind.Close, this.close);
	}

	public void evaluateAll(AbstractEvaluator evaluator)
	{
		this.allSections.values().forEach(section -> section.evaluateAll(evaluator));
	}

	//region interface Mutable
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
		this.allSections.values().forEach(Section::saved);
	}

	//endregion

	//region interface IWindow
	@Override
	public void setName(String name)
	{
		this.changed = true;
		this.name = name;
	}

	@Override
	public String getName()
	{
		return this.name;
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

		return this.allSections.values()
				.stream()
				.anyMatch(section -> section.hasReferences(control));
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
		Section section = this.allSections.computeIfAbsent(kind, sectionKind -> new Section());
		section.addControl(control);
		section.setSection(this, kind);
	}

	@Override
	public Collection<IControl> getControls(SectionKind kind)
	{
		if (kind == null)
		{
			return this.allSections.values()
					.stream()
					.map(ISection::getControls)
					.flatMap(Collection::stream)
					.collect(Collectors.toList());
		}
		else
		{
			return Optional.ofNullable(this.allSections.get(kind))
					.map(Section::getControls)
					.orElseGet(ArrayList::new);
		}
	}

	@Override
	public IControl getFirstControl(SectionKind kind)
	{
		if (kind == null)
		{
			kind = SectionKind.Run;
		}
		return Optional.ofNullable(this.allSections.get(kind))
				.map(ISection::getFirstControl)
				.orElse(null);
	}

	@Override
	public IControl getControlForName(SectionKind kind, String name)
	{
		if (kind == null)
		{
			return this.allSections.values()
					.stream()
					.map(section -> section.getControlById(name))
					.filter(Objects::nonNull)
					.findFirst()
					.orElse(null);
		}
		else
		{
			return Optional.ofNullable(this.allSections.get(kind))
					.map(section -> section.getControlById(name))
					.orElse(null);
		}
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

				if (Str.areEqual(controlFieldName, fieldName))
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

		if (!errors.isEmpty())
		{
			throw new Exception(R.WINDOW_CHECK_PARAMS_EXCEPTION.get() + Arrays.toString(errors.toArray()));
		}
	}

	@Override
	public boolean containsControl(String controlName)
	{
		return this.allSections.values()
				.stream()
				.map(section -> section.getControlById(name))
				.anyMatch(Objects::nonNull);
	}

	@Override
	public IControl getSelfControl()
	{
		return Optional.ofNullable(this.self)
				.map(selfSection -> selfSection.getControls().iterator())
				.filter(Iterator::hasNext)
				.map(Iterator::next)
				.orElse(null);
	}

	@Override
	public IControl getOwnerControl(IControl control)
	{
		if (control != null && control.getOwnerID() != null && !control.getOwnerID().isEmpty())
		{
			return this.getControlForName(null, control.getOwnerID());
		}

		return null;
	}

	@Override
	public IControl getReferenceControl(IControl control)
	{
		if (control != null && control.getRefID() != null && !control.getRefID().isEmpty())
		{
			return this.getControlForName(null, control.getRefID());
		}

		return null;
	}

	@Override
	public IControl getRowsControl(IControl control)
	{
		if (control != null && control.getRowsId() != null && !control.getRowsId().isEmpty())
		{
			return this.getControlForName(null, control.getRowsId());
		}

		return null;
	}

	@Override
	public IControl getHeaderControl(IControl control)
	{
		if (control != null && control.getHeaderId() != null && !control.getHeaderId().isEmpty())
		{
			return this.getControlForName(null, control.getHeaderId());
		}

		return null;
	}

	@Override
	public List<IControl> allMatched(BiFunction<ISection, IControl, Boolean> predicate)
	{
		//TODO think about, why we use bi predicate
		List<IControl> res = new ArrayList<>();
		this.allSections.values().forEach(s -> s.getControls().forEach(c -> {
			if (predicate.apply(s, c))
			{
				res.add(c);
			}
		}));
		return res;
	}
	//endregion

	@Override
	public String toString()
	{
		return this.name;
	}

	//region private methods
	private void correctOne(SectionKind kind, Section section)
	{
		this.allSections.put(kind, section);
		section.setSection(this, kind);
	}
	//endregion
}
