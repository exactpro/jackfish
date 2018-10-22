/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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

