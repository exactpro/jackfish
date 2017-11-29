////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

public interface IControl extends Mutable
{
	ControlKind 		getBindedClass();

	ISection 			getSection();
	void 				setSection(ISection section);

	String 				getID();
	String 				getOwnerID();
    String              getRefID();
	String 				getUID();
	String 				getXpath();
	String 				getClazz();
	String 				getName();
	String 				getTitle();
	String 				getAction();
	String 				getText();
	String 				getTooltip();
	
	String 				getExpression();
	String 				getRowsId();
	String 				getHeaderId();
	String				getColumns();
	boolean 			isWeak();
	boolean				useNumericHeader();
	int					getTimeout();
	Addition 			getAddition();
	Visibility			getVisibility();
	IExtraInfo          getInfo();

	Locator				locator();
	
	void prepare(Part operationPart, Object value)  throws Exception;
	OperationResult operate(IRemoteApplication remote, IWindow window, Object value)  throws Exception;
	CheckingLayoutResult checkLayout(IRemoteApplication remote, IWindow window, Object value)  throws Exception;

	String DUMMY = "$DUMMY$";
}
