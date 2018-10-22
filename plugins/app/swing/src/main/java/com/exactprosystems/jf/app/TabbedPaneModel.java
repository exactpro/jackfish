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
