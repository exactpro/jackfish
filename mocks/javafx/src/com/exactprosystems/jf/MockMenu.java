
/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
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