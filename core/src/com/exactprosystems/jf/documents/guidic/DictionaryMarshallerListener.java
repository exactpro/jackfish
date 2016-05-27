////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
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
