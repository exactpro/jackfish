////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.lang.model.type.NullType;

import com.exactprosystems.jf.documents.matrix.parser.Tokens;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MatrixItemAttribute
{
	String description();
	Class<?>[] seeAlsoClass() default {};
	String examples() default "";
	Tokens[] shouldContain();
	Tokens[] mayContain();
	Class<?>[] parents() default {};
	Class<?> closes() default NullType.class;
	boolean real();
	boolean hasValue();
	boolean hasParameters();
    boolean hasChildren();
    boolean raw() default false;
}
