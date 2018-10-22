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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A xml bean for any ClientEntry from a configuration
 *
 * @see Configuration
 * @see Configuration#getClientEntries()
 * @see Entry
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ClientEntry extends Entry
{
	@XmlElement(name = Configuration.clientDescription)
	protected String descriptionValue;
	
	@XmlElement(name = Configuration.clientJar)
	protected String clientJarNameValue;

	@XmlElement(name = Configuration.clientLimit)
	protected int clientLimitValue;

	@XmlElement(name = Configuration.clientDictionary)
	protected String clientDictionaryValue;

	@Override
	protected String getDerived(String name)
	{
		switch (name)
		{
			case Configuration.clientDescription:	return this.descriptionValue;
			case Configuration.clientJar:			return this.clientJarNameValue;
			case Configuration.clientLimit:			return Integer.toString(this.clientLimitValue);
			case Configuration.clientDictionary:	return this.clientDictionaryValue;
			default: return null;
		}
	}

	@Override
	protected void setDerived(String name, Object value)
	{
		switch (name)
		{
			case Configuration.clientDescription:	this.descriptionValue		= "" + value;	return;
			case Configuration.clientJar:			this.clientJarNameValue		= "" + value;	return;
			case Configuration.clientLimit:			this.clientLimitValue		= Integer.parseInt("" +value);	return;
			case Configuration.clientDictionary:	this.clientDictionaryValue	= "" + value;	return;
			default: return;
		}
	}
}