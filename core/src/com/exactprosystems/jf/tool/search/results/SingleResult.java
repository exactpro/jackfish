package com.exactprosystems.jf.tool.search.results;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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

		if (this.matches == null || this.matches.isEmpty())
		{
			return new Text(this.line);
		}
		else
		{
			if (this.line.contains(System.lineSeparator()))
			{
				VBox box = new VBox();
				String[] lines = this.line.split(System.lineSeparator());
				int topLineIndex = Math.max(this.lineNumber - 1, 0);
				Pair<Integer, Integer> pair = matches.get(0);
				Integer start = pair.getKey();
				Integer end = pair.getValue();
				int sum = IntStream.range(0, topLineIndex)
						.mapToObj(i -> lines[i])
						.mapToInt(String::length)
						//add line separator
						.map(i -> i + 1)
						//remove last line separator
						.sum() - 1;

				start -= sum;
				end -= sum;

				String line = lines[topLineIndex];
				end -= (line.length() + 1);
				int max = Math.max(start - 1, 0);
				String s0 = line.substring(0, max);
				String s1 = line.substring(max);
				HBox hb = new HBox();
				Text t1 = new Text(s1);
				t1.setFill(Color.DARKORANGE);
				hb.getChildren().addAll(new Text(s0), t1);
				box.getChildren().add(hb);

				int nextLineIndex = topLineIndex + 1;
				if (nextLineIndex < lines.length)
				{
					String nextLine = lines[nextLineIndex] + "\n";
					int endMax = Math.max(end - 1, 0);

					endMax = Math.min(endMax, nextLine.length());
					String s2 = nextLine.substring(0, endMax);
					String s3 = nextLine.substring(endMax);
					HBox hb1 = new HBox();
					Text t2 = new Text(s2);
					t2.setFill(Color.DARKORANGE);
					hb1.getChildren().addAll(t2, new Text(s3));
					box.getChildren().add(hb1);
				}
				return box;

			}
			else
			{
				HBox box = new HBox();
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
				return box;
			}
		}
	}
}
