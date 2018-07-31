/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions;

import com.exactprosystems.jf.api.common.i18n.R;

import javax.lang.model.type.NullType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionAttribute
{
	ActionGroups group();
	String suffix() default "";
	boolean additionFieldsAllowed();
	Class<?> outputType() default NullType.class;
	R constantGeneralDescription() default R.DEFAULT;
	R constantAdditionalDescription() default R.DEFAULT;
	R constantOutputDescription() default R.DEFAULT_OUTPUT_DESCRIPTION;
	Class<?>[] seeAlsoClass() default {};
	R constantExamples() default R.DEFAULT;
}
