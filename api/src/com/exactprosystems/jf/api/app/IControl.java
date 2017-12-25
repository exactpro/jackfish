////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.common.Str;

import java.util.Arrays;
import java.util.List;

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
	OperationResult operate(IRemoteApplication remote, IWindow window, Object value, ITemplateEvaluator templateEvaluator)  throws Exception;
	CheckingLayoutResult checkLayout(IRemoteApplication remote, IWindow window, Object value, ITemplateEvaluator templateEvaluator)  throws Exception;

	String DUMMY = "$DUMMY$";

	static Locator evaluateTemplate(Locator locator, ITemplateEvaluator templateEvaluator)
	{
		if (locator == null)
		{
			return null;
		}
		List<LocatorFieldKind> kinds = Arrays.asList(
				LocatorFieldKind.XPATH, LocatorFieldKind.UID, LocatorFieldKind.CLAZZ,
				LocatorFieldKind.NAME, LocatorFieldKind.TITLE, LocatorFieldKind.ACTION,
				LocatorFieldKind.TEXT, LocatorFieldKind.TOOLTIP);
		for (LocatorFieldKind kind : kinds)
		{
			Object value = locator.get(kind);
			if (value != null && !Str.IsNullOrEmpty(String.valueOf(value)))
			{
				String stringValue = String.valueOf(value);
				try
				{
					stringValue = templateEvaluator.templateEvaluate(stringValue);
				}
				catch (Exception ignored)
				{
					//nothing
				}
				locator.set(kind, stringValue);
			}
		}
		return locator;
	}

	static Locator evaluateTemplate(IControl control, ITemplateEvaluator templateEvaluator)
	{
		if (control == null)
		{
			return null;
		}
		return evaluateTemplate(control.locator(), templateEvaluator);
	}
}
