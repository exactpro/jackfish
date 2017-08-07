package com.exactprosystems.jf.api.app;

public enum ChartKind
{
	Line("Line chart"),
	Bar("Bar chart"),
	Pie("Pie chart"),
	Gannt("Gannt chart");

	private String description;

	ChartKind(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}

}
