////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.dictionary.dialog;

import java.util.HashMap;
import java.util.Map;

public class WizardSetting 
{
	public WizardSetting() 
	{
	}
	
	public enum Kind { TYPE, PATH, SIZE, POSITION, ATTR }
	
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

	private Map<Kind, Double> maxs = new HashMap<>();
	private Map<Kind, Double> mins = new HashMap<>();
	private double threshold = 0.0;
}
