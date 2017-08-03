package com.exactprosystems.jf.tool.search.results;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class SingleResult extends AbstractResult
{
	private String                       line;
	private int                          lineNumber;
	private List<Pair<Integer, Integer>> matches;
	private String                       fileName;

	public SingleResult(String fileName, String line, int lineNumber, List<Pair<Integer, Integer>> matches)
	{
		this.line = line;
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.matches = matches;
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
		if (this.matches == null || this.matches.isEmpty())
		{
			box.getChildren().addAll(new Text(this.line));
		}
		else
		{
			int lastIndex = 0;
			ArrayList<Pair<Integer, Integer>> pairs = new ArrayList<>(this.matches);
			Pair<Integer, Integer> firstPair = pairs.get(0);
			int startIndex = 0;
			if (firstPair.getKey() == 0)
			{
				Text t = new Text(this.line.substring(0, firstPair.getValue()));
				t.setFill(Color.DARKORANGE);
				box.getChildren().add(t);
				startIndex = 1;
				lastIndex = firstPair.getValue();
			}
			for (int i = startIndex; i < this.matches.size(); i++)
			{
				Pair<Integer, Integer> pair = this.matches.get(i);

				box.getChildren().add(new Text(this.line.substring(lastIndex, pair.getKey())));

				Text colorText = new Text(this.line.substring(pair.getKey(), pair.getValue()));
				colorText.setFill(Color.DARKORANGE);

				box.getChildren().add(colorText);

				lastIndex = pair.getValue();
			}
			if (lastIndex != this.line.length())
			{
				box.getChildren().add(new Text(this.line.substring(lastIndex)));
			}
		}

		return box;
	}
}
