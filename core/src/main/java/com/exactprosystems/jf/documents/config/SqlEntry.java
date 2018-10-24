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
 * A xml bean for any SqlEntry from a configuration
 *
 * @see Configuration
 * @see Configuration#getSqlEntries()
 * @see Entry
 */
@XmlAccessorType(XmlAccessType.NONE)
public class SqlEntry extends Entry
{
	@XmlElement(name = Configuration.sqlJar)
	protected String sqlJarNameValue;

	@XmlElement(name = Configuration.sqlConnection)
	protected String sqlConnectionStringValue;

	@Override
	protected String getDerived(String name)
	{
		switch (name)
		{
			case Configuration.sqlJar: 			return this.sqlJarNameValue;
			case Configuration.sqlConnection:	return this.sqlConnectionStringValue;
		}
		return null;
	}

	@Override
	protected void setDerived(String name, Object value)
	{
		switch (name)
		{
			case Configuration.sqlJar: 			this.sqlJarNameValue			= "" + value;	return;
			case Configuration.sqlConnection:	this.sqlConnectionStringValue	= "" + value;	return;
		}
	}
}