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

package com.exactprosystems.jf.documents.msgdic;

import com.exactprosystems.jf.api.client.IAttribute;
import com.exactprosystems.jf.api.client.IType;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Attribute", propOrder = {"value"})
public class Attribute implements IAttribute
{
	@XmlAttribute(name = "name", required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlSchemaType(name = "NMTOKEN")
	protected String name;

	@XmlValue
	protected String value;

	@XmlAttribute(name = "type")
	protected JavaType type;

	public Attribute()
	{
	}

	@Override
	public String toString()
	{
		return this.name + "=" + this.value;
	}

	//region interface IAttribute
	@Override
	public String getValue()
	{
		return this.value;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

	//endregion

}
