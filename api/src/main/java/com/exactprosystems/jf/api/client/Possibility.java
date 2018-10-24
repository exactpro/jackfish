/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
