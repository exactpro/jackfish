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

package com.exactprosystems.jf.api.error.app;

import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFRemoteException;

public class TooManyElementsException extends JFRemoteException
{
	private static final long	serialVersionUID	= 9087420795591504557L;

	public TooManyElementsException(String msg)
	{
		super(msg, null);
	}

	public TooManyElementsException(String msg, Locator locator)
	{
		super(msg +" " + locator, null);
	}

	public TooManyElementsException(Locator locator)
	{
		super(String.format(R.TOO_MANY_ELEMENTS_EXCEPTION_MESSAGE.get(), locator), null);
	}

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.TOO_MANY_ELEMENTS;
	}
}
