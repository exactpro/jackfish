package com.exactprosystems.jf;

class MainModel
{
    private final MockMenu menu;
    private MockTree tree;
    private String[] data;
    private MockTable table;

    MainModel()
    {
        this.data = new String[]{"Green", "Yellow", "Orange", "Blue"};
        this.table = new MockTable();
        this.tree = new MockTree();
        this.menu = new MockMenu();
    }

    String[] getData()
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
}
