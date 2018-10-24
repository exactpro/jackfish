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
package com.exactprosystems.jf.tool.custom.store;

import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.tool.Common;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StoreVariable
{
	private StoreVariableController controller;
	private Configuration config;

	public StoreVariable(Configuration config)
	{
		this.controller = Common.loadController(StoreVariable.class);
		this.config = config;
		this.controller.init(this, this.config.getStoreMap().entrySet()
				.stream()
				.map(entry -> new StoreVariableController.StoreBean(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList()));
	}

	private Map<String, Object> convert(List<StoreVariableController.StoreBean> list)
	{
		return list.stream().collect(Collectors.toMap(StoreVariableController.StoreBean::getName, StoreVariableController.StoreBean::getValue));
	}

	public void save(List<StoreVariableController.StoreBean> list)
	{
		this.config.storeMap(convert(list));
	}

	public void remove(String name)
	{
		this.config.storeGlobal(name, null);
	}

	public void show()
	{
		this.controller.show();
	}
}
