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
 * A xml bean for any AppEntry from a configuration
 *
 * @see Configuration
 * @see Configuration#getAppEntries()
 * @see Entry
 */
@XmlAccessorType(XmlAccessType.NONE)
public class AppEntry extends Entry
{
	@XmlElement(name = Configuration.appDescription)
	protected String descriptionValue;
	
	@XmlElement(name = Configuration.appDicPath)
	protected String appDicPathValue;
	
	@XmlElement(name = Configuration.appJar)
	protected String appJarNameValue;

	@XmlElement(name = Configuration.appWorkDir)
	protected String appWorkDirValue;
	
	@XmlElement(name = Configuration.appStartPort)
	protected String appStartPortValue;

	@Override
	protected String getDerived(String name)
	{
		switch (name)
		{
			case Configuration.appDescription: 	return this.descriptionValue;
			case Configuration.appDicPath: 		return this.appDicPathValue;
			case Configuration.appJar: 			return this.appJarNameValue;
			case Configuration.appWorkDir: 		return this.appWorkDirValue;
			case Configuration.appStartPort:	return this.appStartPortValue;
			default: return null;
		}
	}

	@Override
	protected void setDerived(String name, Object value)
	{
		switch (name)
		{
			case Configuration.appDescription: 	this.descriptionValue	= "" + value;	return;
			case Configuration.appDicPath: 		this.appDicPathValue	= "" + value;	return;
			case Configuration.appJar: 			this.appJarNameValue	= "" + value;	return;
			case Configuration.appWorkDir: 		this.appWorkDirValue	= "" + value;	return;
			case Configuration.appStartPort:	this.appStartPortValue	= "" + value;	return;
			default: return;
		}
	}
}