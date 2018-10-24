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

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFRemoteException;

public class ProxyException extends JFRemoteException
{
	private static final long	serialVersionUID	= -7998700941400982238L;

	public ProxyException(String message, String shortMessage, Throwable cause)
	{
        super(message, cause);
        this.message = message;
        this.shortMessage = shortMessage;
        this.stackTrace = cause.getStackTrace();
	}
	
	public String getFullMessage()
	{
		return this.message;
	}
	
	@Override
	public StackTraceElement[] getStackTrace()
	{
		return this.stackTrace;
	}
	
	@Override
	public String getMessage()
	{
		return this.shortMessage;
	}	
	
	private StackTraceElement[] stackTrace;
	
	private String message = null;

	private String shortMessage = null;

	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.EXCEPTION;
	}
}
