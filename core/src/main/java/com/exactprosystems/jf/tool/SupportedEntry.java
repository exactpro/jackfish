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

package com.exactprosystems.jf.tool;

import com.exactprosystems.jf.api.common.i18n.R;

public class SupportedEntry
{
	private boolean isSupported;

	public SupportedEntry(boolean isSupported)
	{
		this.isSupported = isSupported;
	}

	public boolean isSupported()
	{
		return isSupported;
	}

	public void setIsSupported(boolean isSupported)
	{
		this.isSupported = isSupported;
	}

	@Override
	public String toString()
	{
		return isSupported ? R.SUPPORTED_ENTRY_TRUE.get() : R.SUPPORTED_ENTRY_FALSE.get();
	}
}
