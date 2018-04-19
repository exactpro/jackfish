/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.tool.custom.scaledimage;

import com.exactprosystems.jf.api.common.i18n.R;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.ArrayList;

public class CustomGrid
{
	public static final int STEP = 10;

	private ArrayList<Line> horizontalLines;
	private ArrayList<Line> verticalLines;

	private int h;
	private int w;
	private Group group;

	private boolean isShow = false;


	public CustomGrid()
	{
		this.horizontalLines = new ArrayList<>();
		this.verticalLines = new ArrayList<>();
	}

	public CustomGrid(int w, int h)
	{
		this();
		this.setSize(w, h);
	}

	public void setGroup(Group group)
	{
		this.group = group;
	}

	public void setSize(int w, int h)
	{
		this.w = w;
		this.h = h;
		if (this.isShow)
		{
			this.hide();
			this.show();
		}
	}

	public void show()
	{
		if (this.group == null)
		{
			throw new RuntimeException(R.CUSTOM_GRID_ERROR.get());
		}
		if (!isShow)
		{
			this.createLines(w, h);
			this.horizontalLines.stream().peek(this.group.getChildren()::add).forEach(line -> line.setVisible(true));
			this.verticalLines.stream().peek(this.group.getChildren()::add).forEach(line -> line.setVisible(true));
			isShow = true;
		}
	}

	public void hide()
	{
		if (isShow)
		{
			this.horizontalLines.stream().peek(this.group.getChildren()::remove).forEach(line -> line.setVisible(false));
			this.verticalLines.stream().peek(this.group.getChildren()::remove).forEach(line -> line.setVisible(false));
			this.horizontalLines.clear();
			this.verticalLines.clear();
			isShow = false;
		}
	}

	public boolean isVisible()
	{
		return this.isShow;
	}

	private void createLines(int w, int h)
	{
		for(int i = STEP; i <= h; i+=STEP)
		{
			Line line = new Line(0, i, w, i);
			line.setStrokeWidth(1);
			line.setStroke(new Color(0, 0, 0, 0.1));
			line.setVisible(false);
			this.horizontalLines.add(line);
		}
		for(int i = STEP; i <= w; i+=STEP)
		{
			Line line = new Line(i, 0, i, h);
			line.setStrokeWidth(1);
			line.setStroke(new Color(0, 0, 0, 0.1));
			line.setVisible(false);
			this.verticalLines.add(line);
		}
	}

}
