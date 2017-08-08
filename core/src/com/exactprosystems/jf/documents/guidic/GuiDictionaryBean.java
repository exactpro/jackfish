////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.guidic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.exactprosystems.jf.api.app.Addition;
import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.documents.guidic.controls.AbstractControl;
import com.exactprosystems.jf.documents.matrix.parser.items.MutableArrayList;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GuiDictionary", propOrder = { "windows" })
@XmlRootElement(name = "dictionary")
public class GuiDictionaryBean implements Mutable
{
    @XmlElement(name = "window")
    protected MutableArrayList<Window> windows;

    public static final Class<?>[] jaxbContextClasses =
        {
            GuiDictionaryBean.class,
            Window.class,
            Section.class,
            AbstractControl.class,
            ExtraInfo.class,
            Rect.class,
            Attr.class,
            Addition.class,
        };

    public GuiDictionaryBean()
    {
        this.windows = new MutableArrayList<>();
    }

    @Override
    public boolean isChanged()
    {
        for (Window window : this.windows)
        {
            if (window.isChanged())
            {
                return true;
            }
        }
        return this.windows.isChanged();
    }

    @Override
    public void saved()
    {
        for (Window window : this.windows)
        {
            window.saved();
        }
        this.windows.saved();
    }

}
