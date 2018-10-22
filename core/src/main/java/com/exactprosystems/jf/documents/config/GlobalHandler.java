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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.documents.config.Configuration.*;

/**
 * A xml bean for GlobalHandler from a configuration
 *
 * @see Configuration
 */
@XmlAccessorType(XmlAccessType.NONE)
public class GlobalHandler implements Mutable
{
	private boolean changed = false;

	@XmlElement(name = onTestCaseStart)
	protected MutableString onTestCaseStartValue;

	@XmlElement(name = onTestCaseFinish)
	protected MutableString onTestCaseFinishValue;

	@XmlElement(name = onTestCaseError)
	protected MutableString onTestCaseErrorValue;

	@XmlElement(name = onStepStart)
	protected MutableString onStepStartValue;

	@XmlElement(name = onStepFinish)
	protected MutableString onStepFinishValue;

	@XmlElement(name = onStepError)
	protected MutableString onStepErrorValue;

	@XmlAttribute(name = globalHandlerEnable)
	protected Boolean enabled;

	public GlobalHandler()
	{
		this.onTestCaseStartValue = new MutableString();
		this.onTestCaseFinishValue = new MutableString();
		this.onTestCaseErrorValue = new MutableString();
		this.onStepStartValue = new MutableString();
		this.onStepFinishValue = new MutableString();
		this.onStepErrorValue = new MutableString();
		this.enabled = Boolean.TRUE;
	}

	@Override
	public boolean isChanged()
	{
		return this.changed
				|| this.onTestCaseStartValue.isChanged()
				|| this.onTestCaseFinishValue.isChanged()
				|| this.onTestCaseErrorValue.isChanged()
				|| this.onStepStartValue.isChanged()
				|| this.onStepFinishValue.isChanged()
				|| this.onStepErrorValue.isChanged();
	}

	@Override
	public void saved()
	{
		this.changed = false;
		this.onTestCaseStartValue.saved();
		this.onTestCaseFinishValue.saved();
		this.onTestCaseErrorValue.saved();
		this.onStepStartValue.saved();
		this.onStepFinishValue.saved();
		this.onStepErrorValue.saved();
	}

	public void setValue(GlobalHandler handler)
	{
		this.onTestCaseStartValue.set(handler.onTestCaseStartValue);
		this.onTestCaseFinishValue.set(handler.onTestCaseFinishValue);
		this.onTestCaseErrorValue.set(handler.onTestCaseErrorValue);
		this.onStepStartValue.set(handler.onStepStartValue);
		this.onStepFinishValue.set(handler.onStepFinishValue);
		this.onStepErrorValue.set(handler.onStepErrorValue);
		
		this.enabled = handler.enabled;
	}

	public void setHandler(HandlerKind kind, String newValue)
	{
		this.changed = true;
		switch (kind)
		{
			case OnTestCaseStart: 	this.onTestCaseStartValue.set(newValue);break;
			case OnTestCaseFinish:	this.onTestCaseFinishValue.set(newValue);break;
			case OnTestCaseError:	this.onTestCaseErrorValue.set(newValue);break;
			case OnStepStart:		this.onStepStartValue.set(newValue);break;
			case OnStepFinish:		this.onStepFinishValue.set(newValue);break;
			case OnStepError:		this.onStepErrorValue.set(newValue);break;
		}
	}

	public MutableString getGlobalHandler(HandlerKind kind)
	{
		switch (kind)
		{
			case OnTestCaseStart: 	return this.onTestCaseStartValue;
			case OnTestCaseFinish:	return this.onTestCaseFinishValue;
			case OnTestCaseError:	return this.onTestCaseErrorValue;
			case OnStepStart:		return this.onStepStartValue;
			case OnStepFinish:		return this.onStepFinishValue;
			case OnStepError:		return this.onStepErrorValue;
			default:				return new MutableString();
		}
	}

	public boolean isEnabled()
	{
		return this.enabled != null && this.enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
		this.changed = true;
	}

	public Map<HandlerKind, String> getMap()
	{
		return Arrays.stream(HandlerKind.values())
				.collect(Collectors.toMap(
						  Function.identity()
						, kind -> this.getGlobalHandler(kind).get()
						, (u,v) -> u /*never happened, because key is enum*/
						, () -> new EnumMap<>(HandlerKind.class))
				);
	}
}
