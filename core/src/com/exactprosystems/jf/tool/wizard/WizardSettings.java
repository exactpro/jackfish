/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
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