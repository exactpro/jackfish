package com.exactprosystems.jf.tool.wizard.related;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.documents.config.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WizardCommonHelper
{
	private WizardCommonHelper()
	{

	}

	public static List<ConnectionBean> getAllConnections(Configuration config)
	{
		Map<String, Object> storeMap = config.getStoreMap();
		//get stored connection
		List<ConnectionBean> list = storeMap.entrySet().stream()
				.filter(entry -> entry.getValue() instanceof AppConnection)
				.map(entry -> new ConnectionBean(entry.getKey(), ((AppConnection) entry.getValue())))
				.collect(Collectors.toList());

		return list;
	}

}
