////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.wizard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WizardAttribute
{
	/**
	 * @return the string representation name of a wizard.
	 *
	 * @see Wizard
	 */
	String name();

	/**
	 * @return the name of picture for a wizard
	 *
	 * @see Wizard
	 */
	String pictureName();

	/**
	 * @return the short description for a wizard
	 *
	 * @see Wizard
	 */
	String shortDescription();

	/**
	 * @return the detailed description for a wizard
	 *
	 * @see Wizard
	 */
	String detailedDescription();

	/**
	 * @return the WizardCategory enum for a wizard
	 *
	 * @see Wizard
	 */
	WizardCategory category();

	/**
	 * Indicate, what that is a wizard is experimental ( not stable) or not
	 *
	 * @see Wizard
	 */
	boolean experimental();

	/**
	 * Indicate, that a wizard has strong criteries.<br>
	 * This means, that all passed criteries are presented in the wizard
	 *
	 * @see Wizard
	 */
	boolean strongCriteries();

	/**
	 * @return Array of criteries, which need for work a wizard
	 *
	 * @see Wizard
	 */
	Class<?>[] criteries() default {};
}