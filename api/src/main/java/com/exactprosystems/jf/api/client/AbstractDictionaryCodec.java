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

package com.exactprosystems.jf.api.client;

import com.exactprosystems.jf.api.common.i18n.R;

import java.util.Map;

public abstract class AbstractDictionaryCodec implements ICodec
{
	public AbstractDictionaryCodec(IMessageDictionary dictionary) throws Exception
	{
		if (dictionary == null)
		{
			throw new Exception(R.ABSTRACT_DICTIONARY_CODEC_DICTIONARY_IS_NULL.get());
		}
		this.dictionary = dictionary;
	}
	

	@Override
	public MapMessage convert(String messageType, Map<String, Object> message) throws Exception
	{
		MapMessage result = new MapMessage((String)null);
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
			throw new Exception(String.format(R.ABSTRACT_DICTIONARY_CODEC_UNKNOWN_MESSAGE_TYPE.get(), messageType));
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
