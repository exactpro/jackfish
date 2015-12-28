////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.scale;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.Optional;

public class ScalePane extends HBox
{
	private double[] zooms = new double[]{0.25, 0.5, 0.75, 1, 1.5, 2, 2.5, 3, 4};
	private double currentZoom = 1;
	private int currentZoomPosition = 3;

	private Button btnZoomPlus;
	private Button btnZoomMinus;
	private Label labelZoom;

	private IScaleListener listener;

	public ScalePane(IScaleListener listener)
	{
		super();
		this.setAlignment(Pos.CENTER_LEFT);
		this.listener = listener;
		this.btnZoomMinus = new Button();
		this.btnZoomPlus = new Button();
		this.labelZoom = new Label("100%");
		this.labelZoom.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2)
			{
				this.currentZoomPosition = 3;
				this.currentZoom = zooms[this.currentZoomPosition];
				this.labelZoom.setText("100%");
				this.listener();
			}
		});
		this.setSpacing(5);
		Platform.runLater(() -> {
			Common.customizeLabeled(this.btnZoomMinus, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.ZOOM_MINUS);
			Common.customizeLabeled(this.btnZoomPlus, CssVariables.TRANSPARENT_BACKGROUND, CssVariables.Icons.ZOOM_PLUS);
		});

		this.btnZoomMinus.setOnAction(event -> {
			if (currentZoomPosition == 0)
			{
				return;
			}
			this.currentZoom = this.zooms[--this.currentZoomPosition];
			this.displayScale();
			this.listener();
		});

		this.btnZoomPlus.setOnAction(event -> {
			if (this.currentZoomPosition == this.zooms.length - 1)
			{
				return;
			}
			this.currentZoom = zooms[++this.currentZoomPosition];
			this.displayScale();
			this.listener();
		});

		this.btnZoomMinus.setOpacity(0.5);
		this.btnZoomPlus.setOpacity(0.5);
		this.btnZoomMinus.setOnMouseEntered(event -> this.btnZoomMinus.setOpacity(1.0));
		this.btnZoomMinus.setOnMouseExited(event -> this.btnZoomMinus.setOpacity(0.5));

		this.btnZoomPlus.setOnMouseEntered(event -> this.btnZoomPlus.setOpacity(1.0));
		this.btnZoomPlus.setOnMouseExited(event -> this.btnZoomPlus.setOpacity(0.5));

		this.getChildren().addAll(this.btnZoomMinus, this.labelZoom, this.btnZoomPlus);
	}

	public ScalePane()
	{
		this(null);
	}

	public void setListener(IScaleListener listener)
	{
		this.listener = listener;
	}

	private void listener()
	{
		Optional.ofNullable(this.listener).ifPresent(scale -> scale.changeScale(this.currentZoom));
	}

	private void displayScale()
	{
		this.labelZoom.setText(String.valueOf((int) (this.currentZoom * 100)) + "%");
	}
}
