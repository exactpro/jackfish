////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.search.results;

import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.search.Search;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SingleResult extends AbstractResult
{
	private String                       line;
	private int                          lineNumber;
	private int                          itemLineNumber;
	private List<Pair<Integer, Integer>> matches;
	private Search 						 model;
	private DocumentKind 				 kind;
	private final File 	 				 file;

	public SingleResult(File file, String line, int lineNumber, int itemLineNumber, List<Pair<Integer, Integer>> matches, Search model, DocumentKind kind)
	{
		this.line = line;
		this.lineNumber = lineNumber;
		this.itemLineNumber = itemLineNumber;
		this.matches = matches;
		this.model = model;
		this.kind = kind;
		this.file = file;
	}

	@Override
	public Node toView()
	{
		BorderPane pane = new BorderPane();

		Label label = new Label(file.getName() + " : " + this.itemLineNumber);
		label.setAlignment(Pos.CENTER_LEFT);
		label.setTextAlignment(TextAlignment.LEFT);
		BorderPane.setAlignment(label, Pos.CENTER_LEFT);

		switch (kind)
		{
			case LIBRARY:
			case MATRIX:
				Button btnGoToLineInDoc = new Button();
				btnGoToLineInDoc.setOnAction(event -> model.openAsDocWithNavToRow(file, itemLineNumber, kind));
				btnGoToLineInDoc.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);
				btnGoToLineInDoc.setId("btnOpenAsDocument");
				pane.setRight(btnGoToLineInDoc);
		}

		pane.setCenter(label);
		return pane;
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
