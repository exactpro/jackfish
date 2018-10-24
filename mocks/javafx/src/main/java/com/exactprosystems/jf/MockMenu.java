
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

import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;

import java.util.ArrayList;
import java.util.List;

class MockMenu
{
    private List<Menu> menus;

    MockMenu()
    {
        menus = new ArrayList<>();
        Menu menuItem = new Menu("MenuItem");
        Menu menuItem2 = new Menu("MenuItem2");
        MenuItem menuItem3 = new MenuItem("MenuItem3");

        menuItem2.getItems().add(menuItem3);
        menuItem.getItems().add(menuItem2);

        Menu menu = new Menu("Menu");
        menu.getItems().add(menuItem);
        menus.add(menu);
        menus.add(new Menu("Menu2"));
    }

    Menu[] getMenus()
    {
        return menus.toArray(new Menu[0]);
    }
}