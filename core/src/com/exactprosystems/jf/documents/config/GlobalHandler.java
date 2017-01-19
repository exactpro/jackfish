package com.exactprosystems.jf.documents.config;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.common.MutableString;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.exactprosystems.jf.documents.config.Configuration.*;

@XmlAccessorType(XmlAccessType.NONE)
public class GlobalHandler implements Mutable
{
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

	public static void main(String[] args)
	{
		System.out.println(Arrays.toString("03032018".split("(?!^)")));
	}

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

	private boolean changed = false;

	public void setValue(GlobalHandler handler)
	{
		this.onTestCaseStartValue.set(handler.onTestCaseStartValue);
		this.onTestCaseFinishValue.set(handler.onTestCaseFinishValue);
		this.onTestCaseErrorValue.set(handler.onTestCaseErrorValue);
		this.onStepStartValue.set(handler.onStepStartValue);
		this.onStepFinishValue.set(handler.onStepFinishValue);
		this.onStepErrorValue.set(handler.onStepErrorValue);
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

	public Boolean getEnabled()
	{
		return enabled;
	}

	public void setEnabled(Boolean enabled)
	{
		this.enabled = enabled;
		this.changed = true;
	}

	public Map<HandlerKind, String> getMap()
	{
		Map<HandlerKind, String> map = new LinkedHashMap<>();
		Arrays.stream(HandlerKind.values()).forEach(h -> map.put(h, getGlobalHandler(h).get()));
		return map;
	}
}
