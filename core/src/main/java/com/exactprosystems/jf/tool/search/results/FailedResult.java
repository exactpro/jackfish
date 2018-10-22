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

package com.exactprosystems.jf.tool.search.results;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class FailedResult extends SingleResult
{
	private String msg;

	public FailedResult(String msg)
	{
		super(null, null, 0, 0, null, null, null);
		this.msg = msg;
	}

	@Override
	public Node toView()
	{
		Label label = new Label(this.msg);
		label.getStyleClass().addAll(CssVariables.INCORRECT_FIELD);
		return label;
	}
}
