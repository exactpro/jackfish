/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
