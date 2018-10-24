/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
				String evaluatedString = templateEvaluator.tryEvaluateTemplate(String.valueOf(value));
				if (evaluatedString != null) {
					locator.set(kind, evaluatedString);
				}
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
