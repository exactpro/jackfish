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
