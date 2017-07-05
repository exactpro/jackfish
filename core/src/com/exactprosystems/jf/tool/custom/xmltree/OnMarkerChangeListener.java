////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.xmltree;

import org.w3c.dom.Node;

import com.exactprosystems.jf.tool.wizard.related.MarkerStyle;

@FunctionalInterface
public interface OnMarkerChangeListener
{
    void changed(Node selection, MarkerStyle oldValue, MarkerStyle newValue);

}
