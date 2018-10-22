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

package com.exactprosystems.jf.documents.config;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.documents.matrix.parser.items.MutableArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A xml bean for any Entry from a configuration
 *
 * @see Configuration
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class Entry implements Mutable
{
	private boolean changed = false;

	@XmlElement(name = Configuration.entryName)
	protected String entryNameValue;

	@XmlElement(name = Configuration.parametersEntry)
	protected MutableArrayList<Parameter> parameters;

	@Override
	public String toString()
	{
		return Optional.ofNullable(this.entryNameValue).orElse("");
	}

	//region interface Mutable
	@Override
	public boolean isChanged()
	{
		if (this.changed)
		{
			return true;
		}
		if (this.parameters != null && this.parameters.isChanged())
		{
			return true;
		}

		return false;
	}

	@Override
	public void saved()
	{
		if (this.parameters != null)
		{
			this.parameters.saved();
		}
		this.changed = false;
	}
	//endregion

	//region public methods
	public final String get(String name)
	{
		if (Str.areEqual(name, Configuration.entryName))
		{
			return this.entryNameValue;
		}
		return this.getParameter(name).orElse(this.getDerived(name));
	}

	public final void set(String name, Object value)
	{
		if (Str.areEqual(name, Configuration.entryName))
		{
			this.entryNameValue = ""+value;
			return;
		}
		Parameter o = new Parameter();
		o.setKey(name);
		int index;
		if ((index = this.getParameters().indexOf(o)) != -1)
		{
			this.getParameters().get(index).setValue("" + value);
			return;
		}
		this.setDerived(name, value);
	}

	public List<Parameter> getParameters()
	{
		if (this.parameters == null)
		{
			this.parameters = new MutableArrayList<>();
		}
		return this.parameters;
	}
	//endregion

	//region abstract methods
	protected abstract String getDerived(String name);
	
	protected abstract void setDerived(String name, Object value);
	//endregion

	//region private methods
	private Optional<String> getParameter(String key)
	{
		return this.getParameters()
				.stream()
				.filter(p -> Objects.nonNull(p.key) && p.key.equals(key))
				.map(Parameter::getValue)
				.findFirst();
	}
	//endregion
}