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

package com.exactprosystems.jf.tool.wizard;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.Settings.SettingsValue;

import java.util.HashMap;
import java.util.Map;

public class WizardSettings
{
	public WizardSettings(Settings settings)
	{
		for (Kind kind : Kind.values())
		{
			SettingsValue min = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.WIZARD_NAME, kind.name() + Settings.MIN);
			SettingsValue max = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.WIZARD_NAME, kind.name() + Settings.MAX);
			setMin(kind, Double.parseDouble(min.getValue()));
			setMax(kind, Double.parseDouble(max.getValue()));
		}

		SettingsValue threshold = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.WIZARD_NAME, Settings.THRESHOLD);
		setThreshold(Double.parseDouble(threshold.getValue()));
	}

	public enum Kind
	{
		TYPE, PATH, SIZE, POSITION, ATTR
	}

	public double scale()
	{
		if (this.scale == null)
		{
			this.scale = 0.0;
			for (Kind kind : Kind.values())
			{
				this.scale += getMax(kind);
			}

			this.scale = this.scale == 0.0 ? 1 : 1 / this.scale();
		}

		return this.scale.doubleValue();
	}

	public double getMax(Kind kind)
	{
		Double d = this.maxs.get(kind);
		return d == null ? 0.0 : d.doubleValue();
	}

	public double getMin(Kind kind)
	{
		Double d = this.mins.get(kind);
		return d == null ? 0.0 : d.doubleValue();
	}

	public void setMax(Kind kind, double d)
	{
		this.maxs.put(kind, d);
		this.scale = null;
	}

	public void setMin(Kind kind, double d)
	{
		this.mins.put(kind, d);
	}

	public double getThreshold()
	{
		return threshold;
	}

	public void setThreshold(double threshold)
	{
		this.threshold = threshold;
	}

	private Map<Kind, Double> maxs      = new HashMap<>();
	private Map<Kind, Double> mins      = new HashMap<>();
	private double            threshold = 0.0;
	private Double            scale     = null;
}