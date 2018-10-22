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

package com.exactprosystems.jf.api.service;

import com.exactprosystems.jf.api.common.i18n.R;

import java.util.HashSet;
import java.util.Set;

public class ServiceHelper
{
	public static Set<ServicePossibility> possebilities(Class<?> clazz)
	{
		Set<ServicePossibility> res = new HashSet<ServicePossibility>();
		ServiceAttribute attr = clazz.getAnnotation(ServiceAttribute.class);
		for (ServicePossibility possibility : attr.possibilities())
		{
			res.add(possibility);
		}
		return res;
	}
	
	public static void errorIfDisable(Class<?> clazz, ServicePossibility possibility) throws ServicePossibilityIsDisabled
	{
		ServiceAttribute attr = clazz.getAnnotation(ServiceAttribute.class);
		for (ServicePossibility poss : attr.possibilities())
		{
			if (poss == possibility)
			{
				return;
			}
		}
		
		throw new ServicePossibilityIsDisabled(String.format(R.SERVICE_HELPER_POSSIBILITY_IS_NOT_ALLOWED.get(), clazz.getSimpleName(), possibility.getDescription()));
	}
	

}
