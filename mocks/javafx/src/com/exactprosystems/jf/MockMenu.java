
package com.exactprosystems.jf;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MockMenu
{
    private List<Menu> menus;

    public MockMenu()
    {
        menus = new ArrayList<>();
        Menu menuItem = new Menu("MenuItem");
        Menu menuItem2 = new Menu("MenuItem2");

        menuItem2.getItems().add(new MenuItem("MenuItem3"));
        menuItem.getItems().add(menuItem2);

        menus.add(new Menu("Menu2"));
        menus.add(menuItem);
    }

    public Menu[] getMenus()
    {
        return menus.toArray(new Menu[0]);
    }
}