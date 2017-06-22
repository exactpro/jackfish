package com.exactprosystems.jf.app;


import javax.swing.*;
import javax.swing.event.ListDataListener;

public class TabbedPaneModel implements ListModel {

    private JTabbedPane pane;

    public TabbedPaneModel(JTabbedPane pane) {
        this.pane = pane;
    }


    @Override

    public int getSize() {
        return this.pane.getTabCount();
    }

    @Override
    public String getElementAt(int index) {

        return this.pane.getTitleAt(index);

    }

    @Override
    public void addListDataListener(ListDataListener l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        throw new UnsupportedOperationException();
    }
}
