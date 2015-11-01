////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app.js;

import com.exactprosystems.jf.app.Browser;

public class JSInjectionFactory
{
	public static final String returnLocation = "return document.myMouseLocation";

	public static JSInjection getJSInjection(Browser browser)
	{
		switch (browser)
		{
			case CHROME:
				return new JSInjectionChrome();

			case FIREFOX:
				return new JSInjectionFirefox();

			default:
				return new JSInjectionStub();
		}
	}
}
