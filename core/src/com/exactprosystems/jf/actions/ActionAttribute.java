////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions;

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

	String generalDescription();
	String additionalDescription() default "";
	String outputDescription() default "No output value.";
    Class<?>[] seeAlsoClass() default {};
	String examples() default "";
}
