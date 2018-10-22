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
package com.exactprosystems.jf;

import javafx.scene.control.TreeItem;

import java.util.Arrays;

class MockTree
{
    private TreeItem<String> root;
    private TreeItem<String> colors;

    MockTree()
    {
        this.colors = new TreeItem <>("colors");
        this.colors.setExpanded(true);
        this.colors.getChildren().addAll(
                Arrays.asList(new TreeItem<>("red"), new TreeItem<>("Blue"))
        );

        this.root = new TreeItem<>("Green");
        this.root.setExpanded(true);
        this.root.getChildren().addAll(
                Arrays.asList(new TreeItem <>("Yellow"),new TreeItem <>("Orange"),new TreeItem <>("Blue"), this.colors)
        );
    }

    TreeItem<String> getRoot()
    {
        return this.root;
    }

    TreeItem<String> getColors()
    {
        return this.colors;
    }
}
