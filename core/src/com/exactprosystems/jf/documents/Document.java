////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.common.ChangeListener;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.config.Configuration;

import java.io.Reader;

public interface Document extends Mutable
{
	Configuration getConfiguration();
	
	boolean hasName();
	
	void load(Reader reader) throws Exception;

	void create() throws Exception;
	
	void display() throws Exception;
	
	boolean canClose() throws Exception;

	void close(Settings settings) throws Exception;
	
	void save(String fileName) throws Exception;

	void undo();

	void redo();

    String getName();

	void setOnChange(ChangeListener listener);
}
