////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

import java.util.Map;

public interface ICodec
{
	MapMessage 	decode	(byte[] bytes) throws Exception;
	MapMessage 	convert	(String messageType, Map<String, Object> message) throws Exception;
	void 		tune	(String messageType, Map<String, Object> message) throws Exception;
	byte[] 		encode	(String messageType, Map<String, Object> message) throws Exception;
}
