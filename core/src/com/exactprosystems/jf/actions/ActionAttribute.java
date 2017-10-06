////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

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

	@Deprecated
	String generalDescription() default "";
	R constantGeneralDescription() default R.DEFAULT;

	@Deprecated
	String additionalDescription() default "";
	R constantAdditionalDescription() default R.DEFAULT;

	@Deprecated
	String outputDescription() default "No output value.";
	R constantOutputDescription() default R.DEFAULT_OUTPUT_DESCRIPTION;

    Class<?>[] seeAlsoClass() default {};

    @Deprecated
	String examples() default "";
    R constantExamples() default R.DEFAULT;

	R WebPluginDifference() default R.DEFAULT;
	R WinPluginDifference() default R.DEFAULT;
	R SwingPluginDifference() default R.DEFAULT;
}
