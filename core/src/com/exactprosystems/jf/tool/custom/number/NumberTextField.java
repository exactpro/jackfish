////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.number;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.tool.Common;
import javafx.scene.control.TextField;

public class NumberTextField extends TextField
{
	private int minValue;
	private int maxValue;
	private boolean round;

	public NumberTextField()
	{
		this(0);
	}

	public NumberTextField(int startValue)
	{
		this(startValue, 0, Integer.MAX_VALUE);
	}

	public NumberTextField(int startValue, int minValue, int maxValue)
	{
		super();
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.setText(checkRange(startValue));
		this.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (oldValue && !newValue)
			{
				if (Str.IsNullOrEmpty(this.getText()))
				{
					this.setValue(this.minValue);
				}
			}
		});
		this.textProperty().addListener((observableValue, s, t1) -> {
			if (!t1.isEmpty())
			{
				if (!t1.matches(Common.INT_REGEXP))
				{
					NumberTextField.this.setText(s);
				}
				else
				{
					try
					{
						int value = Integer.parseInt(t1);
						this.setText(checkRange(value));
					}
					catch (NumberFormatException e)
					{
						this.setText(checkRange(Integer.parseInt(s)));
					}
				}
			}
		});
	}

	public int getValue()
	{
		return Integer.parseInt(this.getText().isEmpty() ? "0" : this.getText());
	}

	public void setValue(int value)
	{
		this.setText(checkRange(value));
	}

	public int getMinValue()
	{
		return minValue;
	}

	public int getMaxValue()
	{
		return maxValue;
	}

	public boolean isRound()
	{
		return round;
	}

	public void setRound(boolean round)
	{
		this.round = round;
	}

	//============================================================
	// private methods
	//============================================================
	private String checkRange(int value)
	{
		if (value > maxValue)
		{
			return round ? String.valueOf(minValue) : String.valueOf(maxValue);
		}
		if (value < minValue)
		{
			return round ? String.valueOf(maxValue) : String.valueOf(minValue);
		}
		return String.valueOf(value);
	}
}