package com.exactprosystems.jf.tool.newconfig;

import java.io.File;
import java.util.Comparator;

public enum CompareEnum
{
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
					return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
				}
			};
		}

		@Override
		public String getName()
		{
			return "Alphabet asc";
		}
	},

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
					return f2.getName().toLowerCase().compareTo(f1.getName().toLowerCase());
				}
			};
		}

		@Override
		public String getName()
		{
			return "Alphabet desc";
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
			return "Date asc";
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
			return "Date desc";
		}
	};

	public abstract Comparator<File> getComparator();

	public abstract String getName();
}
