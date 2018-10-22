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

package com.exactprosystems.jf.app;

public enum WindowTreeScope
{
    Element 	(1), // found only on element
    Children 	(2), // found only children of current element
    Descendants (4), // found on children of children of current component
    Subtree 	(7), // found from root element
    Parent 		(8), // not supported
    Ancestors 	(16);// not supported
    
    WindowTreeScope(int value)
    {
    	this.value = value;
    }
    
    public int getValue()
    {
    	return this.value;
    }
    
    private int value;
}