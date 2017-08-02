package com.exactprosystems.jf.tool.search.results;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class SingleResult extends AbstractResult
{
	private String line;
	private int lineNumber;
	private String matchesLine;
	private String fileName;

	public SingleResult(String fileName, String line, int lineNumber, String matchesLine)
	{
		this.line = line;
		this.lineNumber = lineNumber;
		this.matchesLine = matchesLine;
		this.fileName = fileName;
	}

	@Override
	public Node toView()
	{
		return new Label(fileName + " : " + this.lineNumber);
	}

	@Override
	public Node help()
	{
		HBox box = new HBox();
		int index;
		String str = this.line;
		while ((index = str.indexOf(this.matchesLine)) > -1)
		{
			String s = str.substring(0, index);
			Text t1 = new Text(s);
			if (s.equals(this.matchesLine))
			{
				t1.setFill(Color.DARKORANGE);
			}

			String s1 = str.substring(index, index + this.matchesLine.length());
			Text t2 = new Text(s1);
			if (s1.equals(this.matchesLine))
			{
				t2.setFill(Color.DARKORANGE);
			}
			box.getChildren().addAll(t1, t2);
			str = str.substring(index + this.matchesLine.length());
		}
		if (!str.isEmpty())
		{
			Text t1 = new Text(str);
			box.getChildren().add(t1);
		}
		box.setAlignment(Pos.CENTER_LEFT);
		return box;
	}
}
