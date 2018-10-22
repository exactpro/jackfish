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

package com.exactprosystems.jf.tool.matrix.params;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.tool.Common;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Map;

public class ShowAllParams
{
	private ShowAllParamsController controller;

	public ShowAllParams(Map<ReadableValue, TypeMandatory> map, Parameters parameters, String title)
	{
		this.controller = Common.loadController(ShowAllParams.class.getResource("showAllParams.fxml"));
		this.controller.setContent(map, parameters, title);
	}

	public ArrayList<Pair<ReadableValue, TypeMandatory>> show()
	{
		return this.controller.show();
	}
}
