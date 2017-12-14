////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

public enum ControlType
{
    Any("Any", -1),
    Wait("Wait", -1),
    Button("Button",50000),
    Calendar("Calendar",50001),
    CheckBox("CheckBox",50002),
    ComboBox("ComboBox",50003),
    Edit("Edit",50004),
    Hyperlink("Hyperlink",50005),
    Image("Image",50006),
    ListItem("ListItem",50007),
    List("List",50008),
    Menu("Menu",50009),
    MenuBar("MenuBar",50010),
    MenuItem("MenuItem",50011),
    ProgressBar("ProgressBar",50012),
    RadioButton("RadioButton",50013),
    ScrollBar("ScrollBar",50014),
    Slider("Slider",50015),
    Spinner("Spinner",50016),
    StatusBar("StatusBar",50017),
    Tab("Tab",50018),
    TabItem("TabItem",50019),
    Text("Text",50020),
    ToolBar("ToolBar",50021),
    ToolTip("ToolTip",50022),
    Tree("Tree",50023),
    TreeItem("TreeItem",50024),
    Custom("Custom",50025),
    Group("Group",50026),
    Thumb("Thumb",50027),
    DataGrid("DataGrid",50028),
    DataItem("DataItem",50029),
    Document("Document",50030),
    SplitButton("SplitButton",50031),
    Window("Window",50032),
    Pane("Pane",50033),
    Header("Header",50034),
    HeaderItem("HeaderItem",50035),
    Table("Table",50036),
    TitleBar("TitleBar",50037),
    Separator("Separator",50038);

    private String name;
    private int id;

    ControlType(String name, int id)
    {
        this.name = name;
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public int getId()
    {
        return id;
    }

    public String getStringId()
    {
        return Integer.toString(id);
    }

    public static ControlType get(String name)
    {
        for (ControlType type : values())
        {
            if (type.getName().equals(name))
            {
                return type;
            }
        }
        return Any;
    }
}
