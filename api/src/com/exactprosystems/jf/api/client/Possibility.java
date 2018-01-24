////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

import com.exactprosystems.jf.api.common.i18n.R;

public enum Possibility
{
	Receiving			(R.POSSIBILITY_RECEIVING_MESSAGES),
	Sending				(R.POSSIBILITY_SENDING_MESSAGES),
	RawSending			(R.POSSIBILITY_RAW_SENDING),
	Encoding			(R.POSSIBILITY_ENCODING),
	Decoding 			(R.POSSIBILITY_DECODING),
	
	;
	
	Possibility(R description)
	{
		this.description = description;
	}
	
	public String getDescription()
	{
		return this.description.get();
	}
	
	private R description;
}
