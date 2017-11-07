package com.exactprosystems.jf;

import javafx.scene.control.TreeItem;

public class MockTree
{
    private TreeItem<String> root;

    MockTree()
    {
        TreeItem <String> colors = new TreeItem <>("colors");
        colors.setExpanded(true);
        colors.getChildren().addAll(
                new TreeItem <>("red"),
                new TreeItem <>("Blue")
        );

        this.root = new TreeItem<>("Green");
        this.root.setExpanded(true);
        this.root.getChildren().addAll(
                new TreeItem <>("Yellow"),
                new TreeItem <>("Orange"),
                new TreeItem <>("Blue"),
                colors
                );
    }

    TreeItem<String> getRoot()
    {
        return this.root;
    }
}
