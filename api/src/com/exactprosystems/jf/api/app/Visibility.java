////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
