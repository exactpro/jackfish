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

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.error.ErrorKind;

/**
 * The class representing the matrix error bean.
 * Has fields:
 * <ul>
 * <li>{@link MatrixError#Where} : the place ( item), where was error</li>
 * <li>{@link MatrixError#Message} : the error message</li>
 * <li>{@link MatrixError#Kind} : the error kind of error</li>
 * </ul>
 *
 * @see ErrorKind
 * @see MatrixItem
 */
public class MatrixError
{
	public MatrixItem Where;
	public String     Message;
	public ErrorKind  Kind;

	public MatrixError(MatrixError error)
	{
		if (error != null)
		{
			this.Where = error.Where.makeCopy();
			this.Message = error.Message;
			this.Kind = error.Kind;
		}
	}

	public MatrixError(String message, ErrorKind kind, MatrixItem where)
	{
		this.Message = message;
		this.Kind = kind;
		this.Where = where;
	}

	@Override
	public String toString()
	{
		return this.Message;
	}
}
