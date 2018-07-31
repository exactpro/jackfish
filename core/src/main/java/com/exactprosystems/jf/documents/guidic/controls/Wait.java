/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.guidic.controls;

import com.exactprosystems.jf.api.app.Addition;
import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.Part;
import com.exactprosystems.jf.common.ControlsAttributes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@ControlsAttributes(
		bindedClass 		= ControlKind.Wait
)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Wait extends AbstractControl
{
	public Wait()
	{
	}
	
	@Override
	public void prepare(Part part, Object value) throws Exception
	{
		if (value == null)
		{
			part.setLocator(locator());
			part.setInt(getTimeout());
			part.setToAppear(this.addition == Addition.WaitToAppear);
		}
		if (value instanceof String)
		{
			part.setText(String.valueOf(value));
		}
		else if (value instanceof Number)
		{
			part.setInt(((Number)value).intValue());
		}
	}
}