package com.exactprosystems.jf.common.highlighter;

public class StyleWithRange
{
	private String style;
	private int range;

	public StyleWithRange(String style, int range)
	{
		this.style = style;
		this.range = range;
	}

	public String getStyle()
	{
		return style;
	}

	public int getRange()
	{
		return range;
	}

	@Override
	public String toString()
	{
		return this.style + " [" + this.range + "]";
	}
}
