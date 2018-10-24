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
package com.exactprosystems.jf;

import java.util.Arrays;
import java.util.List;

class MainModel
{
    private int counter;
    private final MockMenu menu;
    private MockTree tree;
    private List<String> data;
    private MockTable table;

    MainModel()
    {
        this.data = Arrays.asList("Green", "Yellow", "Orange", "Blue");
        this.table = new MockTable();
        this.tree = new MockTree();
        this.menu = new MockMenu();
        clearCounter();
    }

    List<String> getData()
    {
        return this.data;
    }

    MockTable getTable()
    {
        return this.table;
    }

    MockTree getTree()
    {
        return tree;
    }

    MockMenu getMenu()
    {
        return this.menu;
    }

    int getCounter()
    {
        return this.counter;
    }

    void plusCounter()
    {
        this.counter++;
    }

    void clearCounter()
    {
        this.counter = 0;
    }
}
