////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.store;

import com.exactprosystems.jf.common.Configuration;
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
		this.controller = Common.loadController(StoreVariable.class.getResource("StoreVariable.fxml"));
		this.config = config;
		List<StoreVariableController.StoreBean> list = this.config.getStoreMap().entrySet().stream().map(entry -> new StoreVariableController.StoreBean(entry.getKey(), entry.getValue())).collect(Collectors.toList());
		this.controller.init(this, list);
	}

	private Map<String, Object> convert(List<StoreVariableController.StoreBean> list)
	{
		return list.stream().collect(Collectors.toMap(StoreVariableController.StoreBean::getName, StoreVariableController.StoreBean::getValue));
	}

	public void save(List<StoreVariableController.StoreBean> list)
	{
		this.config.storeMap(convert(list));
	}

	public void show()
	{
		this.controller.show();
	}
}
