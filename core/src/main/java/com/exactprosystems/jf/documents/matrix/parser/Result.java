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

package com.exactprosystems.jf.documents.matrix.parser;

public enum Result
{
	Break      ("Break"),
	Continue   ("Continue"),
	Return     ("Return"),
	Passed     ("Passed"),
	Failed     ("Failed"),
    StepFailed ("Failed"),
	Ignored    ("Ignored"),
	NotExecuted("NotExecuted"),
	Off        ("Off"),
	Stopped    ("Stopped"),
	;
	private String name;

	Result(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return this.name;
	}

	public boolean isFail()
	{
		return this == Failed || this == StepFailed;
	}

	public String getStyle()
	{
		return this.name;
	}

}
