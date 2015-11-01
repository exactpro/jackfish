////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool;

import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import com.exactprosystems.jf.common.parser.FormulaGenerator;

public class DragDetector
{
	public DragDetector(FormulaGenerator generator)
	{
		this.generator = generator;
	}

	public void onDragDetected(MouseEvent event)
	{
		String text = this.generator.generate();
		Text txt = new Text(text);
		txt.applyCss();
		
		Canvas canvas = new Canvas(txt.getLayoutBounds().getWidth(), txt.getLayoutBounds().getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, Math.round(canvas.getWidth() / 2), Math.round(canvas.getHeight() / 2) );

		
		Dragboard dragboard = ((Node)event.getSource()).startDragAndDrop(TransferMode.ANY);
		WritableImage snapshot = canvas.snapshot(new SnapshotParameters(), null);
		dragboard.setDragView(snapshot);
		ClipboardContent content = new ClipboardContent();
		content.put(DataFormat.PLAIN_TEXT, text);
		dragboard.setContent(content);
		event.consume();
	}
	
	private FormulaGenerator generator;
}

