////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

import java.util.List;

public interface IMessageDictionary
{
    String          getFilePath();
    String			getDescription();
	IField 			getField(String name);
	List<IField>	getFields();
	IMessage 		getMessageByName(String name);
	IMessage 		getMessage(String name);
	List<IMessage>	getMessages();
}
