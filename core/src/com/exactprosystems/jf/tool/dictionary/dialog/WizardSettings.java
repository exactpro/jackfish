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

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.Settings.SettingsValue;

public class WizardSettings 
{
    public WizardSettings(Settings settings) 
    {
        for(Kind kind : Kind.values())
        {
            SettingsValue min = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.WIZARD_NAME, kind.name() + "MIN", "0.0");
            SettingsValue max = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.WIZARD_NAME, kind.name() + "MAX", "0.0");
            setMin(kind, Double.parseDouble(min.getValue()));
            setMax(kind, Double.parseDouble(max.getValue()));
        }
        
        SettingsValue threshold = settings.getValueOrDefault(Settings.GLOBAL_NS, Settings.WIZARD_NAME, Settings.THRESHOLD, "0.0");
        setThreshold(Double.parseDouble(threshold.getValue()));
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