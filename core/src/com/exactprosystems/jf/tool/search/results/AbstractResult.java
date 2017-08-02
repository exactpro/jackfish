package com.exactprosystems.jf.tool.search.results;

import javafx.scene.Node;

public abstract class AbstractResult
{
	public abstract Node toView();

	public Node help()
	{
		return null;
	}
}
