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

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.Part;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.common.NullParameterException;
import com.exactprosystems.jf.common.ControlsAttributes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@ControlsAttributes(
		bindedClass 		= ControlKind.CheckBox
)
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CheckBox extends AbstractControl
{
	@Override
	public void prepare(Part part, Object value) throws Exception
	{
		if (value == null)
		{
			throw new NullParameterException(String.format(R.API_NULL_PARAMETER_EXCEPTION.get(), R.COMMON_VALUE.get()));
		}
		if (value instanceof Boolean)
		{
			part.setBool((Boolean) value);
		}
	}
}