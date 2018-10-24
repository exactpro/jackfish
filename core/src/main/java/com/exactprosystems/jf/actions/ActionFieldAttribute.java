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

package com.exactprosystems.jf.actions;

import com.exactprosystems.jf.api.common.i18n.R;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionFieldAttribute
{
	/**
	 * @return field name for injecting values
	 */
	String name();

	/**
	 * @return description for the field.
	 */
	R constantDescription() default R.DEFAULT;

	/**
	 * Mandatory mean's, that field should has not null value.
	 * @return true, if the field is mandatory and false otherwise <br>
	 */
	boolean mandatory();

	/**
	 * This attribute need for <b>not mandatory</b> and not filled fields
	 * @return true, if parameter should be filled. Otherwise false
	 */
	boolean shouldFilled() default true;

	/**
	 * @return default value to field from DefaultValuePool.
	 *
	 * @see com.exactprosystems.jf.actions.DefaultValuePool
	 */
	DefaultValuePool def() default DefaultValuePool.Null;

	/**
	 * @return false, if the parameter is not deprecated. Otherwise true
	 */
	boolean deprecated() default false;
}
