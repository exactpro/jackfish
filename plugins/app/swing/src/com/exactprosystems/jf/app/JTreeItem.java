package com.exactprosystems.jf.app;

import javax.swing.*;
import javax.swing.tree.TreePath;

public class JTreeItem extends JComponent
{
    JTree tree;
    TreePath path;

    JTreeItem(JTree tree, TreePath path)
    {
        this.tree = tree;
        this.path = path;
    }

    JTree getTree()
    {
        return tree;
    }

    TreePath getPath()
    {
        return path;
    }

    @Override
    public boolean isVisible()
    {
        return this.tree.isVisible(this.path);
    }
}
