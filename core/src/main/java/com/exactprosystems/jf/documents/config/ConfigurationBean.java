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
import com.exactprosystems.jf.common.MutableString;
import com.exactprosystems.jf.documents.matrix.parser.items.MutableArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A xml bean for configuration
 * 
 * @see Configuration
 */
@XmlRootElement(name="configuration")
@XmlAccessorType(XmlAccessType.NONE)
public class ConfigurationBean implements Mutable
{
	static final Class<?>[] jaxbContextClasses =
		{
			ConfigurationBean.class,
			SqlEntry.class,
			ClientEntry.class,
			ServiceEntry.class,
			AppEntry.class,
			GlobalHandler.class,
		};

	
	@XmlElement(name = Configuration.time)
	protected MutableString timeValue;

	@XmlElement(name = Configuration.date)
	protected MutableString dateValue;

	@XmlElement(name = Configuration.dateTime)
	protected MutableString dateTimeValue;

	@XmlElement(name = Configuration.formats)
	protected MutableArrayList<MutableString> formatsValue;

	@XmlElement(name = Configuration.reports)
	protected MutableString reportsValue;

	@XmlElement(name = Configuration.version)
	protected MutableString versionValue;

	@XmlElement(name = Configuration.imports)
	protected MutableArrayList<MutableString> importsValue;

	@XmlElement(name = Configuration.vars)
	protected MutableString varsValue;

	@XmlElement(name = Configuration.userVars)
	protected MutableArrayList<MutableString> userVarsValue;

	@XmlElement(name = Configuration.matrix)
	protected MutableArrayList<MutableString> matricesValue;

	@XmlElement(name = Configuration.appDict)
	protected MutableArrayList<MutableString> appDictionariesValue;

	@XmlElement(name = Configuration.clientDict)
	protected MutableArrayList<MutableString> clientDictionariesValue;

	@XmlElement(name = Configuration.library)
	protected MutableArrayList<MutableString> librariesValue;

	@XmlElement(name = Configuration.globalHandler)
	public GlobalHandler globalHandlerValue;

	@XmlElement(name = Configuration.sqlEntry)
	public MutableArrayList<SqlEntry> sqlEntriesValue;

	@XmlElement(name = Configuration.clientEntry)
	public MutableArrayList<ClientEntry> clientEntriesValue;

	@XmlElement(name = Configuration.serviceEntry)
	public MutableArrayList<ServiceEntry> serviceEntriesValue;

	@XmlElement(name = Configuration.appEntry)
	public MutableArrayList<AppEntry> appEntriesValue;

	public ConfigurationBean()
	{
		this.timeValue				= new MutableString();
		this.dateValue				= new MutableString();
		this.dateTimeValue			= new MutableString();
		this.formatsValue			= new MutableArrayList<>();
		this.reportsValue			= new MutableString();
		this.versionValue			= new MutableString();

		this.globalHandlerValue		 = new GlobalHandler();

		this.sqlEntriesValue		= new MutableArrayList<>();
		this.clientEntriesValue		= new MutableArrayList<>();
		this.serviceEntriesValue	= new MutableArrayList<>();
		this.appEntriesValue		= new MutableArrayList<>();
		this.importsValue			= new MutableArrayList<>();
		this.varsValue				= new MutableString();
		this.userVarsValue			= new MutableArrayList<>();
		this.matricesValue			= new MutableArrayList<>();
		this.appDictionariesValue	= new MutableArrayList<>();
		this.clientDictionariesValue= new MutableArrayList<>();
		this.librariesValue			= new MutableArrayList<>();
	}

	@Override
	public boolean isChanged()
	{
		return this.timeValue.isChanged()
				|| this.dateValue.isChanged()
				|| this.dateTimeValue.isChanged()
				|| this.formatsValue.isChanged()
				|| this.versionValue.isChanged()
				|| this.reportsValue.isChanged()
				|| this.sqlEntriesValue.isChanged()
				|| this.clientEntriesValue.isChanged()
				|| this.serviceEntriesValue.isChanged()
				|| this.appEntriesValue.isChanged()
				|| this.importsValue.isChanged()
				|| this.varsValue.isChanged()
				|| this.userVarsValue.isChanged()
				|| this.matricesValue.isChanged()
				|| this.appDictionariesValue.isChanged()
				|| this.clientDictionariesValue.isChanged()
				|| this.librariesValue.isChanged();
	}


	@Override
	public void saved()
	{
		this.timeValue.saved();
		this.dateValue.saved();
		this.dateTimeValue.saved();
		this.formatsValue.saved();
		this.versionValue.saved();
		this.reportsValue.saved();
		this.appEntriesValue.saved();
		this.clientEntriesValue.saved();
		this.serviceEntriesValue.saved();
		this.sqlEntriesValue.saved();
		this.importsValue.saved();
		this.varsValue.saved();
		this.userVarsValue.saved();
		this.matricesValue.saved();
		this.appDictionariesValue.saved();
		this.clientDictionariesValue.saved();
		this.librariesValue.saved();
	}
	
}
