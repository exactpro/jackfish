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
package com.exactprosystems.jf.tool.custom.scaledimage;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.Optional;
import java.util.function.DoubleConsumer;

public class ScalePane extends HBox
{
    private static double[]  scales       = new double[] { 0.25, 0.5, 0.75, 1, 1.5, 2, 2.5, 3, 4 };
    private static int       defaultIndex = 3;
    private int              currentIndex = defaultIndex;
    private DoubleConsumer   scaleChanged;
    private Label            labelZoom;

	public ScalePane()
	{
		super();
		
		this.getStyleClass().addAll(CssVariables.SCALE_PANE);
		this.setAlignment(Pos.CENTER_LEFT);

		Button btnZoomMinus = new Button();
		btnZoomMinus.setId(CssVariables.SCALE_PANE_ZOOM_MINUS);
		btnZoomMinus.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);

		Button btnZoomPlus = new Button();
		btnZoomPlus.setId(CssVariables.SCALE_PANE_ZOOM_PLUS);
		btnZoomPlus.getStyleClass().addAll(CssVariables.TRANSPARENT_BACKGROUND);

		this.labelZoom = new Label();
		this.labelZoom.setOnMouseClicked(event -> 
		{
			if (event.getClickCount() == 2)
			{
				this.currentIndex = defaultIndex;
	            displayScale();
				this.onScaleChanged();
			}
		});
        displayScale();
		
		btnZoomMinus.setOnAction(event ->
		{
			if (currentIndex == 0)
			{
				return;
			}
			--this.currentIndex;
			displayScale();
			onScaleChanged();
		});

		btnZoomPlus.setOnAction(event ->
		{
			if (this.currentIndex == scales.length - 1)
			{
				return;
			}
			++this.currentIndex;
			displayScale();
			onScaleChanged();
		});
		this.getChildren().addAll(btnZoomMinus, this.labelZoom, btnZoomPlus);
	}
	
	public double getScale()
	{
	    return scales[this.currentIndex];
	}

	public void setOnScaleChanged(DoubleConsumer scaleChanged)
	{
		this.scaleChanged = scaleChanged;
	}
	

	private void onScaleChanged()
	{
		Optional.ofNullable(this.scaleChanged).ifPresent(lis -> lis.accept(getScale()));
	}

	private void displayScale()
	{
		this.labelZoom.setText(String.valueOf((int) (getScale() * 100)) + "%");
	}
}
