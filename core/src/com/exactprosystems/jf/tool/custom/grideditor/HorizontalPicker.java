////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import com.sun.javafx.scene.control.skin.TableColumnHeader;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.util.Stack;

public class HorizontalPicker extends StackPane
{

	private static final String PICKER_INDEX = "PickerIndex"; //$NON-NLS-1$

	private final HorizontalHeader horizontalHeader;

	private final SpreadsheetView spv;
	private final Stack<Label> pickerPile;
	private final Stack<Label> pickerUsed;

	private final InnerHorizontalPicker innerPicker = new InnerHorizontalPicker();

	public HorizontalPicker(HorizontalHeader horizontalHeader, SpreadsheetView spv)
	{
		this.horizontalHeader = horizontalHeader;
		this.spv = spv;

		pickerPile = new Stack<>();
		pickerUsed = new Stack<>();

		Rectangle clip = new Rectangle();
		clip.setSmooth(true);
		clip.setHeight(VerticalHeader.PICKER_SIZE);
		clip.widthProperty().bind(horizontalHeader.widthProperty());
		setClip(clip);

		getChildren().add(innerPicker);

		horizontalHeader.getRootHeader().getColumnHeaders().addListener(layoutListener);
		spv.getColumnPickers().addListener(layoutListener);
	}

	@Override
	protected void layoutChildren()
	{
		innerPicker.relocate(horizontalHeader.getRootHeader().getLayoutX(), snappedTopInset());
		for (Label label : pickerUsed)
		{
			label.setVisible(label.getLayoutX() + innerPicker.getLayoutX() + label.getWidth() > horizontalHeader.gridViewSkin.fixedColumnWidth);
		}
	}

	public void updateScrollX()
	{
		requestLayout();
	}

	private Label getPicker(Picker picker)
	{
		Label pickerLabel;
		if (pickerPile.isEmpty())
		{
			pickerLabel = new Label();
			pickerLabel.getStyleClass().addListener(layoutListener);
			pickerLabel.setOnMouseClicked(pickerMouseEvent);
		}
		else
		{
			pickerLabel = pickerPile.pop();
		}
		pickerUsed.push(pickerLabel);
		pickerLabel.getStyleClass().setAll(picker.getStyleClass());
		pickerLabel.getProperties().put(PICKER_INDEX, picker);
		return pickerLabel;
	}

	private final EventHandler<MouseEvent> pickerMouseEvent = (MouseEvent mouseEvent) -> {
		Label picker = (Label) mouseEvent.getSource();

		((Picker) picker.getProperties().get(PICKER_INDEX)).onClick();
	};


	private class InnerHorizontalPicker extends Region
	{

		@Override
		protected void layoutChildren()
		{
			pickerPile.addAll(pickerUsed.subList(0, pickerUsed.size()));
			for (Label label : pickerUsed)
			{
				label.layoutXProperty().unbind();
				label.setVisible(true);
			}
			pickerUsed.clear();

			getChildren().clear();
			int index = 0;
			for (TableColumnHeader column : horizontalHeader.getRootHeader().getColumnHeaders())
			{
				if (spv.getColumnPickers().containsKey(index))
				{
					Label label = getPicker(spv.getColumnPickers().get(index));
					label.resize(column.getWidth(), VerticalHeader.PICKER_SIZE);
					label.layoutXProperty().bind(column.layoutXProperty());
					getChildren().add(0, label);
				}
				index++;
			}
		}
	}

	private final InvalidationListener layoutListener = (Observable arg0) -> innerPicker.requestLayout();
}
