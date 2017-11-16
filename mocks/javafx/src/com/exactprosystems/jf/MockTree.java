////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf;

import javafx.scene.control.TreeItem;

import java.util.Arrays;

class MockTree
{
    private TreeItem<String> root;

    MockTree()
    {
        TreeItem <String> colors = new TreeItem <>("colors");
        colors.setExpanded(true);
        colors.getChildren().addAll(
                Arrays.asList(new TreeItem<>("red"), new TreeItem<>("Blue"))
        );

        this.root = new TreeItem<>("Green");
        this.root.setExpanded(true);
        this.root.getChildren().addAll(
                Arrays.asList(new TreeItem <>("Yellow"),new TreeItem <>("Orange"),new TreeItem <>("Blue"),colors)
        );
    }

    TreeItem<String> getRoot()
    {
        return this.root;
    }
}
