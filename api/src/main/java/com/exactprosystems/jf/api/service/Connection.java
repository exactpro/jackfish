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

package com.exactprosystems.jf.api.service;

import java.net.Socket;
import java.io.*;

public class Connection
{
	public Connection(Socket socket, Writer writer, Reader reader)
	{
		super();
		this.Socket = socket;
		this.Writer = writer;
		this.Reader = reader;
	}
	
	@Override
	public String toString()
	{
		return Connection.class.getSimpleName() + "{" + this.Socket + ":" +hashCode() + "}";
	}
	
	public Socket Socket;
	public Writer Writer;
	public Reader Reader;
}
