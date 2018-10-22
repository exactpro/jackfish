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

import com.exactprosystems.jf.api.common.i18n.R;

import java.io.File;
import java.util.Comparator;

public enum CompareEnum
{
	ALPHABET_1_0
	{
		@Override
		public Comparator<File> getComparator()
		{
			return (f1, f2) ->
			{
				if (f1.isDirectory() && !f2.isDirectory())
				{
					return -1;
				}
				else if (!f1.isDirectory() && f2.isDirectory())
				{
					return 1;
				}
				else
				{
					return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
				}
			};
		}

		@Override
		public String getName()
		{
			return R.COMPARE_ENUM_ALPHABET_ASC.get();
		}
	},

	ALPHABET_0_1
	{
		@Override
		public Comparator<File> getComparator()
		{
			return (f1, f2) ->
			{
				if (f1.isDirectory() && !f2.isDirectory())
				{
					return -1;
				}
				else if (!f1.isDirectory() && f2.isDirectory())
				{
					return 1;
				}
				else
				{
					return f2.getName().toLowerCase().compareTo(f1.getName().toLowerCase());
				}
			};
		}

		@Override
		public String getName()
		{
			return R.COMPARE_ENUM_ALPHABET_DESC.get();
		}
	},

	DATE_0_1
	{
		@Override
		public Comparator<File> getComparator()
		{
			return (f1, f2) ->
			{
				if (f1.isDirectory() && !f2.isDirectory())
				{
					return -1;
				}
				else if (!f1.isDirectory() && f2.isDirectory())
				{
					return 1;
				}
				else
				{
					return Long.compare(f1.lastModified(), f2.lastModified());
				}
			};
		}

		@Override
		public String getName()
		{
			return R.COMPARE_ENUM_DATE_ASC.get();
		}
	},

	DATE_1_0
	{
		@Override
		public Comparator<File> getComparator()
		{
			return (f1, f2) ->
			{
				if (f1.isDirectory() && !f2.isDirectory())
				{
					return -1;
				}
				else if (!f1.isDirectory() && f2.isDirectory())
				{
					return 1;
				}
				else
				{
					return Long.compare(f2.lastModified(), f1.lastModified());
				}
			};
		}

		@Override
		public String getName()
		{
			return R.COMPARE_ENUM_DATE_DESC.get();
		}
	};

	public abstract Comparator<File> getComparator();

	public abstract String getName();
}
