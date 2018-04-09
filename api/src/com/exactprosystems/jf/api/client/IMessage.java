/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.client;

import java.util.List;

public interface IMessage extends IField
{
	/**
	 * @return List of all fields from the message.
	 * All fields means, that fields get from children of the message, if the child is message
	 */
	List<IField> getFields();
	/**
	 * @return list of fields from the message ( only from the message, not inside children)
	 */
	List<IField> getMessageField();
	/**
	 * @param name name of found field
	 *
	 * @return return a field from the message ( only from the message, not inside children) from the message.
	 * If a field not found, will return null.
	 */
	IField getField(String name);
	/**
	 * @param name name of found field
	 *
	 * @return return a field from the message ( inside children messages ) from the message.
	 * If a field not found, will return null.
	 */
	IField getDeepField(String name);
}
