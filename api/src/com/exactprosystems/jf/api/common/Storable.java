/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
