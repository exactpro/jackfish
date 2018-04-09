/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
