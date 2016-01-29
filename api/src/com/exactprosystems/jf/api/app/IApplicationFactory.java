////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.common.IFactory;
import com.exactprosystems.jf.api.common.VersionSupported;

public interface IApplicationFactory  extends VersionSupported, IFactory
{
	String helpFileName	=	"helpFile.html";
	String				getHelp();
	void				init(IGuiDictionary dictionary);
	
	String[]			wellKnownStartArgs();
	String[]			wellKnownConnectArgs();
	ControlKind[]		supportedControlKinds();

	IApplication 		createApplication() throws Exception;
	String 				getRemoteClassName();
	
	IGuiDictionary 		getDictionary();

	String[] supportedListeningParameters();
}
