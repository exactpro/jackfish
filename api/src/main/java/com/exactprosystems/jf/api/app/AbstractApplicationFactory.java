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

package com.exactprosystems.jf.api.app;

import java.util.Set;

public abstract class AbstractApplicationFactory implements IApplicationFactory
{
	public static final String trimTextName = "TrimText";

	private IGuiDictionary dictionary = null;

	//region IApplicationFactory
	@Override
	public void init(IGuiDictionary dictionary)
	{
		this.dictionary = dictionary;
	}

	@Override
	public IGuiDictionary getDictionary()
	{
		return this.dictionary;
	}

	@Override
	public boolean isAllowed(ControlKind kind, OperationKind operation)
	{
		return getInfo().isAllowed(kind, operation);
	}

	@Override
	public boolean isSupported(ControlKind kind)
	{
		return getInfo().isSupported(kind);
	}

	@Override
	public Set<ControlKind> supportedControlKinds()
	{
		return getInfo().supportedControlKinds();
	}

	//endregion
}
