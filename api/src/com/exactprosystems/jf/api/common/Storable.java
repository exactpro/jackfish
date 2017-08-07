////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
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
