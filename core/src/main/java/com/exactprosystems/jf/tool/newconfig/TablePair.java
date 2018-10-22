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
package com.exactprosystems.jf.tool.newconfig;

import java.io.File;
import java.util.function.Supplier;

public class TablePair
{
	private final String key;
	private final String value;
	private String tooltipSeparator = null;
	private boolean isEditable = true;
	private Supplier<File> pathFunction;
	private boolean isRequired = false;

	public TablePair(String key, String value)
	{
		this.key = key;
		this.value = value;
	}

	public String getKey()
	{
		return key;
	}

	public String getValue()
	{
		return value;
	}

	public boolean isPath()
	{
		return this.pathFunction != null;
	}

	public boolean isEditable()
	{
		return isEditable;
	}

	public boolean isRequired() {
		return this.isRequired;
	}

	public String getTooltipSeparator()
	{
		return tooltipSeparator;
	}

	public Supplier<File> getPathFunction()
	{
		return pathFunction;
	}

	public static class TablePairBuilder
	{
		private TablePair tablePair;

		public static TablePairBuilder create(String key, String value)
		{
			TablePairBuilder builder = new TablePairBuilder();
			builder.tablePair = new TablePair(key, value);
			return builder;
		}

		public TablePairBuilder pathFunc(Supplier<File> pathFunc)
		{
			this.tablePair.pathFunction = pathFunc;
			return this;
		}

		public TablePairBuilder edit(boolean flag)
		{
			this.tablePair.isEditable = flag;
			return this;
		}

		public TablePairBuilder tooltipSeparator(String s)
		{
			this.tablePair.tooltipSeparator = s;
			return this;
		}

		public TablePairBuilder required() {
			tablePair.isRequired = true;
			return this;
		}

		public TablePair build()
		{
			return this.tablePair;
		}
	}
}
