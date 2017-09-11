package com.exactprosystems.jf.tool.wizard.related;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.documents.config.Configuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
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

		//get connection from pool
		config.getApplicationPool().getConnections().stream()
				.filter(appCon -> list.stream().map(ConnectionBean::getConnection).noneMatch(appCon::equals))
				.map(appCon -> new ConnectionBean(appCon.getId(), appCon))
				.forEach(list::add);

		return list;
	}

	public static void shutdownExec(ExecutorService exec)
	{
		if (exec != null)
		{
			exec.shutdown();
			try
			{
				if (!exec.awaitTermination(800, TimeUnit.MILLISECONDS))
				{
					exec.shutdownNow();
				}
			}
			catch (InterruptedException e)
			{
				exec.shutdownNow();
			}
		}
	}

}
