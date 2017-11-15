////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.msgdictionary;

import javax.xml.bind.annotation.XmlRootElement;

import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.msgdic.MessageDictionary;

@XmlRootElement(name = "dictionary")
public class MessageDictionaryFx extends MessageDictionary
{
	public MessageDictionaryFx()
	{
		super();
	}
	
	public MessageDictionaryFx(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);
	}


}
