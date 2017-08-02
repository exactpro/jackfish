package com.exactprosystems.jf.tool.search;

import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;

import java.io.File;
import java.util.List;

class SearchResult
{
	protected File file;
	private int lineNumber;
	private DocumentKind kind;
	private String line;

	public SearchResult(File file, int lineNumber, DocumentKind kind, String line)
	{
		this.file = file;
		this.lineNumber = lineNumber;
		this.kind = kind;
		this.line = line;
	}

	public File getFile()
	{
		return file;
	}

	public DocumentKind getKind()
	{
		return kind;
	}

	public String getLine()
	{
		return line;
	}

	@Override
	public String toString()
	{
		return Common.getRelativePath(file.getPath()) + " : " + lineNumber;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		SearchResult that = (SearchResult) o;

		if (lineNumber != that.lineNumber)
		{
			return false;
		}
		return file != null ? ConfigurationFx.path(file).equals(ConfigurationFx.path(that.file)) : that.file == null;
	}

	@Override
	public int hashCode()
	{
		int result = file != null ? file.hashCode() : 0;
		result = 31 * result + lineNumber;
		return result;
	}

	static class FailedSearchResult extends SearchResult
	{
		private String failMessage;

		public FailedSearchResult(String failMessage)
		{
			super(null, 0, null, null);
			this.failMessage = failMessage;
		}

		@Override
		public String toString()
		{
			return this.failMessage;
		}
	}

	static class AggregateSearchResult extends SearchResult
	{
		private List<SearchResult> results;

		public AggregateSearchResult(File file,  DocumentKind kind, List<SearchResult> results)
		{
			super(file, 0, kind, null);
			this.results = results;
		}

		public List<SearchResult> getResults()
		{
			return results;
		}

		public boolean isEmpty()
		{
			return this.results == null || this.results.isEmpty();
		}

		@Override
		public String toString()
		{
			return Common.getRelativePath(file.getPath()) + " ( " + this.results.size() + " )";
		}
	}
}
