package com.exactprosystems.jf.tool.search;

import com.exactprosystems.jf.tool.Common;

import java.io.File;

class SearchResult
{
	private File file;
	private int lineNumber;

	public SearchResult(File file, int lineNumber)
	{
		this.file = file;
		this.lineNumber = lineNumber;
	}

	public File getFile()
	{
		return file;
	}

	public int getLineNumber()
	{
		return lineNumber;
	}

	@Override
	public String toString()
	{
		return "File : " + Common.getRelativePath(file.getPath()) + " : " + lineNumber;
	}

	static class FailedSearchResult extends SearchResult
	{
		private String failMessage;

		public FailedSearchResult(String failMessage)
		{
			super(null, 0);
			this.failMessage = failMessage;
		}

		@Override
		public String toString()
		{
			return this.failMessage;
		}
	}
}
