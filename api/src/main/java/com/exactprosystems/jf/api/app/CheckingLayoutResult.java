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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CheckingLayoutResult implements Serializable
{
	private static final long	serialVersionUID	= 3562209509406788900L;

	private boolean ok = true;
	private List<String> errors = new ArrayList<>();
	private List<CheckingLayoutResultBean> beans = new ArrayList<>();

	public boolean isOk()
	{
		return this.ok;
	}
	
	public List<String> getErrors()
	{
		return this.errors;
	}

	public List<CheckingLayoutResultBean> getNewErrors()
	{
		return this.beans;
	}

	public void error(Piece piece, String string)
	{
		this.ok = false;
		this.errors.add("Processing part is [" + piece + "] " + string);
	}

	public void newError(Piece piece, String actual, String expected)
	{
		this.ok = false;
		this.beans.add(new CheckingLayoutResultBean(piece.name, piece.toString(), actual, expected));
	}

	public void set(boolean b)
	{
		if (!b)
		{
			this.ok = false;
		}
	}
}
