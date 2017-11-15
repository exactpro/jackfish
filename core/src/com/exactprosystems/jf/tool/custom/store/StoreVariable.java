////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
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
