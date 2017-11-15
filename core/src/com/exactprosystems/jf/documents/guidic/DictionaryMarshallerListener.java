////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.guidic;

import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;

import javax.xml.bind.Marshaller.Listener;

public class DictionaryMarshallerListener extends Listener
{
	@Override
	public void beforeMarshal(Object source)
	{
		if (source  instanceof AbstractControl)
		{
			((AbstractControl) source).correctAllText();
		}
	}

	@Override
	public void afterMarshal(Object source)
	{
		if (source  instanceof AbstractControl)
		{
			((AbstractControl) source).correctAllXml();
		}
	}
}
