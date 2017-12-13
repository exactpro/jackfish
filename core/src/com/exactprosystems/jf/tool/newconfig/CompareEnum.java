////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
