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
