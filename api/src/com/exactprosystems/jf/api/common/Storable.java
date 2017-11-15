////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.io.Serializable;
import java.util.List;

public interface Storable extends Serializable
{
	String			getName();
	List<String> 	getFileList();
	byte[] 			getData(String file)  throws Exception;
	void 			addFile(String file, byte[] data)  throws Exception;
}
