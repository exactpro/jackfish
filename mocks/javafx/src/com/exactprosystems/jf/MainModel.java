package com.exactprosystems.jf;

class MainModel
{
    private String[] data;
    private Table table;

    MainModel()
    {
        this.data = new String[]{"Green", "Yellow", "Orange", "Blue"};
        this.table = new Table();
    }

    String[] getData()
    {
        return this.data;
    }

    Table getTable()
    {
        return table;
    }
}
