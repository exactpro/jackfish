////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
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
