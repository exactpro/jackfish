////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool;

import com.exactprosystems.jf.documents.matrix.parser.FormulaGenerator;

import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.text.Text;

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
		Dragboard dragboard = ((Node)event.getSource()).startDragAndDrop(TransferMode.ANY);
		WritableImage snapshot = txt.snapshot(new SnapshotParameters(), null);
		dragboard.setDragView(snapshot);
		ClipboardContent content = new ClipboardContent();
		content.put(DataFormat.PLAIN_TEXT, text);
		if (text != null)
		{
			dragboard.setContent(content);
		}
		event.consume();
	}
	
	private FormulaGenerator generator;
}

