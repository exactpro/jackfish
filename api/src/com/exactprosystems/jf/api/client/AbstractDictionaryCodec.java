////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

import java.util.Map;

public abstract class AbstractDictionaryCodec implements ICodec
{
	public AbstractDictionaryCodec(IMessageDictionary dictionary) throws Exception
	{
		if (dictionary == null)
		{
			throw new Exception("The dictionary is null");
		}
		this.dictionary = dictionary;
	}
	

	@Override
	public MapMessage convert(String messageType, Map<String, Object> message) throws Exception
	{
		MapMessage result = new MapMessage(null);
		IMessage mess = this.dictionary.getMessage(messageType);
		if (mess == null)
		{
			result.addError("Unknown message type " + messageType);
			return result;
		}
		
		convertStart(messageType, this.dictionary, mess, result);
		
		for (IField field : mess.getFields())
		{
			String name = field.getName();
			if (name != null)
			{
				Object value = message.get(name);
				
				convertAddField(this.dictionary, mess, field, value, result);
			}
		}
		
		convertFinish(messageType, this.dictionary, mess, result);
		
		return result;
	}
	
	@Override
	public byte[] encode(String messageType, Map<String, Object> message) throws Exception
	{
		IMessage mess = this.dictionary.getMessage(messageType);
		if (mess == null)
		{
			throw new Exception("Unknown message type " + messageType);
		}
		
		tune(messageType, message);
		
		FlexBuffer result = new FlexBuffer(10);
		encodeStart(messageType, this.dictionary, mess, result); 
		
		for (IField field : mess.getFields())
		{
			String name = field.getName();
			if (name != null)
			{
				Object value = message.get(name);
				encodeAddField(this.dictionary, mess, field, value, result);
			}
		}
		
		encodeFinish(messageType, this.dictionary, mess, result);
		
		return result.trim().buffer().array();
	}

	
	protected abstract void convertStart(String messageType, IMessageDictionary dictionary, IMessage mess, MapMessage result);

	protected abstract void convertAddField(IMessageDictionary dictionary, IMessage mess, IField field, Object value, MapMessage result);

	protected abstract void convertFinish(String messageType, IMessageDictionary dictionary, IMessage mess, MapMessage result);

	
	
	protected abstract void encodeStart(String messageType, IMessageDictionary dictionary, IMessage mess, FlexBuffer result);

	protected abstract void encodeAddField(IMessageDictionary dictionary, IMessage mess, IField field, Object value, FlexBuffer result);

	protected abstract void encodeFinish(String messageType, IMessageDictionary dictionary, IMessage mess, FlexBuffer result);

	
	
	private IMessageDictionary dictionary;
}
