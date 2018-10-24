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
package com.exactprosystems.jf.tool.documents.guidic;

import com.exactprosystems.jf.api.app.AppConnection;

class ApplicationStatusBean
{
	private ApplicationStatus status;
	private AppConnection appConnection;
	private Throwable throwable;

	public ApplicationStatusBean(ApplicationStatus status, AppConnection appConnection, Throwable throwable)
	{
		this.status = status;
		this.appConnection = appConnection;
		this.throwable = throwable;
	}

	public ApplicationStatus getStatus()
	{
		return status;
	}

	public AppConnection getAppConnection()
	{
		return appConnection;
	}

	public Throwable getThrowable()
	{
		return throwable;
	}
}
