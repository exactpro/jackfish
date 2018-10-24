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