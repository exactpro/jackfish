////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
}
