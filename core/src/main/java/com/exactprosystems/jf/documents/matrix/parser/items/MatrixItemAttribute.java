/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.documents.matrix.parser.Tokens;

import javax.lang.model.type.NullType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MatrixItemAttribute
{
	/**
	 * @return the general description for the item
	 */
	R constantGeneralDescription() default R.DEFAULT;
	/**
	 * @return array of see also classes
	 */
	Class<?>[] seeAlsoClass() default {};
	/**
	 * @return the example for the item
	 */
	R constantExamples() default R.DEFAULT;
	/**
	 * @return array of tokens, which are should contains in the item
	 */
	Tokens[] shouldContain();
	/**
	 * @return array of tokens, which are may contains in the item
	 */
	Tokens[] mayContain();
	/**
	 * @return array of classes, which can are parents for the item
	 */
	Class<?>[] parents() default {};
	/**
	 * @return if the item is End*, the item should override this property and set, which type the End* is closed
	 */
	Class<?> closes() default NullType.class;
	/**
	 * @return true, if item is real ( it mean, what the item can be displayed)
	 */
	boolean real();
	/**
	 * @return true, if the item has values ( e.g. id, off, repOff, global and ignoreErr)
	 */
	boolean hasValue();
	/**
	 * @return true, if the item can add the additional parameters
	 */
	boolean hasParameters();
	/**
	 * @return true, if the item can has children
	 */
	boolean hasChildren();
	/**
	 * @return true, if the item is raw
	 *
	 * @see RawMessage
	 * @see RawTable
	 * @see RawText
	 */
	boolean raw() default false;
}
