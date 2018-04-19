/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.app;

import java.io.Serializable;

import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.i18n.R;

public enum Visibility implements Serializable
{
    @DescriptionAttribute(text = R.VISIBILITY_VISIBLE)
	Visible,
	@DescriptionAttribute(text = R.VISIBILITY_ENABLE)
    Enable;
    

	private static final long serialVersionUID = 3590062511245720428L;
}
