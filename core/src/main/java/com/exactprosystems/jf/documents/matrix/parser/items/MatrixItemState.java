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

package com.exactprosystems.jf.documents.matrix.parser.items;

/**
 * A state enum for a item
 * Use this enum for indicate icon on a GUI
 */
public enum MatrixItemState
{
	/**
	 * This enum used for no icon
	 */
	None,
	/**
	 * This enum used for display, that a item is on breakpoint
	 */
	BreakPoint,
	/**
	 * This enum used for display, that a item is executing now
	 */
	Executing,
	/**
	 * This enum used for a item, which are executing now and on breakpoint
	 */
	ExecutingWithBreakPoint,
	/**
	 * This enum used for a parent item, which executing now
	 */
	ExecutingParent,
}
